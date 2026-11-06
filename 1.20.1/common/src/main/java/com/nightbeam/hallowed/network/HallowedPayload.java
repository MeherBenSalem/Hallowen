package com.nightbeam.hallowed.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** A server-to-client packet: its channel id and how it writes itself. Each payload also has a static {@code read}. */
public interface HallowedPayload {
    ResourceLocation id();

    void write(FriendlyByteBuf buf);
}
