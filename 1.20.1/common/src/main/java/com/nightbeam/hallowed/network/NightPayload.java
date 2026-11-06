package com.nightbeam.hallowed.network;

import com.nightbeam.hallowed.Hallowed;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Server to client: whether night {@code nightIndex} is a Hallowed Night. The client works out every phase from its
 * own clock (see {@code NightClock}), so this is only sent when the answer changes.
 */
public record NightPayload(boolean hallowed, long nightIndex) implements HallowedPayload {
    public static final ResourceLocation ID = Hallowed.id("night");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(hallowed);
        buf.writeVarLong(nightIndex);
    }

    public static NightPayload read(FriendlyByteBuf buf) {
        return new NightPayload(buf.readBoolean(), buf.readVarLong());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
