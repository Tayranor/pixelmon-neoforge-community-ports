package de.waterdu.atlantis.util.server;

import com.mojang.authlib.GameProfile;
import de.waterdu.atlantis.AtlantisLogger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.SocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public final class EmptyPlayerList extends PlayerList {
    
    public static final EmptyPlayerList INSTANCE = new EmptyPlayerList();
    
    private EmptyPlayerList() {
        super(null, null, null, 0);
    }

    @Override
    public void placeNewPlayer(@NonNull Connection connection, @NonNull ServerPlayer player, @NonNull CommonListenerCookie cookie) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    protected void updateEntireScoreboard(@NonNull ServerScoreboard scoreboard, @NonNull ServerPlayer player) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void addWorldborderListener(@NonNull ServerLevel level) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public @NonNull Optional<CompoundTag> load(@NonNull ServerPlayer player) {
        AtlantisLogger.warn("PlayerList accessed too early");
        return Optional.empty();
    }

    @Override
    protected void save(@NonNull ServerPlayer player) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void remove(@NonNull ServerPlayer player) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public @NonNull Component canPlayerLogin(@NonNull SocketAddress socketAddress, @NonNull GameProfile gameProfile) {
        AtlantisLogger.warn("PlayerList accessed too early");
        return Component.empty();
    }

    @Override
    public ServerPlayer getPlayerForLogin(@NonNull GameProfile gameProfile, @NonNull ClientInformation clientInformation) {
        AtlantisLogger.warn("PlayerList accessed too early");
        return null;
    }

    @Override
    public boolean disconnectAllPlayersWithProfile(@NonNull GameProfile gameProfile) {
        AtlantisLogger.warn("PlayerList accessed too early");
        return false;
    }

    @Override
    public ServerPlayer respawn(@NonNull ServerPlayer player, boolean keepInventory, Entity.@NonNull RemovalReason reason) {
        AtlantisLogger.warn("PlayerList accessed too early");
        return null;
    }

    @Override
    public void sendActivePlayerEffects(@NonNull ServerPlayer player) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void sendActiveEffects(@NonNull LivingEntity entity, @NonNull ServerGamePacketListenerImpl connection) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void sendPlayerPermissionLevel(@NonNull ServerPlayer player) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void tick() {

    }

    @Override
    public void broadcastAll(@NonNull Packet<?> packet) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void broadcastAll(@NonNull Packet<?> packet, @NonNull ResourceKey<Level> dimension) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void broadcastSystemToTeam(@NonNull Player player, @NonNull Component message) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void broadcastSystemToAllExceptTeam(@NonNull Player player, @NonNull Component message) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public String @NonNull [] getPlayerNamesArray() {
        AtlantisLogger.warn("PlayerList accessed too early");
        return new String[0];
    }

    @Override
    public void op(@NonNull GameProfile profile) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void deop(@NonNull GameProfile profile) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public boolean isWhiteListed(@NonNull GameProfile profile) {
        AtlantisLogger.warn("PlayerList accessed too early");
        return false;
    }

    @Override
    public boolean isOp(@NonNull GameProfile profile) {
        AtlantisLogger.warn("PlayerList accessed too early");
        return false;
    }

    @Override
    public @Nullable ServerPlayer getPlayerByName(@NonNull String username) {
        AtlantisLogger.warn("PlayerList accessed too early");
        return null;
    }

    @Override
    public void broadcast(@Nullable Player except, double x, double y, double z, double radius, ResourceKey<Level> dimension, Packet<?> packet) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void saveAll() {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public String @NonNull [] getWhiteListNames() {
        AtlantisLogger.warn("PlayerList accessed too early");
        return new String[0];
    }

    @Override
    public String @NonNull [] getOpNames() {
        AtlantisLogger.warn("PlayerList accessed too early");
        return new String[0];
    }

    @Override
    public void reloadWhiteList() {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void sendLevelInfo(@NonNull ServerPlayer player, @NonNull ServerLevel level) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void sendAllPlayerInfo(@NonNull ServerPlayer player) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public int getPlayerCount() {
        AtlantisLogger.warn("PlayerList accessed too early");
        return 0;
    }

    @Override
    public int getMaxPlayers() {
        AtlantisLogger.warn("PlayerList accessed too early");
        return 0;
    }

    @Override
    public boolean isUsingWhitelist() {
        AtlantisLogger.warn("PlayerList accessed too early");
        return false;
    }

    @Override
    public void setUsingWhiteList(boolean whitelistEnabled) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public @NonNull List<ServerPlayer> getPlayersWithAddress(@NonNull String address) {
        AtlantisLogger.warn("PlayerList accessed too early");
        return List.of();
    }

    @Override
    public int getViewDistance() {
        AtlantisLogger.warn("PlayerList accessed too early");
        return 0;
    }

    @Override
    public int getSimulationDistance() {
        AtlantisLogger.warn("PlayerList accessed too early");
        return 0;
    }

    @Override
    public MinecraftServer getServer() {
        AtlantisLogger.warn("PlayerList accessed too early");
        return null;
    }

    @Override
    public @NonNull CompoundTag getSingleplayerData() {
        AtlantisLogger.warn("PlayerList accessed too early");
        return new CompoundTag();
    }

    @Override
    public void setAllowCommandsForAllPlayers(boolean allowCommandsForAllPlayers) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void removeAll() {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void broadcastSystemMessage(@NonNull Component message, boolean bypassHiddenChat) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void broadcastSystemMessage(@NonNull Component serverMessage, @NonNull Function<ServerPlayer, Component> playerMessageFactory, boolean bypassHiddenChat) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void broadcastChatMessage(@NonNull PlayerChatMessage message, @NonNull CommandSourceStack sender, ChatType.@NonNull Bound boundChatType) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void broadcastChatMessage(@NonNull PlayerChatMessage message, @NonNull ServerPlayer sender, ChatType.@NonNull Bound boundChatType) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public PlayerAdvancements getPlayerAdvancements(@NonNull ServerPlayer player) {
        AtlantisLogger.warn("PlayerList accessed too early");
        return null;
    }

    @Override
    public void setViewDistance(int viewDistance) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public void setSimulationDistance(int simulationDistance) {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public @NonNull List<ServerPlayer> getPlayers() {
        AtlantisLogger.warn("PlayerList accessed too early");
        return List.of();
    }

    @Override
    public @Nullable ServerPlayer getPlayer(@NonNull UUID playerUUID) {
        AtlantisLogger.warn("PlayerList accessed too early");
        return null;
    }

    @Override
    public boolean canBypassPlayerLimit(@NonNull GameProfile profile) {
        AtlantisLogger.warn("PlayerList accessed too early");
        return false;
    }

    @Override
    public void reloadResources() {
        AtlantisLogger.warn("PlayerList accessed too early");
    }

    @Override
    public boolean isAllowCommandsForAllPlayers() {
        AtlantisLogger.warn("PlayerList accessed too early");
        return false;
    }

    @Override
    public ServerStatsCounter getPlayerStats(@NonNull Player player) {
        AtlantisLogger.warn("PlayerList accessed too early");
        return null;
    }
}
