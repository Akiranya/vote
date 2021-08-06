package co.mcsky.vote.gui;

import co.mcsky.moecore.gui.SeamlessGui;
import co.mcsky.moecore.gui.SoundRegistry;
import co.mcsky.vote.object.Game;
import co.mcsky.vote.object.Work;
import me.lucko.helper.metadata.Metadata;
import me.lucko.helper.metadata.MetadataMap;
import org.bukkit.entity.Player;

import java.util.Optional;

import static co.mcsky.vote.VoteMain.plugin;

public class ListingGui extends SeamlessGui {

    public ListingGui(Player player, Game game) {
        super(player, 5, plugin.message(player, "gui.work-listing.title"), gui -> new ListingView(gui, game));
        SoundRegistry.bindClickingSound(this);
        SoundRegistry.bindOpeningSound(this);

        final MetadataMap metadataMap = Metadata.provideForPlayer(player);
        final Optional<Work> work = metadataMap.get(ListingView.selectedKey);
        if (work.isPresent()) {
            this.switchView(new OptionView(this, new ListingView(this, game)));
        }
    }

}
