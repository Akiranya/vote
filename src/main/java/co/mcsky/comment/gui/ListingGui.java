package co.mcsky.comment.gui;

import co.mcsky.mewcore.gui.SeamlessGui;
import co.mcsky.mewcore.gui.SoundRegistry;
import co.mcsky.comment.Main;
import co.mcsky.comment.object.Game;
import co.mcsky.comment.object.Artwork;
import me.lucko.helper.metadata.Metadata;
import me.lucko.helper.metadata.MetadataKey;
import me.lucko.helper.metadata.MetadataMap;
import org.bukkit.entity.Player;

import java.util.Optional;

public class ListingGui extends SeamlessGui {

    // metadata key for selected artwork
    public static final MetadataKey<Artwork> SELECTED_ARTWORK_KEY = MetadataKey.create("selected-work", Artwork.class);

    public ListingGui(Player player, Game game) {
        super(player, 5, Main.config().getGuiListing().getString("title", "Title"), gui -> new ListingView(gui, game));
        SoundRegistry.bindClickingSound(this);
        SoundRegistry.bindOpeningSound(this);

        final MetadataMap metadataMap = Metadata.provideForPlayer(player);
        final Optional<Artwork> work = metadataMap.get(SELECTED_ARTWORK_KEY);
        if (work.isPresent()) {
            switchView(new OptionView(this, new ListingView(this, game)));
        }
    }

}
