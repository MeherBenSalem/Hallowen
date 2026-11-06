package com.nightbeam.hallowed.mixin.client;

import com.nightbeam.hallowed.client.HallowedClient;
import com.nightbeam.hallowed.config.Configs;
import net.minecraft.client.renderer.SkyRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** The Harvest Moon: on a Hallowed Night the moon swells and burns pumpkin-orange. */
@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
    @ModifyArg(method = "renderMoon", at = @At(value = "INVOKE",
            target = "Lorg/joml/Matrix4fStack;scale(FFF)Lorg/joml/Matrix4f;"),
            index = 0)
    private float hallowed$moonSizeX(float scale) {
        return scale * (1 + 0.8f * moon());
    }

    @ModifyArg(method = "renderMoon", at = @At(value = "INVOKE",
            target = "Lorg/joml/Matrix4fStack;scale(FFF)Lorg/joml/Matrix4f;"),
            index = 2)
    private float hallowed$moonSizeZ(float scale) {
        return scale * (1 + 0.8f * moon());
    }

    @ModifyArg(method = "renderMoon", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/DynamicUniforms;writeTransform(Lorg/joml/Matrix4f;Lorg/joml/Vector4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"),
            index = 1)
    private Vector4f hallowed$moonColor(Vector4f color) {
        float i = moon();
        if (i <= 0.001f) return color;
        return new Vector4f(color.x, color.y * (1 - 0.45f * i), color.z * (1 - 0.88f * i), color.w);
    }

    private static float moon() {
        return Configs.client().moon ? Math.min(1, HallowedClient.intensity()) : 0;
    }
}
