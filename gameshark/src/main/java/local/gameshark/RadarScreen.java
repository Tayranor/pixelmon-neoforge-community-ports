package local.gameshark;

import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

public final class RadarScreen extends Screen {
    private static final String[] ANCHORS = {"TOP_RIGHT", "TOP_LEFT", "BOTTOM_RIGHT", "BOTTOM_LEFT"};
    private int tab, modePage, trackedPage;
    private EditBox search, radius, offsetX, offsetY, interval, hudScale;
    private String searchText = "";
    private final List<String> species = new ArrayList<>();
    private final Map<String, String> translated = new HashMap<>();
    private final List<Button> suggestions = new ArrayList<>();
    private final List<String> suggestionNames = new ArrayList<>();

    public RadarScreen() {
        super(Component.literal("GameShark"));
        loadSpecies();
    }

    private void loadSpecies() {
        if (!species.isEmpty()) return;
        try {
            for (var entry : PixelmonSpecies.getAll()) {
                String name = entry.getName();
                species.add(name);
                try { translated.put(name, entry.getNameTranslation().getString()); }
                catch (RuntimeException ignored) { translated.put(name, name); }
            }
            species.sort(String.CASE_INSENSITIVE_ORDER);
        } catch (RuntimeException ignored) {}
    }

    @Override protected void init() {
        clearWidgets(); suggestions.clear(); suggestionNames.clear(); loadSpecies();
        int left = width / 2 - 115, top = height / 2 - 105;
        addRenderableWidget(Button.builder(Component.literal("Categories"), b -> switchTab(0)).bounds(left, top, 75, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Pokémon"), b -> switchTab(1)).bounds(left + 77, top, 76, 18).build());
        addRenderableWidget(Button.builder(Component.literal("HUD"), b -> switchTab(2)).bounds(left + 155, top, 75, 18).build());
        if (tab == 0) categories(left, top);
        if (tab == 1) pokemon(left, top);
        if (tab == 2) hud(left, top);
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> { save(); onClose(); }).bounds(left, top + 187, 230, 20).build());
    }

    private void switchTab(int next) { save(); tab = next; rebuildWidgets(); }

    private void categories(int left, int top) {
        Mode[] modes = Mode.values();
        for (int i = 0; i < 8; i++) {
            int index = modePage * 8 + i;
            if (index >= modes.length) break;
            Mode mode = modes[index];
            addRenderableWidget(Button.builder(Component.literal((RadarClient.SETTINGS.enabled.contains(mode) ? "[ON] " : "[OFF] ") + mode.label), b -> {
                if (!RadarClient.SETTINGS.enabled.remove(mode)) RadarClient.SETTINGS.enabled.add(mode);
                RadarClient.SETTINGS.save(); rebuildWidgets();
            }).bounds(left, top + 22 + i * 18, 230, 17).build());
        }
        addRenderableWidget(Button.builder(Component.literal("<"), b -> { modePage = Math.max(0, modePage - 1); rebuildWidgets(); }).bounds(left, top + 168, 25, 17).build());
        addRenderableWidget(Button.builder(Component.literal("Page " + (modePage + 1) + "/3"), b -> {}).bounds(left + 27, top + 168, 176, 17).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> { modePage = Math.min(2, modePage + 1); rebuildWidgets(); }).bounds(left + 205, top + 168, 25, 17).build());
    }

    private void pokemon(int left, int top) {
        search = new EditBox(font, left, top + 25, 230, 18, Component.literal("Search Pokémon"));
        search.setHint(Component.literal("Type a Pokémon name"));
        search.setValue(searchText);
        search.setResponder(value -> { searchText = value; updateSuggestions(); });
        addRenderableWidget(search);
        for (int i = 0; i < 5; i++) {
            int slot = i;
            suggestionNames.add("");
            Button button = Button.builder(Component.empty(), b -> {
                String name = suggestionNames.get(slot);
                if (!RadarClient.SETTINGS.trackedPokemon.remove(name)) RadarClient.SETTINGS.trackedPokemon.add(name);
                RadarClient.SETTINGS.save(); rebuildWidgets();
            }).bounds(left, top + 44 + i * 16, 230, 15).build();
            suggestions.add(button); addRenderableWidget(button);
        }
        updateSuggestions();
        var tracked = List.copyOf(RadarClient.SETTINGS.trackedPokemon);
        if (trackedPage * 3 >= tracked.size()) trackedPage = Math.max(0, (tracked.size() - 1) / 3);
        for (int i = 0; i < 3; i++) {
            int index = trackedPage * 3 + i;
            if (index >= tracked.size()) break;
            String name = tracked.get(index);
            addRenderableWidget(Button.builder(Component.literal("Remove " + name), b -> {
                RadarClient.SETTINGS.trackedPokemon.remove(name);
                RadarClient.SETTINGS.save(); rebuildWidgets();
            }).bounds(left, top + 144 + i * 14, 230, 13).build());
        }
        addRenderableWidget(Button.builder(Component.literal("<"), b -> { trackedPage = Math.max(0, trackedPage - 1); rebuildWidgets(); }).bounds(left + 170, top + 126, 27, 17).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> { trackedPage++; rebuildWidgets(); }).bounds(left + 202, top + 126, 28, 17).build());
    }

    private void updateSuggestions() {
        String query = searchText.trim().toLowerCase(Locale.ROOT);
        List<String> matches = query.isEmpty() ? List.of() : species.stream()
                .filter(name -> name.toLowerCase(Locale.ROOT).contains(query)
                        || translated.getOrDefault(name, "").toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator.comparingInt(name -> name.toLowerCase(Locale.ROOT).startsWith(query) ? 0 : 1))
                .limit(5).toList();
        for (int i = 0; i < suggestions.size(); i++) {
            Button button = suggestions.get(i);
            button.visible = i < matches.size();
            if (button.visible) {
                String name = matches.get(i);
                suggestionNames.set(i, name);
                String translation = translated.getOrDefault(name, name);
                String label = name.equalsIgnoreCase(translation) ? name : name + " (" + translation + ")";
                button.setMessage(Component.literal((RadarClient.SETTINGS.trackedPokemon.contains(name) ? "[ON] " : "+ ") + label));
            }
        }
    }

    private void hud(int left, int top) {
        RadarSettings settings = RadarClient.SETTINGS;
        addRenderableWidget(Button.builder(Component.literal("Position: " + settings.anchor.replace('_', ' ')), b -> {
            int index = Arrays.asList(ANCHORS).indexOf(settings.anchor);
            settings.anchor = ANCHORS[(index + 1) % ANCHORS.length];
            settings.save(); rebuildWidgets();
        }).bounds(left, top + 25, 230, 19).build());
        offsetX = numberField(left, top + 66, settings.offsetX, "X margin");
        offsetY = numberField(left + 118, top + 66, settings.offsetY, "Y margin");
        radius = numberField(left, top + 109, settings.radius, "Range (16–256)");
        interval = numberField(left + 118, top + 109, settings.updateTicks, "Refresh ticks");
        addRenderableWidget(Button.builder(Component.literal("Through-wall outlines: " + (settings.outlines ? "ON" : "OFF")), b -> {
            save(); settings.outlines = !settings.outlines;
            settings.save(); rebuildWidgets();
        }).bounds(left, top + 136, 230, 19).build());
        hudScale = numberField(left, top + 168, settings.hudScale, "HUD size (%)");
        hudScale.setWidth(230);
    }

    private EditBox numberField(int x, int y, int value, String name) {
        EditBox field = new EditBox(font, x, y, 112, 19, Component.literal(name));
        field.setValue(Integer.toString(value)); addRenderableWidget(field); return field;
    }

    private static int parse(EditBox field, int fallback, int min, int max) {
        if (field == null) return fallback;
        try { return Math.max(min, Math.min(max, Integer.parseInt(field.getValue()))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private void save() {
        RadarSettings settings = RadarClient.SETTINGS;
        settings.radius = parse(radius, settings.radius, 16, 256);
        settings.offsetX = parse(offsetX, settings.offsetX, 0, 5000);
        settings.offsetY = parse(offsetY, settings.offsetY, 0, 5000);
        settings.updateTicks = parse(interval, settings.updateTicks, 5, 200);
        settings.hudScale = parse(hudScale, settings.hudScale, 50, 200);
        settings.save();
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = width / 2 - 115, top = height / 2 - 105;
        graphics.drawCenteredString(font, title, width / 2, top - 14, 0xFFFFFFFF);
        if (tab == 1) graphics.drawString(font, "Tracked: " + RadarClient.SETTINGS.trackedPokemon.size(), left, top + 130, 0xFFFFFFFF);
        if (tab == 2) {
            graphics.drawString(font, "Margins from selected corner", left, top + 53, 0xFFFFFFFF);
            graphics.drawString(font, "Range", left, top + 96, 0xFFFFFFFF);
            graphics.drawString(font, "Refresh ticks", left + 118, top + 96, 0xFFFFFFFF);
            graphics.drawString(font, "HUD size (50–200%)", left, top + 158, 0xFFFFFFFF);
        }
    }
}
