package co.mcsky.vote.type;

import co.mcsky.vote.util.MainUtil;

import java.util.UUID;

/**
 * Represents a single vote.
 */
public class Vote {
    // The UUID of the owner of this vote
    private final UUID rater;

    // Whether this vote is marked as absent or not
    private final boolean absent;

    public Vote(UUID rater, boolean absent) {
        this.rater = rater;
        this.absent = absent;
    }

    /**
     * @return the owner of this vote
     */
    public UUID getRater() {
        return this.rater;
    }

    /**
     * @return the name of this vote owner
     */
    public String getRaterName() {
        return MainUtil.getPlayerName(this.rater);
    }

    /**
     * @return true, if the vote is marked as absent, otherwise false
     */
    public boolean isAbsent() {
        return this.absent;
    }

    /**
     * @return true, if the vote is marked as present, otherwise false
     */
    public boolean isPresent() {
        return !this.absent;
    }

    @Override
    public int hashCode() {
        return this.rater.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vote vote = (Vote) o;
        return rater.equals(vote.rater);
    }
}
