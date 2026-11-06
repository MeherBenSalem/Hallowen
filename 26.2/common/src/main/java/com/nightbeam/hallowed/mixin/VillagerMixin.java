package com.nightbeam.hallowed.mixin;

import com.nightbeam.hallowed.trick.TrickOrTreat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A Treat Pail held out to a villager asks for a treat instead of opening trades. */
@Mixin(Villager.class)
public abstract class VillagerMixin {
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void hallowed$trickOrTreat(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = TrickOrTreat.interact(player, (Villager) (Object) this, hand);
        if (result != null) cir.setReturnValue(result);
    }
}
