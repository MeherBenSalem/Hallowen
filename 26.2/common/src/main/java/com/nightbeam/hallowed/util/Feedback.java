package com.nightbeam.hallowed.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/** Sounds and messages for one player. */
public final class Feedback {
    private Feedback() {
    }

    /** A sound only this player hears, at their position. */
    public static void sound(ServerPlayer player, SoundEvent sound, SoundSource source, float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), source,
                player.getX(), player.getEyeY(), player.getZ(), volume, pitch, player.getRandom().nextLong()));
    }

    public static void actionBar(ServerPlayer player, Component message) {
        player.sendOverlayMessage(message);
    }
}
