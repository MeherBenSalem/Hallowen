package com.nightbeam.hallowed.mixin.client;

import com.nightbeam.hallowed.client.Atmosphere;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The Hallowed Night's sky, clouds and stars, and its ambient motes (see {@link Atmosphere}). */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void hallowed$sky(Vec3 pos, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        cir.setReturnValue(Atmosphere.skyColor(cir.getReturnValue()));
    }

    @Inject(method = "getCloudColor", at = @At("RETURN"), cancellable = true)
    private void hallowed$clouds(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        cir.setReturnValue(Atmosphere.cloudColor(cir.getReturnValue()));
    }

    @Inject(method = "getStarBrightness", at = @At("RETURN"), cancellable = true)
    private void hallowed$stars(float partialTick, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(Atmosphere.stars(cir.getReturnValueF()));
    }

    @Inject(method = "doAnimateTick", at = @At("TAIL"))
    private void hallowed$ambient(int x, int y, int z, int range, RandomSource random, Block marker, BlockPos.MutableBlockPos pos,
            CallbackInfo ci) {
        ClientLevel level = (ClientLevel) (Object) this;
        if (!level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)) {
            Atmosphere.ambientParticles(level, level.random, pos);
        }
    }
}
