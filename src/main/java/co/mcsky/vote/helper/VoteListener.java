package co.mcsky.vote.helper;

import co.mcsky.vote.VoteMain;
import co.mcsky.vote.Votes;
import co.mcsky.vote.events.PlayerVoteEvent;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;

import javax.annotation.Nonnull;

/**
 * The {@link VoteListener} must associate with an instance of {@link Votes} (one-to-one relationship).
 */
public class VoteListener implements TerminableModule {

    private final Votes votes;

    public VoteListener(Votes votes) {
        this.votes = votes;
    }

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        // Don't allow to vote if the vote system is not ready yet
        Events.subscribe(PlayerVoteEvent.class)
                .filter(e -> !this.votes.getPlugin().config.allowVoteWhenNotEnded)
                .filter(e -> !this.votes.ready())
                .handler(e -> {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(VoteMain.plugin.getMessage(e.getPlayer(), "chat-message.cannot-vote-for-game-not-ended"));
                })
                .bindWith(consumer);
        // Don't allow to vote if the work is undone yet
        Events.subscribe(PlayerVoteEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> !this.votes.getPlugin().config.allowVoteWhenUndone)
                .filter(e -> !e.getWork().done())
                .handler(e -> {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(VoteMain.plugin.getMessage(e.getPlayer(), "chat-message.cannot-vote-for-work-undone"));
                })
                .bindWith(consumer);
    }

}
