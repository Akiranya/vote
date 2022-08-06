package co.mcsky.comment.object.factory;

import co.mcsky.comment.object.GamePlot;
import co.mcsky.comment.object.plotsquared.PlotSquaredPlot;
import com.plotsquared.core.plot.Plot;

public final class PlotFactory {

    public static <T> GamePlot of(T plot) {
        if (plot instanceof Plot) {
            return new PlotSquaredPlot((Plot) plot);
        }
        // (adds new condition for additional API as needed)

        throw new IllegalStateException();
    }
}
