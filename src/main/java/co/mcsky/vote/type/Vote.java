package co.mcsky.vote.type;

import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * Represents a single vote.
 */
public class Vote {
    // The UUID of the owner of this vote
    private final UUID rater;

    // Whether this vote is marked as absent or not
    private final boolean absent;

    /**
     * Vote can be only constructed with {@link VoteBuilder}.
     */
    private Vote(UUID rater, boolean absent) {
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
        return Players.getOffline(rater).map(OfflinePlayer::getName).orElse("Not Cached");
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

    public static VoteBuilder create(UUID rater) {
        return new VoteBuilder(rater);
    }

    /**
     * Builder for {@link Vote}.
     */
    public static class VoteBuilder {
        private final UUID owner;
        private boolean absent;

        private VoteBuilder(UUID owner) {
            this.owner = owner;
            this.absent = false;
        }

        public VoteBuilder absent(boolean absent) {
            this.absent = absent;
            return this;
        }

        public Vote build() {
            return new Vote(owner, absent);
        }
    }
}
