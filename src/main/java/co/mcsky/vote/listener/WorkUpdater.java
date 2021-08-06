package co.mcsky.vote.listener;

import co.mcsky.vote.object.Game;
import co.mcsky.vote.object.factory.PlotFactory;
import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.events.PlayerClaimPlotEvent;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import me.lucko.helper.terminable.Terminable;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static co.mcsky.vote.VoteMain.plugin;

/**
 * The {@link WorkUpdater} must associate with an instance of {@link Game} (one-to-one relationship).
 */
@SuppressWarnings("UnstableApiUsage")
public class WorkUpdater implements Terminable {

    private final Game game;
    private final Logger logger;
    private final PlotAPI plotApi;

    public WorkUpdater(Game game) {
        this.game = game;
        this.logger = plugin.getLogger();
        this.plotApi = new PlotAPI();
        this.plotApi.registerListener(this);
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
            this.game.createEntry(workOwner, PlotFactory.of(plot));
            logger.info("[VoteUpdater] created : " + plotPlayer.getName());
        }
    }

    // From the source, I see that this event is fired AFTER the plot is actually deleted.
    @Subscribe
    public void onPlotDelete(PlotDeleteEvent event) {
        if (validateWorld(event.getWorld())) {
            Optional.ofNullable(event.getPlot().getOwnerAbs()).ifPresent(workOwner -> {
                this.game.deleteEntry(workOwner);
                logger.info("[VoteUpdater] removed : " + event.getPlotId().toString());
            });
        }
    }

    private boolean validateWorld(String plotWorld) {
        return this.game.getWorld().equalsIgnoreCase(plotWorld);
    }

    @Override
    public void close() {
        this.plotApi.getPlotSquared().getEventDispatcher().unregisterListener(this);
    }
}
