package co.mcsky.vote.object;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents an abstract single plot.
 */
public interface GamePlot {

    /**
     * Teleports the player to this plot.
     *
     * @param player the player to be teleported
     */
    void teleport(Player player);

    /**
     * Gets the UUID of this plot owner.
     *
     * @return the UUID of this plot owner
     */
    UUID getOwner();

    /**
     * Gets the name of this plot owner.
     *
     * @return the name of this plot owner
     */
    String getOwnerName();

    /**
     * Check if this plot has owner.
     *
     * @return true if this plot has owner
     */
    boolean hasOwner();

    /**
     * Check if this plot is marked as done.
     *
     * @return true if this plot is marked as done
     */
    boolean isDone();

    /**
     * Gets the name of the world where this plot is in.
     *
     * @return the name of the world where this plot is in
     */
    String getWorldName();

    /**
     * Gets the ID of this plot. The ID should be distinct to different plots.
     *
     * @return the ID of this plot
     */
    String getId();

}
