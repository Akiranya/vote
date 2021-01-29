package co.mcsky.vote.type;

import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static co.mcsky.vote.VoteMain.plugin;

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

    private Work(UUID owner, Set<Vote> votes, Plot plot) {
        this.owner = owner;
        this.votes = votes;
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
        return Players.getOffline(owner).map(OfflinePlayer::getName).orElse("Not Cached");
    }

    /**
     * @return the plot related to this work
     */
    public Plot getPlot() {
        return plot;
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
        return DoneFlag.isDone(this.plot);
    }

    /**
     * @param player the player to be teleported to this work
     */
    public void teleport(Player player) {
        this.plot.teleportPlayer(
                PlotPlayer.wrap(player),
                TeleportCause.PLUGIN,
                b -> player.sendMessage(plugin.getMessage(player, "gui-message.teleport-to-plot", "player", getOwnerName()))
        );
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

    public static WorkBuilder create(UUID work, Plot plot) {
        return new WorkBuilder(work, plot);
    }

    public static class WorkBuilder {

        private final UUID owner;
        private final Set<Vote> votes;
        private Plot plot;

        private WorkBuilder(UUID owner, Plot plot) {
            this.owner = owner;
            this.votes = new HashSet<>();
            this.plot = plot;
        }

        public WorkBuilder vote(Vote vote) {
            this.votes.add(vote);
            return this;
        }

        public WorkBuilder plot(Plot plot) {
            this.plot = plot;
            return this;
        }

        public Work build() {
            return new Work(this.owner, this.votes, this.plot);
        }

    }

}
