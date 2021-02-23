package co.mcsky.vote.gui.base;

import me.lucko.helper.menu.Gui;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

/**
 * An extension of {@link Gui} which supports non-flickering GUI switching by utilising {@link GuiView}.
 */
public class SeamlessGui extends Gui {
    private GuiView currentView;

    /**
     * @param player    the player
     * @param lines     the number of lines for this GUI
     * @param title     the title of this GUI
     * @param startView the starting view of this GUI
     */
    public SeamlessGui(Player player, int lines, String title, @Nonnull Function<SeamlessGui, GuiView> startView) {
        super(player, lines, title);
        Objects.requireNonNull(currentView, "startView");
        this.currentView = startView.apply(this);
    }

    @Override
    public final void redraw() {
        clearItems();
        this.currentView.render();
    }

    /**
     * Switch from current view to another view.
     *
     * @param view the view to switch into.
     */
    public void switchView(GuiView view) {
        this.currentView = view;
        redraw();
    }

}
