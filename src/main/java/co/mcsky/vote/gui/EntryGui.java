package co.mcsky.vote.gui;

import co.mcsky.vote.gui.base.SeamlessGui;
import co.mcsky.vote.gui.base.SoundRegistry;
import co.mcsky.vote.type.Game;
import org.bukkit.entity.Player;

import static co.mcsky.vote.VoteMain.plugin;

public class EntryGui extends SeamlessGui {

    public EntryGui(Player player, Game game) {
        super(player, 5, plugin.getMessage(player, "gui.work-listing.title"), gui -> new ListingView(gui, game));
        SoundRegistry.bindClickingSound(this);
        SoundRegistry.bindOpeningSound(this);
    }

}
