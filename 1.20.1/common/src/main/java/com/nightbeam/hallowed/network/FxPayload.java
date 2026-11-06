package com.nightbeam.hallowed.network;

import com.nightbeam.hallowed.Hallowed;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Server to client: a one-off screen effect (a banner, the Spook flash, a camera shake). */
public record FxPayload(Kind kind, float strength) implements HallowedPayload {
    public static final ResourceLocation ID = Hallowed.id("fx");

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

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(kind.ordinal());
        buf.writeFloat(strength);
    }

    public static FxPayload read(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        Kind[] kinds = Kind.values();
        return new FxPayload(kinds[Math.max(0, Math.min(kinds.length - 1, ordinal))], buf.readFloat());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
