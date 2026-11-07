package de.waterdu.atlantis.ui.api;

import de.waterdu.atlantis.Atlantis;
import de.waterdu.atlantis.AtlantisLogger;
import de.waterdu.atlantis.file.auto.FileBackedObject;
import de.waterdu.atlantis.file.datatypes.NamedData;
import de.waterdu.atlantis.util.server.ServerUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@FileBackedObject(mod = Atlantis.ID, path = "ui")
public class Decorations implements NamedData {

    private String classpath;
    private boolean nbt = false;
    private Map<Integer, String> decorations;

    private transient Set<Button> createdDecorations = null;
    private transient Class<? extends Page> clazz = null;
    private final transient UUID uuid = UUID.randomUUID();

    public Decorations() {

    }

    public Decorations(String classpath) {
        this(classpath, new HashMap<>());
    }

    public Decorations(String classpath, Map<Integer, String> decorations) {
        this.classpath = classpath;
        this.decorations = decorations;
    }

    public void setChanged() {
        this.createdDecorations = null;
    }

    public String getClasspath() {
        return this.classpath;
    }

    public Map<Integer, String> getDecorations() {
        return this.decorations;
    }

    public boolean isNbt() {
        return this.nbt;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    public void setDecorations(Map<Integer, String> decorations) {
        this.decorations = decorations;
    }

    public void setNbt(boolean nbt) {
        this.nbt = nbt;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Page> getClassDef() {
        if (this.clazz == null) {
            try {
                this.clazz = (Class<? extends Page>) Class.forName(this.classpath);
            } catch (Exception e) {
                AtlantisLogger.error("Failed to read UIDef for classpath " + this.classpath);
                e.printStackTrace();
            }
        }
        return this.clazz;
    }

    public Set<Button> getButtons() {
        if (this.createdDecorations == null) {
            this.createdDecorations = new HashSet<>();
            for (Map.Entry<Integer, String> decoration : this.decorations.entrySet()) {
                if (this.nbt) {
                    try {
                        String[] split = decoration.getValue().split(" ");
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < split.length; i++) {
                            if (!builder.isEmpty()) {
                                builder.append(" ");
                            }
                            builder.append(split[i]);
                        }
                        var nbt = TagParser.parseTag(builder.toString());
                        ItemStack stack = ItemStack.parseOptional(ServerUtils.registryAccess(), nbt);
                        this.createdDecorations.add(Button.builder().item(stack).index(decoration.getKey()).stackOverride().build());
                    } catch (Exception e) {
                        AtlantisLogger.error("Failed to read decoration in UI def file! " + this.classpath + " - " + decoration);
                        e.printStackTrace();
                    }
                } else {
                    this.createdDecorations.add(Decoration.from(decoration));
                }
            }
        }
        return this.createdDecorations;
    }

    @Override
    public UUID uuid() {
        return this.uuid;
    }

    @Override
    public void setUUID(UUID uuid) {

    }

    @Override
    public String identifier() {
        return this.classpath;
    }

    @Override
    public String name() {
        return this.classpath;
    }

    @Override
    public void setName(String name) {
        this.classpath = name;
    }

    @Override
    public int hashCode() {
        return this.defaultHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.defaultEquals(obj);
    }

    @Override
    public String toString() {
        return "Decorations(" + this.classpath + ")";
    }
}
