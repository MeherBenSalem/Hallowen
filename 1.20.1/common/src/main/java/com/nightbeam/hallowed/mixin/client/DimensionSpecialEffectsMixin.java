package com.nightbeam.hallowed.mixin.client;

import com.nightbeam.hallowed.client.Atmosphere;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A redder dusk glow on a Hallowed Night. */
@Mixin(DimensionSpecialEffects.class)
public abstract class DimensionSpecialEffectsMixin {
    @Inject(method = "getSunriseColor", at = @At("RETURN"))
    private void hallowed$dusk(float timeOfDay, float partialTick, CallbackInfoReturnable<float[]> cir) {
        Atmosphere.sunsetColor(cir.getReturnValue());
    }
}
