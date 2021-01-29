package co.mcsky.vote;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.*;
import co.mcsky.vote.io.VoteStoragePool;
import co.mcsky.vote.gui.MainGui;
import co.mcsky.vote.util.PlayerUtil;
import co.mcsky.vote.helper.VoteCalculator;
import co.mcsky.vote.type.Vote;
import co.mcsky.vote.type.VotesPool;
import co.mcsky.vote.type.Work;
import com.plotsquared.core.plot.Plot;
import me.lucko.helper.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@CommandAlias("votes")
public class VoteCommands extends BaseCommand {

    // Dependency injection does not work for this instance
    private final PaperCommandManager commands;

    @Dependency
    private VoteMain plugin;
    @Dependency
    private VotesPool votesPool;
    @Dependency
    private VoteStoragePool voteStoragePool;

    public VoteCommands(PaperCommandManager commands) {
        this.commands = commands;
        registerConditions();
        registerCompletions();
    }

    private void registerCompletions() {
        commands.getCommandCompletions().registerCompletion("world", c -> Bukkit.getWorlds().stream()
                .map(World::getName)
                .collect(Collectors.toUnmodifiableList()));
        commands.getCommandCompletions().registerCompletion("rate", c -> votesPool.peek()
                .map(votes -> votes.getCalculator().rawRaters()
                        .map(PlayerUtil::getName)
                        .collect(Collectors.toUnmodifiableList()))
                .orElse(List.of("none")));
        commands.getCommandCompletions().registerCompletion("work", c -> votesPool.peek()
                .map(votes -> votes.getWorks().stream()
                        .map(Work::getOwner)
                        .map(PlayerUtil::getName)
                        .collect(Collectors.toUnmodifiableList()))
                .orElse(List.of("none")));
    }

    private void registerConditions() {
        commands.getCommandConditions().addCondition("ready", c -> {
            if (!votesPool.containsAny()) {
                throw new ConditionFailedException(plugin.getMessage(c.getIssuer().getIssuer(), "chat-message.vote-system-not-available"));
            }
        });
        commands.getCommandConditions().addCondition(World.class, "plotworld", (context, execContext, value) -> VoteMain.plotApi.getPlotSquared().getPlotAreas(value.getName()).parallelStream()
                .flatMap(plotArea -> plotArea.getPlots().stream())
                .map(Plot::hasOwner) // Only count owned plots
                .findAny()
                .orElseThrow(() -> new ConditionFailedException(plugin.getMessage(execContext.getSender(), "chat-message.world-no-plots", "world", value.getName()))));
    }

    @Default
    @Conditions("ready")
    public void open(Player player) {
        new MainGui(player, votesPool.get()).open();
    }

    @SuppressWarnings("ConstantConditions")
    @Subcommand("ready")
    @Conditions("ready")
    @CommandPermission("votes.admin")
    public void ready(CommandSender sender) {
        votesPool.get().setReady(!votesPool.get().isReady());
        sender.sendMessage(plugin.getMessage(sender, "chat-message.mark-vote-system", "state", votesPool.get().isReady()));
    }

    @Subcommand("reload")
    @CommandPermission("votes.admin")
    public void reload(CommandSender sender) {
        this.plugin.loadLanguages();
        this.plugin.config.load();
        sender.sendMessage(plugin.getMessage(sender, "chat-message.plugin-reloaded"));
    }

    @Subcommand("pull")
    @CommandCompletion("@world")
    @CommandPermission("votes.admin")
    public void pull(CommandSender sender, @Conditions("plotworld") World world) {
        if (votesPool.register(world.getName())) {
            sender.sendMessage(plugin.getMessage(sender, "chat-message.plot-information-pulled"));
        } else {
            sender.sendMessage(plugin.getMessage(sender, "chat-message.plot-information-pulled-already"));
        }
    }

    @Subcommand("purge")
    @CommandCompletion("@world")
    @CommandPermission("votes.admin")
    public void purge(CommandSender sender, @Conditions("plotworld") World world) {
        if (votesPool.unregister(world.getName())) {
            sender.sendMessage(plugin.getMessage(sender, "chat-message.plot-information-deleted"));
        } else {
            sender.sendMessage(plugin.getMessage(sender, "chat-message.plot-information-deleted-already"));
        }

    }

    @Subcommand("save")
    @CommandPermission("votes.admin")
    public void save(CommandSender sender) {
        voteStoragePool.saveAll();
        sender.sendMessage(plugin.getMessage(sender, "chat-message.saved-all"));
    }

    @SuppressWarnings("StringBufferReplaceableByString ConstantConditions")
    @Subcommand("stats")
    @Conditions("ready")
    public class Stats extends BaseCommand {
        // Portable way to get the line separator
        final String TITLE = ChatColor.translateAlternateColorCodes('&', "&8=-=-=-=-=-=-= &6比赛评分统计概览&8 =-=-=-=-=-=-=");
        final String LIST_SEPARATOR = ChatColor.translateAlternateColorCodes('&', " &8-&r ");

        final String LINE_SEPARATOR = System.lineSeparator();

        // These methods should be thread-safe, so run them async

        @Default
        public void overview(CommandSender sender) {
            Schedulers.builder().async().now().run(() -> {
                VoteCalculator calc = votesPool.get().getCalculator();

                int validRatersCount = calc.validRaters().size();
                long invalidRatersCount = calc.invalidRaters().size();
                Collector<CharSequence, ?, String> joining = Collectors.joining(LIST_SEPARATOR);
                String invalidRaters = calc.invalidRaters().stream().map(PlayerUtil::getName).collect(joining);
                String validRaters = calc.validRaters().stream().map(PlayerUtil::getName).collect(joining);

                StringBuilder sb = new StringBuilder()
                        .append(TITLE).append(LINE_SEPARATOR)
                        .append(plugin.getMessage(sender, "chat-message.invalid-rater-list", "count", invalidRatersCount, "list", invalidRaters)).append(LINE_SEPARATOR)
                        .append(plugin.getMessage(sender, "chat-message.valid-rater-list", "count", validRatersCount, "list", validRaters)).append(LINE_SEPARATOR);

                sb.append(TITLE).append(LINE_SEPARATOR);
                votesPool.get().getWorks().stream().map(Work::getOwner).forEach(uuid -> {
                    int redVotesCount = calc.redVotes(uuid).size();
                    int greenVotesCount = calc.greenVotes(uuid).size();
                    float greenVoteProportion = 100F * greenVotesCount / validRatersCount;
                    sb.append(String.format(plugin.getMessage(sender, "chat-message.work-information-line"), greenVotesCount, redVotesCount, validRatersCount, greenVoteProportion, PlayerUtil.getName(uuid)));
                    sb.append(LINE_SEPARATOR);
                });

                Schedulers.builder().sync().now().run(() -> sender.sendMessage(sb.toString()));
            });
        }

        @Subcommand("work")
        @CommandCompletion("@work")
        public void work(CommandSender sender, OfflinePlayer work) {
            Schedulers.builder().async().now().run(() -> {
                VoteCalculator calc = votesPool.get().getCalculator();

                UUID workOwner = work.getUniqueId();
                long greenRatersCount = calc.greenVotes(workOwner).size();
                long redRatersCount = calc.redVotes(workOwner).size();
                Collector<CharSequence, ?, String> joining = Collectors.joining(LIST_SEPARATOR);
                String greenRaters = calc.greenVotes(workOwner).stream()
                        .map(Vote::getRater)
                        .map(PlayerUtil::getName)
                        .collect(joining);
                String redRaters = calc.redVotes(workOwner).stream()
                        .map(Vote::getRater)
                        .map(PlayerUtil::getName)
                        .collect(joining);

                StringBuilder sb = new StringBuilder()
                        .append(TITLE).append(LINE_SEPARATOR)
                        .append(plugin.getMessage(sender, "chat-message.green-rater-list", "count", greenRatersCount, "list", greenRaters)).append(LINE_SEPARATOR)
                        .append(plugin.getMessage(sender, "chat-message.red-rater-list", "count", redRatersCount, "list", redRaters)).append(LINE_SEPARATOR);

                Schedulers.builder().sync().now().run(() -> sender.sendMessage(sb.toString()));
            });
        }

        @Subcommand("rate")
        @CommandCompletion("@rate")
        public void rater(CommandSender sender, OfflinePlayer rater) {
            Schedulers.builder().async().now().run(() -> {
                VoteCalculator calc = votesPool.get().getCalculator();

                UUID raterUuid = rater.getUniqueId();
                long greenWorksCount = calc.greenWorks(raterUuid).size();
                long redWorksCount = calc.redWorks(raterUuid).size();
                Collector<CharSequence, ?, String> joining = Collectors.joining(LIST_SEPARATOR);
                String greenWorks = calc.greenWorks(raterUuid).stream()
                        .map(Work::getOwnerName)
                        .collect(joining);
                String redWorks = calc.redWorks(raterUuid).stream()
                        .map(Work::getOwnerName)
                        .collect(joining);

                StringBuilder sb = new StringBuilder()
                        .append(TITLE).append(LINE_SEPARATOR)
                        .append(plugin.getMessage(sender, "chat-message.green-work-list", "count", greenWorksCount, "list", greenWorks)).append(LINE_SEPARATOR)
                        .append(plugin.getMessage(sender, "chat-message.red-work-list", "count", redWorksCount, "list", redWorks)).append(LINE_SEPARATOR);

                Schedulers.builder().sync().now().run(() -> sender.sendMessage(sb.toString()));
            });
        }
    }

}
