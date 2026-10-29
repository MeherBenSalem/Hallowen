package com.nightbeam.hallowed.client;

import com.nightbeam.hallowed.config.ClientConfig;
import com.nightbeam.hallowed.config.Configs;
import com.nightbeam.hallowed.content.ModContent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.AmbientAdditionsSettings;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.MoonPhase;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

/**
 * How a Hallowed Night looks and sounds, as layers on the client level's environment attributes: the same system
 * vanilla biomes, weather and the day cycle use, so every renderer (sky, fog, clouds, lighting, particles, ambience)
 * picks it up with no rendering hooks. Each layer blends toward the Hallowed look by tonight's intensity.
 */
public final class Atmosphere {
    private static final Vector3fc SKY = new Vector3f(0.13f, 0.05f, 0.21f);
    private static final Vector3fc FOG = new Vector3f(0.20f, 0.08f, 0.17f);
    private static final Vector3fc FOG_WITCHING = new Vector3f(0.29f, 0.11f, 0.12f);
    private static final Vector4fc CLOUDS = new Vector4f(0.16f, 0.09f, 0.24f, 0.85f);
    private static final Vector3fc MOONLIGHT = new Vector3f(0.80f, 0.62f, 1.0f);
    private static final Vector3fc FLAMELIGHT = new Vector3f(1.0f, 0.62f, 0.30f);
    private static final Vector4fc DUSK_GLOW = new Vector4f(0.95f, 0.38f, 0.10f, 0.9f);

    private Atmosphere() {
    }

    public static void addLayers(EnvironmentAttributeSystem.Builder builder) {
        builder.addTimeBasedLayer(EnvironmentAttributes.SKY_COLOR, (base, id) -> sky() ? ARGB.srgbLerp(i() * 0.85f, base, SKY) : base);
        builder.addTimeBasedLayer(EnvironmentAttributes.FOG_COLOR, (base, id) -> {
            if (!fog()) return base;
            Vector3fc target = ARGB.srgbLerp(HallowedClient.witching(), FOG, FOG_WITCHING);
            return ARGB.srgbLerp(i() * 0.8f, base, target);
        });
        builder.addTimeBasedLayer(EnvironmentAttributes.FOG_END_DISTANCE, (base, id) -> fog() ? base * (1 - 0.3f * HallowedClient.witching() * scale()) : base);
        builder.addTimeBasedLayer(EnvironmentAttributes.CLOUD_COLOR, (base, id) -> sky() ? ARGB.srgbLerp(i() * 0.8f, base, CLOUDS) : base);
        builder.addTimeBasedLayer(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, (base, id) -> sky() ? ARGB.srgbLerp(i() * 0.7f, base, DUSK_GLOW) : base);
        builder.addTimeBasedLayer(EnvironmentAttributes.SKY_LIGHT_COLOR, (base, id) -> sky() ? ARGB.srgbLerp(i() * 0.55f, base, MOONLIGHT) : base);
        builder.addTimeBasedLayer(EnvironmentAttributes.BLOCK_LIGHT_TINT, (base, id) -> sky() ? ARGB.srgbLerp(i() * 0.45f, base, FLAMELIGHT) : base);
        builder.addTimeBasedLayer(EnvironmentAttributes.STAR_BRIGHTNESS, (base, id) -> sky() ? Math.min(1, base + i() * 0.35f) : base);
        builder.addTimeBasedLayer(EnvironmentAttributes.MOON_PHASE, (base, id) -> moon() && i() > 0.05f ? MoonPhase.FULL_MOON : base);
        builder.addTimeBasedLayer(EnvironmentAttributes.AMBIENT_PARTICLES, (base, id) -> {
            float i = i();
            if (i < 0.02f || !Configs.client().ambientParticles) return base;
            List<AmbientParticle> list = new ArrayList<>(base);
            list.add(new AmbientParticle(ModContent.EMBER.get(), 0.0016f * i));
            list.add(new AmbientParticle(ModContent.AUTUMN_LEAF.get(), 0.0011f * i));
            list.add(new AmbientParticle(ModContent.SOUL_MOTE.get(), 0.0005f * i + 0.0012f * HallowedClient.witching()));
            return list;
        });
        builder.addTimeBasedLayer(EnvironmentAttributes.AMBIENT_SOUNDS, (base, id) -> {
            float i = i();
            if (i < 0.05f || !Configs.client().ambientSounds) return base;
            List<AmbientAdditionsSettings> additions = new ArrayList<>(base.additions());
            float w = HallowedClient.witching();
            additions.add(new AmbientAdditionsSettings(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModContent.AMBIENT_OWL.get()), 0.0009 * i));
            additions.add(new AmbientAdditionsSettings(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModContent.AMBIENT_WIND.get()), 0.0006 * i));
            additions.add(new AmbientAdditionsSettings(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModContent.AMBIENT_CHIMES.get()), 0.0004 * i));
            additions.add(new AmbientAdditionsSettings(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModContent.AMBIENT_WHISPERS.get()), 0.0002 * i + 0.0010 * w));
            return new AmbientSounds(base.loop(), base.mood(), additions);
        });
    }

    private static float scale() {
        return Mth.clamp(Configs.client().intensity, 0, 1.5f);
    }

    private static float i() {
        return Mth.clamp(HallowedClient.intensity(), 0, 1);
    }

    private static boolean sky() {
        ClientConfig c = Configs.client();
        return c.skyTint && HallowedClient.intensity() > 0.001f;
    }

    private static boolean fog() {
        return Configs.client().fog && HallowedClient.intensity() > 0.001f;
    }

    private static boolean moon() {
        return Configs.client().moon;
    }
}
