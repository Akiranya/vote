package co.mcsky.comment.listener;

import co.mcsky.comment.object.Game;
import co.mcsky.comment.object.factory.PlotFactory;
import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.events.PlayerClaimPlotEvent;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.utils.Log;

import java.util.Optional;
import java.util.UUID;

/**
 * The {@link ArtworkUpdater} must associate with an instance of {@link Game}
 * (one-to-one relationship).
 */
public final class ArtworkUpdater implements Terminable {

    private final Game game;
    private final PlotAPI plotApi;

    public ArtworkUpdater(Game game) {
        this.game = game;
        this.plotApi = new PlotAPI();
        this.plotApi.registerListener(this);
    }

    // Warning: this event is fired before the plot is actually claimed by the player
    // so, it is necessary to check if the player can really claim this plot.
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
            Log.info("[VoteUpdater] created : " + plotPlayer.getName());
        }
    }

    // From the source, I see that this event is fired AFTER the plot is actually deleted.
    @Subscribe
    public void onPlotDelete(PlotDeleteEvent event) {
        if (validateWorld(event.getWorld())) {
            Optional.ofNullable(event.getPlot().getOwnerAbs()).ifPresent(workOwner -> {
                this.game.deleteEntry(workOwner);
                Log.info("[VoteUpdater] removed : " + event.getPlotId().toString());
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