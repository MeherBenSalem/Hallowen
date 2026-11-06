package com.nightbeam.hallowed.mixin.client;

import com.nightbeam.hallowed.client.ScreenShake;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies {@link ScreenShake} to the camera only; the player's aim is untouched. */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "setup", at = @At("TAIL"))
    private void hallowed$shake(BlockGetter level, Entity entity, boolean detached, boolean mirrored, float partialTick, CallbackInfo ci) {
        float[] offsets = ScreenShake.offsets(partialTick);
        if (offsets == null) return;
        Camera camera = (Camera) (Object) this;
        setRotation(camera.getYRot() + offsets[0], camera.getXRot() + offsets[1]);
    }
}
