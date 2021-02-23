package co.mcsky.vote.gui.base;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.Slot;
import me.lucko.helper.menu.paginated.PageInfo;
import me.lucko.helper.menu.scheme.MenuScheme;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class PaginatedView implements GuiView {
    private final SeamlessGui gui;
    private List<Item> content;

    // page starts at 1
    private int page;

    public PaginatedView(SeamlessGui gui) {
        this.gui = gui;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public final void render() {
        // draw the subview
        renderSubview();

        // draw background schema
        getBackgroundSchema().apply(this.gui);

        // get available slots for items
        List<Integer> slots = new ArrayList<>(getItemSlots());

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
            Slot slot = this.gui.getSlot(getPreviousPageSlot());
            slot.clearBindings();
            if (slot.hasItem() && slot.getItem().getType() == getPreviousPageItem().apply(PageInfo.create(0, 0)).getType()) {
                slot.clearItem();
            }
        } else {
            this.gui.setItem(getPreviousPageSlot(), ItemStackBuilder.of(getPreviousPageItem().apply(PageInfo.create(this.page, pages.size())))
                    .build(() -> {
                        this.page = this.page - 1;
                        this.gui.redraw();
                    }));
        }

        if (this.page >= pages.size()) {
            // can't go forward a page
            // remove the item if the current slot contains a next page item type
            Slot slot = this.gui.getSlot(getNextPageSlot());
            slot.clearBindings();
            if (slot.hasItem() && slot.getItem().getType() == getNextPageItem().apply(PageInfo.create(0, 0)).getType()) {
                slot.clearItem();
            }
        } else {
            this.gui.setItem(getNextPageSlot(), ItemStackBuilder.of(getNextPageItem().apply(PageInfo.create(this.page, pages.size())))
                    .build(() -> {
                        this.page = this.page + 1;
                        this.gui.redraw();
                    }));
        }

        // remove previous items
        if (!this.gui.isFirstDraw()) {
            slots.forEach(this.gui::removeItem);
        }

        // place the actual items
        for (Item item : page) {
            int index = slots.remove(0);
            this.gui.setItem(index, item);
        }
    }

    public void updateContent(List<Item> content) {
        this.content = ImmutableList.copyOf(content);
    }

    abstract public void renderSubview();

    abstract public MenuScheme getBackgroundSchema();

    abstract public int getNextPageSlot();

    abstract public int getPreviousPageSlot();

    abstract public Function<PageInfo, ItemStack> getNextPageItem();

    abstract public Function<PageInfo, ItemStack> getPreviousPageItem();

    abstract public List<Integer> getItemSlots();
}
