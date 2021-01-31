package co.mcsky.vote.gui;

import me.lucko.helper.Events;
import me.lucko.helper.menu.Gui;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

@SuppressWarnings("ConstantConditions")
public abstract class VoicedGui extends Gui {

    public VoicedGui(Player player, int lines, String title) {
        super(player, lines, title);

        // play sound when clicking items
        Events.subscribe(InventoryClickEvent.class)
                .filter(e -> e.getInventory().getHolder() != null)
                .filter(e -> e.getInventory().getHolder().equals(getPlayer()))
                .filter(e -> e.getCurrentItem() != null)
                .handler(e -> getPlayer().playSound(getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F))
                .bindWith(this);

        // play sound when open the GUI
        Events.subscribe(InventoryOpenEvent.class)
                .filter(e -> e.getPlayer().equals(getPlayer()))
                .handler(e -> getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F))
                .bindWith(this);
    }
}
