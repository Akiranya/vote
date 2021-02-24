package co.mcsky.vote.gui.base;

import me.lucko.helper.Events;
import me.lucko.helper.menu.Gui;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class SoundRegistry {
    /**
     * @param gui the GUI for which to add sound upon clicking an item
     */
    public static void bindClickingSound(Gui gui) {
        clickSound(gui, Sound.UI_BUTTON_CLICK);
    }


    /**
     * @param gui   the GUI for which to add sound upon clicking an item
     * @param sound the sound to add
     */
    public static void bindClickingSound(Gui gui, Sound sound) {
        clickSound(gui, sound);
    }

    /**
     * @param gui the GUI for which to add sound upon opening
     */
    public static void bindOpeningSound(Gui gui) {
        openSound(gui, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
    }

    /**
     * @param gui   the GUI for which to add sound upon opening
     * @param sound the sound to add
     */
    public static void bindOpeningSound(Gui gui, Sound sound) {
        openSound(gui, sound);
    }

    @SuppressWarnings("ConstantConditions")
    private static void clickSound(Gui gui, Sound sound) {
        Events.subscribe(InventoryClickEvent.class)
                .filter(e -> e.getInventory().getHolder() != null)
                .filter(e -> e.getInventory().getHolder().equals(gui.getPlayer()))
                .filter(e -> e.getCurrentItem() != null)
                .handler(e -> gui.getPlayer().playSound(gui.getPlayer().getLocation(), sound, 1F, 1F))
                .bindWith(gui);
    }

    private static void openSound(Gui gui, Sound sound) {
        Events.subscribe(InventoryOpenEvent.class)
                .filter(e -> e.getPlayer().equals(gui.getPlayer()))
                .handler(e -> gui.getPlayer().playSound(gui.getPlayer().getLocation(), sound, 1F, 1F))
                .bindWith(gui);
    }
}
