package de.waterdu.atlantis.ui.api;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.waterdu.atlantis.file.typeadapters.NumberListTypeAdapter;
import de.waterdu.atlantis.util.entity.PlayerReference;
import de.waterdu.atlantis.util.item.ConfigurableItemStack;
import de.waterdu.atlantis.util.java.JsonUtils;
import de.waterdu.atlantis.util.java.NumberUtils;
import de.waterdu.atlantis.util.java.collection.CompactJsonArrayList;
import de.waterdu.atlantis.util.text.Text;
import de.waterdu.atlantis.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class UIDef<P extends Page> {

    private int rows;
    private Text title;
    private List<Data> data;
    private List<List<Integer>> slotRanges;
    private List<ConfigurableItemStack> items;

    private final Map<String, Action<P>> actions = new HashMap<>();

    private static final Set<Action<Page>> DEFAULT_ACTIONS = new HashSet<>();

    static {
        UIDef.addDefaultAction("prev", ((page, clickData) -> clickData.prevPage()), false);
        UIDef.addDefaultAction("next", ((page, clickData) -> clickData.nextPage()), false);
        UIDef.addDefaultAction("close", ((page, clickData) -> clickData.closePage()), false);
    }

    public static void addDefaultAction(String key, BiConsumer<Page, ClickData> action, boolean sync) {
        DEFAULT_ACTIONS.add(Action.of(key, action, sync));
    }

    public UIDef() {
        this.rows = 3;
        this.title = Text.empty();
        this.data = new ArrayList<>();
        this.slotRanges = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    protected UIDef(int rows, Text title, List<Data> data) {
        this.rows = rows;
        this.title = title;
        this.data = data;
        this.slotRanges = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    public static <P extends Page> UIDef<P> of(int rows, Text title, Data... data) {
        return new UIDef<>(rows, title, Lists.newArrayList(data));
    }

    public static <P extends Page> UIDef<P> of(int rows, String title, Data... data) {
        return new UIDef<>(rows, Text.of(title), Lists.newArrayList(data));
    }

    public UIDef<P> withSlotRange(int min, int max) {
        return this.withSlotRange(NumberUtils.arrayOfRange(min, max));
    }

    public UIDef<P> withSlotRange(int... values) {
        List<Integer> list = new CompactJsonArrayList<>(values.length);
        for (int value : values) {
            list.add(value);
        }
        return this.withSlotRange(list);
    }

    public UIDef<P> withSlotRange(List<Integer> values) {
        this.slotRanges.add(values);
        return this;
    }

    public UIDef<P> withItems(ConfigurableItemStack... items) {
        return this.withItems(Lists.newArrayList(items));
    }

    public UIDef<P> withItems(Collection<ConfigurableItemStack> items) {
        this.items.addAll(items);
        return this;
    }

    @SafeVarargs
    public final UIDef<P> withActions(Action<P>... actions) {
        if (this.actions.isEmpty()) {
            for (Action<P> action : actions) {
                this.actions.put(action.key, action);
            }
            for (Action<Page> action : DEFAULT_ACTIONS) {
                this.actions.put(action.key, Action.of(action.key, action.action::accept, action.sync));
            }
        }
        return this;
    }

    public UIDef<P> resetActions() {
        this.actions.clear();
        return this;
    }

    public Component getTitle(Object... args) {
        return TextUtils.format(this.title, args).compile();
    }

    public PageOptions.Builder getOptions() {
        return PageOptions.builder().inventoryHidden(true).rows(this.rows).title(this.title).titleAlignment(PageOptions.TextAlignment.CENTER);
    }

    public Data getDataForOrdinal(int ordinal) {
        return this.data.get(ordinal);
    }

    public Button.Builder getButton(int ordinal, Object... args) {
        Data data = this.getDataForOrdinal(ordinal);
        Button.Builder builder = Button.builder()
                .index(data.getIndex())
                .item(data.getItem(), 1)
                .name(data.getName().format(args));

        for (Text lore : data.getLore()) {
            builder.loreLine(lore.format(args));
        }
        if (data.hasAction()) {
            Action<P> action = this.actions.getOrDefault(data.getAction(), null);
            if (action != null) {
                if (action.sync) {
                    builder.sync().singlePress();
                }
                builder.clickAction((BiConsumer<Page, ClickData>) action.action);
            }
        }
        return builder;
    }

    public int getButtonCount() {
        return this.data.size();
    }

    public int getRows() {
        return this.rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public Text getTitle() {
        return this.title;
    }

    public void setTitle(Text title) {
        this.title = title;
    }

    public List<Data> getData() {
        return this.data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public List<List<Integer>> getSlotRanges() {
        return this.slotRanges;
    }

    public void setSlotRanges(List<List<Integer>> slotRanges) {
        this.slotRanges = slotRanges;
    }

    public List<Integer> getSlotRange(int index) {
        if (index < 0 || index >= this.slotRanges.size()) {
            return new ArrayList<>();
        }
        return this.slotRanges.get(index);
    }

    public List<ConfigurableItemStack> getItems() {
        return this.items;
    }

    public void setItems(List<ConfigurableItemStack> items) {
        this.items = items;
    }

    public ConfigurableItemStack getItem(int index) {
        if (index < 0 || index >= this.items.size()) {
            return ConfigurableItemStack.empty();
        }
        return this.items.get(index);
    }

    public Page makeSimplePage(Object... args) {
        return new Page() {
            @Override
            public PageOptions getPageOptions(PlayerReference player) {
                return UIDef.this.getOptions().title(UIDef.this.getTitle().copy().format(args)).build();
            }

            @Override
            public void addButtons(PlayerReference player, ButtonCollector buttons) {
                for (int i = 0; i < UIDef.this.getButtonCount(); i++) {
                    buttons.collect(UIDef.this.getButton(i, args));
                }
            }
        };
    }

    public void openSimplePage(Object playerLike, Object... args) {
        PlayerReference.getSafely(playerLike).ifPresent(reference -> reference.open(this.makeSimplePage(args)));
    }

    public static class Action<P extends Page> {

        private final String key;
        private final BiConsumer<P, ClickData> action;
        private final boolean sync;

        private Action(String key, BiConsumer<P, ClickData> action, boolean sync) {
            this.key = key;
            this.action = action;
            this.sync = sync;
        }

        public static <P extends Page> Action<P> of(String key, BiConsumer<P, ClickData> action) {
            return Action.of(key, action, false);
        }

        public static <P extends Page> Action<P> of(String key, BiConsumer<P, ClickData> action, boolean sync) {
            return new Action<>(key, action, sync);
        }
    }

    public static class TypeAdapter implements JsonSerializer<UIDef<? extends Page>>, JsonDeserializer<UIDef<? extends Page>> {

        @Override
        public UIDef<? extends Page> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            int rows = object.getAsJsonPrimitive("rows").getAsInt();
            Text title = context.deserialize(object.get("title"), Text.class);
            JsonArray data = object.getAsJsonArray("data");
            UIDef.Data[] dataArray = new UIDef.Data[data.size()];
            int i = 0;
            for (JsonElement e : data) {
                dataArray[i++] = context.deserialize(e, UIDef.Data.class);
            }
            UIDef<? extends Page> def = UIDef.of(rows, title, dataArray);
            if (object.has("lists")) {
                List<List<Integer>> lists = JsonUtils.deserializeList(object.getAsJsonArray("lists"), e -> context.deserialize(e, NumberListTypeAdapter.INTEGER.getType()));
                for (List<Integer> list : lists) {
                    def.withSlotRange(new CompactJsonArrayList<>(list));
                }
            }
            return def;
        }

        @Override
        public JsonElement serialize(UIDef<? extends Page> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("rows", src.getRows());
            object.add("title", context.serialize(src.getTitle()));
            JsonArray array = new JsonArray();
            for (UIDef.Data data : src.getData()) {
                array.add(context.serialize(data));
            }
            object.add("data", array);
            object.add("lists", JsonUtils.serializeList(src.getSlotRanges(), list -> context.serialize(list, NumberListTypeAdapter.INTEGER.getType())));
            return object;
        }
    }

    public static class Data {

        private int index;
        private int page;
        private String action;
        private ConfigurableItemStack item;
        private Text name;
        private Text[] lore;

        public Data() {}

        protected Data(int index, int page, String action, ConfigurableItemStack item, Text name, Text... lore) {
            this.index = index;
            this.page = page;
            this.action = action;
            this.item = item;
            this.name = name;
            this.lore = lore;
        }

        public static Data of(int index, int page, String action, ConfigurableItemStack item, Text name, Text... lore) {
            return new Data(index, page, action, item, name, lore);
        }

        public static Data of(int index, int page, String action, String item, String name, String... lore) {
            return new Data(index, page, action, ConfigurableItemStack.of(item), Text.of(name), TextUtils.toArray(lore));
        }

        public int getIndex() {
            return this.index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getPage() {
            return this.page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public String getAction() {
            return this.action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public boolean hasAction() {
            return !this.action.isEmpty();
        }

        public ConfigurableItemStack getItem() {
            return this.item;
        }

        public void setItem(ConfigurableItemStack item) {
            this.item = item;
        }

        public Text getName() {
            return this.name;
        }

        public void setName(Text name) {
            this.name = name;
        }

        public Text[] getLore() {
            return this.lore;
        }

        public void setLore(Text[] lore) {
            this.lore = lore;
        }

        public static class TypeAdapter implements JsonSerializer<Data>, JsonDeserializer<Data> {

            @Override
            public UIDef.Data deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject object = json.getAsJsonObject();
                int index = JsonUtils.getOrDefault(object, "index", int.class, 0, context);
                int page = JsonUtils.getOrDefault(object, "page", int.class, -1, context);
                String action = JsonUtils.getOrDefault(object, "action", String.class, "", context);
                ConfigurableItemStack item = JsonUtils.getOrDefault(object, "item", ConfigurableItemStack.class, ConfigurableItemStack.of(Items.AIR), context);
                Text name = JsonUtils.getOrDefault(object, "name", Text.class, Text.empty(), context);
                List<Text> lore;
                if (object.has("lore")) {
                    lore = JsonUtils.deserializeList(object.getAsJsonArray("lore"), e -> context.deserialize(e, Text.class));
                } else {
                    lore = new ArrayList<>();
                }
                return UIDef.Data.of(index, page, action, item, name, lore.toArray(new Text[0]));
            }

            @Override
            public JsonElement serialize(UIDef.Data src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject object = new JsonObject();
                object.addProperty("index", src.getIndex());
                object.addProperty("page", src.getPage());
                object.addProperty("action", src.getAction());
                object.add("item", context.serialize(src.getItem()));
                object.add("name", context.serialize(src.getName()));
                JsonArray lore = new JsonArray();
                for (Text line : src.getLore()) {
                    lore.add(context.serialize(line));
                }
                object.add("lore", lore);
                return object;
            }
        }
    }
}
