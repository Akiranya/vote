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

/**
 * An abstraction of paginated view.
 * <p>
 * The abstract methods define the necessary components of the paginated view.
 */
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
        // draw background schema
        backgroundSchema().apply(this.gui);

        // get available slots for items
        List<Integer> slots = new ArrayList<>(itemSlots());

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
            Slot slot = this.gui.getSlot(previousPageSlot());
            slot.clearBindings();
            if (slot.hasItem() && slot.getItem().getType() == previousPageItem().apply(PageInfo.create(0, 0)).getType()) {
                slot.clearItem();
            }
        } else {
            this.gui.setItem(previousPageSlot(), ItemStackBuilder.of(previousPageItem().apply(PageInfo.create(this.page, pages.size())))
                    .build(() -> {
                        this.page = this.page - 1;
                        this.gui.redraw();
                    }));
        }

        if (this.page >= pages.size()) {
            // can't go forward a page
            // remove the item if the current slot contains a next page item type
            Slot slot = this.gui.getSlot(nextPageSlot());
            slot.clearBindings();
            if (slot.hasItem() && slot.getItem().getType() == nextPageItem().apply(PageInfo.create(0, 0)).getType()) {
                slot.clearItem();
            }
        } else {
            this.gui.setItem(nextPageSlot(), ItemStackBuilder.of(nextPageItem().apply(PageInfo.create(this.page, pages.size())))
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

        // draw the subview lastly (to override background if there is any)
        renderSubview();
    }

    public void updateContent(List<Item> content) {
        this.content = ImmutableList.copyOf(content);
    }

    abstract public void renderSubview();

    abstract public MenuScheme backgroundSchema();

    abstract public int nextPageSlot();

    abstract public int previousPageSlot();

    abstract public Function<PageInfo, ItemStack> nextPageItem();

    abstract public Function<PageInfo, ItemStack> previousPageItem();

    abstract public List<Integer> itemSlots();
}
