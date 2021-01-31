package co.mcsky.vote.listener;

import co.mcsky.vote.VoteMain;
import co.mcsky.vote.type.Votes;
import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.events.PlayerClaimPlotEvent;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import me.lucko.helper.terminable.Terminable;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * The {@link WorkUpdater} must associate with an instance of {@link Votes} (one-to-one relationship).
 */
@SuppressWarnings("UnstableApiUsage")
public class WorkUpdater implements Terminable {

    private final Votes votes;
    private final Logger logger;

    public WorkUpdater(Votes votes) {
        this.votes = votes;
        this.logger = VoteMain.plugin.getLogger();
        VoteMain.plotApi.registerListener(this);
    }

    // Warning: this event is fired before the plot is actually claimed by the player
    // so it is necessary to check if the player can really claim this plot.
    // Also, the javadoc says nothing when exactly the event is fired!
    @Subscribe
    public void onPlayerClaimPlot(PlayerClaimPlotEvent event) {
        //noinspection rawtypes
        PlotPlayer plotPlayer = event.getPlotPlayer();
        Plot plot = event.getPlot();

        // Check claimable and validate the world
        if (plot.canClaim(plotPlayer) && validateWorld(plot.getWorldName())) {
            UUID workOwner = plotPlayer.getUUID();
            this.votes.createEntry(workOwner, plot);
            logger.info("[VoteUpdater] Created : " + plotPlayer.getName());
        }
    }

    // From the source, I see that this event is fired AFTER the plot is actually deleted.
    @Subscribe
    public void onPlotDelete(PlotDeleteEvent event) {
        if (validateWorld(event.getWorld())) {
            Optional.ofNullable(event.getPlot().getOwnerAbs()).ifPresent(workOwner -> {
                this.votes.deleteEntry(workOwner);
                logger.info("[VoteUpdater] Removed : " + event.getPlotId().toString());
            });
        }
    }

    private boolean validateWorld(String plotWorld) {
        return this.votes.getPlotWorld().equalsIgnoreCase(plotWorld);
    }

    @Override
    public void close() {
        VoteMain.plotApi.getPlotSquared().getEventDispatcher().unregisterListener(this);
    }
}
