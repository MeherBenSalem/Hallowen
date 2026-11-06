package com.nightbeam.hallowed.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nightbeam.hallowed.client.Atmosphere;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The Hallowed Night's violet fog (wine-red in the Witching Hour), which also closes in a little at midnight. */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Shadow
    private static float fogRed;
    @Shadow
    private static float fogGreen;
    @Shadow
    private static float fogBlue;

    @Inject(method = "setupColor", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V"))
    private static void hallowed$color(Camera camera, float partialTick, ClientLevel level, int renderDistance, float bossColorModifier,
            CallbackInfo ci) {
        if (camera.getFluidInCamera() != FogType.NONE) return;
        float[] rgb = {fogRed, fogGreen, fogBlue};
        Atmosphere.fogColor(rgb);
        fogRed = rgb[0];
        fogGreen = rgb[1];
        fogBlue = rgb[2];
    }

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void hallowed$distance(Camera camera, FogRenderer.FogMode mode, float farPlane, boolean thick, float partialTick,
            CallbackInfo ci) {
        if (mode == FogRenderer.FogMode.FOG_TERRAIN && camera.getFluidInCamera() == FogType.NONE) {
            RenderSystem.setShaderFogEnd(Atmosphere.fogEnd(RenderSystem.getShaderFogEnd()));
        }
    }
}
