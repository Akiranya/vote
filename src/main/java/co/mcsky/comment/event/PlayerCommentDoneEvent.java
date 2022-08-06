package co.mcsky.comment.event;

import co.mcsky.comment.object.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerCommentDoneEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Game votes;

    public PlayerCommentDoneEvent(Player who, Game votes) {
        super(who);
        this.votes = votes;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Game getVotes() {
        return votes;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
