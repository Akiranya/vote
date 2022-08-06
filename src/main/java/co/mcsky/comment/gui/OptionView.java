package co.mcsky.comment.gui;

import co.mcsky.mewcore.gui.GuiView;
import co.mcsky.mewcore.gui.SeamlessGui;
import co.mcsky.mewcore.skull.SkullCache;
import co.mcsky.comment.Main;
import co.mcsky.comment.event.PlayerCommentSubmitEvent;
import co.mcsky.comment.object.Game;
import co.mcsky.comment.object.Comment;
import co.mcsky.comment.object.Artwork;
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
    private final ListingView view;

    public OptionView(SeamlessGui gui, ListingView view) {
        this.gui = gui;
        this.view = view;
    }

    @Override
    public void render() {
        Game game = view.getGame();

        // place background
        VOTE_BACKGROUND.apply(gui);

        MenuPopulator optionPopulator = VOTE_OPTION_SCHEME.newPopulator(gui);

        MetadataMap metadataMap = Metadata.provideForPlayer(gui.getPlayer());
        Optional<Artwork> optionalWork = metadataMap.get(ListingGui.SELECTED_ARTWORK_KEY);

        // this should never execute
        if (optionalWork.isEmpty()) return;
        Artwork artwork = optionalWork.get();

        // place head
        HEAD.newPopulator(gui).accept(ItemStackBuilder.of(Material.PLAYER_HEAD)
                .name(Main.config().getGuiOption().getString("items.head.name", artwork.getOwnerName())
                        .replace("{player}", artwork.getOwnerName()))
                .lore(Main.config().getGuiOption().getStringList("items.head.lore"))
                .transform(item -> SkullCache.INSTANCE.itemWithUuid(item, artwork.getOwner()))
                .buildItem()
                .build());

        // place option: teleport
        optionPopulator.accept(ItemStackBuilder.of(Material.MINECART)
                .name(Main.config().getGuiOption().getString("items.teleport.name", "Teleport"))
                .lore(Main.config().getGuiOption().getStringList("items.teleport.lore"))
                .build(() -> artwork.teleport(gui.getPlayer())));

        // place option: rate green
        UUID reviewer = gui.getPlayer().getUniqueId();
        optionPopulator.accept(ItemStackBuilder.of(Material.GREEN_WOOL)
                .name(Main.config().getGuiOption().getString("items.like.name", "Like"))
                .lore(Main.config().getGuiOption().getStringList("items.like.lore"))
                .build(() -> {
                    Comment comment = new Comment(reviewer, false);
                    if (!Events.callAndReturn(new PlayerCommentSubmitEvent(gui.getPlayer(), artwork, comment, game)).isCancelled()) {
                        game.comment(artwork.getOwner(), comment);
                        gui.getPlayer().sendMessage(Main.lang().get(gui.getPlayer(), "gui.comment-work", "player", artwork.getOwnerName()));
                    }
                }));

        // place option: rate red
        optionPopulator.accept(ItemStackBuilder.of(Material.RED_WOOL)
                .name(Main.config().getGuiOption().getString("items.dislike.name", "Dislike"))
                .lore(Main.config().getGuiOption().getStringList("items.dislike.lore"))
                .build(() -> {
                    Comment comment = new Comment(reviewer, true);
                    if (!Events.callAndReturn(new PlayerCommentSubmitEvent(gui.getPlayer(), artwork, comment, game)).isCancelled()) {
                        game.comment(artwork.getOwner(), comment);
                        gui.getPlayer().sendMessage(Main.lang().get(gui.getPlayer(), "gui.comment-work-absent", "player", artwork.getOwnerName()));
                    }
                }));

        // place back button
        BACK_SCHEME.newPopulator(gui).accept(ItemStackBuilder.of(Material.REDSTONE)
                .name(Main.config().getGuiBase().getString("exit.name", "Exit"))
                .lore(Main.config().getGuiBase().getStringList("exit.lore"))
                .build(() -> {
                    // remove the metadata
                    metadataMap.remove(ListingGui.SELECTED_ARTWORK_KEY);
                    // update content before going back to the listing
                    view.updateContent();
                    // then switch back to the listing view to show updated content
                    gui.switchView(view);
                }));
    }
}
