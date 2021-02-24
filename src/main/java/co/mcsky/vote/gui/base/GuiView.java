package co.mcsky.vote.gui.base;

import me.lucko.helper.menu.Gui;

/**
 * Represents a view of the GUI.
 */
public interface GuiView {
    /**
     * Draws the view for the GUI.
     *
     * It should be called within {@link Gui#redraw()}.
     */
    void render();
}
