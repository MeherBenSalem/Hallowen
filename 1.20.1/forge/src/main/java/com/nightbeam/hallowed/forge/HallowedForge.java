package com.nightbeam.hallowed.forge;

import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.HallowedEvents;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.entity.HollowHorseman;
import com.nightbeam.hallowed.entity.Spirit;
import com.nightbeam.hallowed.network.FxPayload;
import com.nightbeam.hallowed.network.HallowedPayload;
import com.nightbeam.hallowed.network.NightPayload;
import com.nightbeam.hallowed.platform.Platform;
import com.nightbeam.hallowed.platform.Registrar;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;

@Mod(Hallowed.MOD_ID)
public final class HallowedForge {
    private static final String PROTOCOL = "1";
    /** Optional on both sides: players without the mod can still join, they just miss the effects. */
    static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(Hallowed.id("main"), () -> PROTOCOL,
            NetworkRegistry.acceptMissingOr(PROTOCOL), NetworkRegistry.acceptMissingOr(PROTOCOL));

    private final Map<ResourceKey<?>, DeferredRegister<?>> registers = new LinkedHashMap<>();

    public HallowedForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Hallowed.init(new ForgePlatform(), new ForgeRegistrar());
        registers.values().forEach(deferred -> deferred.register(modBus));
        registerPackets();

        modBus.addListener((EntityAttributeCreationEvent event) -> {
            event.put(ModContent.SPIRIT.get(), Spirit.createAttributes().build());
            event.put(ModContent.HOLLOW_HORSEMAN.get(), HollowHorseman.createAttributes().build());
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, (LivingDeathEvent event) -> {
            if (!event.isCanceled()) {
                HallowedEvents.onLivingDeath(event.getEntity(), event.getSource());
            }
        });
        MinecraftForge.EVENT_BUS.addListener((ServerStartedEvent event) -> HallowedEvents.onServerStarted(event.getServer()));
        MinecraftForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> HallowedEvents.onServerStopping(event.getServer()));
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) HallowedEvents.onServerTick(event.getServer());
        });
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) HallowedEvents.onPlayerJoin(player);
        });
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) HallowedEvents.onPlayerLeave(player);
        });
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> HallowedEvents.registerCommands(event.getDispatcher(), event.getBuildContext()));

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> HallowedForgeClient.init(modBus));
    }

    private static void registerPackets() {
        CHANNEL.messageBuilder(NightPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(NightPayload::write).decoder(NightPayload::read)
                .consumerMainThread((payload, context) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> HallowedForgeClient.handleNight(payload)))
                .add();
        CHANNEL.messageBuilder(FxPayload.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FxPayload::write).decoder(FxPayload::read)
                .consumerMainThread((payload, context) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> HallowedForgeClient.handleFx(payload)))
                .add();
    }

    @SuppressWarnings("unchecked")
    private <R> DeferredRegister<R> register(ResourceKey<? extends Registry<R>> registry) {
        return (DeferredRegister<R>) registers.computeIfAbsent(registry, key -> DeferredRegister.create(registry, Hallowed.MOD_ID));
    }

    private final class ForgeRegistrar implements Registrar {
        @Override
        public <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registry, String path, Function<ResourceLocation, T> factory) {
            ResourceLocation id = Hallowed.id(path);
            return HallowedForge.this.register(registry).register(path, () -> factory.apply(id));
        }
    }

    private static final class ForgePlatform implements Platform {
        @Override
        public Path configDir() {
            return FMLPaths.CONFIGDIR.get();
        }

        @Override
        public boolean isModLoaded(String modId) {
            return ModList.get().isLoaded(modId);
        }

        @Override
        public void sendToPlayer(ServerPlayer player, HallowedPayload payload) {
            if (CHANNEL.isRemotePresent(player.connection.connection)) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
            }
        }

        @Override
        public boolean isFakePlayer(ServerPlayer player) {
            return player instanceof FakePlayer;
        }

        @Override
        public boolean isClient() {
            return FMLEnvironment.dist == Dist.CLIENT;
        }

        @Override
        public CreativeModeTab.Builder tabBuilder() {
            return CreativeModeTab.builder();
        }

        @Override
        public Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight, Item.Properties properties) {
            return new ForgeSpawnEggItem(type, background, highlight, properties);
        }

        @Override
        public Item musicDisc(int comparatorOutput, Supplier<SoundEvent> sound, Item.Properties properties, int lengthSeconds) {
            return new RecordItem(comparatorOutput, sound, properties, lengthSeconds * 20);
        }
    }
}
