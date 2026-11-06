package com.nightbeam.hallowed.mixin;

import com.nightbeam.hallowed.content.ModContent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A Hallowed carved pumpkin hides your gaze from endermen, like a vanilla one. */
@Mixin(EnderMan.class)
public abstract class EnderManMixin {
    @Inject(method = "isLookingAtMe", at = @At("HEAD"), cancellable = true)
    private void hallowed$disguise(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(ModContent.CARVED_PUMPKIN_ITEM.get())) cir.setReturnValue(false);
    }
}
