package co.mcsky.vote.listener;

import co.mcsky.vote.event.PlayerVoteDoneEvent;
import co.mcsky.vote.type.Votes;
import co.mcsky.vote.event.PlayerVoteSubmitEvent;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;

import javax.annotation.Nonnull;

import static co.mcsky.vote.VoteMain.*;

/**
 * The {@link VoteLimiter} must associate with an instance of {@link Votes} (one-to-one relationship).
 */
public class VoteLimiter implements TerminableModule {

    private final Votes votes;

    public VoteLimiter(Votes votes) {
        this.votes = votes;
    }

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        // Stop voting if the vote system is not ready yet
        Events.subscribe(PlayerVoteSubmitEvent.class)
                .filter(e -> votes.getWorld().equalsIgnoreCase(e.getVotes().getWorld()))
                .filter(e -> !plugin.config.allowVoteWhenNotEnded)
                .filter(e -> !votes.isReady())
                .handler(e -> {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(plugin.getMessage(e.getPlayer(), "chat-message.cannot-vote-for-game-not-ended"));
                })
                .bindWith(consumer);

        // Stop voting if the work is undone yet
        Events.subscribe(PlayerVoteSubmitEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> votes.getWorld().equalsIgnoreCase(e.getVotes().getWorld()))
                .filter(e -> !plugin.config.allowVoteWhenUndone)
                .filter(e -> !e.getWork().isDone())
                .handler(e -> {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(plugin.getMessage(e.getPlayer(), "chat-message.cannot-vote-for-work-undone"));
                })
                .bindWith(consumer);

        // Stop use 'done' button if the game is not ended
        Events.subscribe(PlayerVoteDoneEvent.class)
                .filter(e -> votes.getWorld().equalsIgnoreCase(e.getVotes().getWorld()))
                .filter(e -> !votes.isReady())
                .handler(e -> {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(plugin.getMessage(e.getPlayer(), "chat-message.cannot-done-for-game-not-ended"));
                })
                .bindWith(consumer);

    }

}
