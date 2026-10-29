package com.nightbeam.hallowed.mixin.client;

import com.nightbeam.hallowed.client.Atmosphere;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds the Hallowed Night's layers to the client level's environment attributes (see {@link Atmosphere}). */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method = "addEnvironmentAttributeLayers", at = @At("RETURN"))
    private void hallowed$atmosphere(EnvironmentAttributeSystem.Builder builder, CallbackInfoReturnable<EnvironmentAttributeSystem.Builder> cir) {
        Atmosphere.addLayers(cir.getReturnValue());
    }
}
