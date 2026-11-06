package com.nightbeam.hallowed.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nightbeam.hallowed.client.Atmosphere;
import com.nightbeam.hallowed.client.HallowedClient;
import com.nightbeam.hallowed.config.Configs;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The Harvest Moon: on a Hallowed Night the moon is full, swells and burns pumpkin-orange. */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Unique
    private float[] hallowed$moonColor;

    @ModifyConstant(method = "renderSky", constant = @Constant(floatValue = 20.0F))
    private float hallowed$moonSize(float size) {
        return size * (1 + 0.8f * hallowed$moon());
    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMoonPhase()I"))
    private int hallowed$fullMoon(ClientLevel level) {
        return Atmosphere.fullMoon() ? 0 : level.getMoonPhase();
    }

    @Inject(method = "renderSky", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V", ordinal = 1, shift = At.Shift.AFTER))
    private void hallowed$tintMoon(CallbackInfo ci) {
        float i = hallowed$moon();
        if (i <= 0.001f) {
            hallowed$moonColor = null;
            return;
        }
        float[] color = RenderSystem.getShaderColor();
        hallowed$moonColor = color.clone();
        RenderSystem.setShaderColor(color[0], color[1] * (1 - 0.45f * i), color[2] * (1 - 0.88f * i), color[3]);
    }

    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getStarBrightness(F)F"))
    private void hallowed$untintMoon(CallbackInfo ci) {
        float[] color = hallowed$moonColor;
        if (color != null) {
            RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
            hallowed$moonColor = null;
        }
    }

    @Unique
    private static float hallowed$moon() {
        return Configs.client().moon ? Math.min(1, HallowedClient.intensity()) : 0;
    }
}
