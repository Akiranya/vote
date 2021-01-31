package co.mcsky.vote.listener;

import co.mcsky.vote.VoteMain;
import co.mcsky.vote.type.Votes;
import co.mcsky.vote.event.PlayerVoteEvent;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;

import javax.annotation.Nonnull;

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
        // Don't allow to vote if the vote system is not ready yet
        Events.subscribe(PlayerVoteEvent.class)
                .filter(e -> votes.getPlotWorld().equalsIgnoreCase(e.getVotes().getPlotWorld()))
                .filter(e -> !VoteMain.plugin.config.allowVoteWhenNotEnded)
                .filter(e -> !votes.isReady())
                .handler(e -> {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(VoteMain.plugin.getMessage(e.getPlayer(), "chat-message.cannot-vote-for-game-not-ended"));
                })
                .bindWith(consumer);

        // Don't allow to vote if the work is undone yet
        Events.subscribe(PlayerVoteEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> votes.getPlotWorld().equalsIgnoreCase(e.getVotes().getPlotWorld()))
                .filter(e -> !VoteMain.plugin.config.allowVoteWhenUndone)
                .filter(e -> !e.getWork().isDone())
                .handler(e -> {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(VoteMain.plugin.getMessage(e.getPlayer(), "chat-message.cannot-vote-for-work-undone"));
                })
                .bindWith(consumer);
    }

}
