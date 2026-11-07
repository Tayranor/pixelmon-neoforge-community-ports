package de.waterdu.atlantis.mod.pixelmon.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.pixelmonmod.api.AbstractSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import de.waterdu.atlantis.util.text.TextUtils;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

public class ConfigurablePokemon {

    private static final Field REQUIREMENTS_FIELD;
    static {
        try {
            REQUIREMENTS_FIELD = AbstractSpecification.class.getDeclaredField("requirements");
            ConfigurablePokemon.REQUIREMENTS_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private final String originalSpec;

    private PokemonSpecification spec = null;
    private Pokemon instance = null;
    private ItemStack sprite = null;

    private int requirementCount = -1;

    private ConfigurablePokemon(String originalSpec) {
        this.originalSpec = originalSpec;
    }

    public static ConfigurablePokemon of(String... specs) {
        return new ConfigurablePokemon(TextUtils.concat(specs));
    }

    public static ConfigurablePokemon from(PokemonSpecification spec) {
        return new ConfigurablePokemon(spec.toString());
    }

    public PokemonSpecification spec() {
        if (this.spec == null) {
            this.spec = PokemonSpecificationProxy.create(this.originalSpec.split(" ")).get();
        }
        return this.spec;
    }

    public int requirementCount() {
        if (this.requirementCount == -1) {
            try {
                List<?> requirements = (List<?>) ConfigurablePokemon.REQUIREMENTS_FIELD.get(this.spec());
                this.requirementCount = requirements.size();
            } catch (IllegalAccessException ignored) { /* Unreachable */ }
        }
        return this.requirementCount;
    }

    public Pokemon instance() {
        if (this.instance == null) {
            this.instance = this.newInstance();
        }
        return this.instance;
    }

    public Pokemon newInstance() {
        return PokemonFactory.create(this.spec());
    }

    public ItemStack sprite() {
        if (this.sprite == null) {
            this.sprite = SpriteItemHelper.getPhoto(this.instance());
        }
        return this.sprite.copy();
    }

    public StoredPokemon stored() {
        return StoredPokemon.of(this);
    }

    public boolean isValid() {
        return !this.originalSpec.isEmpty() && this.requirementCount() > 0;
    }

    public String originalSpec() {
        return this.originalSpec;
    }

    @Override
    public int hashCode() {
        return this.originalSpec.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ConfigurablePokemon other)) {
            return false;
        }
        return this.originalSpec.equalsIgnoreCase(other.originalSpec);
    }

    @Override
    public String toString() {
        return "LazyPokemonSpecification[" + this.originalSpec + "]";
    }

    public static class TypeAdapter implements JsonSerializer<ConfigurablePokemon>, JsonDeserializer<ConfigurablePokemon> {

        @Override
        public ConfigurablePokemon deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                return ConfigurablePokemon.of(json.getAsString());
            } else {
                return PokemonSpecificationSerializer.deserialize(json.getAsJsonObject());
            }
        }

        @Override
        public JsonElement serialize(ConfigurablePokemon src, Type typeOfSrc, JsonSerializationContext context) {
            return PokemonSpecificationSerializer.serialize(src);
        }
    }
}