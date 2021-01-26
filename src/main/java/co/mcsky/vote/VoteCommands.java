package co.mcsky.vote;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.*;
import co.mcsky.vote.gui.WorkListingGui;
import co.mcsky.vote.helper.MiscUtil;
import co.mcsky.vote.helper.VoteCalculator;
import co.mcsky.vote.type.Vote;
import co.mcsky.vote.type.Work;
import com.plotsquared.core.api.PlotAPI;
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

    private final PlotAPI api;

    @Dependency
    private VoteMain plugin;
    private Votes votes;

    public VoteCommands(PaperCommandManager commands) {
        this.api = new PlotAPI();

        // Command completions
        commands.getCommandCompletions().registerCompletion("world", c -> Bukkit.getWorlds().stream()
                .map(World::getName)
                .collect(Collectors.toUnmodifiableList()));
        commands.getCommandCompletions().registerCompletion("rate", c -> this.votes == null ? List.of("none") : this.votes.getCalculator().rawRaters()
                .map(MiscUtil::getPlayerName)
                .collect(Collectors.toUnmodifiableList()));
        commands.getCommandCompletions().registerCompletion("work", c -> this.votes == null ? List.of("none") : this.votes.getWorks().stream()
                .map(Work::getOwner)
                .map(MiscUtil::getPlayerName)
                .collect(Collectors.toUnmodifiableList()));

        // Command conditions
        commands.getCommandConditions().addCondition("ready", c -> {
            if (this.votes == null) {
                throw new ConditionFailedException(plugin.getMessage(c.getIssuer().getIssuer(), "chat-message.vote-system-not-available"));
            }
        });
        commands.getCommandConditions().addCondition(World.class, "plotworld", (context, execContext, value) -> this.api.getPlotSquared().getPlotAreas(value.getName()).parallelStream()
                .flatMap(plotArea -> plotArea.getPlots().stream())
                .map(Plot::hasOwner) // Only count owned plots
                .findAny().orElseThrow(() -> new ConditionFailedException(plugin.getMessage(execContext.getSender(), "chat-message.world-no-plots", "world", value.getName()))));
    }

    @Default
    @Conditions("ready")
    public void open(Player player) {
        new WorkListingGui(player, this.votes).open();
    }

    @Subcommand("ready")
    @Conditions("ready")
    @CommandPermission("votes.admin")
    public void ready(CommandSender sender) {
        this.votes.ready(!this.votes.ready());
        sender.sendMessage(plugin.getMessage(sender, "chat-message.mark-vote-system", "state", this.votes.ready()));
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
        if (this.votes != null) {
            // Cleanup existing vote system
            try {
                this.votes.close();
            } catch (Exception e) {
                this.plugin.getLogger().severe(e.getMessage());
            }
        }
        this.votes = new Votes(world.getName());
        sender.sendMessage(plugin.getMessage(sender, "chat-message.plot-information-pulled"));
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @Subcommand("stats")
    @Conditions("ready")
    public class Stats extends BaseCommand {
        // Portable way to get the line separator
        final String TITLE = ChatColor.translateAlternateColorCodes('&', "&8=-=-=-=-=-=-= &6比赛投票统计数据&8 =-=-=-=-=-=-=");
        final String LIST_SEPARATOR = ChatColor.translateAlternateColorCodes('&', " &8-&r ");
        final String LINE_SEPARATOR = System.lineSeparator();

        // These methods should be thread-safe, so run them async

        @Default
        public void overview(CommandSender sender) {
            Schedulers.builder().async().now().run(() -> {
                VoteCalculator calc = votes.getCalculator();

                int validRatersCount = calc.validRaters().size();
                long invalidRatersCount = calc.invalidRaters().size();
                Collector<CharSequence, ?, String> joining = Collectors.joining(LIST_SEPARATOR);
                String invalidRaters = calc.invalidRaters().stream().map(MiscUtil::getPlayerName).collect(joining);
                String validRaters = calc.validRaters().stream().map(MiscUtil::getPlayerName).collect(joining);

                StringBuilder sb = new StringBuilder()
                        .append(TITLE).append(LINE_SEPARATOR)
                        .append(plugin.getMessage(sender, "chat-message.invalid-rater-list", "count", invalidRatersCount, "list", invalidRaters)).append(LINE_SEPARATOR)
                        .append(plugin.getMessage(sender, "chat-message.valid-rater-list", "count", validRatersCount, "list", validRaters)).append(LINE_SEPARATOR);

                sb.append(TITLE).append(LINE_SEPARATOR);
                votes.getWorks().stream().map(Work::getOwner).forEach(uuid -> {
                    float greenVoteProportion = (float) calc.greenVotes(uuid).size() / calc.validVotes(uuid).count();
                    sb.append(String.format(ChatColor.translateAlternateColorCodes('&', plugin.getMessage(sender, "chat-message.work-information-line")),
                            MiscUtil.getPlayerName(uuid), calc.greenVotes(uuid).size(), calc.redVotes(uuid).size(), calc.validVotes(uuid).count(), greenVoteProportion));
                    sb.append(LINE_SEPARATOR);
                });

                sender.sendMessage(sb.toString());
            });
        }

        @Subcommand("work")
        @CommandCompletion("@work")
        public void work(CommandSender sender, OfflinePlayer work) {
            Schedulers.builder().async().now().run(() -> {
                VoteCalculator calc = votes.getCalculator();

                UUID workOwner = work.getUniqueId();
                long greenRatersCount = calc.greenVotes(workOwner).size();
                long redRatersCount = calc.redVotes(workOwner).size();
                Collector<CharSequence, ?, String> joining = Collectors.joining(LIST_SEPARATOR);
                String greenRaters = calc.greenVotes(workOwner).stream()
                        .map(Vote::getRater)
                        .map(MiscUtil::getPlayerName)
                        .collect(joining);
                String redRaters = calc.redVotes(workOwner).stream()
                        .map(Vote::getRater)
                        .map(MiscUtil::getPlayerName)
                        .collect(joining);

                StringBuilder sb = new StringBuilder()
                        .append(TITLE).append(LINE_SEPARATOR)
                        .append(plugin.getMessage(sender, "chat-message.green-rater-list", "count", greenRatersCount, "list", greenRaters)).append(LINE_SEPARATOR)
                        .append(plugin.getMessage(sender, "chat-message.red-rater-list", "count", redRatersCount, "list", redRaters)).append(LINE_SEPARATOR);

                sender.sendMessage(sb.toString());
            });
        }

        @Subcommand("rate")
        @CommandCompletion("@rate")
        public void rater(CommandSender sender, OfflinePlayer rater) {
            Schedulers.builder().async().now().run(() -> {
                VoteCalculator calc = votes.getCalculator();

                UUID raterUuid = rater.getUniqueId();
                long greenWorksCount = calc.greenWorks(raterUuid).size();
                long redWorksCount = calc.redWorks(raterUuid).size();
                Collector<CharSequence, ?, String> joining = Collectors.joining(LIST_SEPARATOR);
                String greenWorks = calc.greenWorks(raterUuid).stream()
                        .map(Work::getOwner)
                        .map(MiscUtil::getPlayerName)
                        .collect(joining);
                String redWorks = calc.redWorks(raterUuid).stream()
                        .map(Work::getOwner)
                        .map(MiscUtil::getPlayerName)
                        .collect(joining);

                StringBuilder sb = new StringBuilder()
                        .append(TITLE).append(LINE_SEPARATOR)
                        .append(plugin.getMessage(sender, "chat-message.green-work-list", "count", greenWorksCount, "list", greenWorks)).append(LINE_SEPARATOR)
                        .append(plugin.getMessage(sender, "chat-message.red-work-list", "count", redWorksCount, "list", redWorks)).append(LINE_SEPARATOR);

                sender.sendMessage(sb.toString());
            });
        }
    }

}
