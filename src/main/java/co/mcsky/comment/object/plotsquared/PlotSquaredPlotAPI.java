package co.mcsky.comment.object.plotsquared;

import co.mcsky.comment.object.GamePlot;
import co.mcsky.comment.object.GamePlots;
import co.mcsky.comment.object.factory.PlotFactory;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.plot.Plot;
import org.bukkit.World;

import java.util.Set;
import java.util.stream.Collectors;

public final class PlotSquaredPlotAPI implements GamePlots {

    private final PlotAPI api;

    public PlotSquaredPlotAPI() {
        this.api = new PlotAPI();
    }

    @Override
    public Set<GamePlot> getAllPlots() {
        return this.api.getAllPlots().stream().map(PlotFactory::of).collect(Collectors.toSet());
    }

    @Override
    public boolean isPlotWorld(World world) {
        return this.api.getPlotAreas(world.getName()).stream()
                .flatMap(plotArea -> plotArea.getPlots().stream())
                .map(Plot::hasOwner) // only count owned plots
                .findAny()
                .isPresent();
    }

}
