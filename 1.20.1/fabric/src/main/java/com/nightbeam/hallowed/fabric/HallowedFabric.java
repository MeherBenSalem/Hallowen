package com.nightbeam.hallowed.fabric;

import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.HallowedEvents;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.entity.HollowHorseman;
import com.nightbeam.hallowed.entity.Spirit;
import com.nightbeam.hallowed.network.HallowedPayload;
import com.nightbeam.hallowed.platform.Platform;
import com.nightbeam.hallowed.platform.Registrar;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;

public final class HallowedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Hallowed.init(new FabricPlatform(), new FabricRegistrar());

        FabricDefaultAttributeRegistry.register(ModContent.SPIRIT.get(), Spirit.createAttributes());
        FabricDefaultAttributeRegistry.register(ModContent.HOLLOW_HORSEMAN.get(), HollowHorseman.createAttributes());

        ServerLivingEntityEvents.AFTER_DEATH.register(HallowedEvents::onLivingDeath);
        ServerLifecycleEvents.SERVER_STARTED.register(HallowedEvents::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(HallowedEvents::onServerStopping);
        ServerTickEvents.END_SERVER_TICK.register(HallowedEvents::onServerTick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> HallowedEvents.onPlayerJoin(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> HallowedEvents.onPlayerLeave(handler.getPlayer()));
        CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> HallowedEvents.registerCommands(dispatcher, context));

        // Vanilla gives spawn eggs their dispenser behaviour at bootstrap, before mod items exist.
        for (Supplier<Item> egg : List.of(ModContent.SPIRIT_SPAWN_EGG, ModContent.WISP_SPAWN_EGG, ModContent.HORSEMAN_SPAWN_EGG)) {
            DispenserBlock.registerBehavior(egg.get(), DISPENSE_EGG);
        }
    }

    /** The vanilla spawn egg dispense behaviour. */
    private static final DispenseItemBehavior DISPENSE_EGG = new DefaultDispenseItemBehavior() {
        @Override
        public ItemStack execute(BlockSource source, ItemStack stack) {
            Direction face = source.getBlockState().getValue(DispenserBlock.FACING);
            EntityType<?> type = ((SpawnEggItem) stack.getItem()).getType(stack.getTag());
            try {
                type.spawn(source.getLevel(), stack, null, source.getPos().relative(face), MobSpawnType.DISPENSER, face != Direction.UP, false);
            } catch (Exception e) {
                LOGGER.error("Error while dispensing spawn egg from dispenser at {}", source.getPos(), e);
                return ItemStack.EMPTY;
            }
            stack.shrink(1);
            source.getLevel().gameEvent(null, GameEvent.ENTITY_PLACE, source.getPos());
            return stack;
        }
    };

    private static final class FabricRegistrar implements Registrar {
        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registry, String path, Function<ResourceLocation, T> factory) {
            ResourceLocation id = Hallowed.id(path);
            T value = factory.apply(id);
            if (value instanceof PoiType poi) {
                // POI types also need their block states mapped; Fabric's helper does both.
                T registered = (T) PointOfInterestHelper.register(id, poi.maxTickets(), poi.validRange(), poi.matchingStates());
                return () -> registered;
            }
            Registry<R> target = (Registry<R>) BuiltInRegistries.REGISTRY.get(registry.location());
            Registry.register(target, id, value);
            return () -> value;
        }
    }

    private static final class FabricPlatform implements Platform {
        @Override
        public Path configDir() {
            return FabricLoader.getInstance().getConfigDir();
        }

        @Override
        public boolean isModLoaded(String modId) {
            return FabricLoader.getInstance().isModLoaded(modId);
        }

        @Override
        public void sendToPlayer(ServerPlayer player, HallowedPayload payload) {
            if (ServerPlayNetworking.canSend(player, payload.id())) {
                FriendlyByteBuf buf = PacketByteBufs.create();
                payload.write(buf);
                ServerPlayNetworking.send(player, payload.id(), buf);
            }
        }

        @Override
        public boolean isFakePlayer(ServerPlayer player) {
            return player instanceof FakePlayer;
        }

        @Override
        public boolean isClient() {
            return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
        }

        @Override
        public CreativeModeTab.Builder tabBuilder() {
            return FabricItemGroup.builder();
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight, Item.Properties properties) {
            return new SpawnEggItem((EntityType) type.get(), background, highlight, properties);
        }

        @Override
        public Item musicDisc(int comparatorOutput, Supplier<SoundEvent> sound, Item.Properties properties, int lengthSeconds) {
            return new RecordItem(comparatorOutput, sound.get(), properties, lengthSeconds);
        }
    }
}
