package com.nightbeam.hallowed.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nightbeam.hallowed.entity.PumpkinBomb;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

/** A tumbling, burning jack o'lantern (or, when it is his head, the Horseman's own, turning to face its prey). */
public class PumpkinBombRenderer extends EntityRenderer<PumpkinBomb> {
    private final ItemRenderer itemRenderer;

    public PumpkinBombRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.3f;
    }

    @Override
    protected int getBlockLightLevel(PumpkinBomb entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(PumpkinBomb bomb, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        boolean head = bomb.isHead();
        float spin = (bomb.tickCount + partialTick) * (head ? 6 : 17);
        pose.pushPose();
        pose.translate(0, 0.3, 0);
        if (head) {
            pose.mulPose(Axis.YP.rotationDegrees(-entityRenderDispatcher.camera.getYRot() + 180));
            pose.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(spin * 0.05) * 12));
        } else {
            pose.mulPose(Axis.YP.rotationDegrees(spin * 0.6f));
            pose.mulPose(Axis.XP.rotationDegrees(spin));
        }
        pose.scale(1.1f, 1.1f, 1.1f);
        itemRenderer.renderStatic(bomb.getItem(), ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, bomb.level(),
                bomb.getId());
        pose.popPose();
        super.render(bomb, yaw, partialTick, pose, buffers, light);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ResourceLocation getTextureLocation(PumpkinBomb bomb) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
