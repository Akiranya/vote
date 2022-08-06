package co.mcsky.comment.event;

import co.mcsky.comment.object.Artwork;
import co.mcsky.comment.object.Comment;
import co.mcsky.comment.object.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerCommentSubmitEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Artwork artwork;
    private final Comment comment;
    private final Game game;

    public PlayerCommentSubmitEvent(Player who, Artwork artwork, Comment comment, Game game) {
        super(who);
        this.artwork = artwork;
        this.comment = comment;
        this.game = game;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Artwork getWork() {
        return artwork;
    }

    public Comment getVote() {
        return comment;
    }

    public Game getGame() {
        return game;
    }

    @NotNull
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
