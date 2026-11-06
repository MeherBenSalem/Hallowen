package com.nightbeam.hallowed.mixin;

import com.nightbeam.hallowed.content.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Sugar Rush mines 15% faster per level (newer versions do this through the block break speed attribute). */
@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
    @Inject(method = "getDestroyProgress", at = @At("RETURN"), cancellable = true)
    private void hallowed$sugarRush(BlockState state, Player player, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (ModContent.SUGAR_RUSH == null) return;
        MobEffectInstance rush = player.getEffect(ModContent.SUGAR_RUSH.get());
        if (rush != null) cir.setReturnValue(cir.getReturnValueF() * (1 + 0.15f * (rush.getAmplifier() + 1)));
    }
}
