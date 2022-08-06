package co.mcsky.comment.object.factory;

import co.mcsky.comment.object.GamePlots;
import co.mcsky.comment.object.plotsquared.PlotSquaredPlotAPI;

public final class PlotsFactory {

    public static GamePlots create() {
        return new PlotSquaredPlotAPI();
    }
}
