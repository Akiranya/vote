package co.mcsky.comment.object;

import co.mcsky.comment.util.MainUtil;

import java.util.UUID;

/**
 * Represents a single comment.
 */
public class Comment {

    // The UUID of the owner of this comment
    private final UUID reviewer;

    // Whether this comment is marked as absent or not
    private final boolean absent;

    public Comment(UUID reviewer, boolean absent) {
        this.reviewer = reviewer;
        this.absent = absent;
    }

    /**
     * @return the owner of this comment
     */
    public UUID getReviewer() {
        return this.reviewer;
    }

    /**
     * @return the name of this comment owner
     */
    public String getReviewerName() {
        return MainUtil.getPlayerName(this.reviewer);
    }

    /**
     * @return true, if the comment is marked as present, otherwise false
     */
    public boolean isPresent() {
        return !this.absent;
    }

    /**
     * @return true, if the comment is marked as absent, otherwise false
     */
    public boolean isAbsent() {
        return this.absent;
    }

    @Override
    public int hashCode() {
        return this.reviewer.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return reviewer.equals(comment.reviewer);
    }
}
