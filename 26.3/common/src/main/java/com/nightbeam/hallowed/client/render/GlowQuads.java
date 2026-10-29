package com.nightbeam.hallowed.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;

/** Double-sided, full-bright billboard quads for additive glows: black is clear, colour is brightness. */
final class GlowQuads {
    private GlowQuads() {
    }

    /** A square of half-size {@code half} in the pose's XY plane, centred on the origin. */
    static void square(VertexConsumer vc, PoseStack.Pose pose, float half, float r, float g, float b, float a) {
        vertex(vc, pose, -half, -half, 0, 1, r, g, b, a);
        vertex(vc, pose, -half, half, 0, 0, r, g, b, a);
        vertex(vc, pose, half, half, 1, 0, r, g, b, a);
        vertex(vc, pose, half, -half, 1, 1, r, g, b, a);
        vertex(vc, pose, half, -half, 1, 1, r, g, b, a);
        vertex(vc, pose, half, half, 1, 0, r, g, b, a);
        vertex(vc, pose, -half, half, 0, 0, r, g, b, a);
        vertex(vc, pose, -half, -half, 0, 1, r, g, b, a);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float u, float v, float r, float g, float b,
            float a) {
        vc.addVertex(pose, x, y, 0).setColor(r, g, b, a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}
