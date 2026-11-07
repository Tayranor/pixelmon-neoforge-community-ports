package de.waterdu.atlantis.ui.api;

import de.waterdu.atlantis.Settings;
import net.minecraft.world.inventory.ClickType;

public class ClickState {

    private ClickData data;

    private final MouseButton button;
    private final ClickStyle style;
    private ClickRate rate = ClickRate.SINGLE;

    private final long time = System.currentTimeMillis();

    public ClickState(MouseButton button, ClickStyle style) {
        this.button = button;
        this.style = style;
    }

    public ClickState withData(ClickData data) {
        this.data = data;
        return this;
    }

    public boolean isSameButton(ClickState state) {
        return state != null && this.button == state.button && this.style == state.style && this.rate == state.rate;
    }

    public void tryDoubleClick(ClickState previous) {
        if (this.isSameButton(previous) && this.time - previous.time <= Settings.get().getDoubleClickDelay()) {
            this.rate = ClickRate.DOUBLE;
        }
    }

    public ClickData data() {
        return this.data;
    }

    public MouseButton button() {
        return this.button;
    }

    public ClickStyle style() {
        return this.style;
    }

    public ClickRate rate() {
        return this.rate;
    }

    public enum MouseButton {

        LEFT,
        RIGHT,
        MIDDLE,
        UNKNOWN;

        public static MouseButton get(int button) {
            if (button >= 0 && button <= 2) {
                return MouseButton.values()[button];
            }
            return MouseButton.UNKNOWN;
        }
    }

    public enum ClickStyle {

        NORMAL,
        SHIFT,
        DRAG,
        UNKNOWN;

        public static ClickStyle get(ClickType type) {
            if (type == ClickType.CLONE || type == ClickType.PICKUP) {
                return ClickStyle.NORMAL;
            } else if (type == ClickType.QUICK_MOVE) {
                return ClickStyle.SHIFT;
            } else if (type == ClickType.QUICK_CRAFT) {
                return ClickStyle.DRAG;
            }
            return ClickStyle.UNKNOWN;
        }
    }

    public enum ClickRate {

        SINGLE,
        DOUBLE
    }
}
