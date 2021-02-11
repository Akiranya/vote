package co.mcsky.vote.type;

import co.mcsky.vote.type.plotsquared.PlotSquaredPlotAPI;

public class PlotsFactory {

    public static Plots create() {
        return new PlotSquaredPlotAPI();
    }
}
