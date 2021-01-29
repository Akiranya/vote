package co.mcsky.vote.gui;

import co.mcsky.vote.events.PlayerVoteEvent;
import co.mcsky.vote.type.Vote;
import co.mcsky.vote.type.Votes;
import co.mcsky.vote.type.Work;
import com.destroystokyo.paper.Title;
import com.google.common.collect.Lists;
import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.Slot;
import me.lucko.helper.menu.paginated.PageInfo;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.menu.scheme.StandardSchemeMappings;
import me.lucko.helper.utils.Players;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static co.mcsky.vote.VoteMain.plugin;

@SuppressWarnings("ConstantConditions")
public class MainGui extends VoicedGui {

    enum GuiView {
        LISTING, OPTIONS
    }

    /* define vote option scheme */
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

    /* define work listing scheme */
    private final MenuScheme listingBackground = new MenuScheme(StandardSchemeMappings.STAINED_GLASS)
            .mask("111101111")
            .mask("110000011")
            .mask("110000011")
            .mask("110000011")
            .mask("111111111")
            .scheme(3, 3, 3, 3, 3, 3, 3, 3)
            .scheme(3, 3, 3, 3, 3, 3, 3, 3, 3)
            .scheme(3, 3, 3, 3)
            .scheme(3, 3, 3, 3)
            .scheme(3, 3, 3, 3, 3, 3, 3, 3, 3);
    private final MenuScheme listingTips = new MenuScheme()
            .mask("000010000");
    private final List<Integer> itemSlots = new MenuScheme()
            .maskEmpty(1)
            .mask("001111100")
            .mask("001111100")
            .mask("001111100")
            .getMaskedIndexesImmutable();
    private final int doneSlot = new MenuScheme()
            .maskEmpty(4)
            .mask("000010000")
            .getMaskedIndexesImmutable().get(0);
    private final int nextPageSlot = new MenuScheme()
            .maskEmpty(4)
            .mask("000000100")
            .getMaskedIndexesImmutable().get(0);
    private final int previousPageSlot = new MenuScheme()
            .maskEmpty(4)
            .mask("001000000")
            .getMaskedIndexesImmutable().get(0);
    private final Function<PageInfo, ItemStack> nextPageItem;
    private final Function<PageInfo, ItemStack> previousPageItem;

    // work listing GUI stuff
    private List<Item> content;

    // currently applied filter
    Predicate<Work> filter;
    // currently viewing GUI
    GuiView view;
    // currently selected work
    Work work;

    // page starts at 1
    private int page;

    // the backed instances
    private final Votes votes;

    public MainGui(Player player, Votes votes) {
        super(player, 5, plugin.getMessage(player, "gui.work-listing.title"));
        this.votes = votes;

        // setup next page item
        this.nextPageItem = pageInfo -> ItemStackBuilder.of(Material.PAPER)
                .name(plugin.getMessage(player, "gui.work-listing.next-page.name"))
                .lore(plugin.getMessage(player, "gui.work-listing.next-page.lore1"))
                .lore(plugin.getMessage(player, "gui.work-listing.next-page.lore2", "current-page", pageInfo.getCurrent(), "total-page", pageInfo.getSize()))
                .build();
        // setup previous page item
        this.previousPageItem = pageInfo -> ItemStackBuilder.of(Material.PAPER)
                .name(plugin.getMessage(player, "gui.work-listing.previous-page.name"))
                .lore(plugin.getMessage(player, "gui.work-listing.previous-page.lore1"))
                .lore(plugin.getMessage(player, "gui.work-listing.previous-page.lore2", "current-page", pageInfo.getCurrent(), "total-page", pageInfo.getSize()))
                .build();

        // by default, show work listing
        this.view = GuiView.LISTING;
        // by default, currently selected work is null
        this.work = null;
        // show all works by default
        this.filter = WorkFilter.all();
        updateContent();
    }

    @Override
    public void redraw() {
        switch (view) {
            case LISTING:
                drawWorkList();
                break;
            case OPTIONS:
                drawVoteOption();
                break;
            default:
                getPlayer().sendMessage("Unknown GUI view");
        }
    }

    public void drawWorkList() {
        // clear items and update current view
        clearItems();
        this.view = GuiView.LISTING;

        // place the background
        this.listingBackground.apply(this);

        // place the tips
        this.listingTips.newPopulator(this).accept(ItemStackBuilder.of(Material.BOOK)
                .name(plugin.getMessage(getPlayer(), "gui.work-listing.menu-tips.name"))
                .lore(plugin.getMessage(getPlayer(), "gui.work-listing.menu-tips.lore1"))
                .lore(plugin.getMessage(getPlayer(), "gui.work-listing.menu-tips.lore2"))
                .lore(plugin.getMessage(getPlayer(), "gui.work-listing.menu-tips.lore3"))
                .buildItem().build());

        // place the done button
        setItem(this.doneSlot, ItemStackBuilder.of(Material.APPLE)
                .name(plugin.getMessage(getPlayer(), "gui.work-listing.submit.name"))
                .lore(plugin.getMessage(getPlayer(), "gui.work-listing.submit.lore1"))
                .lore(plugin.getMessage(getPlayer(), "gui.work-listing.submit.lore2"))
                .lore(plugin.getMessage(getPlayer(), "gui.work-listing.submit.lore3"))
                .lore(plugin.getMessage(getPlayer(), "gui.work-listing.submit.lore4"))
                .lore(plugin.getMessage(getPlayer(), "gui.work-listing.submit.lore5"))
                .build(() -> {
                    boolean invalid = this.votes.getCalculator().invalid(getPlayer().getUniqueId());

                    // send prompts
                    if (invalid) {
                        // prompt the player that he has not finished the vote yet
                        getPlayer().sendMessage(plugin.getMessage(getPlayer(), "gui-message.vote-not-done"));
                        getPlayer().sendMessage(plugin.getMessage(getPlayer(), "gui-message.must-vote-filtered"));
                        // then filter content to only show the works he needs to vote for
                        updateContent(WorkFilter.undone(getPlayer().getUniqueId()));
                        redraw();
                    } else {
                        // TODO use metadata to set a cool down for the prompt
                        // prompt the player that he has done
                        getPlayer().sendMessage(plugin.getMessage(getPlayer(), "gui-message.vote-all-done"));
                        getPlayer().playEffect(EntityEffect.TOTEM_RESURRECT);
                        getPlayer().sendTitle(Title.builder()
                                .title(plugin.getMessage(getPlayer(), "title-message.vote-finished.title"))
                                .subtitle(plugin.getMessage(getPlayer(), "title-message.vote-finished.subtitle"))
                                .fadeIn(20).fadeOut(20).stay(120)
                                .build());
                        // simply close the GUI after the player has done the vote
                        close();
                    }
                }));

        /* pagination stuff starts */

        // get available slots for items
        List<Integer> slots = new ArrayList<>(this.itemSlots);

        // work out the items to display on this page
        List<List<Item>> pages = Lists.partition(this.content, slots.size());

        // normalize page number
        if (this.page < 1) {
            this.page = 1;
        } else if (this.page > pages.size()) {
            this.page = Math.max(1, pages.size());
        }

        List<Item> page = pages.isEmpty() ? new ArrayList<>() : pages.get(this.page - 1);

        // place prev/next page buttons
        if (this.page == 1) {
            // can't go back further
            // remove the item if the current slot contains a previous page item type
            Slot slot = getSlot(this.previousPageSlot);
            slot.clearBindings();
            if (slot.hasItem() && slot.getItem().getType() == this.previousPageItem.apply(PageInfo.create(0, 0)).getType()) {
                slot.clearItem();
            }
        } else {
            setItem(this.previousPageSlot, ItemStackBuilder.of(this.previousPageItem.apply(PageInfo.create(this.page, pages.size())))
                    .build(() -> {
                        this.page = this.page - 1;
                        redraw();
                    }));
        }

        if (this.page >= pages.size()) {
            // can't go forward a page
            // remove the item if the current slot contains a next page item type
            Slot slot = getSlot(this.nextPageSlot);
            slot.clearBindings();
            if (slot.hasItem() && slot.getItem().getType() == this.nextPageItem.apply(PageInfo.create(0, 0)).getType()) {
                slot.clearItem();
            }
        } else {
            setItem(this.nextPageSlot, ItemStackBuilder.of(this.nextPageItem.apply(PageInfo.create(this.page, pages.size())))
                    .build(() -> {
                        this.page = this.page + 1;
                        redraw();
                    }));
        }

        // remove previous items
        if (!isFirstDraw()) {
            slots.forEach(this::removeItem);
        }

        // place the actual items
        for (Item item : page) {
            int index = slots.remove(0);
            setItem(index, item);
        }

        /* pagination stuff ends */
    }

    public void drawVoteOption() {
        // clear items and update current view
        clearItems();
        this.view = GuiView.OPTIONS;

        // place background
        this.voteBackground.apply(this);

        // place options
        MenuPopulator optionPopulator = this.voteOptionScheme.newPopulator(this);
        optionPopulator.accept(ItemStackBuilder.of(Material.MINECART)
                .name(plugin.getMessage(getPlayer(), "gui.vote-options.teleport-to-plot.name"))
                .lore(plugin.getMessage(getPlayer(), "gui.vote-options.teleport-to-plot.lore1"))
                .lore(plugin.getMessage(getPlayer(), "gui.vote-options.teleport-to-plot.lore2"))
                .build(() -> this.work.teleport(getPlayer())));
        UUID rater = getPlayer().getUniqueId();
        optionPopulator.accept(ItemStackBuilder.of(Material.GREEN_WOOL)
                .name(plugin.getMessage(getPlayer(), "gui.vote-options.vote-work.name"))
                .lore(plugin.getMessage(getPlayer(), "gui.vote-options.vote-work.lore1"))
                .lore(plugin.getMessage(getPlayer(), "gui.vote-options.vote-work.lore2"))
                .build(() -> {
                    Vote vote = Vote.create(rater).absent(false).build();
                    if (!Events.callAndReturn(new PlayerVoteEvent(getPlayer(), this.work, vote, this.votes)).isCancelled()) {
                        this.votes.vote(this.work.getOwner(), vote);
                        getPlayer().sendMessage(plugin.getMessage(getPlayer(), "gui-message.vote-work", "player", this.work.getOwnerName()));
                    }
                }));
        optionPopulator.accept(ItemStackBuilder.of(Material.RED_WOOL)
                .name(plugin.getMessage(getPlayer(), "gui.vote-options.vote-work-absent.name"))
                .lore(plugin.getMessage(getPlayer(), "gui.vote-options.vote-work-absent.lore1"))
                .lore(plugin.getMessage(getPlayer(), "gui.vote-options.vote-work-absent.lore2"))
                .build(() -> {
                    Vote vote = Vote.create(rater).absent(true).build();
                    if (!Events.callAndReturn(new PlayerVoteEvent(getPlayer(), this.work, vote, this.votes)).isCancelled()) {
                        this.votes.vote(this.work.getOwner(), vote);
                        getPlayer().sendMessage(plugin.getMessage(getPlayer(), "gui-message.vote-work-absent", "player", this.work.getOwnerName()));
                    }
                }));

        // place back button
        this.backScheme.newPopulator(this).accept(ItemStackBuilder.of(Material.REDSTONE)
                .name(plugin.getMessage(getPlayer(), "gui.vote-options.back.name"))
                .lore(plugin.getMessage(getPlayer(), "gui.vote-options.back.lore1"))
                .lore(plugin.getMessage(getPlayer(), "gui.vote-options.back.lore2"))
                .build(() -> {
                    // update content before going back to the listing
                    updateContent();
                    // then draw the listing to show updated content
                    drawWorkList();
                }));
    }

    private void updateContent(Predicate<Work> predicate) {
        this.filter = predicate;
        updateContent();
    }

    private void updateContent() {
        this.content = this.votes
                .getWorks()
                .stream()
                .filter(this.filter)
                .map(work -> ItemStackBuilder.of(Material.PLAYER_HEAD)
                        .name(plugin.getMessage(getPlayer(), "gui.work-listing.work-entry.name", "player", work.getOwnerName()))
                        .lore(plugin.getMessage(getPlayer(), "gui.work-listing.work-entry.lore1"))
                        .lore(plugin.getMessage(getPlayer(), "gui.work-listing.work-entry.lore2", "done", work.isDone() ? plugin.getMessage(getPlayer(), "gui.work-listing.done") : plugin.getMessage(getPlayer(), "gui.work-listing.undone")))
                        .lore(plugin.getMessage(getPlayer(), "gui.work-listing.work-entry.lore3", "done", work.voted(getPlayer().getUniqueId()) ? plugin.getMessage(getPlayer(), "gui.work-listing.done") : plugin.getMessage(getPlayer(), "gui.work-listing.undone")))
                        .lore(plugin.getMessage(getPlayer(), "gui.work-listing.work-entry.lore4"))
                        .lore(plugin.getMessage(getPlayer(), "gui.work-listing.work-entry.lore5"))
                        .transformMeta(itemMeta -> {
                            // TODO support offline skull skin display
                            Players.get(work.getOwner()).ifPresent(p -> {
                                SkullMeta skullMeta = (SkullMeta) itemMeta;
                                skullMeta.setPlayerProfile(p.getPlayerProfile());
                            });
                        })
                        .build(() -> {
                            this.work = work;
                            drawVoteOption();
                        }))
                .collect(Collectors.toUnmodifiableList());
    }

}
