package de.waterdu.atlantis.self.event;

import de.waterdu.atlantis.ui.api.Button;
import de.waterdu.atlantis.ui.api.ClickData;
import de.waterdu.atlantis.ui.api.Decorations;
import de.waterdu.atlantis.ui.api.Page;
import de.waterdu.atlantis.ui.api.PageOptions;
import de.waterdu.atlantis.ui.internal.Buttons;
import de.waterdu.atlantis.util.entity.PlayerReference;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.Set;

public abstract class PageEvent extends Event {

    private final PlayerReference player;
    private final Page page;

    private PageEvent(PlayerReference player, Page page) {
        this.player = player;
        this.page = page;
    }

    public PlayerReference player() {
        return this.player;
    }

    public Page page() {
        return this.page;
    }

    public static class Construct extends PageEvent implements ICancellableEvent {

        private Buttons buttons;
        private PageOptions options;

        public Construct(PlayerReference player, Page page, Buttons buttons, PageOptions options) {
            super(player, page);
            this.buttons = buttons;
            this.options = options;
        }

        public Buttons buttons() {
            return this.buttons;
        }

        public void setButtons(Buttons buttons) {
            this.buttons = buttons;
        }

        public void addButton(Button button) {
            this.buttons.setButton(button.index(), button.group(), button.page(), button);
        }

        public PageOptions options() {
            return this.options;
        }

        public void setOptions(PageOptions options) {
            this.options = options;
        }
    }

    public static class Decorate extends PageEvent implements ICancellableEvent {

        private final Decorations instance;
        private Set<Button> decorations;

        public Decorate(PlayerReference player, Page page, Decorations instance, Set<Button> decorations) {
            super(player, page);
            this.instance = instance;
            this.decorations = decorations;
        }

        public Decorations instance() {
            return this.instance;
        }

        public Set<Button> decorations() {
            return this.decorations;
        }

        public void addDecoration(Button button) {
            this.decorations.add(button);
        }

        public void setDecorations(Set<Button> decorations) {
            this.decorations = decorations;
        }
    }

    public static class Destruct extends PageEvent implements ICancellableEvent {

        public Destruct(PlayerReference player, Page page) {
            super(player, page);
        }
    }

    public static class Press extends PageEvent implements ICancellableEvent {

        private final Button button;
        private final ClickData clickData;

        public Press(PlayerReference player, Page page, Button button, ClickData clickData) {
            super(player, page);
            this.button = button;
            this.clickData = clickData;
        }

        public Button button() {
            return this.button;
        }

        public ClickData clickData() {
            return this.clickData;
        }
    }
}
