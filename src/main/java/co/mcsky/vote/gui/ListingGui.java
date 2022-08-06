package co.mcsky.vote.gui;

import co.mcsky.mewcore.gui.SeamlessGui;
import co.mcsky.mewcore.gui.SoundRegistry;
import co.mcsky.vote.VoteMain;
import co.mcsky.vote.object.Game;
import co.mcsky.vote.object.Work;
import me.lucko.helper.metadata.Metadata;
import me.lucko.helper.metadata.MetadataKey;
import me.lucko.helper.metadata.MetadataMap;
import org.bukkit.entity.Player;

import java.util.Optional;

public class ListingGui extends SeamlessGui {

    // metadata key for selected work
    public static final MetadataKey<Work> SELECTED_KEY = MetadataKey.create("selected-work", Work.class);

    public ListingGui(Player player, Game game) {
        super(player, 5, VoteMain.lang().get(player, "gui.work-listing.title"), gui -> new ListingView(gui, game));
        SoundRegistry.bindClickingSound(this);
        SoundRegistry.bindOpeningSound(this);

        final MetadataMap metadataMap = Metadata.provideForPlayer(player);
        final Optional<Work> work = metadataMap.get(SELECTED_KEY);
        if (work.isPresent()) {
            switchView(new OptionView(this, new ListingView(this, game)));
        }
    }

}
