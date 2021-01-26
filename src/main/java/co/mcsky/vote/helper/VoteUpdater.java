package co.mcsky.vote.helper;

import co.mcsky.vote.Votes;
import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.events.PlayerAutoPlotEvent;
import com.plotsquared.core.events.PlayerClaimPlotEvent;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.events.PlotEvent;
import com.plotsquared.core.plot.Plot;
import me.lucko.helper.terminable.Terminable;

import java.util.Optional;
import java.util.UUID;

/**
 * The {@link VoteUpdater} must associate with an instance of {@link Votes} (one-to-one relationship).
 */
@SuppressWarnings("UnstableApiUsage")
public class VoteUpdater implements Terminable {

    private final Votes votes;

    public VoteUpdater(Votes votes) {
        this.votes = votes;
        this.votes.getApi().registerListener(this);
    }

    @Subscribe
    public void onPlayerClaimPlot(PlayerClaimPlotEvent event) {
        if (validateWorld(event)) {
            UUID workOwner = event.getPlotPlayer().getUUID();
            Plot plot = event.getPlot();
            this.votes.createEntry(workOwner, plot);
        }
    }

    @Subscribe
    public void onPlayerAutoPlot(PlayerAutoPlotEvent event) {
        if (validateWorld(event)) {
            UUID workOwner = event.getPlayer().getUUID();
            Plot plot = event.getPlot();
            this.votes.createEntry(workOwner, plot);
        }
    }

    @Subscribe
    public void onPlotDelete(PlotDeleteEvent event) {
        if (validateWorld(event)) {
            Optional.ofNullable(event.getPlot().getOwnerAbs()).ifPresent(votes::deleteEntry);
        }
    }

    private boolean validateWorld(PlotEvent event) {
        return votes.getWorldName().equalsIgnoreCase(event.getPlot().getWorldName());
    }

    @Override
    public void close() throws Exception {
        this.votes.getApi().getPlotSquared().getEventDispatcher().unregisterListener(this);
    }
}
