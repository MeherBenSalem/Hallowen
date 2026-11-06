package com.nightbeam.hallowed.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.entity.Spirit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Translucent, always lit, with glowing eyes; it flickers, and rises out of the ground when it appears. */
public class SpiritRenderer extends MobRenderer<Spirit, SpiritModel> {
    private static final ResourceLocation TEXTURE = Hallowed.id("textures/entity/spirit/spirit.png");
    private static final ResourceLocation EYES = Hallowed.id("textures/entity/spirit/spirit_eyes.png");
    private static final RenderType EYES_TYPE = RenderType.eyes(EYES);

    public SpiritRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiritModel(context.bakeLayer(ModelLayersHallowed.SPIRIT)), 0.0f);
        addLayer(new EyesLayer<>(this) {
            @Override
            public void render(PoseStack pose, MultiBufferSource buffers, int light, Spirit spirit, float limbSwing, float limbSwingAmount,
                    float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                float alpha = getParentModel().alpha;
                getParentModel().alpha = 1;
                super.render(pose, buffers, light, spirit, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
                getParentModel().alpha = alpha;
            }

            @Override
            public RenderType renderType() {
                return EYES_TYPE;
            }
        });
    }

    @Override
    public void render(Spirit spirit, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        float rise = Math.max(0, spirit.riseTicks() - partialTicks);
        float risen = 1 - Mth.clamp(rise / Spirit.RISE_TICKS, 0, 1);
        float t = spirit.tickCount + partialTicks;
        float flicker = 0.78f + 0.12f * Mth.sin(t * 0.21f) + 0.06f * Mth.sin(t * 0.53f + 1.7f);
        model.risen = risen;
        model.alpha = Mth.clamp(flicker * (0.35f + 0.65f * risen), 0.1f, 1);
        super.render(spirit, yaw, partialTicks, pose, buffers, light);
        model.alpha = 1;
    }

    @Override
    protected void scale(Spirit spirit, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.9f, 0.9f, 0.9f);
    }

    @Override
    public ResourceLocation getTextureLocation(Spirit spirit) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(Spirit entity, BlockPos pos) {
        return 15;
    }
}
