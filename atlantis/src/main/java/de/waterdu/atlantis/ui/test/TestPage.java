package de.waterdu.atlantis.ui.test;

import de.waterdu.atlantis.ui.api.Button;
import de.waterdu.atlantis.ui.api.ButtonCollector;
import de.waterdu.atlantis.ui.api.ClickState;
import de.waterdu.atlantis.ui.api.Page;
import de.waterdu.atlantis.ui.api.PageOptions;
import de.waterdu.atlantis.util.entity.PlayerReference;
import de.waterdu.atlantis.util.item.ConfigurableItemStack;
import net.minecraft.world.item.Items;

public class TestPage implements Page {

    @Override
    public PageOptions getPageOptions(PlayerReference player) {
        return PageOptions.builder()
                .title("Test Page")
                .rows(3)
                .inventoryHidden(true)
                .build();
    }

    @Override
    public void addButtons(PlayerReference player, ButtonCollector buttons) {
        for (int page = 0; page < 10; page++) {
            for (int i = 0; i < 9; i++) {
                buttons.collect(Button.builder()
                        .name("&r&fUpdate Test Page " + (page + 1))
                        .item(ConfigurableItemStack.of(Items.DIAMOND))
                        .index(i)
                        .page(page)
                        .updateTask(30, (button, tick, task) -> button.setItem(Items.NETHER_STAR))
                        .updateTask(30, 15, (button, tick, task) -> button.setItem(Items.DIAMOND))
                        .clickAction(clickData -> {
                            if (clickData.clickState().style() == ClickState.ClickStyle.NORMAL) {
                                clickData.nextPage();
                            }
                        })
                        .build()
                );
            }
        }
    }
}
