package com.nightbeam.hallowed.platform;

import com.nightbeam.hallowed.network.HallowedPayload;
import java.nio.file.Path;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

/** The few things common code needs from the loader. Each loader passes an implementation to {@code Hallowed.init}. */
public interface Platform {
    Path configDir();

    boolean isModLoaded(String modId);

    /** Sends a server-to-client packet, if the player's client has the mod. */
    void sendToPlayer(ServerPlayer player, HallowedPayload payload);

    /** True for automation players (deployers, turtles, fake players of other mods). */
    boolean isFakePlayer(ServerPlayer player);

    boolean isClient();

    /** A creative tab builder that the loader places on its own page. */
    CreativeModeTab.Builder tabBuilder();

    /** A spawn egg for an entity type that may not be registered yet. */
    Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight, Item.Properties properties);

    /** A music disc for a sound event that may not be registered yet. */
    Item musicDisc(int comparatorOutput, Supplier<SoundEvent> sound, Item.Properties properties, int lengthSeconds);
}
