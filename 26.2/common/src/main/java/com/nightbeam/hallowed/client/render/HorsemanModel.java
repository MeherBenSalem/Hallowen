package com.nightbeam.hallowed.client.render;

import com.nightbeam.hallowed.entity.HollowHorseman;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The Horseman's body: a humanoid with no head (the burning jack o'lantern is his head item), a high collar around
 * the stump, a long coat and a cape that streams behind him when he rides.
 */
public class HorsemanModel extends HumanoidModel<HorsemanRenderState> {
    private final ModelPart cape;
    private final ModelPart coat;

    public HorsemanModel(ModelPart root) {
        super(root);
        this.cape = body.getChild("cape");
        this.coat = body.getChild("coat");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");
        body.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(0, 32).addBox(-5.0f, 0.0f, 0.0f, 10, 18, 1),
                PartPose.offsetAndRotation(0, 0.5f, 2.6f, 0.08f, 0, 0));
        body.addOrReplaceChild("coat", CubeListBuilder.create().texOffs(24, 32).addBox(-4.5f, 0.0f, -2.5f, 9, 8, 5),
                PartPose.offset(0, 11.0f, 0));
        body.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(0, 52).addBox(-4.5f, -2.5f, -2.5f, 9, 3, 5),
                PartPose.ZERO);
        body.addOrReplaceChild("pauldron_right", CubeListBuilder.create().texOffs(32, 48).addBox(-3.0f, -2.5f, -2.5f, 5, 3, 5,
                new CubeDeformation(0.1f)), PartPose.offset(-5.0f, 1.0f, 0));
        body.addOrReplaceChild("pauldron_left", CubeListBuilder.create().texOffs(32, 48).mirror().addBox(-2.0f, -2.5f, -2.5f, 5, 3, 5,
                new CubeDeformation(0.1f)), PartPose.offset(5.0f, 1.0f, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(HorsemanRenderState state) {
        super.setupAnim(state);
        head.visible = false;
        hat.visible = false;
        float t = state.ageInTicks;
        float speed = Math.min(1, state.walkAnimationSpeed * 1.5f);
        cape.xRot = 0.1f + (state.mounted ? 0.2f + 0.75f * speed : 0.08f + 0.6f * speed) + 0.06f * Mth.sin(t * 0.25f);
        cape.zRot = 0.03f * Mth.sin(t * 0.17f);
        coat.visible = !state.mounted;
        coat.xRot = 0.05f * Mth.sin(t * 0.1f);
        float age = state.actionAge;
        switch (state.action) {
            case HollowHorseman.THROW, HollowHorseman.HEAD_TOSS -> {
                float k = Mth.clamp(age / 9f, 0, 1);
                float throwArc = age < 9 ? -2.6f * k : Mth.lerp(Mth.clamp((age - 9) / 6f, 0, 1), -2.6f, -0.6f);
                leftArm.xRot = throwArc;
                leftArm.zRot = -0.15f;
            }
            case HollowHorseman.CALL -> {
                float k = Mth.clamp(age / 10f, 0, 1);
                rightArm.xRot = Mth.lerp(k, rightArm.xRot, -2.9f);
                leftArm.xRot = Mth.lerp(k, leftArm.xRot, -2.9f);
                rightArm.zRot = 0.35f * k;
                leftArm.zRot = -0.35f * k;
            }
            case HollowHorseman.WINDUP -> {
                rightArm.xRot = -2.2f - 0.2f * Mth.sin(age * 0.8f);
                rightArm.zRot = 0.5f;
            }
            case HollowHorseman.CHARGE -> {
                rightArm.xRot = -1.2f;
                rightArm.yRot = -0.5f;
                rightArm.zRot = 0.9f;
                body.xRot = 0.35f;
            }
            case HollowHorseman.STUNNED -> {
                body.xRot = 0.4f + 0.05f * Mth.sin(t * 0.4f);
                rightArm.xRot = 0.4f;
                leftArm.xRot = 0.5f;
            }
            case HollowHorseman.FALL -> {
                float k = Mth.clamp(age / 20f, 0, 1);
                body.xRot = -0.4f * Mth.sin(k * Mth.PI);
                rightArm.xRot = -1.2f * Mth.sin(k * Mth.PI);
                leftArm.xRot = -1.0f * Mth.sin(k * Mth.PI);
            }
            default -> {
            }
        }
    }
}
