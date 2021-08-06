package co.mcsky.vote.gui;

import co.mcsky.moecore.gui.GuiView;
import co.mcsky.moecore.gui.SeamlessGui;
import co.mcsky.vote.event.PlayerVoteSubmitEvent;
import co.mcsky.vote.object.Game;
import co.mcsky.vote.object.Vote;
import co.mcsky.vote.object.Work;
import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.menu.scheme.StandardSchemeMappings;
import org.bukkit.Material;

import java.util.UUID;

import static co.mcsky.vote.VoteMain.plugin;

public class OptionView implements GuiView {

    private final SeamlessGui gui;

    private final MenuScheme voteBackground = new MenuScheme(StandardSchemeMappings.STAINED_GLASS)
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
    private final MenuScheme voteOptionScheme = new MenuScheme()
            .maskEmpty(2)
            .mask("001010100");
    private final MenuScheme backScheme = new MenuScheme()
            .maskEmpty(4)
            .mask("000010000");

    // the backed listing view
    private final ListingView listingView;

    public OptionView(SeamlessGui gui, ListingView listingView) {
        this.gui = gui;
        this.listingView = listingView;
    }

    @Override
    public void render() {
        Game game = listingView.getGame();
        Work selectedWork = listingView.getSelectedWork();

        // place background
        this.voteBackground.apply(this.gui);

        MenuPopulator optionPopulator = this.voteOptionScheme.newPopulator(this.gui);

        // place option: teleport
        optionPopulator.accept(ItemStackBuilder.of(Material.MINECART)
                .name(plugin.message(this.gui.getPlayer(), "gui.vote-options.teleport-to-plot.name"))
                .lore(plugin.message(this.gui.getPlayer(), "gui.vote-options.teleport-to-plot.lore1"))
                .lore(plugin.message(this.gui.getPlayer(), "gui.vote-options.teleport-to-plot.lore2"))
                .build(() -> selectedWork.teleport(this.gui.getPlayer())));

        // place option: rate green
        UUID rater = this.gui.getPlayer().getUniqueId();
        optionPopulator.accept(ItemStackBuilder.of(Material.GREEN_WOOL)
                .name(plugin.message(this.gui.getPlayer(), "gui.vote-options.vote-work.name"))
                .lore(plugin.message(this.gui.getPlayer(), "gui.vote-options.vote-work.lore1"))
                .lore(plugin.message(this.gui.getPlayer(), "gui.vote-options.vote-work.lore2"))
                .build(() -> {
                    Vote vote = new Vote(rater, false);
                    if (!Events.callAndReturn(new PlayerVoteSubmitEvent(this.gui.getPlayer(), selectedWork, vote, game)).isCancelled()) {
                        game.vote(selectedWork.getOwner(), vote);
                        this.gui.getPlayer().sendMessage(plugin.message(this.gui.getPlayer(), "gui-message.vote-work", "player", selectedWork.getOwnerName()));
                    }
                }));

        // place option: rate red
        optionPopulator.accept(ItemStackBuilder.of(Material.RED_WOOL)
                .name(plugin.message(this.gui.getPlayer(), "gui.vote-options.vote-work-absent.name"))
                .lore(plugin.message(this.gui.getPlayer(), "gui.vote-options.vote-work-absent.lore1"))
                .lore(plugin.message(this.gui.getPlayer(), "gui.vote-options.vote-work-absent.lore2"))
                .build(() -> {
                    Vote vote = new Vote(rater, true);
                    if (!Events.callAndReturn(new PlayerVoteSubmitEvent(this.gui.getPlayer(), selectedWork, vote, game)).isCancelled()) {
                        game.vote(selectedWork.getOwner(), vote);
                        this.gui.getPlayer().sendMessage(plugin.message(this.gui.getPlayer(), "gui-message.vote-work-absent", "player", selectedWork.getOwnerName()));
                    }
                }));

        // place back button
        this.backScheme.newPopulator(this.gui).accept(ItemStackBuilder.of(Material.REDSTONE)
                .name(plugin.message(this.gui.getPlayer(), "gui.vote-options.back.name"))
                .lore(plugin.message(this.gui.getPlayer(), "gui.vote-options.back.lore1"))
                .lore(plugin.message(this.gui.getPlayer(), "gui.vote-options.back.lore2"))
                .build(() -> {
                    // update content before going back to the listing
                    listingView.updateListing();
                    // then switch back to the listing view to show updated content
                    this.gui.switchView(listingView);
                }));
    }
}
