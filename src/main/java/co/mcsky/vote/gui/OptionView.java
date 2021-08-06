package co.mcsky.vote.gui;

import co.mcsky.moecore.gui.GuiView;
import co.mcsky.moecore.gui.SeamlessGui;
import co.mcsky.moecore.skull.SkullCache;
import co.mcsky.vote.event.PlayerVoteSubmitEvent;
import co.mcsky.vote.object.Game;
import co.mcsky.vote.object.Vote;
import co.mcsky.vote.object.Work;
import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.menu.scheme.StandardSchemeMappings;
import me.lucko.helper.metadata.Metadata;
import me.lucko.helper.metadata.MetadataMap;
import org.bukkit.Material;

import java.util.Optional;
import java.util.UUID;

import static co.mcsky.vote.VoteMain.plugin;

public class OptionView implements GuiView {

    private static final MenuScheme HEAD = new MenuScheme()
            .mask("000010000");
    private static final MenuScheme VOTE_BACKGROUND = new MenuScheme(StandardSchemeMappings.STAINED_GLASS)
            .mask("111111111")
            .mask("111111111")
            .mask("110101011")
            .mask("111111111")
            .mask("111111111")
            .scheme(3, 3, 3, 3, 3, 3, 3, 3, 3)
            .scheme(3, 3, 3, 3, 3, 3, 3, 3, 3)
            .scheme(3, 3, 3, 3, 3, 3)
            .scheme(3, 3, 3, 3, 3, 3, 3, 3, 3)
            .scheme(3, 3, 3, 3, 3, 3, 3, 3, 3);
    private static final MenuScheme VOTE_OPTION_SCHEME = new MenuScheme()
            .maskEmpty(2)
            .mask("001010100");
    private static final MenuScheme BACK_SCHEME = new MenuScheme()
            .maskEmpty(4)
            .mask("000010000");

    // the backed GUI
    private final SeamlessGui gui;
    // the backed view
    private final ListingView listingView;

    public OptionView(SeamlessGui gui, ListingView listingView) {
        this.gui = gui;
        this.listingView = listingView;
    }

    @Override
    public void render() {
        Game game = listingView.getGame();

        // place background
        VOTE_BACKGROUND.apply(gui);

        MenuPopulator optionPopulator = VOTE_OPTION_SCHEME.newPopulator(gui);

        final MetadataMap metadataMap = Metadata.provideForPlayer(gui.getPlayer());
        final Optional<Work> work = metadataMap.get(ListingView.selectedKey);

        // this should never execute
        if (work.isEmpty()) return;

        // place head
        HEAD.newPopulator(gui).accept(ItemStackBuilder.of(Material.PLAYER_HEAD)
                .name(plugin.message(gui.getPlayer(), "gui.vote-options.head.name", "player", work.get().getOwnerName()))
                .transform(item -> SkullCache.INSTANCE.itemWithUuid(item, work.get().getOwner()))
                .buildItem()
                .build());

        // place option: teleport
        optionPopulator.accept(ItemStackBuilder.of(Material.MINECART)
                .name(plugin.message(gui.getPlayer(), "gui.vote-options.teleport-to-plot.name"))
                .lore(plugin.message(gui.getPlayer(), "gui.vote-options.teleport-to-plot.lore1"))
                .lore(plugin.message(gui.getPlayer(), "gui.vote-options.teleport-to-plot.lore2"))
                .build(() -> work.get().teleport(gui.getPlayer())));

        // place option: rate green
        UUID rater = gui.getPlayer().getUniqueId();
        optionPopulator.accept(ItemStackBuilder.of(Material.GREEN_WOOL)
                .name(plugin.message(gui.getPlayer(), "gui.vote-options.vote-work.name"))
                .lore(plugin.message(gui.getPlayer(), "gui.vote-options.vote-work.lore1"))
                .lore(plugin.message(gui.getPlayer(), "gui.vote-options.vote-work.lore2"))
                .build(() -> {
                    Vote vote = new Vote(rater, false);
                    if (!Events.callAndReturn(new PlayerVoteSubmitEvent(gui.getPlayer(), work.get(), vote, game)).isCancelled()) {
                        game.vote(work.get().getOwner(), vote);
                        gui.getPlayer().sendMessage(plugin.message(gui.getPlayer(), "gui-message.vote-work", "player", work.get().getOwnerName()));
                    }
                }));

        // place option: rate red
        optionPopulator.accept(ItemStackBuilder.of(Material.RED_WOOL)
                .name(plugin.message(gui.getPlayer(), "gui.vote-options.vote-work-absent.name"))
                .lore(plugin.message(gui.getPlayer(), "gui.vote-options.vote-work-absent.lore1"))
                .lore(plugin.message(gui.getPlayer(), "gui.vote-options.vote-work-absent.lore2"))
                .build(() -> {
                    Vote vote = new Vote(rater, true);
                    if (!Events.callAndReturn(new PlayerVoteSubmitEvent(gui.getPlayer(), work.get(), vote, game)).isCancelled()) {
                        game.vote(work.get().getOwner(), vote);
                        gui.getPlayer().sendMessage(plugin.message(gui.getPlayer(), "gui-message.vote-work-absent", "player", work.get().getOwnerName()));
                    }
                }));

        // place back button
        BACK_SCHEME.newPopulator(gui).accept(ItemStackBuilder.of(Material.REDSTONE)
                .name(plugin.message(gui.getPlayer(), "gui.vote-options.back.name"))
                .lore(plugin.message(gui.getPlayer(), "gui.vote-options.back.lore1"))
                .lore(plugin.message(gui.getPlayer(), "gui.vote-options.back.lore2"))
                .build(() -> {
                    // remove the metadata
                    metadataMap.remove(ListingView.selectedKey);
                    // update content before going back to the listing
                    listingView.updateListing();
                    // then switch back to the listing view to show updated content
                    gui.switchView(listingView);
                }));
    }
}
