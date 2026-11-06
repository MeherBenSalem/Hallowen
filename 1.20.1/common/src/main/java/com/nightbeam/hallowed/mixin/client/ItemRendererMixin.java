package com.nightbeam.hallowed.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightbeam.hallowed.client.HallowedItemModels;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Lets item model properties see where an item is drawn (the Witch Hat has its own model on a head). */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Inject(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",
            at = @At("HEAD"))
    private void hallowed$begin(@Nullable LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack pose,
            MultiBufferSource buffers, @Nullable Level level, int light, int overlay, int seed, CallbackInfo ci) {
        HallowedItemModels.beginRender(context);
    }

    @Inject(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",
            at = @At("RETURN"))
    private void hallowed$end(@Nullable LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack pose,
            MultiBufferSource buffers, @Nullable Level level, int light, int overlay, int seed, CallbackInfo ci) {
        HallowedItemModels.endRender();
    }
}
