package de.waterdu.atlantis.util.placeholder;

import de.waterdu.atlantis.Atlantis;
import de.waterdu.atlantis.self.event.SetPlaceholderProviderEvent;
import de.waterdu.atlantis.util.bootstrap.Bootstrapped;
import de.waterdu.atlantis.util.java.ExceptionUtils;
import de.waterdu.atlantis.util.placeholder.impl.NoProvider;
import de.waterdu.atlantis.util.placeholder.impl.PlaceholderProvider;
import de.waterdu.atlantis.util.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Bootstrapped
public class AtlantisPlaceholderProxy {

    private static final AtomicReference<PlaceholderProvider> PROVIDER = new AtomicReference<>(NoProvider.INSTANCE);
    private static final List<PlaceholderProvider> PROVIDERS = new ArrayList<>();

    private AtlantisPlaceholderProxy() {
        ExceptionUtils.throwCannotInstantiateClassException();
    }

    @SuppressWarnings("unused")
    private static void bootstrap() {
        for (var provider : AtlantisPlaceholderProxy.PROVIDERS) {
            AtlantisPlaceholderProxy.setProvider(provider);
            break;
        }
    }

    public static void addPlaceholderProvider(PlaceholderProvider provider) {
        AtlantisPlaceholderProxy.PROVIDERS.add(provider);
    }

    public static void setProvider(PlaceholderProvider provider) {
        SetPlaceholderProviderEvent event = new SetPlaceholderProviderEvent(provider);
        Atlantis.EVENT_BUS.post(event);
        AtlantisPlaceholderProxy.PROVIDER.set(event.provider());
    }

    public static <T> void register(String id, String name, AtlantisPlaceholderExtension<T> extension, Class<T> type) {
        AtlantisPlaceholderProxy.PROVIDER.get().register(id, name, extension, type);
    }

    public static <T> Text replace(T context, Text text) {
        return AtlantisPlaceholderProxy.PROVIDER.get().replace(context, text);
    }

    public static Text replace(UUID uuid, Text text) {
        return AtlantisPlaceholderProxy.PROVIDER.get().replace(uuid, text);
    }
}
