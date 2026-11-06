package com.nightbeam.hallowed.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * The Horseman's Nightmare steed: the skeleton horse's shape, posed like vanilla's horse model (its walk, rearing and
 * swishing tail) from the rider's own state, since there is no horse entity behind it.
 */
public class NightmareModel {
    private final ModelPart body;
    private final ModelPart headParts;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;

    public NightmareModel(ModelPart root) {
        this.body = root.getChild("body");
        this.headParts = root.getChild("head_parts");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
        this.tail = body.getChild("tail");
        // No saddle or reins: the Nightmare is ridden bare.
        body.getChild("saddle").visible = false;
        for (String part : new String[]{"left_saddle_mouth", "right_saddle_mouth", "left_saddle_line", "right_saddle_line", "head_saddle",
                "mouth_saddle_wrap"}) {
            headParts.getChild(part).visible = false;
        }
    }

    /**
     * Poses the steed. {@code headYaw} is the head's turn relative to the body (degrees), {@code stand} how far it rears
     * (0..1).
     */
    public void setup(float ageInTicks, float walkPos, float walkSpeed, float headYaw, float stand, boolean inWater, boolean animateTail) {
        float l = Mth.clamp(headYaw, -20, 20);
        float m = 0;
        if (walkSpeed > 0.2f) m += Mth.cos(walkPos * 0.8f) * 0.15f * walkSpeed;
        float o = stand;
        float p = 1.0f - o;
        float r = ageInTicks;
        headParts.y = 4.0f;
        headParts.z = -12.0f;
        body.xRot = 0.0f;
        headParts.yRot = l * Mth.DEG_TO_RAD;
        float s = inWater ? 0.2f : 1.0f;
        float t = Mth.cos(s * walkPos * 0.6662f + Mth.PI);
        float u = t * 0.8f * walkSpeed;
        float v = (1.0f - o) * ((float) (Math.PI / 6) + m);
        headParts.xRot = o * ((float) (Math.PI / 12) + m) + v;
        headParts.yRot = o * l * Mth.DEG_TO_RAD + (1.0f - o) * headParts.yRot;
        headParts.y = o * -4.0f + (1.0f - o) * headParts.y;
        headParts.z = o * -4.0f + (1.0f - o) * headParts.z;
        body.xRot = o * (float) (-Math.PI / 4) + p * body.xRot;
        float w = (float) (Math.PI / 12) * o;
        float x = Mth.cos(r * 0.6f + Mth.PI);
        leftFrontLeg.y = 2.0f * o + 14.0f * p;
        leftFrontLeg.z = -6.0f * o - 10.0f * p;
        rightFrontLeg.y = leftFrontLeg.y;
        rightFrontLeg.z = leftFrontLeg.z;
        float y = ((float) (-Math.PI / 3) + x) * o + u * p;
        float z = ((float) (-Math.PI / 3) - x) * o - u * p;
        leftHindLeg.xRot = w - t * 0.5f * walkSpeed * p;
        rightHindLeg.xRot = w + t * 0.5f * walkSpeed * p;
        leftFrontLeg.xRot = y;
        rightFrontLeg.xRot = z;
        tail.xRot = (float) (Math.PI / 6) + walkSpeed * 0.75f;
        tail.y = -5.0f + walkSpeed;
        tail.z = 2.0f + walkSpeed * 2.0f;
        tail.yRot = animateTail ? Mth.cos(r * 0.7f) : 0.0f;
        body.y = 11.0f;
    }

    public void render(PoseStack pose, VertexConsumer consumer, int light, int overlay) {
        headParts.render(pose, consumer, light, overlay);
        body.render(pose, consumer, light, overlay);
        rightHindLeg.render(pose, consumer, light, overlay);
        leftHindLeg.render(pose, consumer, light, overlay);
        rightFrontLeg.render(pose, consumer, light, overlay);
        leftFrontLeg.render(pose, consumer, light, overlay);
    }
}
