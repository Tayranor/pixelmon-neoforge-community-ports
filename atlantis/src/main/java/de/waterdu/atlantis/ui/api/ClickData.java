package de.waterdu.atlantis.ui.api;

import de.waterdu.atlantis.ui.internal.Buttons;
import de.waterdu.atlantis.util.entity.PlayerReference;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public record ClickData(PlayerReference player, Buttons buttons, int index, int group, ClickState clickState) {

    public static final Consumer<ClickData> PREV_PAGE = ClickData::prevPage;
    public static final Consumer<ClickData> NEXT_PAGE = ClickData::nextPage;
    public static final Consumer<ClickData> CLOSE_PAGE = ClickData::closePage;

    public void openPage(Page page) {
        this.openPage(page, false);
    }

    public void openPage(Page page, boolean waitForScreen) {
        this.openPage(page, waitForScreen, null);
    }

    public void openPage(Page page, Buttons buttons) {
        this.openPage(page, false, buttons);
    }

    public void openPage(Page page, boolean waitForScreen, Buttons buttons) {
        AtlantisUI.open(this.player, page, buttons, waitForScreen);
    }

    public void updatePage(Page page) {
        AtlantisUI.open(this.player.entityDirect(), page, null, false, true);
    }

    public void closePage() {
        AtlantisUI.close(this.player);
    }

    public void nextPage() {
        this.nextPage(true);
    }

    public void nextPage(boolean looped) {
        this.nextPage(this.group, looped);
    }

    public void prevPage() {
        this.prevPage(true);
    }

    public void prevPage(boolean looped) {
        this.prevPage(this.group, looped);
    }

    public void nextPage(int group) {
        this.nextPage(group, true);
    }

    public void nextPage(int group, boolean looped) {
        AtlantisUI.nextPage(this.player, group, looped);
    }

    public void prevPage(int group) {
        this.prevPage(group, true);
    }

    public void prevPage(int group, boolean looped) {
        AtlantisUI.prevPage(this.player, group, looped);
    }

    public void setPage(int page) {
        this.setPage(this.group, page);
    }

    public void setPage(int group, int page) {
        AtlantisUI.setPage(this.player, group, page);
    }

    public void changePage(int group, int change) {
        this.changePage(group, change, true);
    }

    public void changePage(int group, int change, boolean looped) {
        AtlantisUI.changePage(this.player, group, change, looped);
    }

    public void setProperty(int property, int value) {
        AtlantisUI.setProperty(this.player, property, value);
    }

    public ServerPlayer entity() {
        return this.player.entityDirect();
    }

    public boolean matches(int index) {
        return this.index == index;
    }

    public boolean matches(int index, ClickState.MouseButton button) {
        return this.matches(index) && this.clickState.button() == button;
    }

    public boolean matches(int index, ClickState.MouseButton button, ClickState.ClickStyle style) {
        return this.matches(index, button) && this.clickState.style() == style;
    }
}
