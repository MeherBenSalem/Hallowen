package com.nightbeam.hallowed.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightbeam.hallowed.client.ItemModels;
import com.nightbeam.hallowed.content.ModContent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The witch hat is a flat icon everywhere but on a head, where it is drawn as a 3D hat. */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    public abstract void render(ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay, BakedModel model);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void hallowed$wornHat(ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (context != ItemDisplayContext.HEAD || stack.isEmpty() || !stack.is(ModContent.WITCH_HAT.get())) return;
        BakedModel worn = ItemModels.wornHat();
        if (worn == null || worn == model) return;
        ci.cancel();
        render(stack, context, leftHand, pose, buffers, light, overlay, worn);
    }
}
