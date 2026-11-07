package de.waterdu.atlantis.mod.pixelmon;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.ability.AbilityRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.PokeBall;
import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.PokeBallRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import com.pixelmonmod.pixelmon.battles.attacks.ImmutableAttack;
import com.pixelmonmod.pixelmon.enums.TextJustification;
import com.pixelmonmod.pixelmon.items.UIElementItem;
import de.waterdu.atlantis.AtlantisLogger;
import de.waterdu.atlantis.mod.pixelmon.util.ConfigurablePokemon;
import de.waterdu.atlantis.mod.pixelmon.util.PokemonTypeAdapter;
import de.waterdu.atlantis.mod.pixelmon.util.StoredPokemon;
import de.waterdu.atlantis.occasion.OccasionManager;
import de.waterdu.atlantis.util.config.Configurable;
import de.waterdu.atlantis.util.item.ConfigurableItemStack;
import de.waterdu.atlantis.util.item.ItemUtils;
import de.waterdu.atlantis.util.java.collection.ArrayUtils;
import de.waterdu.atlantis.util.java.collection.Pair;
import de.waterdu.atlantis.util.text.TextUtils;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;
import java.util.Locale;

public class PixelmonProxy {

    private static final String INVALID_PROCESSOR_ERROR = "Invalid argument for ParsedItemStack processor {}: {}";
    private static final String PNG = ".png";

    private final PixelmonListener listener;

    public PixelmonProxy() {
        this.listener = new PixelmonListener();
    }

    public PixelmonListener getListener() {
        return this.listener;
    }

    public void registerTypeAdapters(GsonBuilder gsonBuilder) {
        gsonBuilder
                .registerTypeAdapter(StoredPokemon.class, new StoredPokemon.TypeAdapter())
                .registerTypeAdapter(ConfigurablePokemon.class, new ConfigurablePokemon.TypeAdapter())
                .registerTypeAdapter(Pokemon.class, new PokemonTypeAdapter());
    }

    public void registerEventBus() {
        OccasionManager.registerEventBus(Pixelmon.EVENT_BUS);
    }

    public ConfigurableItemStack.Preprocessor[] makeItemPreprocessors() {
        return new ConfigurableItemStack.Preprocessor[]{
                new ConfigurableItemStack.Preprocessor((full, args) -> {
                    StringBuilder specBuilder = new StringBuilder();
                    StringBuilder argsBuilder = new StringBuilder();
                    StringBuilder currentBuilder = specBuilder;
                    int i;
                    for (i = 1; i < args.length; i++) {
                        if (!currentBuilder.isEmpty()) {
                            currentBuilder.append(" ");
                        }
                        String arg = args[i];
                        if (arg.equalsIgnoreCase(ConfigurableItemStack.Preprocessor.END_DIRECTIVE)) {
                            currentBuilder = argsBuilder;
                        } else {
                            currentBuilder.append(arg);
                        }
                    }
                    return Pair.of(
                            SpriteItemHelper.getPhoto(PokemonSpecificationProxy.create(specBuilder.toString()).get().create()),
                            argsBuilder.toString().split(" ")
                    );
                }),
                new ConfigurableItemStack.Preprocessor((full, args) -> Pair.of(
                        PokeBallRegistry.getPokeBall(args[1]).orElse(PokeBallRegistry.POKE_BALL).getBallItem(),
                        ArrayUtils.coerceTail(args, new String[args.length - (args.length >= 3 && args[2].equalsIgnoreCase(ConfigurableItemStack.Preprocessor.END_DIRECTIVE) ? 3 : 2)])
                )),
                new ConfigurableItemStack.Preprocessor((full, args) -> Pair.of(
                        PokeBallRegistry.getPokeBall(args[1]).orElse(PokeBallRegistry.POKE_BALL).getLidItem(),
                        ArrayUtils.coerceTail(args, new String[args.length - (args.length >= 3 && args[2].equalsIgnoreCase(ConfigurableItemStack.Preprocessor.END_DIRECTIVE) ? 3 : 2)])
                ))
        };
    }

    public ConfigurableItemStack.Processor[] makeItemProcessors() {
        return new ConfigurableItemStack.Processor[]{
                new ConfigurableItemStack.Processor((kv, s) -> {
                    if (!kv[1].endsWith(PNG)) {
                        kv[1] += PNG;
                    }
                    ItemUtils.tag(s, tag -> tag.putString(UIElementItem.IMAGE, kv[1]));
                }),
                new ConfigurableItemStack.Processor((kv, s) -> {
                    if (!kv[1].endsWith(PNG)) {
                        kv[1] += PNG;
                    }
                    ItemUtils.tag(s, tag -> tag.putString(UIElementItem.HOVER, kv[1]));
                }),
                new ConfigurableItemStack.Processor((kv, s) -> ItemUtils.tag(s, tag -> tag.putString(UIElementItem.TEXT, kv[1]))),
                new ConfigurableItemStack.Processor((kv, s) -> ItemUtils.tag(s, tag -> tag.putString(UIElementItem.HOVER_TEXT, kv[1]))),
                new ConfigurableItemStack.Processor((kv, s) -> {
                    try {
                        short justification;
                        if (TextUtils.isNumberParsable(kv[1])) {
                            justification = Short.parseShort(kv[1]);
                        } else {
                            justification = (short) (TextJustification.valueOf(kv[1].toUpperCase(Locale.ROOT)).ordinal() - 1);
                        }
                        ItemUtils.tag(s, tag -> tag.putShort(UIElementItem.TEXT_JUSTIFICATION, justification));
                    } catch (IllegalArgumentException | NullPointerException e) {
                        AtlantisLogger.warn(INVALID_PROCESSOR_ERROR, ConfigurableItemStack.Processor.UI_TEXT_JUSTIFICATION_IDENTIFIER, kv[1]);
                    }
                }),
                new ConfigurableItemStack.Processor((kv, s) -> this.shortValueProcessor(kv, s, UIElementItem.TEXT_SCALE, ConfigurableItemStack.Processor.UI_TEXT_SCALE_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.intValueProcessor(kv, s, UIElementItem.X_OFFSET, ConfigurableItemStack.Processor.UI_X_OFFSET_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.intValueProcessor(kv, s, UIElementItem.Y_OFFSET, ConfigurableItemStack.Processor.UI_Y_OFFSET_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.dualIntValueProcessor(kv, s, UIElementItem.X_OFFSET, UIElementItem.Y_OFFSET, ConfigurableItemStack.Processor.UI_OFFSET_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.intValueProcessor(kv, s, UIElementItem.X_OVERRIDE, ConfigurableItemStack.Processor.UI_X_OVERRIDE_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.intValueProcessor(kv, s, UIElementItem.Y_OVERRIDE, ConfigurableItemStack.Processor.UI_Y_OVERRIDE_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.dualIntValueProcessor(kv, s, UIElementItem.X_OVERRIDE, UIElementItem.Y_OVERRIDE, ConfigurableItemStack.Processor.UI_OVERRIDE_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.intValueProcessor(kv, s, UIElementItem.Z_LEVEL, ConfigurableItemStack.Processor.UI_Z_LEVEL_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.intValueProcessor(kv, s, UIElementItem.IMAGE_WIDTH, ConfigurableItemStack.Processor.UI_IMAGE_WIDTH_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.intValueProcessor(kv, s, UIElementItem.IMAGE_HEIGHT, ConfigurableItemStack.Processor.UI_IMAGE_HEIGHT_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.dualIntValueProcessor(kv, s, UIElementItem.IMAGE_WIDTH, UIElementItem.IMAGE_HEIGHT, ConfigurableItemStack.Processor.UI_IMAGE_SIZE_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.floatValueProcessor(kv, s, UIElementItem.IMAGE_U, ConfigurableItemStack.Processor.UI_IMAGE_U_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.floatValueProcessor(kv, s, UIElementItem.IMAGE_V, ConfigurableItemStack.Processor.UI_IMAGE_V_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.dualFloatValueProcessor(kv, s, UIElementItem.IMAGE_U, UIElementItem.IMAGE_V, ConfigurableItemStack.Processor.UI_IMAGE_UV_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.floatValueProcessor(kv, s, UIElementItem.SCALE_X, ConfigurableItemStack.Processor.UI_SCALE_X_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.floatValueProcessor(kv, s, UIElementItem.SCALE_Y, ConfigurableItemStack.Processor.UI_SCALE_Y_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.dualFloatValueProcessor(kv, s, UIElementItem.SCALE_X, UIElementItem.SCALE_Y, ConfigurableItemStack.Processor.UI_SCALE_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.floatValueProcessor(kv, s, UIElementItem.ROTATION, ConfigurableItemStack.Processor.UI_ROTATION_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.singleColorValueProcessor(kv, s, UIElementItem.R, ConfigurableItemStack.Processor.UI_RED_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.singleColorValueProcessor(kv, s, UIElementItem.G, ConfigurableItemStack.Processor.UI_GREEN_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.singleColorValueProcessor(kv, s, UIElementItem.B, ConfigurableItemStack.Processor.UI_BLUE_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.singleColorValueProcessor(kv, s, UIElementItem.A, ConfigurableItemStack.Processor.UI_ALPHA_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.colorValueProcessor(kv, s, UIElementItem.R, UIElementItem.G, UIElementItem.B, UIElementItem.A, ConfigurableItemStack.Processor.UI_COLOR_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.singleColorValueProcessor(kv, s, UIElementItem.HR, ConfigurableItemStack.Processor.UI_HOVER_RED_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.singleColorValueProcessor(kv, s, UIElementItem.HG, ConfigurableItemStack.Processor.UI_HOVER_GREEN_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.singleColorValueProcessor(kv, s, UIElementItem.HB, ConfigurableItemStack.Processor.UI_HOVER_BLUE_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.singleColorValueProcessor(kv, s, UIElementItem.HA, ConfigurableItemStack.Processor.UI_HOVER_ALPHA_IDENTIFIER)),
                new ConfigurableItemStack.Processor((kv, s) -> this.colorValueProcessor(kv, s, UIElementItem.HR, UIElementItem.HG, UIElementItem.HB, UIElementItem.HA, ConfigurableItemStack.Processor.UI_HOVER_COLOR_IDENTIFIER))
        };
    }

    private void shortValueProcessor(String[] kv, ItemStack s, String tag, String processor) {
        try {
            ItemUtils.tag(s, c -> c.putShort(tag, Short.parseShort(kv[1])));
        } catch (NumberFormatException e) {
            AtlantisLogger.warn(INVALID_PROCESSOR_ERROR, processor, kv[1]);
        }
    }

    private void intValueProcessor(String[] kv, ItemStack s, String tag, String processor) {
        try {
            ItemUtils.tag(s, c -> c.putInt(tag, Integer.parseInt(kv[1])));
        } catch (NumberFormatException e) {
            AtlantisLogger.warn(INVALID_PROCESSOR_ERROR, processor, kv[1]);
        }
    }

    private void floatValueProcessor(String[] kv, ItemStack s, String tag, String processor) {
        try {
            ItemUtils.tag(s, c -> c.putFloat(tag, Float.parseFloat(kv[1])));
        } catch (NumberFormatException e) {
            AtlantisLogger.warn(INVALID_PROCESSOR_ERROR, processor, kv[1]);
        }
    }
    
    private void dualIntValueProcessor(String[] kv, ItemStack s, String tag1, String tag2, String processor) {
        var splitTemp = kv[1].split(",");
        String[] split;
        if (splitTemp.length == 1) {
            split = new String[]{splitTemp[0], splitTemp[0]};
        } else {
            split = splitTemp;
        }
        try {
            ItemUtils.tag(s, c -> c.putInt(tag1, Short.parseShort(split[0])));
            ItemUtils.tag(s, c -> c.putInt(tag2, Short.parseShort(split[0])));
        } catch (NumberFormatException e) {
            AtlantisLogger.warn(INVALID_PROCESSOR_ERROR, processor, kv[1]);
        }
    }

    private void dualFloatValueProcessor(String[] kv, ItemStack s, String tag1, String tag2, String processor) {
        var splitTemp = kv[1].split(",");
        String[] split;
        if (splitTemp.length == 1) {
            split = new String[]{splitTemp[0], splitTemp[0]};
        } else {
            split = splitTemp;
        }
        try {
            ItemUtils.tag(s, c -> c.putFloat(tag1, Float.parseFloat(split[0])));
            ItemUtils.tag(s, c -> c.putFloat(tag2, Float.parseFloat(split[0])));
        } catch (NumberFormatException e) {
            AtlantisLogger.warn(INVALID_PROCESSOR_ERROR, processor, kv[1]);
        }
    }

    private void singleColorValueProcessor(String[] kv, ItemStack s, String tag, String processor) {
        try {
            float value;
            if (kv[1].contains(".")) {
                value = Float.parseFloat(kv[1]);
            } else {
                value = Integer.parseInt(kv[1]) / 255F;
            }
            ItemUtils.tag(s, c -> c.putFloat(tag, value));
        } catch (NumberFormatException e) {
            AtlantisLogger.warn(INVALID_PROCESSOR_ERROR, processor, kv[1]);
        }
    }

    private void colorValueProcessor(String[] kv, ItemStack s, String r, String g, String b, String a, String processor) {
        try {
            if (!kv[1].startsWith("#")) {
                kv[1] = "#" + kv[1];
            }
            float[] color = Color.decode(kv[1]).getColorComponents(null);
            ItemUtils.tag(s, c -> c.putFloat(r, color[0]));
            ItemUtils.tag(s, c -> c.putFloat(g, color[1]));
            ItemUtils.tag(s, c -> c.putFloat(b, color[2]));
            ItemUtils.tag(s, c -> c.putFloat(a, color.length > 3 ? color[3] : 1F));
        } catch (NumberFormatException | IndexOutOfBoundsException | NullPointerException e) {
            AtlantisLogger.warn(INVALID_PROCESSOR_ERROR, processor, kv[1]);
        }
    }

    public void registerConfigurableContexts(GsonBuilder gson) {
        Configurable.registerContext(gson, Species.class,
                (json, context) -> {
                    JsonPrimitive primitive = json.getAsJsonPrimitive();
                    if (primitive.isString()) {
                        return PixelmonSpecies.fromName(primitive.getAsString()).getValueUnsafe();
                    } else if (primitive.isNumber()) {
                        return PixelmonSpecies.fromNationalDex(primitive.getAsInt());
                    } else {
                        return null;
                    }
                },
                (src, context) -> new JsonPrimitive(src.getName())
        );

        Configurable.registerContext(gson, ImmutableAttack.class,
                (json, context) -> AttackRegistry.getAttackBase(json.getAsString()).orElse(null),
                (src, context) -> new JsonPrimitive(src.getAttackName())
        );

        Configurable.registerContext(gson, Ability.class,
                (json, context) -> AbilityRegistry.getNewAbility(json.getAsString()).orElse(null),
                (src, context) -> new JsonPrimitive(src.getName())
        );

        Configurable.registerContext(gson, PokeBall.class,
                (json, context) -> PokeBallRegistry.getPokeBall(json.getAsString()).getValueUnsafe(),
                (src, context) -> new JsonPrimitive(src.getName())
        );
    }
}
