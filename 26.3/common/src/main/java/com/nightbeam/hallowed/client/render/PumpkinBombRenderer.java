package com.nightbeam.hallowed.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nightbeam.hallowed.entity.PumpkinBomb;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;

/** A tumbling, burning jack o'lantern (or, when it is his head, the Horseman's own, turning to face its prey). */
public class PumpkinBombRenderer extends EntityRenderer<PumpkinBomb, PumpkinBombRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public PumpkinBombRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.shadowRadius = 0.3f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PumpkinBomb bomb, State state, float partialTick) {
        super.extractRenderState(bomb, state, partialTick);
        itemModelResolver.updateForNonLiving(state.item, bomb.getItem(), ItemDisplayContext.FIXED, bomb);
        state.head = bomb.isHead();
        state.spin = (bomb.tickCount + partialTick) * (bomb.isHead() ? 6 : 17);
        state.yaw = bomb.getYRot(partialTick);
    }

    @Override
    protected int getBlockLightLevel(PumpkinBomb entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0, 0.3, 0);
        if (state.head) {
            pose.rotate(Axis.YP.rotationDegrees(-camera.yRot + 180));
            pose.rotate(Axis.ZP.rotationDegrees((float) Math.sin(state.spin * 0.05) * 12));
        } else {
            pose.rotate(Axis.YP.rotationDegrees(state.spin * 0.6f));
            pose.rotate(Axis.XP.rotationDegrees(state.spin));
        }
        pose.scale(1.1f, 1.1f, 1.1f);
        state.item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    public static class State extends EntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
        boolean head;
        float spin;
        float yaw;
    }
}
