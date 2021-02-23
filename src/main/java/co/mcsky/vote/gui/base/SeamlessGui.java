package co.mcsky.vote.gui.base;

import me.lucko.helper.menu.Gui;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.function.Function;

/**
 * An extension of {@link Gui} which supports non-flickering GUI switching by utilising {@link GuiView}.
 */
public class SeamlessGui extends Gui {
    private GuiView currentView;

    /**
     * @param player the player
     * @param lines the number of lines for this GUI
     * @param title the title of this GUI
     * @param startView the starting view of this GUI
     */
    public SeamlessGui(Player player, int lines, String title, Function<SeamlessGui, GuiView> startView) {
        super(player, lines, title);
        this.currentView = startView.apply(this);
    }

    @Override
    public final void redraw() {
        clearItems();
        Objects.requireNonNull(currentView, "currentView");
        this.currentView.render();
    }

    public void switchView(GuiView view) {
        this.currentView = view;
        redraw();
    }

}
