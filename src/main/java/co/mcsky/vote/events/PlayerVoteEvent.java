package co.mcsky.vote.events;

import co.mcsky.vote.Votes;
import co.mcsky.vote.type.Vote;
import co.mcsky.vote.type.Work;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerVoteEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final Work work;
    private final Vote vote;

    private final Votes votes;

    public PlayerVoteEvent(Player who, Work work, Vote vote, Votes votes) {
        super(who);
        this.work = work;
        this.vote = vote;
        this.votes = votes;
    }

    public Work getWork() {
        return work;
    }

    public Vote getVote() {
        return vote;
    }

    public Votes getVotes() {
        return votes;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
