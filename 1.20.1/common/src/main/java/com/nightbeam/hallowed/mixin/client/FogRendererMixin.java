package com.nightbeam.hallowed.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nightbeam.hallowed.client.Atmosphere;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The Hallowed Night's fog colour, and the Witching Hour drawing the fog in. */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Shadow
    private static float fogRed;
    @Shadow
    private static float fogGreen;
    @Shadow
    private static float fogBlue;

    @Inject(method = "setupColor", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V"))
    private static void hallowed$fogColor(CallbackInfo ci) {
        float[] rgb = {fogRed, fogGreen, fogBlue};
        Atmosphere.fogColor(rgb);
        fogRed = rgb[0];
        fogGreen = rgb[1];
        fogBlue = rgb[2];
    }

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void hallowed$fogEnd(Camera camera, FogRenderer.FogMode mode, float renderDistance, boolean thick, float partialTick,
            CallbackInfo ci) {
        if (mode != FogRenderer.FogMode.FOG_TERRAIN || thick || camera.getFluidInCamera() != FogType.NONE) return;
        if (camera.getEntity() instanceof LivingEntity living && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS))) return;
        RenderSystem.setShaderFogEnd(Atmosphere.fogEnd(RenderSystem.getShaderFogEnd()));
    }
}
