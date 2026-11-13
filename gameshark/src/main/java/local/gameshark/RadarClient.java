package local.gameshark;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.lwjgl.glfw.GLFW;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.List;
import java.util.HashSet;

@EventBusSubscriber(modid = "sharkscope", value = Dist.CLIENT)
public final class RadarClient {
    private static final String CATEGORY = "key.categories.sharkscope";
    private static final KeyMapping TOGGLE = key("toggle", GLFW.GLFW_KEY_0);
    private static final KeyMapping TOGGLE_MODE = key("toggle_mode", GLFW.GLFW_KEY_PERIOD);
    private static final KeyMapping PREVIOUS = key("previous", GLFW.GLFW_KEY_1);
    private static final KeyMapping NEXT = key("next", GLFW.GLFW_KEY_2);
    private static final KeyMapping MINIMIZE = key("minimize", GLFW.GLFW_KEY_3);
    private static final KeyMapping CROSSHAIRS = key("crosshairs", GLFW.GLFW_KEY_4);
    private static final KeyMapping CUSTOM = key("custom", GLFW.GLFW_KEY_KP_ENTER);

    public static final RadarSettings SETTINGS = RadarSettings.load();
    private static List<RadarScanner.Hit> hits = List.of();
    private static int tick;
    private static int selected;
    private static Matrix4f viewProjection;
    private static Vec3 cameraPosition;

    private RadarClient() {}

    private static KeyMapping key(String name, int code) {
        return new KeyMapping("key.gameshark." + name, InputConstants.Type.KEYSYM, code, CATEGORY);
    }

    public static void setServerAllowed(boolean allowed) {
        RadarPermission.setClientAllowed(allowed);
        if (!allowed) hits = List.of();
    }

    private static boolean permitted(Minecraft mc) {
        return mc.hasSingleplayerServer() || RadarPermission.clientAllowed();
    }

    @EventBusSubscriber(modid = "sharkscope", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent event) {
            event.register(TOGGLE); event.register(TOGGLE_MODE); event.register(PREVIOUS);
            event.register(NEXT); event.register(MINIMIZE); event.register(CROSSHAIRS); event.register(CUSTOM);
        }

        @SubscribeEvent
        public static void gui(RegisterGuiLayersEvent event) {
            event.registerAboveAll(ResourceLocation.fromNamespaceAndPath("sharkscope", "radar"), RadarClient::render);
        }
    }

    @SubscribeEvent
    public static void login(ClientPlayerNetworkEvent.LoggingIn event) {
        setServerAllowed(false);
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        setServerAllowed(false);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) { hits = List.of(); return; }
        while (TOGGLE.consumeClick()) SETTINGS.active = !SETTINGS.active;
        while (TOGGLE_MODE.consumeClick()) {
            Mode mode = Mode.values()[selected];
            if (!SETTINGS.enabled.remove(mode)) SETTINGS.enabled.add(mode);
        }
        while (PREVIOUS.consumeClick()) selected = Math.floorMod(selected - 1, Mode.values().length);
        while (NEXT.consumeClick()) selected = (selected + 1) % Mode.values().length;
        while (MINIMIZE.consumeClick()) SETTINGS.minimized = !SETTINGS.minimized;
        while (CROSSHAIRS.consumeClick()) SETTINGS.crosshairs = !SETTINGS.crosshairs;
        while (CUSTOM.consumeClick()) mc.setScreen(new RadarScreen());
        tick++;
        if (!permitted(mc)) hits = List.of();
        else if (tick % SETTINGS.updateTicks == 0 && SETTINGS.active) hits = RadarScanner.scan(mc, SETTINGS);
        if (tick % 100 == 0) SETTINGS.save();
    }

    private static void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker delta) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || !SETTINGS.active || mc.options.hideGui) return;
        if (!permitted(mc)) {
            graphics.drawString(mc.font, Component.translatable("sharkscope.server.disabled"),
                    SETTINGS.offsetX, SETTINGS.offsetY, 0xFFFF7777, true);
            return;
        }
        renderMarkers(graphics, mc);
        Mode mode = Mode.values()[selected];
        int width = Mode.values().length * 12;
        int cardWidth = 170;
        int height = SETTINGS.minimized ? 12 : 60;
        float scale = SETTINGS.hudScale / 100f;
        scale = Math.min(scale, Math.min((graphics.guiWidth() - 4f) / width, (graphics.guiHeight() - 4f) / height));
        float screenX = SETTINGS.anchor.endsWith("RIGHT") ? graphics.guiWidth() - width * scale - SETTINGS.offsetX : SETTINGS.offsetX;
        float screenY = SETTINGS.anchor.startsWith("BOTTOM") ? graphics.guiHeight() - height * scale - SETTINGS.offsetY : SETTINGS.offsetY;
        screenX = Mth.clamp(screenX, 2f, Math.max(2f, graphics.guiWidth() - width * scale - 2f));
        screenY = Mth.clamp(screenY, 2f, Math.max(2f, graphics.guiHeight() - height * scale - 2f));
        graphics.pose().pushPose();
        graphics.pose().translate(screenX, screenY, 0);
        graphics.pose().scale(scale, scale, 1f);
        int x = 0, y = 0;
        int color = 0xFF000000 | mode.rgb;
        int stripX = x;
        int cardX = SETTINGS.anchor.endsWith("RIGHT") ? x + width - cardWidth : x;
        for (int i = 0; i < Mode.values().length; i++) {
            Mode option = Mode.values()[i];
            int bx = stripX + i * 12;
            graphics.fill(bx, y, bx + 11, y + 11, i == selected ? 0xFFFFFFFF : 0xFF555555);
            graphics.fill(bx + 1, y + 1, bx + 10, y + 10, 0xFF151515);
            boolean enabled = SETTINGS.enabled.contains(option);
            graphics.fill(bx + 3, y + 3, bx + 8, y + 6, enabled ? 0xFF000000 | option.rgb : 0xFF333333);
            graphics.fill(bx + 3, y + 6, bx + 8, y + 9, enabled ? 0xFFE7E7E7 : 0xFF555555);
            if (!enabled) graphics.drawString(mc.font, "x", bx + 3, y + 1, 0xFFFF5555, true);
        }
        if (SETTINGS.minimized) { graphics.pose().popPose(); return; }
        graphics.fill(cardX, y + 14, cardX + cardWidth, y + 59, 0xB8000000);
        graphics.fill(cardX, y + 14, cardX + 2, y + 59, color);
        boolean enabled = SETTINGS.enabled.contains(mode);
        graphics.drawString(mc.font, mode.label() + (enabled ? "" : " [" + Component.translatable("sharkscope.off").getString() + "]"), cardX + 6, y + 18,
                enabled ? color : 0xFFFF5555, true);
        RadarScanner.Hit nearest = null;
        int count = 0;
        for (var hit : hits) {
            if (hit.mode() != mode) continue;
            if (nearest == null) nearest = hit;
            count++;
        }
        if (!enabled) graphics.drawString(mc.font, Component.translatable("sharkscope.category.disabled"), cardX + 6, y + 39, 0xFFFF7777, true);
        else if (nearest == null) graphics.drawString(mc.font, Component.translatable("sharkscope.no_targets"), cardX + 6, y + 39, 0xFFAAAAAA, true);
        else {
            graphics.drawString(mc.font, mc.font.plainSubstrByWidth(nearest.name(), cardWidth - 12), cardX + 6, y + 31, 0xFFFFFFFF, true);
            String direction = SETTINGS.crosshairs ? bearing(mc, nearest) + "  " : "";
            graphics.drawString(mc.font, direction + Component.translatable("sharkscope.distance", nearest.distance(), count).getString(), cardX + 6, y + 45, color, true);
        }
        graphics.pose().popPose();
    }

    @SubscribeEvent
    public static void captureProjection(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        viewProjection = new Matrix4f(event.getProjectionMatrix()).mul(event.getModelViewMatrix());
        cameraPosition = event.getCamera().getPosition();
    }

    private static void renderMarkers(GuiGraphics graphics, Minecraft mc) {
        if (!SETTINGS.outlines || hits.isEmpty() || viewProjection == null || cameraPosition == null) return;
        var drawnEntities = new HashSet<Integer>();
        var drawnBlocks = new HashSet<net.minecraft.core.BlockPos>();
        for (var hit : hits) {
            AABB box;
            if (hit.entity() != null) {
                if (hit.entity().isRemoved() || !drawnEntities.add(hit.entity().getId())) continue;
                box = hit.entity().getBoundingBox();
            } else if (drawnBlocks.add(hit.pos())) {
                var state = mc.level.getBlockState(hit.pos());
                var shape = state.getShape(mc.level, hit.pos(), CollisionContext.empty());
                box = shape.isEmpty() ? new AABB(hit.pos()) : shape.bounds().move(hit.pos());
            } else continue;
            drawBrackets(graphics, box, 0xFF000000 | hit.mode().rgb);
        }
    }

    private static void drawBrackets(GuiGraphics graphics, AABB box, int color) {
        Vector4f[] corners = new Vector4f[8];
        for (int i = 0; i < 8; i++) {
            double wx = (i & 1) == 0 ? box.minX : box.maxX;
            double wy = (i & 2) == 0 ? box.minY : box.maxY;
            double wz = (i & 4) == 0 ? box.minZ : box.maxZ;
            corners[i] = new Vector4f((float) (wx - cameraPosition.x),
                    (float) (wy - cameraPosition.y), (float) (wz - cameraPosition.z), 1f).mul(viewProjection);
        }
        int bright = 0xFF000000
                | (((((color >> 16) & 255) * 3 + 510) / 5) << 16)
                | (((((color >> 8) & 255) * 3 + 510) / 5) << 8)
                | (((color & 255) * 3 + 510) / 5);
        float left = Float.POSITIVE_INFINITY, top = Float.POSITIVE_INFINITY;
        float right = Float.NEGATIVE_INFINITY, bottom = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < 8; i++) {
            Vector4f corner = corners[i];
            if (corner.w > 0.05f) {
                float x = (corner.x / corner.w * 0.5f + 0.5f) * graphics.guiWidth();
                float y = (0.5f - corner.y / corner.w * 0.5f) * graphics.guiHeight();
                left = Math.min(left, x); right = Math.max(right, x);
                top = Math.min(top, y); bottom = Math.max(bottom, y);
            }
            for (int axis = 0; axis < 3; axis++) {
                Vector4f end = new Vector4f(corner).lerp(corners[i ^ (1 << axis)], 0.28f);
                drawSegment(graphics, corner, end, bright);
            }
        }
        if (right - left < 8 && bottom - top < 8) {
            Vector4f center = new Vector4f((float) (box.getCenter().x - cameraPosition.x),
                    (float) (box.getCenter().y - cameraPosition.y),
                    (float) (box.getCenter().z - cameraPosition.z), 1f).mul(viewProjection);
            if (center.w <= 0.05f) return;
            int x = (int) ((center.x / center.w * 0.5f + 0.5f) * graphics.guiWidth());
            int y = (int) ((0.5f - center.y / center.w * 0.5f) * graphics.guiHeight());
            if (x < 0 || x >= graphics.guiWidth() || y < 0 || y >= graphics.guiHeight()) return;
            graphics.fill(x - 5, y - 1, x + 6, y + 2, 0xB0000000);
            graphics.fill(x - 1, y - 5, x + 2, y + 6, 0xB0000000);
            graphics.fill(x - 4, y, x + 5, y + 1, bright);
            graphics.fill(x, y - 4, x + 1, y + 5, bright);
        }
    }

    private static void drawSegment(GuiGraphics graphics, Vector4f start, Vector4f end, int color) {
        if (start.w <= 0.05f && end.w <= 0.05f) return;
        Vector4f a = new Vector4f(start), b = new Vector4f(end);
        if (a.w <= 0.05f) a.lerp(b, (0.05f - a.w) / (b.w - a.w));
        if (b.w <= 0.05f) b.lerp(a, (0.05f - b.w) / (a.w - b.w));
        float x1 = (a.x / a.w * 0.5f + 0.5f) * graphics.guiWidth();
        float y1 = (0.5f - a.y / a.w * 0.5f) * graphics.guiHeight();
        float x2 = (b.x / b.w * 0.5f + 0.5f) * graphics.guiWidth();
        float y2 = (0.5f - b.y / b.w * 0.5f) * graphics.guiHeight();
        float dx = x2 - x1, dy = y2 - y1;
        float[] clip = {0f, 1f};
        if (!clip(-dx, x1, clip) || !clip(dx, graphics.guiWidth() - x1, clip)
                || !clip(-dy, y1, clip) || !clip(dy, graphics.guiHeight() - y1, clip)) return;
        float sx = x1 + clip[0] * dx, sy = y1 + clip[0] * dy;
        float ex = x1 + clip[1] * dx, ey = y1 + clip[1] * dy;
        float length = (float) Math.hypot(ex - sx, ey - sy);
        if (length < 0.5f) return;
        graphics.pose().pushPose();
        graphics.pose().translate(sx, sy, 0);
        graphics.pose().mulPose(Axis.ZP.rotation((float) Math.atan2(ey - sy, ex - sx)));
        graphics.fill(0, -2, (int) Math.ceil(length) + 1, 3, 0xB0000000);
        graphics.fill(0, -1, (int) Math.ceil(length), 2, color);
        graphics.pose().popPose();
    }

    private static boolean clip(float p, float q, float[] range) {
        if (p == 0f) return q >= 0f;
        float t = q / p;
        if (p < 0f) range[0] = Math.max(range[0], t);
        else range[1] = Math.min(range[1], t);
        return range[0] <= range[1];
    }

    private static String bearing(Minecraft mc, RadarScanner.Hit hit) {
        double dx = hit.pos().getX() + 0.5 - mc.player.getX();
        double dz = hit.pos().getZ() + 0.5 - mc.player.getZ();
        double absolute = Math.toDegrees(Math.atan2(-dx, dz));
        double relative = Mth.wrapDegrees((float) (absolute - mc.player.getYRot()));
        if (relative > 35) return ">>";
        if (relative < -35) return "<<";
        return "^^";
    }
}
