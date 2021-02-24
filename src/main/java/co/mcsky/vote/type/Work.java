package co.mcsky.vote.type;

import co.mcsky.vote.util.PlayerUtil;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents an architecture work.
 */
public class Work {
    // The UUID of the owner of this work
    private final UUID owner;

    // All the votes this work got (can include the owner)
    private final Set<Vote> votes;

    // The plot related to this work
    private final Plot plot;

    public Work(UUID owner, Plot plot) {
        this.owner = owner;
        this.votes = new HashSet<>();
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
        return PlayerUtil.getName(this.owner);
    }

    /**
     * @return the plot related to this work
     */
    public Plot getPlot() {
        return this.plot;
    }

    /**
     * @return all votes of this work
     */
    public Set<Vote> getVotes() {
        return this.votes;
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
     * Votes this work with the given vote data.
     *
     * @param vote the vote data
     */
    public void vote(Vote vote) {
        // Raters can change their votes
        this.votes.remove(vote);
        this.votes.add(vote);
    }

    /**
     * @param rater the owner of the vote
     * @return true, if the given vote owner already voted this work, otherwise false
     */
    public boolean voted(UUID rater) {
        return this.votes.stream()
                .map(Vote::getRater)
                .anyMatch(owner -> owner.equals(rater));
    }

    /**
     * @param rater the owner of the vote
     * @return true, if the given vote owner did not vote this work, otherwise false
     */
    public boolean invoted(UUID rater) {
        return this.votes.stream()
                .map(Vote::getRater)
                .noneMatch(owner -> owner.equals(rater));
    }

    /**
     * @param rater the owner of the vote
     * @return true, if the given vote owner gave a green vote (present vote) for this work, otherwise false
     */
    public boolean present(UUID rater) {
        return this.votes.stream()
                .filter(vote -> vote.getRater().equals(rater))
                .anyMatch(Vote::isPresent);
    }

    /**
     * @param rater the owner of the vote
     * @return true, if the given vote owner gave a red vote (absent vote) for this work, otherwise false
     */
    public boolean absent(UUID rater) {
        return this.votes.stream()
                .filter(vote -> vote.getRater().equals(rater))
                .anyMatch(Vote::isAbsent);
    }
}
