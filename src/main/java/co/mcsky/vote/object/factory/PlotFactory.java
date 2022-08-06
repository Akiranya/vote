package co.mcsky.vote.object.factory;

import co.mcsky.vote.object.GamePlot;
import co.mcsky.vote.object.plotsquared.PlotSquaredPlot;
import com.plotsquared.core.plot.Plot;

public class PlotFactory {

    public static <T> GamePlot of(T plot) {
        if (plot instanceof Plot) {
            return new PlotSquaredPlot((Plot) plot);
        }
        // (adds new condition for additional API as needed)

        throw new IllegalStateException();
    }
}
