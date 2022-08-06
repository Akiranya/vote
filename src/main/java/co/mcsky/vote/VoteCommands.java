package co.mcsky.vote;

import co.mcsky.mewcore.skull.SkullCache;
import co.mcsky.vote.gui.ListingGui;
import co.mcsky.vote.io.GameFileHandlerPool;
import co.mcsky.vote.object.Game;
import co.mcsky.vote.object.GamePool;
import co.mcsky.vote.object.Vote;
import co.mcsky.vote.object.Work;
import co.mcsky.vote.util.MainUtil;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import me.lucko.helper.promise.Promise;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class VoteCommands {

    private static final String COMMAND_NAME = "votes";
    private static final CommandPermission PERM_ADMIN = CommandPermission.fromString("votes.admin");
    private static final CommandPermission PERM_HOST = CommandPermission.fromString("votes.host");

    public void register() {
        // command: votes
        final CommandAPICommand rootCmd = new CommandAPICommand("votes")
                .executesPlayer((player, args) -> {
                    if (GamePool.INSTANCE.containsAny()) {
                        new ListingGui(player, GamePool.INSTANCE.getOrNull()).open();
                    } else {
                        player.sendMessage(VoteMain.lang().get(player, "chat-message.vote-system-not-available"));
                    }
                });

        // command: votes toggle open
        final CommandAPICommand toggleCmd = new CommandAPICommand("toggle")
                .withPermission(PERM_ADMIN)
                .withArguments(new MultiLiteralArgument("open"))
                .executes((sender, args) -> {
                    final Game game = GamePool.INSTANCE.getOrNull();
                    if (game == null) {
                        sender.sendMessage(VoteMain.lang().get(sender, "chat-message.vote-system-not-available"));
                        return;
                    }
                    final boolean ready = game.isReady();
                    game.setReady(!ready);
                    sender.sendMessage(VoteMain.lang().get(sender, "chat-message.mark-vote-system", "state", Boolean.toString(!ready)));
                });

        // command: votes reload
        final CommandAPICommand reloadCmd = new CommandAPICommand("reload")
                .withPermission(PERM_ADMIN)
                .executes((sender, args) -> {
                    VoteMain.inst().reload();
                    sender.sendMessage(VoteMain.lang().get(sender, "chat-message.plugin-reloaded"));
                });

        // command: votes pull <world>
        final CommandAPICommand pullCmd = new CommandAPICommand("pull")
                .withPermission(PERM_ADMIN)
                .withArguments(new StringArgument("world").replaceSuggestions(
                        ArgumentSuggestions.strings(getPlotWorldCompletions())
                ))
                .executes((sender, args) -> {
                    final World world = Bukkit.getWorld((String) args[0]);
                    if (world == null || !VoteMain.inst().getPlots().isPlotWorld(world)) {
                        sender.sendMessage(VoteMain.lang().get(sender, "chat-message.world-no-plots", "world", (String) args[0]));
                    } else {
                        if (GamePool.INSTANCE.register(world.getName())) {
                            sender.sendMessage(VoteMain.lang().get(sender, "chat-message.plot-information-pulled"));
                        } else {
                            sender.sendMessage(VoteMain.lang().get(sender, "chat-message.plot-information-pulled-already"));
                        }
                    }
                });

        // command: votes purge <world>
        final CommandAPICommand purgeCmd = new CommandAPICommand("purge")
                .withPermission(PERM_ADMIN)
                .withArguments(new StringArgument("world").replaceSuggestions(
                        ArgumentSuggestions.strings(getPlotWorldCompletions())
                ))
                .executes((sender, args) -> {
                    final World world = Bukkit.getWorld((String) args[0]);
                    if (world == null || !VoteMain.inst().getPlots().isPlotWorld(world)) {
                        sender.sendMessage(VoteMain.lang().get(sender, "chat-message.world-no-plots", "world", (String) args[0]));
                    } else {
                        if (GamePool.INSTANCE.unregister(world.getName())) {
                            sender.sendMessage(VoteMain.lang().get(sender, "chat-message.plot-information-deleted"));
                        } else {
                            sender.sendMessage(VoteMain.lang().get(sender, "chat-message.plot-information-deleted-already"));
                        }
                    }
                });

        // command: votes cache clear
        final CommandAPICommand cacheCmd = new CommandAPICommand("cache")
                .withPermission(PERM_ADMIN)
                .withArguments(new MultiLiteralArgument("clear"))
                .executes((sender, args) -> {
                    SkullCache.INSTANCE.clear();
                    sender.sendMessage(VoteMain.lang().get(sender, "chat-message.skin-cache-cleared"));
                });

        // command: votes data <load|save>
        final CommandAPICommand dataCmd = new CommandAPICommand("data")
                .withPermission(PERM_ADMIN)
                .withArguments(new MultiLiteralArgument("load", "save"))
                .executes((sender, args) -> {
                    switch ((String) args[0]) {
                        case "load" -> {
                            GameFileHandlerPool.INSTANCE.readAll();
                            sender.sendMessage(VoteMain.lang().get(sender, "chat-message.read-all"));
                        }
                        case "save" -> {
                            GameFileHandlerPool.INSTANCE.saveAll();
                            sender.sendMessage(VoteMain.lang().get(sender, "chat-message.saved-all"));
                        }
                    }
                });

        final String title = ChatColor.translateAlternateColorCodes('&', "&8=-=-=-=-=-=-= &6Overview&8 =-=-=-=-=-=-=");
        final String listSeparator = ChatColor.translateAlternateColorCodes('&', "&8,&r ");
        final String lineSeparator = System.lineSeparator();

        // command: votes stats
        final CommandAPICommand statsCmd = new CommandAPICommand("stats")
                .withPermission(PERM_HOST)
                .executes((sender, args) -> {
                    Promise.supplyingAsync(() -> {
                        var stats = Objects.requireNonNull(GamePool.INSTANCE.getOrNull(), "current game is null").getStatistics();

                        // show list of valid & invalid raters
                        var validRatersCount = stats.getValidRaters().size();
                        var invalidRatersCount = stats.getInvalidRaters().size();
                        var joining = Collectors.joining(listSeparator);
                        var invalidRaters = stats.getInvalidRaters().stream().map(MainUtil::getPlayerName).collect(joining);
                        var validRaters = stats.getValidRaters().stream().map(MainUtil::getPlayerName).collect(joining);

                        var builder = new StringBuilder()
                                .append(title).append(lineSeparator)
                                .append(VoteMain.lang().get(sender, "chat-message.invalid-rater-list", "count", String.valueOf(invalidRatersCount), "list", invalidRaters)).append(lineSeparator)
                                .append(VoteMain.lang().get(sender, "chat-message.valid-rater-list", "count", String.valueOf(validRatersCount), "list", validRaters)).append(lineSeparator);

                        // show details of work ratings
                        builder.append(title).append(lineSeparator);
                        GamePool.INSTANCE.getOrNull().getWorks().forEach(work -> {
                            var uuid = work.getOwner();
                            var redVotesCount = stats.ofRedVotes(uuid).size();
                            var greenVotesCount = stats.ofGreenVotes(uuid).size();
                            var totalVotesCount = greenVotesCount + redVotesCount; // only the votes on this work
                            var greenVoteProportion = 100D * greenVotesCount / totalVotesCount;
                            builder.append(VoteMain.lang().get(sender, "chat-message.work-information-line").formatted(
                                    greenVotesCount, redVotesCount, totalVotesCount,
                                    greenVoteProportion, MainUtil.getPlayerName(uuid),
                                    work.isDone()
                                            ? VoteMain.lang().get(sender, "gui.work-listing.done")
                                            : VoteMain.lang().get(sender, "gui.work-listing.undone")));
                            builder.append(lineSeparator);
                        });

                        return builder.toString();
                    }).thenAcceptSync(sender::sendMessage);
                });

        // command: votes stats work <player>
        // show detailed ratings of a work
        final CommandAPICommand workCmd = new CommandAPICommand("work")
                .withArguments(new OfflinePlayerArgument("player").replaceSuggestions(ArgumentSuggestions.stringsAsync(info -> {
                    final var game = GamePool.INSTANCE.getOrNull();
                    if (game == null) {
                        return CompletableFuture.completedFuture(new String[]{"none"});
                    } else {
                        return CompletableFuture.supplyAsync(() -> game.getWorks().stream()
                                .map(Work::getOwnerName)
                                .filter(name -> name.startsWith(info.currentArg()))
                                .toArray(String[]::new));
                    }
                })))
                .executes((sender, args) -> {
                    Promise.supplyingAsync(() -> {
                        var stats = Objects.requireNonNull(GamePool.INSTANCE.getOrNull(), "current game is null").getStatistics();
                        var workOwner = ((OfflinePlayer) args[1]).getUniqueId();
                        var greenRatersCount = stats.ofGreenVotes(workOwner).size();
                        var redRatersCount = stats.ofRedVotes(workOwner).size();
                        var joining = Collectors.joining(listSeparator);
                        var greenRaters = stats.ofGreenVotes(workOwner).stream()
                                .map(Vote::getRaterName)
                                .collect(joining);
                        var redRaters = stats.ofRedVotes(workOwner).stream()
                                .map(Vote::getRaterName)
                                .collect(joining);
                        return title + lineSeparator +
                               VoteMain.lang().get(sender, "chat-message.green-rater-list", "count", String.valueOf(greenRatersCount), "list", greenRaters)
                               + lineSeparator +
                               VoteMain.lang().get(sender, "chat-message.red-rater-list", "count", String.valueOf(redRatersCount), "list", redRaters)
                               + lineSeparator;
                    }).thenAcceptSync(sender::sendMessage);
                });


        // command: votes stats rate <player>
        // show detailed ratings of a rater
        final CommandAPICommand rateCmd = new CommandAPICommand("rate")
                .withArguments(new OfflinePlayerArgument("player").replaceSuggestions(ArgumentSuggestions.stringsAsync(info -> {
                    final var game = GamePool.INSTANCE.getOrNull();
                    if (game == null) {
                        return CompletableFuture.completedFuture(new String[]{"none"});
                    } else {
                        return CompletableFuture.supplyAsync(() -> game.getStatistics().getRaters()
                                .map(MainUtil::getPlayerName)
                                .filter(name -> name.startsWith(info.currentArg()))
                                .toArray(String[]::new));
                    }
                })))
                .executes((sender, args) -> {
                    Promise.supplyingAsync(() -> {
                        var stats = Objects.requireNonNull(GamePool.INSTANCE.getOrNull(), "current game is null").getStatistics();
                        var raterUuid = ((OfflinePlayer) args[0]).getUniqueId();
                        var greenWorksCount = stats.ofGreenWorks(raterUuid).size();
                        var redWorksCount = stats.ofRedWorks(raterUuid).size();
                        var joining = Collectors.joining(listSeparator);
                        var greenWorks = stats.ofGreenWorks(raterUuid).stream().map(Work::getOwnerName).collect(joining);
                        var redWorks = stats.ofRedWorks(raterUuid).stream().map(Work::getOwnerName).collect(joining);
                        return title + lineSeparator +
                               VoteMain.lang().get(sender, "chat-message.green-work-list", "count", String.valueOf(greenWorksCount), "list", greenWorks)
                               + lineSeparator +
                               VoteMain.lang().get(sender, "chat-message.red-work-list", "count", String.valueOf(redWorksCount), "list", redWorks)
                               + lineSeparator;
                    }).thenAcceptSync(sender::sendMessage);
                });

        // combine and register command
        rootCmd.withSubcommands(
                        toggleCmd,
                        reloadCmd,
                        pullCmd,
                        purgeCmd,
                        cacheCmd,
                        dataCmd,
                        statsCmd.withSubcommands(
                                workCmd,
                                rateCmd))
                .register();
    }

    @NotNull
    private String[] getPlotWorldCompletions() {
        return Bukkit.getWorlds().stream()
                .filter(world -> VoteMain.inst().getPlots().isPlotWorld(world))
                .map(World::getName)
                .toArray(String[]::new);
    }

}
