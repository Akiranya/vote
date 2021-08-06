package co.mcsky.vote.object.factory;

import co.mcsky.vote.object.GamePlots;
import co.mcsky.vote.object.plotsquared.PlotSquaredPlotAPI;

public class PlotsFactory {

    public static GamePlots create() {
        return new PlotSquaredPlotAPI();
    }
}
