package co.mcsky.vote.gui.view;

import co.mcsky.vote.event.PlayerVoteDoneEvent;
import co.mcsky.vote.gui.WorkFilters;
import co.mcsky.vote.gui.base.PaginatedView;
import co.mcsky.vote.gui.base.SeamlessGui;
import co.mcsky.vote.skull.SkullCache;
import co.mcsky.vote.type.Votes;
import co.mcsky.vote.type.Work;
import com.destroystokyo.paper.Title;
import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.paginated.PageInfo;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.menu.scheme.StandardSchemeMappings;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static co.mcsky.vote.VoteMain.plugin;

public class ListingView extends PaginatedView {

    // the backed GUI
    private final SeamlessGui gui;
    private final Player player;

    // the backed instance
    private final Votes votes;
    // currently selected work
    private Work selectedWork;

    // currently applied filter
    private Predicate<Work> filter;

    private final MenuScheme poster = new MenuScheme()
            .mask("000010000");
    private final int doneSlot = new MenuScheme()
            .maskEmpty(4)
            .mask("000010000")
            .getMaskedIndexesImmutable().get(0);

    public ListingView(SeamlessGui gui, Votes votes) {
        super(gui);
        this.gui = gui;
        this.player = gui.getPlayer();
        this.votes = votes;
    }

    @Override
    public void renderSubview() {
        // place the poster
        this.poster.newPopulator(this.gui).accept(ItemStackBuilder.of(Material.BOOK)
                .name(plugin.getMessage(player, "gui.work-listing.menu-tips.name"))
                .lore(plugin.getMessage(player, "gui.work-listing.menu-tips.lore1"))
                .lore(plugin.getMessage(player, "gui.work-listing.menu-tips.lore2"))
                .lore(plugin.getMessage(player, "gui.work-listing.menu-tips.lore3"))
                .buildItem().build());

        // place the done button
        this.gui.setItem(this.doneSlot, ItemStackBuilder.of(Material.APPLE)
                .name(plugin.getMessage(player, "gui.work-listing.submit.name"))
                .lore(plugin.getMessage(player, "gui.work-listing.submit.lore1"))
                .lore(plugin.getMessage(player, "gui.work-listing.submit.lore2"))
                .lore(plugin.getMessage(player, "gui.work-listing.submit.lore3"))
                .lore(plugin.getMessage(player, "gui.work-listing.submit.lore4"))
                .lore(plugin.getMessage(player, "gui.work-listing.submit.lore5"))
                .build(() -> {
                    boolean invalid = this.votes.getCalc().invalid(player.getUniqueId());

                    if (Events.callAndReturn(new PlayerVoteDoneEvent(player, this.votes)).isCancelled()) {
                        return;
                    }

                    // send prompts
                    if (invalid) {
                        // prompt the player that he has not finished the vote yet
                        player.sendMessage(plugin.getMessage(player, "gui-message.vote-not-done"));
                        player.sendMessage(plugin.getMessage(player, "gui-message.must-vote-filtered"));
                        // then filter content to only show the works he needs to vote for
                        this.updateContent(WorkFilters.undone(player.getUniqueId()));
                        this.gui.redraw();
                    } else {
                        // prompt the player that he has done
                        player.sendMessage(plugin.getMessage(player, "gui-message.vote-all-done"));
                        player.playEffect(EntityEffect.TOTEM_RESURRECT);
                        player.sendTitle(Title.builder()
                                .title(plugin.getMessage(player, "title-message.vote-finished.title"))
                                .subtitle(plugin.getMessage(player, "title-message.vote-finished.subtitle"))
                                .fadeIn(20).fadeOut(20).stay(120)
                                .build());
                        // simply close the GUI after the player has done the vote
                        this.gui.close();
                    }
                }));
    }

    @Override
    public MenuScheme getBackgroundSchema() {
        return new MenuScheme(StandardSchemeMappings.STAINED_GLASS)
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
    }

    @Override
    public int getNextPageSlot() {
        return new MenuScheme()
                .maskEmpty(4)
                .mask("000000100")
                .getMaskedIndexesImmutable().get(0);
    }

    @Override
    public int getPreviousPageSlot() {
        return new MenuScheme()
                .maskEmpty(4)
                .mask("001000000")
                .getMaskedIndexesImmutable().get(0);
    }

    @Override
    public Function<PageInfo, ItemStack> getNextPageItem() {
        return pageInfo -> ItemStackBuilder.of(Material.PAPER)
                .name(plugin.getMessage(player, "gui.work-listing.next-page.name"))
                .lore(plugin.getMessage(player, "gui.work-listing.next-page.lore1"))
                .lore(plugin.getMessage(player, "gui.work-listing.next-page.lore2", "current-page", pageInfo.getCurrent(), "total-page", pageInfo.getSize()))
                .build();
    }

    @Override
    public Function<PageInfo, ItemStack> getPreviousPageItem() {
        return pageInfo -> ItemStackBuilder.of(Material.PAPER)
                .name(plugin.getMessage(player, "gui.work-listing.previous-page.name"))
                .lore(plugin.getMessage(player, "gui.work-listing.previous-page.lore1"))
                .lore(plugin.getMessage(player, "gui.work-listing.previous-page.lore2", "current-page", pageInfo.getCurrent(), "total-page", pageInfo.getSize()))
                .build();
    }

    @Override
    public List<Integer> getItemSlots() {
        return new MenuScheme()
                .maskEmpty(1)
                .mask("001111100")
                .mask("001111100")
                .mask("001111100")
                .getMaskedIndexesImmutable();
    }

    public void updateContent() {
        List<Item> content = this.votes.getWorkAll()
                .stream()
                .filter(this.filter)
                .map(work -> ItemStackBuilder.of(Material.PLAYER_HEAD)
                        .name(plugin.getMessage(player, "gui.work-listing.work-entry.name", "player", work.getOwnerName()))
                        .lore(plugin.getMessage(player, "gui.work-listing.work-entry.lore1"))
                        .lore(plugin.getMessage(player, "gui.work-listing.work-entry.lore2", "done", work.isDone() ? plugin.getMessage(player, "gui.work-listing.done") : plugin.getMessage(player, "gui.work-listing.undone")))
                        .lore(plugin.getMessage(player, "gui.work-listing.work-entry.lore3", "done", work.voted(player.getUniqueId()) ? plugin.getMessage(player, "gui.work-listing.done") : plugin.getMessage(player, "gui.work-listing.undone")))
                        .lore(plugin.getMessage(player, "gui.work-listing.work-entry.lore4"))
                        .lore(plugin.getMessage(player, "gui.work-listing.work-entry.lore5"))
                        .transform(item -> SkullCache.INSTANCE.itemWithUuid(item, work.getOwner()))
                        .build(() -> {
                            this.selectedWork = work;
                            this.gui.switchView(new OptionView(this.gui, this));
                        }))
                .collect(Collectors.toUnmodifiableList());
        refreshContent(content);
    }

    private void updateContent(Predicate<Work> predicate) {
        this.filter = predicate;
        this.updateContent();
    }

    public Votes getVotes() {
        return votes;
    }

    public Work getSelectedWork() {
        return selectedWork;
    }
}
