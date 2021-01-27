package co.mcsky.vote.helper;

import co.mcsky.vote.type.Votes;
import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.events.PlayerClaimPlotEvent;
import com.plotsquared.core.events.PlotDeleteEvent;
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
        if (validateWorld(event.getPlot().getWorldName())) {
            UUID workOwner = event.getPlotPlayer().getUUID();
            Plot plot = event.getPlot();
            this.votes.createEntry(workOwner, plot);
            this.votes.getPlugin().getLogger().info("[VoteUpdater] New entry created : " + event.getPlotPlayer().getName());
        }
    }

    // This event is too buggy...
//    @Subscribe
//    public void onPlayerAutoPlot(PlayerAutoPlotEvent event) {
//        if (validateWorld(event.getPlotArea().getWorldName())) {
//            UUID workOwner = event.getPlayer().getUUID();
//            Plot plot = event.getPlot();
//            this.votes.createEntry(workOwner, plot);
//            this.votes.getPlugin().getLogger().info("[VoteUpdater] New entry created : " + event.getPlayer().getName());
//        }
//    }

    @Subscribe
    public void onPlotDelete(PlotDeleteEvent event) {
        if (validateWorld(event.getWorld())) {
            Optional.ofNullable(event.getPlot().getOwnerAbs()).ifPresent(workOwner -> {
                this.votes.deleteEntry(workOwner);
                this.votes.getPlugin().getLogger().info("[VoteUpdater] Entry removed : " + event.getPlotId().toString());
            });
        }
    }

    private boolean validateWorld(String plotWorld) {
        return this.votes.getPlotWorld().equalsIgnoreCase(plotWorld);
    }

    @Override
    public void close() throws Exception {
        this.votes.getApi().getPlotSquared().getEventDispatcher().unregisterListener(this);
    }
}
