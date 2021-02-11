package co.mcsky.vote.type.plotsquared;

import co.mcsky.vote.type.Plot;
import co.mcsky.vote.type.Plots;
import co.mcsky.vote.type.PlotFactory;
import org.bukkit.World;

import java.util.Set;
import java.util.stream.Collectors;

public class PlotSquaredPlotAPI implements Plots {

    private final com.plotsquared.core.api.PlotAPI api;

    public PlotSquaredPlotAPI() {
        this.api = new com.plotsquared.core.api.PlotAPI();
    }

    @Override
    public Set<Plot> getAllPlots() {
        return this.api.getAllPlots().stream()
                .map(PlotFactory::of)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isPlotWorld(World world) {
        return this.api.getPlotSquared().getPlotAreas(world.getName()).stream()
                .flatMap(plotArea -> plotArea.getPlots().stream())
                .map(com.plotsquared.core.plot.Plot::hasOwner) // only count owned plots
                .findAny()
                .isPresent();
    }

}
