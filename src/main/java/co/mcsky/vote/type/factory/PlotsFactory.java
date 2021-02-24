package co.mcsky.vote.type.factory;

import co.mcsky.vote.type.Plots;
import co.mcsky.vote.type.plotsquared.PlotSquaredPlotAPI;

public class PlotsFactory {

    public static Plots create() {
        return new PlotSquaredPlotAPI();
    }
}
