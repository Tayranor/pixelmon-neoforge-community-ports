package local.gameshark;

import net.neoforged.fml.loading.FMLPaths;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ServerSettings {
    private ServerSettings() {}

    public static boolean enabled() {
        Path path = FMLPaths.CONFIGDIR.get().resolve("sharkscope-server.properties");
        if (!Files.isRegularFile(path)) return true;
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
            return Boolean.parseBoolean(properties.getProperty("enabled", "true"));
        } catch (IOException ignored) {
            return false;
        }
    }
}
