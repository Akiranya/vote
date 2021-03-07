package co.mcsky.vote.type.factory;

import co.mcsky.vote.type.GamePlot;
import co.mcsky.vote.type.plotsquared.PlotSquaredPlot;
import com.plotsquared.core.plot.Plot;

public class PlotFactory {

    public static <T> GamePlot of(T plot) {
        if (plot instanceof Plot) {
            return new PlotSquaredPlot((Plot) plot);
        }
        // if... (adds new condition for additional API as needed)

        throw new IllegalStateException();
    }
}
