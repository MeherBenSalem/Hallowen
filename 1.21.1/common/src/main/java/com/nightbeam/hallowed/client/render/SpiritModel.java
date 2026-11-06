package com.nightbeam.hallowed.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nightbeam.hallowed.entity.Spirit;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

/**
 * A spirit: a hooded head over a shroud that narrows into three ragged tails, with long arms that hang until it
 * swoops, then reach. The shroud ripples from the shoulders down, each fold a little behind the one above.
 */
public class SpiritModel extends HierarchicalModel<Spirit> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart skirt;
    private final ModelPart hem;
    private final ModelPart tailLeft;
    private final ModelPart tailMid;
    private final ModelPart tailRight;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public SpiritModel(ModelPart root) {
        super(RenderType::entityTranslucentEmissive);
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.skirt = body.getChild("skirt");
        this.hem = skirt.getChild("hem");
        this.tailLeft = hem.getChild("tail_left");
        this.tailMid = hem.getChild("tail_mid");
        this.tailRight = hem.getChild("tail_right");
        this.rightArm = body.getChild("right_arm");
        this.leftArm = body.getChild("left_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8),
                PartPose.offset(0, 2, 0));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-5.0f, 0.0f, -3.0f, 10, 7, 6),
                PartPose.offset(0, 2, 0));
        PartDefinition skirt = body.addOrReplaceChild("skirt", CubeListBuilder.create().texOffs(0, 30).addBox(-4.5f, 0.0f, -2.5f, 9, 6, 5),
                PartPose.offset(0, 7, 0));
        PartDefinition hem = skirt.addOrReplaceChild("hem", CubeListBuilder.create().texOffs(0, 42).addBox(-4.0f, 0.0f, -2.0f, 8, 5, 4),
                PartPose.offset(0, 6, 0));
        hem.addOrReplaceChild("tail_left", CubeListBuilder.create().texOffs(32, 30).addBox(-1.0f, 0.0f, -1.0f, 2, 4, 2),
                PartPose.offset(2.5f, 5, 0));
        hem.addOrReplaceChild("tail_mid", CubeListBuilder.create().texOffs(40, 30).addBox(-1.0f, 0.0f, -1.0f, 2, 6, 2),
                PartPose.offset(0, 5, 0.3f));
        hem.addOrReplaceChild("tail_right", CubeListBuilder.create().texOffs(48, 30).addBox(-1.0f, 0.0f, -1.0f, 2, 3, 2),
                PartPose.offset(-2.5f, 5, 0));
        body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 0).addBox(-1.5f, -1.0f, -1.5f, 3, 11, 3,
                new CubeDeformation(-0.2f)), PartPose.offset(-6.0f, 1.5f, 0));
        body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(44, 0).addBox(-1.5f, -1.0f, -1.5f, 3, 11, 3,
                new CubeDeformation(-0.2f)), PartPose.offset(6.0f, 1.5f, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** The spirit's flickering opacity this frame (0..1), worked out in {@link #setupAnim}. */
    private float alpha = 1;

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        int a = Math.round((color >>> 24) * alpha);
        super.renderToBuffer(pose, buffer, light, overlay, a << 24 | (color & 0xFFFFFF));
    }

    @Override
    public void setupAnim(Spirit spirit, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        boolean charging = spirit.isCharging();
        float partialTick = ageInTicks - spirit.tickCount;
        float rise = Math.max(0, spirit.riseTicks() - partialTick);
        float risen = 1 - Mth.clamp(rise / Spirit.RISE_TICKS, 0, 1);
        float t = ageInTicks;
        float flicker = 0.78f + 0.12f * Mth.sin(t * 0.21f) + 0.06f * Mth.sin(t * 0.53f + 1.7f);
        alpha = Mth.clamp(flicker * (0.35f + 0.65f * risen), 0.1f, 1);
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD + (charging ? 0.25f : 0.05f * Mth.sin(t * 0.07f));
        float lean = charging ? 0.55f : 0.12f + 0.04f * Mth.sin(t * 0.05f);
        body.xRot = lean * 0.5f;
        skirt.xRot = lean * 0.45f + 0.08f * Mth.sin(t * 0.11f - 0.6f);
        hem.xRot = lean * 0.4f + 0.12f * Mth.sin(t * 0.11f - 1.2f);
        skirt.zRot = 0.05f * Mth.sin(t * 0.08f - 0.4f);
        hem.zRot = 0.08f * Mth.sin(t * 0.08f - 0.9f);
        tailLeft.xRot = 0.3f + 0.25f * Mth.sin(t * 0.16f - 1.8f);
        tailMid.xRot = 0.35f + 0.25f * Mth.sin(t * 0.16f - 2.1f);
        tailRight.xRot = 0.3f + 0.25f * Mth.sin(t * 0.16f - 2.4f);
        tailLeft.zRot = -0.15f + 0.1f * Mth.sin(t * 0.13f);
        tailRight.zRot = 0.15f - 0.1f * Mth.sin(t * 0.13f + 1);
        if (charging) {
            rightArm.xRot = leftArm.xRot = -1.55f + 0.1f * Mth.sin(t * 0.6f);
            rightArm.zRot = 0.18f;
            leftArm.zRot = -0.18f;
        } else {
            float swing = 0.08f * Mth.sin(t * 0.09f);
            rightArm.xRot = -0.25f + swing;
            leftArm.xRot = -0.25f - swing;
            rightArm.zRot = 0.12f + 0.05f * Mth.sin(t * 0.07f);
            leftArm.zRot = -0.12f - 0.05f * Mth.sin(t * 0.07f);
        }
        root.y = -2.0f + Mth.sin(t * 0.08f) * 1.5f + (1 - risen) * 26f;
    }
}
