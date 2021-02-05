package co.mcsky.vote.event;

import co.mcsky.vote.type.Votes;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

@SuppressWarnings("NullableProblems")
public class PlayerVoteDoneEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Votes votes;

    public PlayerVoteDoneEvent(Player who, Votes votes) {
        super(who);
        this.votes = votes;
    }

    public Votes getVotes() {
        return votes;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
