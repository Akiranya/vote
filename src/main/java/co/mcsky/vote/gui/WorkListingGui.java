package co.mcsky.vote.gui;

import co.mcsky.vote.Votes;
import co.mcsky.vote.helper.MiscUtil;
import co.mcsky.vote.type.Work;
import com.destroystokyo.paper.Title;
import com.google.common.collect.Lists;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.Slot;
import me.lucko.helper.menu.paginated.PageInfo;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.menu.scheme.StandardSchemeMappings;
import me.lucko.helper.utils.Players;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static co.mcsky.vote.VoteMain.plugin;

@SuppressWarnings("ConstantConditions")
public class WorkListingGui extends Gui {

    // TODO support offline skull skin display

    private final MenuScheme background = new MenuScheme(StandardSchemeMappings.STAINED_GLASS)
            .mask("111101111")
            .mask("111111111")
            .mask("110000011")
            .mask("110000011")
            .mask("111111111")
            .scheme(3, 3, 3, 3, 3, 3, 3, 3)
            .scheme(3, 3, 3, 3, 3, 3, 3, 3, 3)
            .scheme(3, 3, 3, 3)
            .scheme(3, 3, 3, 3)
            .scheme(3, 3, 3, 3, 3, 3, 3, 3, 3);

    private final MenuScheme menuTips = new MenuScheme()
            .mask("000010000");

    private final List<Integer> itemSlots = new MenuScheme()
            .maskEmpty(2)
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

    private List<Item> content;

    // starts at 1
    private int page;

    // The vote manager.
    private final Votes votes;

    public WorkListingGui(Player player, Votes votes) {
        super(player, 5, plugin.getMessage(player, "gui.work-listing.title"));

        // Next page item
        this.nextPageItem = pageInfo -> ItemStackBuilder.of(Material.PAPER)
                .name(plugin.getMessage(player, "gui.work-listing.next-page.name"))
                .lore(plugin.getMessage(player, "gui.work-listing.next-page.lore1"))
                .lore(plugin.getMessage(player, "gui.work-listing.next-page.lore2", "current-page", pageInfo.getCurrent(), "total-page", pageInfo.getSize()))
                .build();
        // Previous page item
        this.previousPageItem = pageInfo -> ItemStackBuilder.of(Material.PAPER)
                .name(plugin.getMessage(player, "gui.work-listing.previous-page.name"))
                .lore(plugin.getMessage(player, "gui.work-listing.previous-page.lore1"))
                .lore(plugin.getMessage(player, "gui.work-listing.previous-page.lore2", "current-page", pageInfo.getCurrent(), "total-page", pageInfo.getSize()))
                .build();

        this.votes = votes;

        updateContent(work -> true);
    }

    @Override
    public void redraw() {
        this.background.apply(this);
        this.menuTips.newPopulator(this).accept(ItemStackBuilder.of(Material.BOOK)
                .name(plugin.getMessage(getPlayer(), "gui.work-listing.menu-tips.name"))
                .lore(plugin.getMessage(getPlayer(), "gui.work-listing.menu-tips.lore1"))
                .lore(plugin.getMessage(getPlayer(), "gui.work-listing.menu-tips.lore2"))
                .lore(plugin.getMessage(getPlayer(), "gui.work-listing.menu-tips.lore3"))
                .buildItem().build());

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
                        playSound(Sound.UI_BUTTON_CLICK);
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
                        playSound(Sound.UI_BUTTON_CLICK);
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

        // play initial sound for the first drawn
        if (isFirstDraw()) {
            playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        }

        // place the done button
        boolean valid = this.votes.getCalculator().valid(getPlayer().getUniqueId());
        setItem(doneSlot, ItemStackBuilder.of(valid ? Material.ENCHANTED_GOLDEN_APPLE : Material.APPLE)
                .name(plugin.getMessage(getPlayer(), valid ? "gui.work-listing.valid.name" : "gui.work-listing.invalid.name"))
                .lore(plugin.getMessage(getPlayer(), valid ? "gui.work-listing.valid.lore1" : "gui.work-listing.invalid.lore1"))
                .lore(plugin.getMessage(getPlayer(), valid ? "gui.work-listing.valid.lore2" : "gui.work-listing.invalid.lore2"))
                .lore(plugin.getMessage(getPlayer(), valid ? "gui.work-listing.valid.lore3" : "gui.work-listing.invalid.lore3"))
                .lore(plugin.getMessage(getPlayer(), valid ? "gui.work-listing.valid.lore4" : "gui.work-listing.invalid.lore4"))
                .lore(plugin.getMessage(getPlayer(), valid ? "gui.work-listing.valid.lore5" : "gui.work-listing.invalid.lore5"))
                .lore(plugin.getMessage(getPlayer(), valid ? "gui.work-listing.valid.lore6" : "gui.work-listing.invalid.lore6"))
                .lore(plugin.getMessage(getPlayer(), valid ? "gui.work-listing.valid.lore7" : "gui.work-listing.invalid.lore7"))
                .hideAttributes()
                .build(() -> {
                    playSound(Sound.UI_BUTTON_CLICK);
                    if (!valid) {
                        getPlayer().sendMessage(plugin.getMessage(getPlayer(), "gui-message.valid-filtered"));
                        updateContent(work -> work.isDone() && work.invoted(getPlayer().getUniqueId()));
                        redraw();
                    }
                }));

        // play totem resurrect effect when vote is finished
        if (valid) {
            getPlayer().playEffect(EntityEffect.TOTEM_RESURRECT);
            getPlayer().sendTitle(Title.builder()
                    .title(plugin.getMessage(getPlayer(), "title-message.vote-finished.title"))
                    .subtitle(plugin.getMessage(getPlayer(), "title-message.vote-finished.subtitle"))
                    .fadeIn(20)
                    .fadeOut(20)
                    .stay(120)
                    .build());
        }
    }

    /**
     * @param filter the filter to be applied to the content
     */
    public void updateContent(Predicate<? super Work> filter) {
        this.content = this.votes.getWorks().parallelStream()
                .filter(filter)
                .map(work -> ItemStackBuilder.of(Material.PLAYER_HEAD)
                        .name(plugin.getMessage(getPlayer(), "gui.work-listing.work-entry.name", "player", MiscUtil.getPlayerName(work.getOwner())))
                        .lore(plugin.getMessage(getPlayer(), "gui.work-listing.work-entry.lore1"))
                        .lore(plugin.getMessage(getPlayer(), "gui.work-listing.work-entry.lore2", "done", selectDoneString(work.isDone())))
                        .lore(plugin.getMessage(getPlayer(), "gui.work-listing.work-entry.lore3", "done", selectDoneString(work.voted(getPlayer().getUniqueId()))))
                        .lore(plugin.getMessage(getPlayer(), "gui.work-listing.work-entry.lore4"))
                        .lore(plugin.getMessage(getPlayer(), "gui.work-listing.work-entry.lore5"))
                        .transformMeta(itemMeta -> {
                            SkullMeta skullMeta = (SkullMeta) itemMeta;
                            Players.get(work.getOwner()).ifPresent(p -> skullMeta.setPlayerProfile(p.getPlayerProfile()));
                        })
                        .build(() -> {
                            playSound(Sound.UI_BUTTON_CLICK);
                            new VoteOptionGui(getPlayer(), this, this.votes, work).open();
                        }))
                .collect(Collectors.toUnmodifiableList());
    }

    private void playSound(Sound sound) {
        getPlayer().playSound(getPlayer().getLocation(), sound, 1F, 1F);
    }

    private String selectDoneString(boolean state) {
        return state ? plugin.getMessage(getPlayer(), "gui.work-listing.done") : plugin.getMessage(getPlayer(), "gui.work-listing.undone");
    }

}
