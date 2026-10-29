package com.nightbeam.hallowed.mixin;

import com.nightbeam.hallowed.trick.TrickOrTreat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Wandering traders take part in trick-or-treating too (and always treat). */
@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin {
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void hallowed$trickOrTreat(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = TrickOrTreat.interact(player, (WanderingTrader) (Object) this, hand);
        if (result != null) cir.setReturnValue(result);
    }
}
