package co.mcsky.comment.object;

import co.mcsky.comment.util.MainUtil;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents an architecture work.
 */
public class Artwork {
    // The UUID of the owner of this work
    private final UUID owner;

    // All the votes this work got (can include the owner)
    private final Set<Comment> comments;

    // The plot related to this work
    private final GamePlot plot;

    public Artwork(UUID owner, GamePlot plot) {
        this.owner = owner;
        this.comments = new HashSet<>();
        this.plot = plot;
    }

    /**
     * @return the owner of this work
     */
    public UUID getOwner() {
        return this.owner;
    }

    /**
     * @return the name of this work owner
     */
    public String getOwnerName() {
        return MainUtil.getPlayerName(this.owner);
    }

    /**
     * @return the plot related to this work
     */
    public GamePlot getPlot() {
        return this.plot;
    }

    /**
     * @return all votes of this work
     */
    public Set<Comment> getVotes() {
        return this.comments;
    }

    /**
     * @return true, if this work is marked as done, otherwise false
     */
    public boolean isDone() {
        return this.plot.isDone();
    }

    /**
     * @param player the player to be teleported to this work
     */
    public void teleport(Player player) {
        this.plot.teleport(player);
    }

    /**
     * Votes this work with the given comment data.
     *
     * @param comment the comment data
     */
    public void comment(Comment comment) {
        // Reviewers can change their votes
        this.comments.remove(comment);
        this.comments.add(comment);
    }

    /**
     * @param reviewer the owner of the comment
     * @return true, if the given comment owner already voted this work, otherwise false
     */
    public boolean hasVoted(UUID reviewer) {
        return this.comments.stream()
                .map(Comment::getReviewer)
                .anyMatch(owner -> owner.equals(reviewer));
    }

    /**
     * @param reviewer the owner of the comment
     * @return true, if the given comment owner did not comment this work, otherwise false
     */
    public boolean hasNotVoted(UUID reviewer) {
        return this.comments.stream()
                .map(Comment::getReviewer)
                .noneMatch(owner -> owner.equals(reviewer));
    }

    /**
     * @param reviewer the owner of the comment
     * @return true, if the specific reviewer gave a green comment (present comment) for this work, otherwise false
     */
    public boolean isPresent(UUID reviewer) {
        return this.comments.stream()
                .filter(comment -> comment.getReviewer().equals(reviewer))
                .anyMatch(Comment::isPresent);
    }

    /**
     * @param reviewer the owner of the comment
     * @return true, if the specific reviewer gave a red comment (absent comment) for this work, otherwise false
     */
    public boolean isAbsent(UUID reviewer) {
        return this.comments.stream()
                .filter(comment -> comment.getReviewer().equals(reviewer))
                .anyMatch(Comment::isAbsent);
    }
}
