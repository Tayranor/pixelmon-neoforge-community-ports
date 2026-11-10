package local.gameshark;

import com.pixelmonmod.pixelmon.blocks.tileentity.*;
import com.pixelmonmod.pixelmon.entities.DenEntity;
import com.pixelmonmod.pixelmon.entities.WormholeEntity;
import com.pixelmonmod.pixelmon.entities.npcs.NPC;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.api.npc.interaction.result.type.PlayerStartNPCBattleInteractionResult;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RadarScanner {
    public record Hit(Mode mode, String name, BlockPos pos, int distance, AABB initialBox, Entity entity) {
        public AABB box() { return entity == null || entity.isRemoved() ? initialBox : entity.getBoundingBox(); }
    }

    private RadarScanner() {}

    public static List<Hit> scan(Minecraft mc, RadarSettings settings) {
        if (mc.player == null || mc.level == null) return List.of();
        Player player = mc.player;
        var hits = new ArrayList<Hit>();
        int radius2 = settings.radius * settings.radius;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == player || entity.distanceToSqr(player) > radius2) continue;
            if (entity instanceof PixelmonEntity pixelmon) {
                var pokemon = pixelmon.getPokemon();
                if (pokemon == null) continue;
                if (pixelmon.hasOwner()) continue;
                String name = pokemon.getSpecies().getName();
                if (pixelmon.isLegendary()) add(hits, settings, Mode.LEGENDARIES, name, entity.blockPosition(), player, entity);
                if (pokemon.isShiny()) add(hits, settings, Mode.SHINIES, name, entity.blockPosition(), player, entity);
                if (pixelmon.isBossPokemon() || pokemon.isBoss()) add(hits, settings, Mode.BOSSES, name, entity.blockPosition(), player, entity);
                if (pokemon.getSpecies().isUltraBeast()) add(hits, settings, Mode.ULTRA_BEASTS, name, entity.blockPosition(), player, entity);
                if (!pokemon.isDefaultPalette() && !pokemon.isShiny()) add(hits, settings, Mode.SPECIAL_PALETTES, name, entity.blockPosition(), player, entity);
                if (settings.trackedPokemon.stream().anyMatch(wanted -> wanted.equalsIgnoreCase(name)))
                    add(hits, settings, Mode.CUSTOM, name, entity.blockPosition(), player, entity);
            } else if (entity instanceof NPC npc) {
                String npcName = npc.getName().getString();
                if (isQuestGiver(npc)) add(hits, settings, Mode.QUEST_GIVERS, npcName, entity.blockPosition(), player, entity);
                if (isTrainer(npc)) add(hits, settings, Mode.TRAINERS, npcName, entity.blockPosition(), player, entity);
                add(hits, settings, Mode.NPCS, npcName, entity.blockPosition(), player, entity);
            } else if (entity instanceof WormholeEntity) add(hits, settings, Mode.ULTRA_WORMHOLES, label("wormhole"), entity.blockPosition(), player, entity);
            else if (entity instanceof DenEntity) add(hits, settings, Mode.MAX_RAID_DENS, label("raid_den"), entity.blockPosition(), player, entity);
            else if (entity instanceof MinecartChest) add(hits, settings, Mode.CHEST_MINECARTS, label("minecart_chest"), entity.blockPosition(), player, entity);
            else if (entity instanceof Player) add(hits, settings, Mode.PLAYERS, entity.getName().getString(), entity.blockPosition(), player, entity);
        }
        int playerCx = player.blockPosition().getX() >> 4;
        int playerCz = player.blockPosition().getZ() >> 4;
        int chunks = Math.min(16, (settings.radius + 15) / 16);
        for (int cx = playerCx - chunks; cx <= playerCx + chunks; cx++) {
            for (int cz = playerCz - chunks; cz <= playerCz + chunks; cz++) {
                LevelChunk chunk = mc.level.getChunkSource().getChunk(cx, cz, false);
                if (chunk == null) continue;
                for (BlockEntity block : chunk.getBlockEntities().values()) {
                    BlockPos pos = block.getBlockPos();
                    if (pos.distSqr(player.blockPosition()) > radius2) continue;
                    if (block instanceof PokeChestTileEntity) add(hits, settings, Mode.POKE_LOOT, label("poke_loot"), pos, player);
                    else if (block instanceof PokeStopTileEntity) add(hits, settings, Mode.POKE_STOPS, label("poke_stop"), pos, player);
                    else if (block instanceof ZygardeCellTileEntity) add(hits, settings, Mode.ZYGARDE_CELLS, label("zygarde_cell"), pos, player);
                    else if (block instanceof TimespaceAltarTileEntity || block instanceof BellTileEntity || block instanceof BirdShrineTileEntity || block instanceof IlexShrineTileEntity || block instanceof ArcChaliceTileEntity)
                        add(hits, settings, Mode.LEGENDARY_SUMMONS, label("legendary_summon"), pos, player);
                    else if (block instanceof ChestBlockEntity) add(hits, settings, Mode.CHESTS, label("chest"), pos, player);
                }
            }
        }
        hits.sort(Comparator.comparingInt(Hit::distance));
        return List.copyOf(hits);
    }

    private static String label(String name) {
        return Component.translatable("sharkscope.hit." + name).getString();
    }

    private static boolean isTrainer(NPC npc) {
        if (npc.getLinkedPresetKey() != null && npc.getLinkedPresetKey().getPath().startsWith("battle/")) return true;
        if (npc.getParty() != null && npc.getParty().countAblePokemon() > 0) return true;
        if (npc.getInteractions() != null) {
            for (var interaction : npc.getInteractions()) {
                for (var result : interaction.getResults()) {
                    if (result instanceof PlayerStartNPCBattleInteractionResult) return true;
                }
            }
        }
        return false;
    }

    private static boolean isQuestGiver(NPC npc) {
        if (npc.getLinkedPresetKey() != null && npc.getLinkedPresetKey().getPath().contains("quest")) return true;
        if (npc.getIcon() != null && npc.getIcon().getResource() != null) {
            String icon = npc.getIcon().getResource().getPath().toLowerCase(java.util.Locale.ROOT);
            if (icon.contains("quest") || icon.contains("exclamation") || icon.endsWith("/question.png")) return true;
        }
        Component title = npc.getTitle();
        if (title == null) return false;
        String value = title.getString().toLowerCase(java.util.Locale.ROOT);
        if (value.contains("quest") || value.contains("квест")) return true;
        return title.getContents() instanceof TranslatableContents translated
                && translated.getKey().toLowerCase(java.util.Locale.ROOT).contains("quest");
    }

    private static void add(List<Hit> hits, RadarSettings settings, Mode mode, String name, BlockPos pos, Player player) {
        add(hits, settings, mode, name, pos, player, null);
    }

    private static void add(List<Hit> hits, RadarSettings settings, Mode mode, String name, BlockPos pos, Player player, Entity entity) {
        if (settings.enabled.contains(mode)) hits.add(new Hit(mode, name, pos,
                (int) Math.sqrt(pos.distSqr(player.blockPosition())), entity == null ? new AABB(pos) : entity.getBoundingBox(), entity));
    }
}
