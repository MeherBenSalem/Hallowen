package com.nightbeam.hallowed.network;

import com.nightbeam.hallowed.Hallowed;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server to client: whether night {@code nightIndex} is a Hallowed Night. The client works out every phase from its
 * own clock (see {@code NightClock}), so this is only sent when the answer changes.
 */
public record NightPayload(boolean hallowed, long nightIndex) implements CustomPacketPayload {
    public static final Type<NightPayload> TYPE = new Type<>(Hallowed.id("night"));
    public static final StreamCodec<FriendlyByteBuf, NightPayload> CODEC = CustomPacketPayload.codec(NightPayload::write, NightPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeBoolean(hallowed);
        buf.writeVarLong(nightIndex);
    }

    private static NightPayload read(FriendlyByteBuf buf) {
        return new NightPayload(buf.readBoolean(), buf.readVarLong());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
