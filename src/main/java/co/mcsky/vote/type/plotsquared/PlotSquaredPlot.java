package co.mcsky.vote.type.plotsquared;

import co.mcsky.vote.VoteMain;
import co.mcsky.vote.type.GamePlot;
import co.mcsky.vote.util.MainUtil;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlotSquaredPlot implements GamePlot {

    private final com.plotsquared.core.plot.Plot plot;

    public PlotSquaredPlot(com.plotsquared.core.plot.Plot plot) {
        this.plot = plot;
    }

    @Override
    public void teleport(Player player) {
        this.plot.teleportPlayer(
                PlotPlayer.from(player),
                TeleportCause.PLUGIN,
                b -> player.sendMessage(VoteMain.plugin.getMessage(player, "gui-message.teleport-to-plot", "player", getOwnerName()))
        );
    }

    @Override
    public UUID getOwner() {
        return this.plot.getOwner();
    }

    @Override
    public String getOwnerName() {
        return MainUtil.getPlayerName(this.getOwner());
    }

    @Override
    public boolean hasOwner() {
        return this.plot.hasOwner();
    }

    @Override
    public String getWorldName() {
        return this.plot.getWorldName();
    }

    @Override
    public boolean isDone() {
        return DoneFlag.isDone(this.plot);
    }

    @Override
    public String getId() {
        return this.plot.getId().toString();
    }
}
