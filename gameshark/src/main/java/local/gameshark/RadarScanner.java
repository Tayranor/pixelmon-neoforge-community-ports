package local.gameshark;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.blocks.tileentity.*;
import com.pixelmonmod.pixelmon.entities.DenEntity;
import com.pixelmonmod.pixelmon.entities.WormholeEntity;
import com.pixelmonmod.pixelmon.entities.npcs.NPC;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
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
        PokemonSpecification custom = null;
        if (!settings.customSpec.isBlank()) {
            try {
                var parsed = PokemonSpecificationProxy.create(settings.customSpec.split("\\s+"));
                if (parsed.wasSuccess()) custom = parsed.get();
            } catch (RuntimeException ignored) {}
        }
        int radius2 = settings.radius * settings.radius;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == player || entity.distanceToSqr(player) > radius2) continue;
            if (entity instanceof PixelmonEntity pixelmon) {
                var pokemon = pixelmon.getPokemon();
                if (pokemon == null) continue;
                String name = pokemon.getSpecies().getName();
                if (pixelmon.isLegendary()) add(hits, settings, Mode.LEGENDARIES, name, entity.blockPosition(), player, entity);
                if (pokemon.isShiny()) add(hits, settings, Mode.SHINIES, name, entity.blockPosition(), player, entity);
                if (pixelmon.isBossPokemon()) add(hits, settings, Mode.BOSSES, name, entity.blockPosition(), player, entity);
                if (pokemon.getSpecies().isUltraBeast()) add(hits, settings, Mode.ULTRA_BEASTS, name, entity.blockPosition(), player, entity);
                if (!pokemon.isDefaultPalette() && !pokemon.isShiny()) add(hits, settings, Mode.SPECIAL_PALETTES, name, entity.blockPosition(), player, entity);
                if (settings.trackedPokemon.stream().anyMatch(wanted -> wanted.equalsIgnoreCase(name))
                        || custom != null && custom.matches(pixelmon)) add(hits, settings, Mode.CUSTOM, name, entity.blockPosition(), player, entity);
            } else if (entity instanceof NPC npc) {
                if (npc.getParty() != null && npc.getParty().countAblePokemon() > 0) add(hits, settings, Mode.TRAINERS, npc.getName().getString(), entity.blockPosition(), player, entity);
                boolean questTitle = npc.getTitle() != null && npc.getTitle().getString().toLowerCase().contains("quest");
                boolean questIcon = npc.getIcon() != null && npc.getIcon().getResource() != null && npc.getIcon().getResource().getPath().contains("quest");
                if (questTitle || questIcon) add(hits, settings, Mode.QUEST_GIVERS, npc.getName().getString(), entity.blockPosition(), player, entity);
            } else if (entity instanceof WormholeEntity) add(hits, settings, Mode.ULTRA_WORMHOLES, "Wormhole", entity.blockPosition(), player, entity);
            else if (entity instanceof DenEntity) add(hits, settings, Mode.MAX_RAID_DENS, "Raid den", entity.blockPosition(), player, entity);
            else if (entity instanceof MinecartChest) add(hits, settings, Mode.CHEST_MINECARTS, "Minecart chest", entity.blockPosition(), player, entity);
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
                    if (block instanceof PokeChestTileEntity) add(hits, settings, Mode.POKE_LOOT, "Poké Loot", pos, player);
                    else if (block instanceof PokeStopTileEntity) add(hits, settings, Mode.POKE_STOPS, "Poké Stop", pos, player);
                    else if (block instanceof ZygardeCellTileEntity) add(hits, settings, Mode.ZYGARDE_CELLS, "Zygarde cell", pos, player);
                    else if (block instanceof TimespaceAltarTileEntity || block instanceof BellTileEntity || block instanceof BirdShrineTileEntity || block instanceof IlexShrineTileEntity || block instanceof ArcChaliceTileEntity)
                        add(hits, settings, Mode.LEGENDARY_SUMMONS, "Legendary summon", pos, player);
                    else if (block instanceof ChestBlockEntity) add(hits, settings, Mode.CHESTS, "Chest", pos, player);
                }
            }
        }
        hits.sort(Comparator.comparingInt(Hit::distance));
        return List.copyOf(hits);
    }

    private static void add(List<Hit> hits, RadarSettings settings, Mode mode, String name, BlockPos pos, Player player) {
        add(hits, settings, mode, name, pos, player, null);
    }

    private static void add(List<Hit> hits, RadarSettings settings, Mode mode, String name, BlockPos pos, Player player, Entity entity) {
        if (settings.enabled.contains(mode)) hits.add(new Hit(mode, name, pos,
                (int) Math.sqrt(pos.distSqr(player.blockPosition())), entity == null ? new AABB(pos) : entity.getBoundingBox(), entity));
    }
}

