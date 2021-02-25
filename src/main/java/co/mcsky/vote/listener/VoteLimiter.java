package co.mcsky.vote.listener;

import co.mcsky.vote.event.PlayerVoteDoneEvent;
import co.mcsky.vote.event.PlayerVoteSubmitEvent;
import co.mcsky.vote.type.Game;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;

import javax.annotation.Nonnull;

import static co.mcsky.vote.VoteMain.plugin;

/**
 * The {@link VoteLimiter} must associate with an instance of {@link Game} (one-to-one relationship).
 */
public class VoteLimiter implements TerminableModule {

    private final Game game;

    public VoteLimiter(Game game) {
        this.game = game;
    }

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        // Stop voting if the vote system is not ready yet
        Events.subscribe(PlayerVoteSubmitEvent.class)
                .filter(e -> game.getWorld().equalsIgnoreCase(e.getVotes().getWorld()))
                .filter(e -> !plugin.config.allowVoteWhenNotEnded)
                .filter(e -> !game.isReady())
                .handler(e -> {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(plugin.getMessage(e.getPlayer(), "chat-message.cannot-vote-for-game-not-ended"));
                })
                .bindWith(consumer);

        // Stop voting if the work is undone yet
        Events.subscribe(PlayerVoteSubmitEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> game.getWorld().equalsIgnoreCase(e.getVotes().getWorld()))
                .filter(e -> !plugin.config.allowVoteWhenUndone)
                .filter(e -> !e.getWork().isDone())
                .handler(e -> {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(plugin.getMessage(e.getPlayer(), "chat-message.cannot-vote-for-work-undone"));
                })
                .bindWith(consumer);

        // Stop use 'done' button if the game is not ended
        Events.subscribe(PlayerVoteDoneEvent.class)
                .filter(e -> game.getWorld().equalsIgnoreCase(e.getVotes().getWorld()))
                .filter(e -> !game.isReady())
                .handler(e -> {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(plugin.getMessage(e.getPlayer(), "chat-message.cannot-done-for-game-not-ended"));
                })
                .bindWith(consumer);

    }

}
