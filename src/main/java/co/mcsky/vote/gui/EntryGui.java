package co.mcsky.vote.gui;

import co.mcsky.vote.gui.base.SeamlessGui;
import co.mcsky.vote.gui.base.SoundRegistry;
import co.mcsky.vote.gui.view.ListingView;
import co.mcsky.vote.type.Votes;
import org.bukkit.entity.Player;

import static co.mcsky.vote.VoteMain.plugin;

public class EntryGui extends SeamlessGui {

    public EntryGui(Player player, Votes votes) {
        super(player, 5, plugin.getMessage(player, "gui.work-listing.title"), gui -> new ListingView(gui, votes));
        SoundRegistry.bindClickingSound(this);
        SoundRegistry.bindOpeningSound(this);
    }

}
