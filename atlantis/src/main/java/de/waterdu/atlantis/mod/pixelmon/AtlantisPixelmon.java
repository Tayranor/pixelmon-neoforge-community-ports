package de.waterdu.atlantis.mod.pixelmon;

import com.google.gson.GsonBuilder;
import de.waterdu.atlantis.util.item.ConfigurableItemStack;
import de.waterdu.atlantis.util.java.ExceptionUtils;
import de.waterdu.atlantis.util.java.ReflectionUtils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AtlantisPixelmon {

    private static final String PIXELMON_CLASS = "com.pixelmonmod.pixelmon.Pixelmon";
    private static final AtomicBoolean PIXELMON_PRESENT = new AtomicBoolean(false);
    private static final AtomicReference<PixelmonProxy> PROXY = new AtomicReference<>();

    private AtlantisPixelmon() {
        ExceptionUtils.throwCannotInstantiateClassException();
    }

    public static void register() {
        AtlantisPixelmon.PIXELMON_PRESENT.set(ReflectionUtils.doesClassExist(AtlantisPixelmon.PIXELMON_CLASS));
        if (AtlantisPixelmon.isPixelmonPresent()) {
            AtlantisPixelmon.PROXY.set(new PixelmonProxy());
        }
    }

    public static boolean isPixelmonPresent() {
        return AtlantisPixelmon.PIXELMON_PRESENT.get();
    }

    public static void registerTypeAdapters(GsonBuilder gsonBuilder) {
        if (AtlantisPixelmon.PROXY.get() != null) {
            AtlantisPixelmon.PROXY.get().registerTypeAdapters(gsonBuilder);
        }
    }

    public static void registerEventBus() {
        if (AtlantisPixelmon.PROXY.get() != null) {
            AtlantisPixelmon.PROXY.get().registerEventBus();
        }
    }

    public static ConfigurableItemStack.Preprocessor[] makeItemPreprocessors() {
        if (AtlantisPixelmon.PROXY.get() != null) {
            return AtlantisPixelmon.PROXY.get().makeItemPreprocessors();
        }
        return new ConfigurableItemStack.Preprocessor[0];
    }

    public static ConfigurableItemStack.Processor[] makeItemProcessors() {
        if (AtlantisPixelmon.PROXY.get() != null) {
            return AtlantisPixelmon.PROXY.get().makeItemProcessors();
        }
        return new ConfigurableItemStack.Processor[0];
    }

    public static void registerConfigurableContexts(GsonBuilder gson) {
        if (AtlantisPixelmon.PROXY.get() != null) {
            AtlantisPixelmon.PROXY.get().registerConfigurableContexts(gson);
        }
    }
}
