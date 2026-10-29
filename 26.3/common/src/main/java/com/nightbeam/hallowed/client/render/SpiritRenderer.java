package com.nightbeam.hallowed.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.entity.Spirit;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/** Translucent, always lit, with glowing eyes; it flickers, and rises out of the ground when it appears. */
public class SpiritRenderer extends MobRenderer<Spirit, SpiritRenderState, SpiritModel> {
    private static final Identifier TEXTURE = Hallowed.id("textures/entity/spirit/spirit.png");
    private static final Identifier EYES = Hallowed.id("textures/entity/spirit/spirit_eyes.png");
    private static final RenderType EYES_TYPE = RenderTypes.eyes(EYES);

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
    public SpiritRenderState createRenderState() {
        return new SpiritRenderState();
    }

    @Override
    public void extractRenderState(Spirit spirit, SpiritRenderState state, float partialTicks) {
        super.extractRenderState(spirit, state, partialTicks);
        state.charging = spirit.isCharging();
        float rise = Math.max(0, spirit.riseTicks() - partialTicks);
        state.risen = 1 - Mth.clamp(rise / Spirit.RISE_TICKS, 0, 1);
        float t = state.ageInTicks;
        state.flicker = 0.78f + 0.12f * Mth.sin(t * 0.21f) + 0.06f * Mth.sin(t * 0.53f + 1.7f);
    }

    @Override
    protected int getModelTint(SpiritRenderState state) {
        float alpha = Mth.clamp(state.flicker * (0.35f + 0.65f * state.risen), 0.1f, 1);
        return ARGB.colorFromFloat(alpha, 1, 1, 1);
    }

    @Override
    protected void scale(SpiritRenderState state, PoseStack poseStack) {
        poseStack.scale(0.9f, 0.9f, 0.9f);
    }

    @Override
    public Identifier getTextureLocation(SpiritRenderState state) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(Spirit entity, BlockPos pos) {
        return 15;
    }
}
