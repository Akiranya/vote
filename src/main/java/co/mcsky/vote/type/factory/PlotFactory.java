package co.mcsky.vote.type.factory;

import co.mcsky.vote.type.Plot;
import co.mcsky.vote.type.plotsquared.PlotSquaredPlot;

public class PlotFactory {

    public static <T> Plot of(T plot) {
        if (plot instanceof com.plotsquared.core.plot.Plot) {
            return new PlotSquaredPlot((com.plotsquared.core.plot.Plot) plot);
        }
        // if... (adds new condition for additional API as needed)

        throw new IllegalStateException();
    }
}
