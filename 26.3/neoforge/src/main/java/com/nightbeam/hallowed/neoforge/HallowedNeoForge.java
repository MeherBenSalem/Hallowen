package com.nightbeam.hallowed.neoforge;

import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.HallowedEvents;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.entity.HollowHorseman;
import com.nightbeam.hallowed.entity.Spirit;
import com.nightbeam.hallowed.network.FxPayload;
import com.nightbeam.hallowed.network.NightPayload;
import com.nightbeam.hallowed.platform.Platform;
import com.nightbeam.hallowed.platform.Registrar;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Hallowed.MOD_ID)
public final class HallowedNeoForge {
    private final Map<ResourceKey<?>, DeferredRegister<?>> registers = new LinkedHashMap<>();

    public HallowedNeoForge(IEventBus modBus) {
        Hallowed.init(new NeoForgePlatform(), new NeoForgeRegistrar());
        registers.values().forEach(deferred -> deferred.register(modBus));

        modBus.addListener(this::registerPayloads);
        modBus.addListener((EntityAttributeCreationEvent event) -> {
            event.put(ModContent.SPIRIT.get(), Spirit.createAttributes().build());
            event.put(ModContent.HOLLOW_HORSEMAN.get(), HollowHorseman.createAttributes().build());
        });
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (LivingDeathEvent event) -> {
            if (!event.isCanceled()) {
                HallowedEvents.onLivingDeath(event.getEntity(), event.getSource());
            }
        });
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> HallowedEvents.onServerStarted(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> HallowedEvents.onServerStopping(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> HallowedEvents.onServerTick(event.getServer()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) HallowedEvents.onPlayerJoin(player);
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) HallowedEvents.onPlayerLeave(player);
        });
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> HallowedEvents.registerCommands(event.getDispatcher(), event.getBuildContext()));
    }

    @SuppressWarnings("unchecked")
    private <R> DeferredRegister<R> register(ResourceKey<? extends Registry<R>> registry) {
        return (DeferredRegister<R>) registers.computeIfAbsent(registry, key -> DeferredRegister.create(registry, Hallowed.MOD_ID));
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional()
                .playToClient(NightPayload.TYPE, NightPayload.CODEC)
                .playToClient(FxPayload.TYPE, FxPayload.CODEC);
    }

    private final class NeoForgeRegistrar implements Registrar {
        @Override
        public <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registry, String path, Function<Identifier, T> factory) {
            return HallowedNeoForge.this.register(registry).register(path, factory);
        }
    }

    private static final class NeoForgePlatform implements Platform {
        @Override
        public Path configDir() {
            return FMLPaths.CONFIGDIR.get();
        }

        @Override
        public boolean isModLoaded(String modId) {
            return ModList.get().isLoaded(modId);
        }

        @Override
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
            if (player.connection.hasChannel(payload.type())) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }

        @Override
        public void sendToTracking(Entity entity, CustomPacketPayload payload) {
            PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
        }

        @Override
        public boolean isFakePlayer(ServerPlayer player) {
            return player instanceof FakePlayer;
        }

        @Override
        public boolean isClient() {
            return FMLEnvironment.getDist() == Dist.CLIENT;
        }
    }
}
