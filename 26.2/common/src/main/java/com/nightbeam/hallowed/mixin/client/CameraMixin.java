package com.nightbeam.hallowed.mixin.client;

import com.nightbeam.hallowed.client.ScreenShake;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies {@link ScreenShake} to the camera only; the player's aim is untouched. */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Invoker("setRotation")
    protected abstract void hallowed$setRotation(float yRot, float xRot);

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void hallowed$shake(float partialTick, CallbackInfo ci) {
        float[] offsets = ScreenShake.offsets(partialTick);
        if (offsets == null) return;
        Camera camera = (Camera) (Object) this;
        hallowed$setRotation(camera.yRot() + offsets[0], camera.xRot() + offsets[1]);
    }
}
