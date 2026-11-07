package local.gameshark;

import net.neoforged.fml.loading.FMLPaths;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Properties;

public final class RadarSettings {
    public boolean active = true;
    public boolean minimized = false;
    public boolean crosshairs = true;
    public int radius = 128;
    public int updateTicks = 20;
    public int x = -8;
    public int y = 8;
    public String customSpec = "";
    public final Set<String> trackedPokemon = new LinkedHashSet<>();
    public String anchor = "TOP_RIGHT";
    public int offsetX = 8;
    public int offsetY = 8;
    public boolean outlines = true;
    public int hudScale = 100;
    public final EnumSet<Mode> enabled = EnumSet.allOf(Mode.class);

    private static Path path() { return FMLPaths.CONFIGDIR.get().resolve("gameshark-local.properties"); }

    public static RadarSettings load() {
        var result = new RadarSettings();
        var p = new Properties();
        try (InputStream in = Files.newInputStream(path())) { p.load(in); }
        catch (IOException ignored) { return result; }
        result.active = Boolean.parseBoolean(p.getProperty("active", "true"));
        result.minimized = Boolean.parseBoolean(p.getProperty("minimized", "false"));
        result.crosshairs = Boolean.parseBoolean(p.getProperty("crosshairs", "true"));
        result.radius = number(p.getProperty("radius"), 128, 16, 256);
        result.updateTicks = number(p.getProperty("updateTicks"), 20, 5, 200);
        result.x = number(p.getProperty("x"), -8, -5000, 5000);
        result.y = number(p.getProperty("y"), 8, 0, 5000);
        result.customSpec = p.getProperty("customSpec", "");
        String saved = p.getProperty("trackedPokemon", "");
        for (String name : saved.split(",")) if (!name.isBlank()) result.trackedPokemon.add(name.trim());
        if (result.trackedPokemon.isEmpty() && result.customSpec.startsWith("species:")) {
            String old = result.customSpec.substring(8).split("\\s+")[0];
            if (!old.isBlank()) result.trackedPokemon.add(old);
        }
        String anchor = p.getProperty("anchor", "TOP_RIGHT");
        if (Set.of("TOP_LEFT", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_RIGHT").contains(anchor)) result.anchor = anchor;
        result.offsetX = number(p.getProperty("offsetX"), 8, 0, 5000);
        result.offsetY = number(p.getProperty("offsetY"), 8, 0, 5000);
        result.outlines = Boolean.parseBoolean(p.getProperty("outlines", "true"));
        result.hudScale = number(p.getProperty("hudScale"), 100, 50, 200);
        for (var mode : Mode.values()) if (!Boolean.parseBoolean(p.getProperty("mode." + mode.name(), "true"))) result.enabled.remove(mode);
        return result;
    }

    private static int number(String s, int fallback, int min, int max) {
        try { return Math.max(min, Math.min(max, Integer.parseInt(s))); }
        catch (Exception ignored) { return fallback; }
    }

    public void save() {
        var p = new Properties();
        p.setProperty("active", String.valueOf(active));
        p.setProperty("minimized", String.valueOf(minimized));
        p.setProperty("crosshairs", String.valueOf(crosshairs));
        p.setProperty("radius", String.valueOf(radius));
        p.setProperty("updateTicks", String.valueOf(updateTicks));
        p.setProperty("x", String.valueOf(x));
        p.setProperty("y", String.valueOf(y));
        p.setProperty("customSpec", customSpec);
        p.setProperty("trackedPokemon", String.join(",", trackedPokemon));
        p.setProperty("anchor", anchor);
        p.setProperty("offsetX", String.valueOf(offsetX));
        p.setProperty("offsetY", String.valueOf(offsetY));
        p.setProperty("outlines", String.valueOf(outlines));
        p.setProperty("hudScale", String.valueOf(hudScale));
        for (var mode : Mode.values()) p.setProperty("mode." + mode.name(), String.valueOf(enabled.contains(mode)));
        try {
            Files.createDirectories(path().getParent());
            try (OutputStream out = Files.newOutputStream(path())) { p.store(out, "GameShark Local"); }
        } catch (IOException ignored) {}
    }
}
