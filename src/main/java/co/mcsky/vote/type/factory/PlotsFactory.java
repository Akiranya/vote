package co.mcsky.vote.type.factory;

import co.mcsky.vote.type.GamePlots;
import co.mcsky.vote.type.plotsquared.PlotSquaredPlotAPI;

public class PlotsFactory {

    public static GamePlots create() {
        return new PlotSquaredPlotAPI();
    }
}
