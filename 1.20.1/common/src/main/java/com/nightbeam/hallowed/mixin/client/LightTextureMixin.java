package com.nightbeam.hallowed.mixin.client;

import com.nightbeam.hallowed.client.Atmosphere;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Moonlight turns violet and torchlight flame-orange on a Hallowed Night (the lightmap's sky and block colours). */
@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
    @ModifyArg(method = "updateLightTexture", at = @At(value = "INVOKE",
            target = "Lorg/joml/Vector3f;lerp(Lorg/joml/Vector3fc;F)Lorg/joml/Vector3f;", ordinal = 0), index = 0)
    private Vector3fc hallowed$moonlight(Vector3fc white) {
        return Atmosphere.skyLightTarget(new Vector3f(white));
    }

    @ModifyArg(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;set(FFF)Lorg/joml/Vector3f;", ordinal = 0),
            index = 0)
    private float hallowed$flameRed(float value) {
        return Atmosphere.blockLight(value, 0);
    }

    @ModifyArg(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;set(FFF)Lorg/joml/Vector3f;", ordinal = 0),
            index = 1)
    private float hallowed$flameGreen(float value) {
        return Atmosphere.blockLight(value, 1);
    }

    @ModifyArg(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;set(FFF)Lorg/joml/Vector3f;", ordinal = 0),
            index = 2)
    private float hallowed$flameBlue(float value) {
        return Atmosphere.blockLight(value, 2);
    }
}
