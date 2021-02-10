package co.mcsky.vote.type.plotsquared;

import co.mcsky.vote.type.Plot;
import co.mcsky.vote.type.PlotAPI;
import org.bukkit.World;

import java.util.Set;
import java.util.stream.Collectors;

public class PlotSquaredPlotAPI implements PlotAPI<com.plotsquared.core.api.PlotAPI> {

    private final com.plotsquared.core.api.PlotAPI api;

    public PlotSquaredPlotAPI() {
        this.api = new com.plotsquared.core.api.PlotAPI();
    }

    @Override
    public Set<Plot> getAllPlots() {
        return this.api.getAllPlots().stream()
                .map(PlotSquaredPlot::of)
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

    @Override
    public com.plotsquared.core.api.PlotAPI internal() {
        return this.api;
    }

}
