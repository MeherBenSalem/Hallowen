package com.nightbeam.hallowed.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.entity.HollowHorseman;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * The Hollow Horseman: his body (a {@link HorsemanModel} wearing his burning head and holding the Reaper Scythe
 * through the usual humanoid layers) and, while mounted, his Nightmare steed under him (the skeleton horse's shape
 * with its own hide and a burning mane). Both have an ember glow layer. He rises out of the ground when summoned.
 */
public class HorsemanRenderer extends HumanoidMobRenderer<HollowHorseman, HorsemanModel> {
    private static final ResourceLocation TEXTURE = Hallowed.id("textures/entity/hollow_horseman/horseman.png");
    private static final ResourceLocation GLOW = Hallowed.id("textures/entity/hollow_horseman/horseman_glow.png");
    private static final ResourceLocation STEED = Hallowed.id("textures/entity/hollow_horseman/nightmare.png");
    private static final ResourceLocation STEED_GLOW = Hallowed.id("textures/entity/hollow_horseman/nightmare_glow.png");
    private static final RenderType GLOW_TYPE = RenderType.eyes(GLOW);
    private static final RenderType STEED_GLOW_TYPE = RenderType.eyes(STEED_GLOW);
    /** How high the rider sits above the ground while mounted (blocks). */
    private static final float SEAT = 0.72f;

    private final NightmareModel steed;

    public HorsemanRenderer(EntityRendererProvider.Context context) {
        super(context, new HorsemanModel(context.bakeLayer(ModelLayersHallowed.HORSEMAN)), 0.9f);
        this.steed = new NightmareModel(context.bakeLayer(ModelLayers.SKELETON_HORSE));
        addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW_TYPE;
            }
        });
    }

    @Override
    public void render(HollowHorseman horseman, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        boolean mounted = horseman.isMounted();
        float arrival = Math.max(0, horseman.arrivalTicks() - partialTicks) / HollowHorseman.ARRIVAL_TICKS;
        float sink = arrival <= 0 ? 0 : (float) Math.pow(arrival, 1.6) * 3.2f;
        pose.pushPose();
        pose.translate(0, -sink, 0);
        if (mounted) {
            float bodyRot = Mth.rotLerp(partialTicks, horseman.yBodyRotO, horseman.yBodyRot);
            float headRot = Mth.rotLerp(partialTicks, horseman.yHeadRotO, horseman.yHeadRot);
            float walkSpeed = horseman.walkAnimation.speed(partialTicks);
            float walkPos = horseman.walkAnimation.position(partialTicks);
            byte action = horseman.action();
            float actionAge = horseman.actionAge(partialTicks);
            float stand = switch (action) {
                case HollowHorseman.WINDUP -> Mth.sin(Mth.clamp(actionAge / 18f, 0, 1) * Mth.PI);
                case HollowHorseman.FALL -> Mth.clamp(actionAge / 10f, 0, 1);
                default -> 0;
            };
            steed.setup(horseman.tickCount + partialTicks, walkPos,
                    action == HollowHorseman.CHARGE ? Math.max(1.4f, walkSpeed) : walkSpeed,
                    headRot - bodyRot, stand, horseman.isInWater(), walkSpeed > 0.05f);
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(180.0f - bodyRot));
            if (horseman.deathTime > 0) {
                float fall = Mth.sqrt(Math.min(1, (horseman.deathTime + partialTicks - 1) / 20f * 1.6f));
                pose.mulPose(Axis.ZP.rotationDegrees(fall * 90));
            }
            pose.scale(-1.0f, -1.0f, 1.0f);
            pose.translate(0.0f, -1.501f, 0.0f);
            int overlay = LivingEntityRenderer.getOverlayCoords(horseman, 0);
            steed.render(pose, buffers.getBuffer(RenderType.entityCutout(STEED)), light, overlay);
            steed.render(pose, buffers.getBuffer(STEED_GLOW_TYPE), LightTexture.FULL_BRIGHT, overlay);
            pose.popPose();
            pose.translate(0, SEAT, 0);
        }
        super.render(horseman, yaw, partialTicks, pose, buffers, light);
        pose.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(HollowHorseman horseman) {
        return TEXTURE;
    }
}
