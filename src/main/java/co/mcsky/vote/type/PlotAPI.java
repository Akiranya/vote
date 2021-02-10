package co.mcsky.vote.type;

import org.bukkit.World;

import java.util.Set;

public interface PlotAPI<T> {

    Set<Plot> getAllPlots();

    boolean isPlotWorld(World world);

    T internal();

}
