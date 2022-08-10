package co.mcsky.comment.gui;

import co.mcsky.mewcore.gui.PaginatedView;
import co.mcsky.mewcore.gui.SeamlessGui;
import co.mcsky.mewcore.skull.SkinFetchCompleteEvent;
import co.mcsky.mewcore.skull.SkullCache;
import co.mcsky.comment.Main;
import co.mcsky.comment.event.PlayerCommentDoneEvent;
import co.mcsky.comment.object.Game;
import co.mcsky.comment.object.Artwork;
import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.paginated.PageInfo;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.menu.scheme.StandardSchemeMappings;
import me.lucko.helper.metadata.Metadata;
import me.lucko.helper.metadata.MetadataMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class ListingView extends PaginatedView {

    // menu schemes
    private static final MenuScheme POSTER = new MenuScheme()
            .mask("000010000");
    private static final int DONE_SLOT = new MenuScheme()
            .maskEmpty(4)
            .mask("000010000")
            .getMaskedIndexesImmutable().get(0);

    // the backed GUI
    private final SeamlessGui gui;
    // the player who is looking at this GUI
    private final Player player;
    // the backed instance
    private final Game game;
    // applied filter
    private Predicate<Artwork> filter;

    public ListingView(SeamlessGui gui, Game game) {
        super(gui);
        this.gui = gui;
        this.player = gui.getPlayer();
        this.game = game;

        // list all by default
        this.filter = WorkFilters.ALL();
        // initialize listing content
        updateContent();

        // refresh the GUI when a skin is fetched
        Events.subscribe(SkinFetchCompleteEvent.class).handler(e -> gui.redraw()).bindWith(gui);
    }

    public void updateContent() {
        List<Item> content = game.getWorks()
                .stream()
                .filter(filter)
                .map(work -> {
                    String name = Main.config().getGuiListing()
                            .getString("items.player.name", work.getOwnerName())
                            .replace("{player}", work.getOwnerName());
                    List<String> lore = Main.config().getGuiListing()
                            .getStringList("items.player.lore").stream()
                            .map(line -> line
                                    .replace("{artworkStatus}", work.isDone() ? Main.lang().get(player, "misc.done") : Main.lang().get(player, "misc.undone"))
                                    .replace("{commentStatus}", work.hasVoted(player.getUniqueId()) ? Main.lang().get(player, "misc.done") : Main.lang().get(player, "misc.undone")))
                            .toList();
                    return ItemStackBuilder.of(Material.PLAYER_HEAD)
                            .name(name)
                            .lore(lore)
                            .transform(item -> SkullCache.INSTANCE.itemWithUuid(item, work.getOwner()))
                            .build(() -> {
                                // use metadata to store the work which the reviewer is looking at
                                MetadataMap metadataMap = Metadata.provideForPlayer(gui.getPlayer());
                                metadataMap.put(ListingGui.SELECTED_ARTWORK_KEY, work);
                                gui.switchView(new OptionView(gui, this));
                            });
                }).toList();
        super.updateContent(content);
    }

    private void updateListing(Predicate<Artwork> predicate) {
        filter = predicate;
        updateContent();
    }

    @Override
    public void renderSubview() {
        // place the poster
        POSTER.newPopulator(gui).accept(ItemStackBuilder.of(Material.BOOK)
                .name(Main.config().getGuiListing().getString("items.poster.name", "Poster"))
                .lore(Main.config().getGuiListing().getStringList("items.poster.lore"))
                .buildItem().build());

        // place the done button
        gui.setItem(DONE_SLOT, ItemStackBuilder.of(Material.APPLE)
                .name(Main.config().getGuiListing().getString("items.submit.name", "Submit"))
                .lore(Main.config().getGuiListing().getStringList("items.submit.lore"))
                .build(() -> {
                    boolean invalid = game.getStatistics().isInvalidReviewer(player.getUniqueId());

                    if (Events.callAndReturn(new PlayerCommentDoneEvent(player, game)).isCancelled()) {
                        return;
                    }

                    // send prompts
                    if (invalid) {
                        // prompt the player that he has not finished the comment yet
                        player.sendMessage(Main.lang().get(player, "gui.comment-not-done"));
                        player.sendMessage(Main.lang().get(player, "gui.must-comment-filtered"));
                        // then filter content to only show the works he needs to comment for
                        updateListing(WorkFilters.UNDONE(player.getUniqueId()));
                        gui.redraw();
                    } else {
                        // prompt the player that he has done
                        player.sendMessage(Main.lang().get(player, "gui.comment-all-done"));
                        player.playEffect(EntityEffect.TOTEM_RESURRECT);
                        player.showTitle(Title.title(
                                Component.text(Main.lang().get(player, "title.comment-finished.title")),
                                Component.text(Main.lang().get(player, "title.comment-finished.subtitle")),
                                Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(5), Duration.ofSeconds(1))
                        ));
                        // simply close the GUI after the player has done the comment
                        gui.close();
                    }
                }));
    }

    @Override
    public MenuScheme backgroundSchema() {
        return new MenuScheme(StandardSchemeMappings.STAINED_GLASS)
                .mask("111101111")
                .mask("100000001")
                .mask("100000001")
                .mask("100000001")
                .mask("111111111")
                .scheme(3, 3, 3, 3, 3, 3, 3, 3)
                .scheme(3, 3, 3, 3, 3, 3, 3, 3, 3)
                .scheme(3, 3, 3, 3)
                .scheme(3, 3, 3, 3)
                .scheme(3, 3, 3, 3, 3, 3, 3, 3, 3);
    }

    @Override
    public int nextPageSlot() {
        return new MenuScheme()
                .maskEmpty(4)
                .mask("000000100")
                .getMaskedIndexesImmutable().get(0);
    }

    @Override
    public int previousPageSlot() {
        return new MenuScheme()
                .maskEmpty(4)
                .mask("001000000")
                .getMaskedIndexesImmutable().get(0);
    }

    @Override
    public Function<PageInfo, ItemStack> nextPageItem() {
        return pageInfo -> ItemStackBuilder.of(Material.PAPER)
                .name(Main.config().getGuiBase().getString("nextPage.name", "Next page"))
                .lore(Main.config().getGuiBase().getStringList("nextPage.lore"))
                .build();
    }

    @Override
    public Function<PageInfo, ItemStack> previousPageItem() {
        return pageInfo -> ItemStackBuilder.of(Material.PAPER)
                .name(Main.config().getGuiBase().getString("prevPage.name", "Prev page"))
                .lore(Main.config().getGuiBase().getStringList("prevPage.lore"))
                .build();
    }

    @Override
    public List<Integer> itemSlots() {
        return new MenuScheme()
                .maskEmpty(1)
                .mask("011111110")
                .mask("011111110")
                .mask("011111110")
                .getMaskedIndexesImmutable();
    }

    public Game getGame() {
        return game;
    }

}
