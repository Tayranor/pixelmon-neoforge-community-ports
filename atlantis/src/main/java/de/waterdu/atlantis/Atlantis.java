package de.waterdu.atlantis;

import de.waterdu.atlantis.file.AtlantisConfig;
import de.waterdu.atlantis.file.AtlantisConfigFinder;
import de.waterdu.atlantis.file.AtlantisConfigProxy;
import de.waterdu.atlantis.file.AtlantisData;
import de.waterdu.atlantis.level.chunk.AtlantisChunkLoader;
import de.waterdu.atlantis.level.schematic.Schematics;
import de.waterdu.atlantis.mod.pixelmon.AtlantisPixelmon;
import de.waterdu.atlantis.occasion.OccasionManager;
import de.waterdu.atlantis.playerdata.UUIDNameCache;
import de.waterdu.atlantis.self.AtlantisCommand;
import de.waterdu.atlantis.self.AtlantisListeners;
import de.waterdu.atlantis.ui.api.AtlantisUI;
import de.waterdu.atlantis.ui.api.PageOptions;
import de.waterdu.atlantis.util.bootstrap.Bootstrapper;
import de.waterdu.atlantis.util.concurrency.AtlantisThreadPool;
import de.waterdu.atlantis.util.entity.PlayerReference;
import de.waterdu.atlantis.util.event.DeferredModEventBus;
import de.waterdu.atlantis.util.network.NetworkUtils;
import de.waterdu.atlantis.util.server.ServerUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * <h1>Atlantis</h1>
 * <p>
 * Collection of useful tools for plugin developers.
 * Designed to be used as an external library, shaded in, or copied from directly.
 * Also provides meta and convenience functionalities to enhance the server administration experience.
 * <p>
 * ---
 * <p>
 * Atlantis is dual licensed under both GNU LGPL v3 and MIT.
 * License text for both can be found below, and in LICENSE-Atlantis.txt.
 * You are free to choose which license applies to you for your use case.
 * <p>
 * ---
 * <p>
 * GNU LGPL v3:
 * <p>
 * Atlantis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Atlantis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Atlantis. If not, see <a href="https://www.gnu.org/licenses/lgpl.txt">LGPL v3</a>.
 * <p>
 * ---
 * <p>
 * MIT:
 * <p>
 * Copyright 2019-2025 Waterdude Studios Ltd
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p>
 * ---
 * <p>
 * Previously AquaAPI.
 * <p>
 * ---
 * <p>
 * @author Copyright (C) 2019-2025 Atlantis Development (Waterdude Studios Ltd), sam@waterdu.de
 * @version 1.0
 * @since 02/03/2019
 */
@Mod(Atlantis.ID)
public class Atlantis {

    static {
        AtlantisPixelmon.register();
    }

    public static final String ID = "atlantis";
    public static final String NAME = "Atlantis";
    public static final String VERSION = "9.0.0-local.1";

    public static final String UPDATE = "https://waterdu.de/atlantis/update.json";
    public static final AtlantisThreadPool THREAD_POOL = AtlantisThreadPool.createNewThreadPool(Atlantis.NAME, 8, true);

    public static final String WATER_NAME = "Waterdude";
    public static final String[] WATER_NAME_AS_ARRAY = new String[]{Atlantis.WATER_NAME};
    public static final UUID WATER_UUID = UUID.fromString("04e10682-dfe3-4c2f-9bcb-5e04ec7b647d");

    public static final String ERROR_TEXT = "An internal error has occurred in Atlantis! Please inform your server administrator of this!";
    public static final Supplier<MutableComponent> ERROR_MESSAGE = () -> Component.literal(ChatFormatting.DARK_RED + Atlantis.ERROR_TEXT);

    public static Atlantis INSTANCE;
    private static EventHandlers EVENT_HANDLERS;
    private static AtlantisListeners LISTENERS;

    public static AtlantisConfigProxy.Impl CONFIG = AtlantisConfigProxy.of(Atlantis.ID);

    public static final IEventBus EVENT_BUS = BusBuilder.builder().build();
    public static final DeferredModEventBus MOD_EVENT_BUS = new DeferredModEventBus();

    public Atlantis(IEventBus modEventBus, ModContainer modContainer) {
        INSTANCE = this;
        EVENT_HANDLERS = new EventHandlers();
        MOD_EVENT_BUS.setBus(modEventBus);
        modEventBus.addListener(Atlantis::init);
        modEventBus.addListener(Atlantis::startingServer);
        modEventBus.addListener(Atlantis::startingClient);
        AtlantisConfigFinder.find();
        Bootstrapper.bootstrap();
    }

    @SubscribeEvent
    public static void init(final FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(Atlantis.EVENT_HANDLERS);

        LISTENERS = new AtlantisListeners();
        AtlantisCommand.setup(event);

        AtlantisUI.register();
        AtlantisData.register();

        AtlantisChunkLoader.setup(event);
        Schematics.loadAll();

        OccasionManager.register();

        event.enqueueWork(() -> PageOptions.FormFactor.createDefaults(ServerUtils.registryAccess()));

        AtlantisLogger.info("Welcome to Atlantis " + Atlantis.VERSION);
    }

    @SubscribeEvent
    public static void startingClient(final FMLClientSetupEvent event) {
        AtlantisConfigFinder.init(true);
    }

    @SubscribeEvent
    public static void startingServer(final FMLDedicatedServerSetupEvent event) {
        AtlantisConfigFinder.init(true);
    }

    public static AtlantisListeners listeners() {
        return LISTENERS;
    }

    public static boolean isPlayerWater(Player player) {
        return player.getUUID().equals(Atlantis.WATER_UUID);
    }

    public static boolean isWaterOnline() {
        return ServerUtils.player(Atlantis.WATER_UUID).isPresent();
    }

    private static class EventHandlers {

        @SubscribeEvent
        public void onRegisterCommands(final RegisterCommandsEvent event) {
            AtlantisCommand.makeCommand(event.getDispatcher());
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void login(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                UUIDNameCache.add(player);
                PlayerReference.getSafely(player).ifPresent(PlayerReference::reset);
                AtlantisConfig.onPlayerJoin(player);
                NetworkUtils.addAddress(player);
                AtlantisCommand.login(player);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void changeDim(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                PlayerReference.getSafely(player).ifPresent(PlayerReference::reset);
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void logout(PlayerEvent.PlayerLoggedOutEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                AtlantisConfig.onPlayerLeave(player);
                PlayerReference.getSafely(player).ifPresent(PlayerReference::reset);
            }
        }

        @SubscribeEvent
        public void serverStarted(final ServerStartedEvent event) {
            Atlantis.listeners().startLoggingPackets();
        }

        @SubscribeEvent
        public void stoppingServer(final ServerStoppingEvent event) {
            for (var config : AtlantisConfig.allInstances()) {
                config.asyncSaveInner();
            }
        }
    }
}
