package com.nightbeam.hallowed.mixin.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.nightbeam.hallowed.client.Atmosphere;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Moonlight turns violet and flame light warmer on a Hallowed Night. */
@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
    @Redirect(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;setPixelRGBA(III)V"))
    private void hallowed$tint(NativeImage image, int block, int sky, int abgr) {
        image.setPixelRGBA(block, sky, Atmosphere.lightmap(block, sky, abgr));
    }
}
