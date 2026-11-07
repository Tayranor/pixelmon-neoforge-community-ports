package de.waterdu.atlantis.util.bootstrap;

import de.waterdu.atlantis.Atlantis;
import de.waterdu.atlantis.AtlantisLogger;
import de.waterdu.atlantis.util.java.ExceptionUtils;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.lang.reflect.AccessFlag;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Bootstrapper {

    private static final AtomicBoolean BOOTSTRAPPED = new AtomicBoolean(false);
    private static final Type BOOTSTRAPPED_TYPE = Type.getType(Bootstrapped.class);

    private Bootstrapper() {
        ExceptionUtils.throwCannotInstantiateClassException();
    }

    public static void bootstrap() {
        if (!BOOTSTRAPPED.getAndSet(true)) {
            final var annotations = ModList.get().getAllScanData().stream()
                    .map(ModFileScanData::getAnnotations)
                    .flatMap(Collection::stream)
                    .filter(data -> BOOTSTRAPPED_TYPE.equals(data.annotationType()))
                    .toList();

            for (var data : annotations) {
                var className = data.clazz().getClassName();
                try {
                    var clazz = Class.forName(className, false, Atlantis.class.getClassLoader());
                    var method = clazz.getDeclaredMethod(clazz.getAnnotation(Bootstrapped.class).entrypoint());
                    var accessFlags = method.accessFlags();
                    var inaccessible = accessFlags.contains(AccessFlag.PRIVATE) || accessFlags.contains(AccessFlag.PROTECTED);
                    if (inaccessible) {
                        method.setAccessible(true);
                    }
                    method.invoke(null);
                    if (inaccessible) {
                        method.setAccessible(false);
                    }
                } catch (Exception ignored) {
                    continue;
                }
                AtlantisLogger.debug("Bootstrapped class {}", className);
            }
        }
    }
}
