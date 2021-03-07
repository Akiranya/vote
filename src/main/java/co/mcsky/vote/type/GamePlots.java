package co.mcsky.vote.type;

import org.bukkit.World;

import java.util.Set;

/**
 * Represents an abstract plot manager.
 */
public interface GamePlots {

    /**
     * Get all plots in this plot system.
     *
     * @return all plots in this plot system.
     */
    Set<GamePlot> getAllPlots();

    /**
     * Check if the world is a plot world. A world is a plot world if and only if the world has at least one plot which
     * has owner.
     *
     * @param world the world to check
     * @return true if the world is a plot world
     */
    boolean isPlotWorld(World world);

}
