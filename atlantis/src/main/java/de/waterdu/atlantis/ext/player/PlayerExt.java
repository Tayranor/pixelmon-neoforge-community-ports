package de.waterdu.atlantis.ext.player;

import de.waterdu.atlantis.ui.api.Page;
import de.waterdu.atlantis.util.entity.PlayerReference;

import java.util.Optional;

public interface PlayerExt {

    default PlayerReference reference() {
        return null;
    }

    default Optional<PlayerReference> safeReference() {
        return null;
    }

    default void openPage(Page page) {

    }
}
