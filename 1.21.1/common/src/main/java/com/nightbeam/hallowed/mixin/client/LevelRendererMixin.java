package com.nightbeam.hallowed.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nightbeam.hallowed.client.Atmosphere;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The Harvest Moon: on a Hallowed Night the moon is full, swells and burns pumpkin-orange. */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @ModifyConstant(method = "renderSky", constant = @Constant(floatValue = 20.0f))
    private float hallowed$moonSize(float size) {
        return size * (1 + 0.8f * Atmosphere.moon());
    }

    @Inject(method = "renderSky", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V", ordinal = 1))
    private void hallowed$moonColor(CallbackInfo ci) {
        float i = Atmosphere.moon();
        if (i <= 0.001f) return;
        float[] color = RenderSystem.getShaderColor();
        RenderSystem.setShaderColor(color[0], color[1] * (1 - 0.45f * i), color[2] * (1 - 0.88f * i), color[3]);
    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMoonPhase()I"))
    private int hallowed$moonPhase(ClientLevel level) {
        return Atmosphere.moonPhase(level.getMoonPhase());
    }
}
