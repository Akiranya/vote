package co.mcsky.vote.gui;

import co.mcsky.moecore.gui.SeamlessGui;
import co.mcsky.moecore.gui.SoundRegistry;
import co.mcsky.vote.type.Game;
import org.bukkit.entity.Player;

import static co.mcsky.vote.VoteMain.plugin;

public class ListingGui extends SeamlessGui {

    public ListingGui(Player player, Game game) {
        super(player, 5, plugin.getMessage(player, "gui.work-listing.title"), gui -> new ListingView(gui, game));
        SoundRegistry.bindClickingSound(this);
        SoundRegistry.bindOpeningSound(this);
    }

}
