package com.nightbeam.hallowed.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.entity.Spirit;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/** Translucent, always lit, with glowing eyes; it flickers, and rises out of the ground when it appears. */
public class SpiritRenderer extends MobRenderer<Spirit, SpiritModel> {
    private static final ResourceLocation TEXTURE = Hallowed.id("textures/entity/spirit/spirit.png");
    private static final ResourceLocation EYES = Hallowed.id("textures/entity/spirit/spirit_eyes.png");
    private static final RenderType EYES_TYPE = RenderType.eyes(EYES);

    public SpiritRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiritModel(context.bakeLayer(ModelLayersHallowed.SPIRIT)), 0.0f);
        addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return EYES_TYPE;
            }
        });
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
