package co.mcsky.vote.type;

import org.bukkit.World;

import java.util.Set;

public interface PlotAPI<T> {

    /**
     * Get all plots in this plot system.
     *
     * @return all plots in this plot system.
     */
    Set<Plot> getAllPlots();

    /**
     * Check if the world is a plot world. A world is a plot world if and only if the world has at least one plot which
     * has owner.
     *
     * @param world the world to check
     * @return true if the world is a plot world
     */
    boolean isPlotWorld(World world);

    /**
     * Gets the backed internal API.
     *
     * @return the backed internal API.
     */
    T internal();

}
