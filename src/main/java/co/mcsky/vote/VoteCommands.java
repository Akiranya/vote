package co.mcsky.vote;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.*;
import co.mcsky.moecore.skull.SkullCache;
import co.mcsky.vote.file.GameFileHandlerPool;
import co.mcsky.vote.gui.ListingGui;
import co.mcsky.vote.object.GamePool;
import co.mcsky.vote.object.Vote;
import co.mcsky.vote.object.Work;
import co.mcsky.vote.util.MainUtil;
import me.lucko.helper.promise.Promise;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@CommandAlias("votes")
public class VoteCommands extends BaseCommand {

    // Dependency injection does not work for this instance
    private final PaperCommandManager commands;

    @Dependency
    private VoteMain plugin;

    public VoteCommands(PaperCommandManager commands) {
        this.commands = commands;
        registerConditions();
        registerCompletions();
    }

    private void registerCompletions() {
        commands.getCommandCompletions().registerCompletion("world", c -> Bukkit.getWorlds().stream()
                .map(World::getName).toList());
        commands.getCommandCompletions().registerCompletion("rate", c -> GamePool.INSTANCE.peek()
                .map(game -> game.getStatistics().getRaters().map(MainUtil::getPlayerName).toList())
                .orElse(List.of("none")));
        commands.getCommandCompletions().registerCompletion("work", c -> GamePool.INSTANCE.peek()
                .map(game -> game.getWorks().stream()
                        .map(Work::getOwner)
                        .map(MainUtil::getPlayerName).toList())
                .orElse(List.of("none")));
    }

    private void registerConditions() {
        commands.getCommandConditions().addCondition("ready", c -> {
            if (!GamePool.INSTANCE.containsAny()) {
                throw new ConditionFailedException(plugin.message(c.getIssuer().getPlayer(), "chat-message.vote-system-not-available"));
            }
        });
        commands.getCommandConditions().addCondition(World.class, "plotworld", (context, exec, world) -> {
            if (!VoteMain.plugin.getPlots().isPlotWorld(world)) {
                throw new ConditionFailedException(plugin.message(exec.getSender(), "chat-message.world-no-plots", "world", world.getName()));
            }
        });

    }

    @Default
    @Conditions("ready")
    public void open(Player player) {
        new ListingGui(player, GamePool.INSTANCE.get()).open();
    }

    @SuppressWarnings("ConstantConditions")
    @Subcommand("ready")
    @Conditions("ready")
    @CommandPermission("votes.admin")
    public void ready(CommandSender sender) {
        GamePool.INSTANCE.get().setReady(!GamePool.INSTANCE.get().isReady());
        sender.sendMessage(plugin.message(sender, "chat-message.mark-vote-system", "state", GamePool.INSTANCE.get().isReady()));
    }

    @Subcommand("reload")
    @CommandPermission("votes.admin")
    public void reload(CommandSender sender) {
        this.plugin.loadLanguages();
        this.plugin.config.load();
        sender.sendMessage(plugin.message(sender, "chat-message.plugin-reloaded"));
    }

    @Subcommand("pull")
    @CommandCompletion("@world")
    @CommandPermission("votes.admin")
    public void pull(CommandSender sender, @Conditions("plotworld") World world) {
        if (GamePool.INSTANCE.register(world.getName())) {
            sender.sendMessage(plugin.message(sender, "chat-message.plot-information-pulled"));
        } else {
            sender.sendMessage(plugin.message(sender, "chat-message.plot-information-pulled-already"));
        }
    }

    @Subcommand("purge")
    @CommandCompletion("@world")
    @CommandPermission("votes.admin")
    public void purge(CommandSender sender, @Conditions("plotworld") World world) {
        if (GamePool.INSTANCE.unregister(world.getName())) {
            sender.sendMessage(plugin.message(sender, "chat-message.plot-information-deleted"));
        } else {
            sender.sendMessage(plugin.message(sender, "chat-message.plot-information-deleted-already"));
        }
    }

    @Subcommand("cache clear")
    @CommandPermission("votes.admin")
    public void clear(CommandSender sender) {
        SkullCache.INSTANCE.clear();
        sender.sendMessage(plugin.message(sender, "chat-message.skin-cache-cleared"));
    }

    @Subcommand("datasource")
    @CommandPermission("votes.admin")
    public class DatasourceCommand extends BaseCommand {

        @Subcommand("load")
        public void load(CommandSender sender) {
            GameFileHandlerPool.INSTANCE.readAll();
            sender.sendMessage(plugin.message(sender, "chat-message.read-all"));
        }

        @Subcommand("save")
        public void save(CommandSender sender) {
            GameFileHandlerPool.INSTANCE.saveAll();
            sender.sendMessage(plugin.message(sender, "chat-message.saved-all"));
        }
    }


    @SuppressWarnings("StringBufferReplaceableByString ConstantConditions")
    @Subcommand("stats")
    @Conditions("ready")
    @CommandPermission("votes.admin")
    public class StatisticsCommand extends BaseCommand {

        // Portable way to get the line separator
        final String TITLE = ChatColor.translateAlternateColorCodes('&', "&8=-=-=-=-=-=-= &6Overview&8 =-=-=-=-=-=-=");
        final String LIST_SEPARATOR = ChatColor.translateAlternateColorCodes('&', "&8,&r ");
        final String LINE_SEPARATOR = System.lineSeparator();

        // These methods should be thread-safe, so run them async

        @Default
        public void overview(CommandSender sender) {
            Promise.start().thenApplyAsync(n -> {
                var stats = GamePool.INSTANCE.get().getStatistics();

                // show list of valid & invalid raters
                var validRatersCount = stats.getValidRaters().size();
                var invalidRatersCount = stats.getInvalidRaters().size();
                var joining = Collectors.joining(LIST_SEPARATOR);
                var invalidRaters = stats.getInvalidRaters().stream().map(MainUtil::getPlayerName).collect(joining);
                var validRaters = stats.getValidRaters().stream().map(MainUtil::getPlayerName).collect(joining);

                var sb = new StringBuilder()
                        .append(TITLE)
                        .append(LINE_SEPARATOR)
                        .append(plugin.message(sender, "chat-message.invalid-rater-list", "count", invalidRatersCount, "list", invalidRaters))
                        .append(LINE_SEPARATOR)
                        .append(plugin.message(sender, "chat-message.valid-rater-list", "count", validRatersCount, "list", validRaters))
                        .append(LINE_SEPARATOR);

                // show details of work ratings
                sb.append(TITLE).append(LINE_SEPARATOR);
                GamePool.INSTANCE.get().getWorks().forEach(work -> {
                    var uuid = work.getOwner();
                    var redVotesCount = stats.ofRedVotes(uuid).size();
                    var greenVotesCount = stats.ofGreenVotes(uuid).size();
                    var totalVotesCount = greenVotesCount + redVotesCount; // only the votes on this work
                    var greenVoteProportion = 100D * greenVotesCount / totalVotesCount;
                    sb.append(plugin.message(sender, "chat-message.work-information-line").formatted(
                            greenVotesCount, redVotesCount, totalVotesCount,
                            greenVoteProportion, MainUtil.getPlayerName(uuid), work.isDone()
                                    ? plugin.message(sender, "gui.work-listing.done")
                                    : plugin.message(sender, "gui.work-listing.undone")));
                    sb.append(LINE_SEPARATOR);
                });

                return sb.toString();
            }).thenAcceptSync(sender::sendMessage);
        }

        @Subcommand("work")
        @CommandCompletion("@work")
        public void work(CommandSender sender, OfflinePlayer work) {
            Promise.start().thenApplyAsync(n -> {
                var stats = GamePool.INSTANCE.get().getStatistics();

                // show detailed ratings of a work

                var workOwner = work.getUniqueId();
                var greenRatersCount = stats.ofGreenVotes(workOwner).size();
                var redRatersCount = stats.ofRedVotes(workOwner).size();
                var joining = Collectors.joining(LIST_SEPARATOR);
                var greenRaters = stats.ofGreenVotes(workOwner).stream()
                        .map(Vote::getRater)
                        .map(MainUtil::getPlayerName)
                        .collect(joining);
                var redRaters = stats.ofRedVotes(workOwner).stream()
                        .map(Vote::getRater)
                        .map(MainUtil::getPlayerName)
                        .collect(joining);

                var sb = new StringBuilder()
                        .append(TITLE)
                        .append(LINE_SEPARATOR)
                        .append(plugin.message(sender, "chat-message.green-rater-list", "count", greenRatersCount, "list", greenRaters))
                        .append(LINE_SEPARATOR)
                        .append(plugin.message(sender, "chat-message.red-rater-list", "count", redRatersCount, "list", redRaters))
                        .append(LINE_SEPARATOR);

                return sb.toString();
            }).thenAcceptSync(sender::sendMessage);
        }

        @Subcommand("rate")
        @CommandCompletion("@rate")
        public void rater(CommandSender sender, OfflinePlayer rater) {
            Promise.start().thenApplyAsync(n -> {
                var stats = GamePool.INSTANCE.get().getStatistics();

                // show detailed ratings of a rater

                var raterUuid = rater.getUniqueId();
                var greenWorksCount = stats.ofGreenWorks(raterUuid).size();
                var redWorksCount = stats.ofRedWorks(raterUuid).size();
                var joining = Collectors.joining(LIST_SEPARATOR);
                var greenWorks = stats.ofGreenWorks(raterUuid).stream().map(Work::getOwnerName).collect(joining);
                var redWorks = stats.ofRedWorks(raterUuid).stream().map(Work::getOwnerName).collect(joining);

                var sb = new StringBuilder()
                        .append(TITLE)
                        .append(LINE_SEPARATOR)
                        .append(plugin.message(sender, "chat-message.green-work-list", "count", greenWorksCount, "list", greenWorks))
                        .append(LINE_SEPARATOR)
                        .append(plugin.message(sender, "chat-message.red-work-list", "count", redWorksCount, "list", redWorks))
                        .append(LINE_SEPARATOR);

                return sb.toString();
            }).thenAcceptSync(sender::sendMessage);
        }

    }

}
