package com.nightbeam.hallowed.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.entity.Wisp;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * A will-o'-the-wisp: a soft halo, a flickering flame and a white-hot core, all additive and facing the camera.
 * Foxfire green-gold, or violet for a cursed wisp. It brightens while it leads and dims while it waits. The glow
 * textures are premultiplied (alpha follows brightness), so the additive pipeline's alpha cutout drops the dark fringe
 * and nothing behind a wisp is cut out around its glow.
 */
public class WispRenderer extends EntityRenderer<Wisp, WispRenderer.State> {
    private static final RenderType HALO = glow("halo");
    private static final RenderType FLAME = glow("flame");
    private static final RenderType CORE = glow("core");

    public WispRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0;
    }

    private static RenderType glow(String name) {
        return RenderTypes.energySwirl(Hallowed.id("textures/entity/wisp/" + name + ".png"), 0, 0);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(Wisp wisp, State state, float partialTick) {
        super.extractRenderState(wisp, state, partialTick);
        state.cursed = wisp.isCursed();
        state.waiting = wisp.state() == Wisp.WAITING || wisp.state() == Wisp.IDLE;
        state.time = wisp.tickCount + partialTick + (wisp.getId() % 50) * 7;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float t = state.time;
        float pulse = 0.82f + 0.12f * Mth.sin(t * 0.21f) + 0.06f * Mth.sin(t * 0.67f + 1.3f);
        float bright = (state.waiting ? 0.75f : 1.0f) * pulse;
        float r1, g1, b1, r2, g2, b2;
        if (state.cursed) {
            r1 = 0.55f; g1 = 0.22f; b1 = 0.85f;
            r2 = 0.95f; g2 = 0.55f; b2 = 1.0f;
        } else {
            r1 = 0.40f; g1 = 0.75f; b1 = 0.20f;
            r2 = 1.0f; g2 = 0.85f; b2 = 0.45f;
        }
        pose.pushPose();
        pose.translate(0, 0.25, 0);
        pose.rotate(camera.orientation);
        float halo = 0.85f + 0.08f * Mth.sin(t * 0.13f);
        float hb = bright * 0.5f;
        collector.submitCustomGeometry(pose, HALO, (p, vc) -> GlowQuads.square(vc, p, halo, r1 * hb, g1 * hb, b1 * hb, 1.0f));
        float fb = bright * 0.9f;
        pose.pushPose();
        pose.translate(0, 0.06, 0);
        float flame = 0.34f;
        collector.submitCustomGeometry(pose, FLAME, (p, vc) -> GlowQuads.square(vc, p, flame, r2 * fb, g2 * fb, b2 * fb, 1.0f));
        pose.popPose();
        float core = 0.13f + 0.02f * Mth.sin(t * 0.9f);
        float cb = Math.min(1, bright * 1.1f);
        collector.submitCustomGeometry(pose, CORE, (p, vc) -> GlowQuads.square(vc, p, core, cb, cb, cb * 0.92f, 1.0f));
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    public static class State extends EntityRenderState {
        boolean cursed;
        boolean waiting;
        float time;
    }
}
