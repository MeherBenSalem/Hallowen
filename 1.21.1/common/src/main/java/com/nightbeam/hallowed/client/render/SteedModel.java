package com.nightbeam.hallowed.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * The Nightmare steed: the skeleton horse's shape (baked from its model layer) animated the way the horse model
 * animates a ridden horse, but driven by the Horseman rather than by a horse entity. No saddle or reins.
 */
public final class SteedModel {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart headParts;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;

    public SteedModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.headParts = root.getChild("head_parts");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
        this.tail = body.getChild("tail");
        for (String baby : new String[]{"right_hind_baby_leg", "left_hind_baby_leg", "right_front_baby_leg", "left_front_baby_leg"}) {
            root.getChild(baby).visible = false;
        }
        body.getChild("saddle").visible = false;
        for (String tack : new String[]{"left_saddle_mouth", "right_saddle_mouth", "left_saddle_line", "right_saddle_line", "head_saddle",
                "mouth_saddle_wrap"}) {
            headParts.getChild(tack).visible = false;
        }
    }

    /**
     * Poses the steed. {@code walkPos}/{@code walkSpeed} drive the gallop, {@code headYaw} turns the head (degrees,
     * clamped to 20 either way), {@code stand} rears it up (0..1) and {@code swishTail} sets the tail swaying.
     */
    public void setupAnim(float walkPos, float walkSpeed, float ageInTicks, float headYaw, float stand, boolean swishTail, boolean inWater) {
        float l = Mth.clamp(headYaw, -20, 20);
        float m = 0;
        if (walkSpeed > 0.2f) m += Mth.cos(walkPos * 0.8f) * 0.15f * walkSpeed;
        float o = stand;
        float p = 1 - o;
        float r = ageInTicks;
        headParts.y = 4.0f;
        headParts.z = -12.0f;
        body.xRot = 0;
        headParts.yRot = l * Mth.DEG_TO_RAD;
        float s = inWater ? 0.2f : 1.0f;
        float t = Mth.cos(s * walkPos * 0.6662f + Mth.PI);
        float u = t * 0.8f * walkSpeed;
        float v = (1 - o) * (Mth.PI / 6 + m);
        headParts.xRot = o * (Mth.PI / 12 + m) + v;
        headParts.yRot = o * l * Mth.DEG_TO_RAD + (1 - o) * headParts.yRot;
        headParts.y = o * -4.0f + (1 - o) * headParts.y;
        headParts.z = o * -4.0f + (1 - o) * headParts.z;
        body.xRot = o * (-Mth.PI / 4) + p * body.xRot;
        float w = Mth.PI / 12 * o;
        float x = Mth.cos(r * 0.6f + Mth.PI);
        leftFrontLeg.y = 2.0f * o + 14.0f * p;
        leftFrontLeg.z = -6.0f * o - 10.0f * p;
        rightFrontLeg.y = leftFrontLeg.y;
        rightFrontLeg.z = leftFrontLeg.z;
        leftHindLeg.xRot = w - t * 0.5f * walkSpeed * p;
        rightHindLeg.xRot = w + t * 0.5f * walkSpeed * p;
        leftFrontLeg.xRot = (-Mth.PI / 3 + x) * o + u * p;
        rightFrontLeg.xRot = (-Mth.PI / 3 - x) * o - u * p;
        tail.xRot = Mth.PI / 6 + walkSpeed * 0.75f;
        tail.y = -5.0f + walkSpeed;
        tail.z = 2.0f + walkSpeed * 2.0f;
        tail.yRot = swishTail ? Mth.cos(r * 0.7f) : 0;
        body.y = 11.0f;
    }

    public void render(PoseStack pose, VertexConsumer buffer, int light, int overlay) {
        root.render(pose, buffer, light, overlay);
    }
}
