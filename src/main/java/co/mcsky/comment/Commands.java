package co.mcsky.comment;

import co.mcsky.comment.gui.ListingGui;
import co.mcsky.comment.io.GameFileHandlerPool;
import co.mcsky.comment.object.Artwork;
import co.mcsky.comment.object.Comment;
import co.mcsky.comment.object.Game;
import co.mcsky.comment.object.GamePool;
import co.mcsky.comment.util.MainUtil;
import co.mcsky.mewcore.skull.SkullCache;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.promise.Promise;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Commands {

    private static final String COMMAND_NAME = "votes";
    private static final CommandPermission PERM_ADMIN = CommandPermission.fromString("votes.admin");
    private static final CommandPermission PERM_HOST = CommandPermission.fromString("votes.host");

    public void register() {
        // command: votes
        final CommandAPICommand rootCmd = new CommandAPICommand(COMMAND_NAME)
                .executesPlayer((player, args) -> {
                    if (GamePool.INSTANCE.containsAny()) {
                        Promise.supplyingAsync(() -> new ListingGui(player, GamePool.INSTANCE.getOrNull())).thenAcceptSync(Gui::open);
                    } else {
                        player.sendMessage(Main.lang().get(player, "chat.comment-system-not-available"));
                    }
                });

        // command: votes toggle open
        final CommandAPICommand toggleCmd = new CommandAPICommand("toggle")
                .withPermission(PERM_ADMIN)
                .withArguments(new MultiLiteralArgument("open"))
                .executes((sender, args) -> {
                    final Game game = GamePool.INSTANCE.getOrNull();
                    if (game == null) {
                        sender.sendMessage(Main.lang().get(sender, "chat.comment-system-not-available"));
                        return;
                    }
                    game.setOpenVote(!game.isOpenVote());
                    sender.sendMessage(Main.lang().get(sender, "chat.mark-comment-system", "state", game.isOpenVote() ? Main.lang().get(sender, "misc.enabled") : Main.lang().get(sender, "misc.disabled")));
                });

        // command: votes reload
        final CommandAPICommand reloadCmd = new CommandAPICommand("reload")
                .withPermission(PERM_ADMIN)
                .executes((sender, args) -> {
                    Main.inst().reload();
                    sender.sendMessage(Main.lang().get(sender, "chat.plugin-reloaded"));
                });

        // command: votes pull <world>
        final CommandAPICommand pullCmd = new CommandAPICommand("pull")
                .withPermission(PERM_ADMIN)
                .withArguments(new StringArgument("world").replaceSuggestions(
                        ArgumentSuggestions.strings(getPlotWorldCompletions())
                ))
                .executes((sender, args) -> {
                    final World world = Bukkit.getWorld((String) args[0]);
                    if (world == null || !Main.inst().getPlots().isPlotWorld(world)) {
                        sender.sendMessage(Main.lang().get(sender, "chat.world-no-plots", "world", (String) args[0]));
                    } else {
                        if (GamePool.INSTANCE.register(world.getName())) {
                            sender.sendMessage(Main.lang().get(sender, "chat.plot-information-pulled"));
                        } else {
                            sender.sendMessage(Main.lang().get(sender, "chat.plot-information-pulled-already"));
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
                    if (world == null || !Main.inst().getPlots().isPlotWorld(world)) {
                        sender.sendMessage(Main.lang().get(sender, "chat.world-no-plots", "world", (String) args[0]));
                    } else {
                        if (GamePool.INSTANCE.unregister(world.getName())) {
                            sender.sendMessage(Main.lang().get(sender, "chat.plot-information-deleted"));
                        } else {
                            sender.sendMessage(Main.lang().get(sender, "chat.plot-information-deleted-already"));
                        }
                    }
                });

        // command: votes cache clear
        final CommandAPICommand cacheCmd = new CommandAPICommand("cache")
                .withPermission(PERM_ADMIN)
                .withArguments(new MultiLiteralArgument("clear"))
                .executes((sender, args) -> {
                    SkullCache.INSTANCE.clear();
                    sender.sendMessage(Main.lang().get(sender, "chat.skin-cache-cleared"));
                });

        // command: votes data <load|save>
        final CommandAPICommand dataCmd = new CommandAPICommand("data")
                .withPermission(PERM_ADMIN)
                .withArguments(new MultiLiteralArgument("load", "save"))
                .executes((sender, args) -> {
                    switch ((String) args[0]) {
                        case "load" -> {
                            GameFileHandlerPool.INSTANCE.readAll();
                            sender.sendMessage(Main.lang().get(sender, "chat.read-all"));
                        }
                        case "save" -> {
                            GameFileHandlerPool.INSTANCE.saveAll();
                            sender.sendMessage(Main.lang().get(sender, "chat.saved-all"));
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

                        // show list of valid & invalid reviewers
                        var validReviewersCount = stats.getValidReviewers().size();
                        var invalidReviewersCount = stats.getInvalidReviewers().size();
                        var joining = Collectors.joining(listSeparator);
                        var invalidReviewers = stats.getInvalidReviewers().stream().map(MainUtil::getPlayerName).collect(joining);
                        var validReviewers = stats.getValidReviewers().stream().map(MainUtil::getPlayerName).collect(joining);

                        var builder = new StringBuilder()
                                .append(title).append(lineSeparator)
                                .append(Main.lang().get(sender, "chat.invalid-reviewer-list", "count", String.valueOf(invalidReviewersCount), "list", invalidReviewers)).append(lineSeparator)
                                .append(Main.lang().get(sender, "chat.valid-reviewer-list", "count", String.valueOf(validReviewersCount), "list", validReviewers)).append(lineSeparator);

                        // show details of work ratings
                        builder.append(title).append(lineSeparator);
                        GamePool.INSTANCE.getOrNull().getWorks().forEach(work -> {
                            var uuid = work.getOwner();
                            var redVotesCount = stats.ofRedVotes(uuid).size();
                            var greenVotesCount = stats.ofGreenVotes(uuid).size();
                            var totalVotesCount = greenVotesCount + redVotesCount; // only the votes on this work
                            var greenVoteProportion = 100D * greenVotesCount / totalVotesCount;
                            builder.append(Main.lang().get(sender, "chat.work-information-line").formatted(
                                    greenVotesCount, redVotesCount, totalVotesCount,
                                    greenVoteProportion, MainUtil.getPlayerName(uuid),
                                    work.isDone()
                                            ? Main.lang().get(sender, "misc.done")
                                            : Main.lang().get(sender, "misc.undone")));
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
                                .map(Artwork::getOwnerName)
                                .filter(name -> name.startsWith(info.currentArg()))
                                .toArray(String[]::new));
                    }
                })))
                .executes((sender, args) -> {
                    Promise.supplyingAsync(() -> {
                        var stats = Objects.requireNonNull(GamePool.INSTANCE.getOrNull(), "current game is null").getStatistics();
                        var workOwner = ((OfflinePlayer) args[1]).getUniqueId();
                        var greenReviewersCount = stats.ofGreenVotes(workOwner).size();
                        var redReviewersCount = stats.ofRedVotes(workOwner).size();
                        var joining = Collectors.joining(listSeparator);
                        var greenReviewers = stats.ofGreenVotes(workOwner).stream()
                                .map(Comment::getReviewerName)
                                .collect(joining);
                        var redReviewers = stats.ofRedVotes(workOwner).stream()
                                .map(Comment::getReviewerName)
                                .collect(joining);
                        return title + lineSeparator +
                               Main.lang().get(sender, "chat.green-reviewer-list", "count", String.valueOf(greenReviewersCount), "list", greenReviewers)
                               + lineSeparator +
                               Main.lang().get(sender, "chat.red-reviewer-list", "count", String.valueOf(redReviewersCount), "list", redReviewers)
                               + lineSeparator;
                    }).thenAcceptSync(sender::sendMessage);
                });


        // command: votes stats rate <player>
        // show detailed ratings of a reviewer
        final CommandAPICommand rateCmd = new CommandAPICommand("rate")
                .withArguments(new OfflinePlayerArgument("player").replaceSuggestions(ArgumentSuggestions.stringsAsync(info -> {
                    final var game = GamePool.INSTANCE.getOrNull();
                    if (game == null) {
                        return CompletableFuture.completedFuture(new String[]{"none"});
                    } else {
                        return CompletableFuture.supplyAsync(() -> game.getStatistics().getReviewers()
                                .map(MainUtil::getPlayerName)
                                .filter(name -> name.startsWith(info.currentArg()))
                                .toArray(String[]::new));
                    }
                })))
                .executes((sender, args) -> {
                    Promise.supplyingAsync(() -> {
                        var stats = Objects.requireNonNull(GamePool.INSTANCE.getOrNull(), "current game is null").getStatistics();
                        var reviewerUuid = ((OfflinePlayer) args[0]).getUniqueId();
                        var greenWorksCount = stats.ofGreenWorks(reviewerUuid).size();
                        var redWorksCount = stats.ofRedWorks(reviewerUuid).size();
                        var joining = Collectors.joining(listSeparator);
                        var greenWorks = stats.ofGreenWorks(reviewerUuid).stream().map(Artwork::getOwnerName).collect(joining);
                        var redWorks = stats.ofRedWorks(reviewerUuid).stream().map(Artwork::getOwnerName).collect(joining);
                        return title + lineSeparator +
                               Main.lang().get(sender, "chat.green-work-list", "count", String.valueOf(greenWorksCount), "list", greenWorks)
                               + lineSeparator +
                               Main.lang().get(sender, "chat.red-work-list", "count", String.valueOf(redWorksCount), "list", redWorks)
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
                .filter(world -> Main.inst().getPlots().isPlotWorld(world))
                .map(World::getName)
                .toArray(String[]::new);
    }

}
