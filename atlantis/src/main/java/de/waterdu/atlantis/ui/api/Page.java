package de.waterdu.atlantis.ui.api;

import de.waterdu.atlantis.ui.internal.AtlantisInventoryInternal;
import de.waterdu.atlantis.ui.internal.Buttons;
import de.waterdu.atlantis.util.entity.PlayerReference;
import net.minecraft.world.entity.player.Player;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interface for creating {@link AtlantisUI} pages.
 *
 * @author      Sam Plummer sam@waterdu.de
 * @version     1.0
 * @since       02/03/2019
 */
public interface Page {

    Page CLOSURE_PAGE = new Page() {
        @Override
        public boolean closureMarker() {
            return true;
        }
    };

    /**
     * Returns the {@link PageOptions} object for all pages.
     *
     * @param       player          The user this page is being shown to.
     * @return                      The options to apply to this page.
     * @see                         PageOptions
     */
    default PageOptions getPageOptions(PlayerReference player) {
        return PageOptions.defaults();
    }

    /**
     * Add buttons to a page in order to be shown.
     * Buttons are instances of {@link Button}.
     *
     * @param       player          The user this page is being shown to.
     * @param       collector       The button collector to supply buttons to.
     * @see                         Button
     */
    default void addButtons(PlayerReference player, final ButtonCollector collector) {

    }

    @Deprecated
    default void addButtons(PlayerReference player, final Set<Button> buttons) {

    }

    /**
     * Called when a button on the page is pressed.
     *
     * @param       player          The user this page is being shown to.
     * @param       index           The index of the button pressed.
     * @param       clickState      The {@link ClickState} of the click.
     */
    default void onButtonClick(PlayerReference player, int index, ClickState clickState) {

    }

    /**
     * Called each tick.
     *
     * @param       tick        The tick this call represents.
     * @param       player      The user this page is being shown to.
     * @param       buttons     The buttons object for this page.
     * @return                  The buttons to now show on this page.
     * @see                     Buttons
     */
    default Buttons onUpdate(long tick, PlayerReference player, Buttons buttons) {
        return buttons;
    }

    /**
     * Called when the page is about to be closed.
     *
     * @param       player      The user this page is being shown to.
     * @return                  Whether to continue with the closure.
     */
    default boolean onClose(PlayerReference player) {
        return true;
    }

    /**
     * Called after the page is closed.
     *
     * @param       player      The user this page is being shown to.
     */
    default void afterClose(PlayerReference player) {

    }

    /**
     * Called when a draggable page has been changed.
     *
     * @param       player      The user this page is being shown to.
     * @param       inventory   The inventory of this page.
     * @param       closed      Whether this change occurred due to the page being closed or not.
     */
    default void onChanged(PlayerReference player, AtlantisInventoryInternal inventory, boolean closed) {

    }

    /**
     * Open the page for a given player.
     *
     * @param       player      The user to show this page to.
     */
    default void open(PlayerReference player) {
        AtlantisUI.open(player, this);
    }

    /**
     * Open the page for a given player.
     *
     * @param       player      The user to show this page to.
     */
    default void open(Player player) {
        AtlantisUI.open(player, this);
    }

    /**
     * Marker for whether this page should be used for page closure, due to how {@link ConcurrentHashMap} is
     * implemented.
     * This should probably be left alone if you don't know what this is for.
     *
     * @return Whether this page is a closure marker.
     *
     * @deprecated It's not. This is just to raise a warning in IDEs.
     */
    @Deprecated
    default boolean closureMarker() {
        return false;
    }
}
