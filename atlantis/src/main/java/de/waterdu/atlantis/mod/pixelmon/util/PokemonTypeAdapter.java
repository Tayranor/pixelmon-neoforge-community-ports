package de.waterdu.atlantis.mod.pixelmon.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import de.waterdu.atlantis.AtlantisLogger;
import de.waterdu.atlantis.util.server.ServerUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

import java.lang.reflect.Type;

public class PokemonTypeAdapter implements JsonDeserializer<Pokemon>, JsonSerializer<Pokemon> {

    @Override
    public Pokemon deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String nbt = json.getAsString();
        try {
            return PokemonFactory.create(TagParser.parseTag(nbt), ServerUtils.registryAccess());
        } catch (CommandSyntaxException e) {
            AtlantisLogger.error("Failed to read Pokémon data! json = {}", nbt);
            e.printStackTrace();
            return PokemonFactory.create(PixelmonSpecies.MISSINGNO.getValueUnsafe());
        }
    }

    @Override
    public JsonElement serialize(Pokemon src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.writeToNBT(new CompoundTag(), ServerUtils.registryAccess()).toString());
    }
}