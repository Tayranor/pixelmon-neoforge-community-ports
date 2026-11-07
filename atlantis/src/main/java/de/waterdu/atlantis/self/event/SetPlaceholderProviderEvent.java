package de.waterdu.atlantis.self.event;

import de.waterdu.atlantis.util.placeholder.impl.NoProvider;
import de.waterdu.atlantis.util.placeholder.impl.PlaceholderProvider;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class SetPlaceholderProviderEvent extends Event implements ICancellableEvent {

    private PlaceholderProvider provider;

    public SetPlaceholderProviderEvent(PlaceholderProvider provider) {
        this.provider = provider;
    }

    public PlaceholderProvider provider() {
        return this.provider;
    }

    public void setProvider(PlaceholderProvider provider) {
        this.provider = provider;
    }

    @Override
    public void setCanceled(boolean cancel) {
        ICancellableEvent.super.setCanceled(cancel);
        this.provider = NoProvider.INSTANCE;
    }
}
