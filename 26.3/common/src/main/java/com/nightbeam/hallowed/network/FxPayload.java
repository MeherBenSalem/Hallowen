package com.nightbeam.hallowed.network;

import com.nightbeam.hallowed.Hallowed;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to client: a one-off screen effect (a banner, the Spook flash, a camera shake). */
public record FxPayload(Kind kind, float strength) implements CustomPacketPayload {
    public static final Type<FxPayload> TYPE = new Type<>(Hallowed.id("fx"));
    public static final StreamCodec<FriendlyByteBuf, FxPayload> CODEC = CustomPacketPayload.codec(FxPayload::write, FxPayload::read);

    public enum Kind {
        DUSK_BANNER,
        WITCHING_BANNER,
        HORSEMAN_BANNER,
        HORSEMAN_FELLED,
        HORSEMAN_LEFT,
        SPOOK,
        SHAKE,
        TREAT,
        TRICK
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(kind.ordinal());
        buf.writeFloat(strength);
    }

    private static FxPayload read(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        Kind[] kinds = Kind.values();
        return new FxPayload(kinds[Math.max(0, Math.min(kinds.length - 1, ordinal))], buf.readFloat());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
