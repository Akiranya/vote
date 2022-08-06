package co.mcsky.comment.listener;

import co.mcsky.comment.Main;
import co.mcsky.comment.event.PlayerCommentDoneEvent;
import co.mcsky.comment.event.PlayerCommentSubmitEvent;
import co.mcsky.comment.object.Game;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;

import javax.annotation.Nonnull;

/**
 * The {@link CommentLimiter} must associate with an instance of {@link Game}
 * (one-to-one relationship).
 */
public final class CommentLimiter implements TerminableModule {

    private final Game game;

    public CommentLimiter(Game game) {
        this.game = game;
    }

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        // Stop voting if the comment system is not ready yet
        Events.subscribe(PlayerCommentSubmitEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> game.getWorld().equalsIgnoreCase(e.getGame().getWorld()))
                .filter(e -> Main.config().getVoteConditionEnded() && !game.isOpenVote())
                .handler(e -> {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(Main.lang().get(e.getPlayer(), "chat.cannot-comment-for-game-not-ended"));
                })
                .bindWith(consumer);

        // Stop voting if the work is undone yet
        Events.subscribe(PlayerCommentSubmitEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> game.getWorld().equalsIgnoreCase(e.getGame().getWorld()))
                .filter(e -> Main.config().getVoteConditionDone() && !e.getWork().isDone())
                .handler(e -> {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(Main.lang().get(e.getPlayer(), "chat.cannot-comment-for-work-undone"));
                })
                .bindWith(consumer);

        // Stop use 'done' button if the game is not ended
        Events.subscribe(PlayerCommentDoneEvent.class)
                .filter(e -> game.getWorld().equalsIgnoreCase(e.getVotes().getWorld()))
                .filter(e -> !game.isOpenVote())
                .handler(e -> {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(Main.lang().get(e.getPlayer(), "chat.cannot-done-for-game-not-ended"));
                })
                .bindWith(consumer);
    }

}
