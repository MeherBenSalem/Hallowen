package com.nightbeam.hallowed.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.entity.HollowHorseman;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;

/**
 * The Hollow Horseman: his body (a {@link HorsemanModel} wearing his burning head and holding the Reaper Scythe
 * through the usual humanoid layers) and, while mounted, his Nightmare steed under him (the skeleton horse's shape
 * with its own hide and a burning mane). Both have an ember glow layer. He rises out of the ground when summoned.
 */
public class HorsemanRenderer extends HumanoidMobRenderer<HollowHorseman, HorsemanRenderState, HorsemanModel> {
    private static final Identifier TEXTURE = Hallowed.id("textures/entity/hollow_horseman/horseman.png");
    private static final Identifier GLOW = Hallowed.id("textures/entity/hollow_horseman/horseman_glow.png");
    private static final Identifier STEED = Hallowed.id("textures/entity/hollow_horseman/nightmare.png");
    private static final Identifier STEED_GLOW = Hallowed.id("textures/entity/hollow_horseman/nightmare_glow.png");
    private static final RenderType GLOW_TYPE = RenderTypes.eyes(GLOW);
    private static final RenderType STEED_GLOW_TYPE = RenderTypes.eyes(STEED_GLOW);
    /** How high the rider sits above the ground while mounted (blocks). */
    private static final float SEAT = 0.72f;

    private final HorseModel steed;

    public HorsemanRenderer(EntityRendererProvider.Context context) {
        super(context, new HorsemanModel(context.bakeLayer(ModelLayersHallowed.HORSEMAN)), 0.9f);
        this.steed = new HorseModel(context.bakeLayer(ModelLayers.SKELETON_HORSE));
        addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW_TYPE;
            }
        });
    }

    @Override
    public HorsemanRenderState createRenderState() {
        return new HorsemanRenderState();
    }

    @Override
    public void extractRenderState(HollowHorseman horseman, HorsemanRenderState state, float partialTicks) {
        super.extractRenderState(horseman, state, partialTicks);
        state.mounted = horseman.isMounted();
        state.headless = horseman.isHeadless();
        state.action = horseman.action();
        state.actionAge = horseman.actionAge(partialTicks);
        state.isPassenger = state.mounted;
        float arrival = Math.max(0, horseman.arrivalTicks() - partialTicks) / HollowHorseman.ARRIVAL_TICKS;
        state.sink = arrival <= 0 ? 0 : (float) Math.pow(arrival, 1.6) * 3.2f;
        state.overlay = LivingEntityRenderer.getOverlayCoords(state, 0);
        var steedState = state.steed;
        steedState.ageInTicks = state.ageInTicks;
        steedState.walkAnimationPos = state.walkAnimationPos;
        steedState.walkAnimationSpeed = state.action == HollowHorseman.CHARGE ? Math.max(1.4f, state.walkAnimationSpeed) : state.walkAnimationSpeed;
        steedState.yRot = Mth.clamp(state.yRot, -20, 20);
        steedState.xRot = 0;
        steedState.bodyRot = state.bodyRot;
        steedState.isRidden = true;
        steedState.animateTail = state.walkAnimationSpeed > 0.05f;
        steedState.ageScale = 1;
        steedState.lightCoords = state.lightCoords;
        steedState.hasRedOverlay = state.hasRedOverlay;
        steedState.isInWater = state.isInWater;
        steedState.standAnimation = switch (state.action) {
            case HollowHorseman.WINDUP -> Mth.sin(Mth.clamp(state.actionAge / 18f, 0, 1) * Mth.PI);
            case HollowHorseman.FALL -> Mth.clamp(state.actionAge / 10f, 0, 1);
            default -> 0;
        };
    }

    @Override
    public void submit(HorsemanRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0, -state.sink, 0);
        if (state.mounted) {
            pose.pushPose();
            pose.rotate(Axis.YP.rotationDegrees(180.0f - state.bodyRot));
            if (state.deathTime > 0) {
                float fall = Mth.sqrt(Math.min(1, (state.deathTime - 1) / 20f * 1.6f));
                pose.rotate(Axis.ZP.rotationDegrees(fall * 90));
            }
            pose.scale(-1.0f, -1.0f, 1.0f);
            pose.translate(0.0f, -1.501f, 0.0f);
            collector.submitModel(steed, state.steed, pose, RenderTypes.entityCutout(STEED), state.lightCoords, state.overlay, -1, null,
                    state.outlineColor);
            collector.submitModel(steed, state.steed, pose, STEED_GLOW_TYPE, LightCoordsUtil.FULL_BRIGHT, state.overlay, -1, null, 0);
            pose.popPose();
            pose.translate(0, SEAT, 0);
        }
        super.submit(state, pose, collector, camera);
        pose.popPose();
    }

    @Override
    public Identifier getTextureLocation(HorsemanRenderState state) {
        return TEXTURE;
    }
}
