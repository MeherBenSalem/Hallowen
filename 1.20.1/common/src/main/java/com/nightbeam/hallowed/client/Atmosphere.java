package com.nightbeam.hallowed.client;

import com.nightbeam.hallowed.config.ClientConfig;
import com.nightbeam.hallowed.config.Configs;
import com.nightbeam.hallowed.content.ModContent;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * How a Hallowed Night looks and sounds. Newer versions layer this onto the level's environment attributes; here the
 * same blends are applied where 1.20.1 computes each value (see the client mixins): sky, fog, clouds, the sunset
 * glow, sky and block light, stars and the moon, plus ambient particles and sounds. Each blends toward the Hallowed
 * look by tonight's intensity.
 */
public final class Atmosphere {
    private static final Vector3f SKY = new Vector3f(0.13f, 0.05f, 0.21f);
    private static final Vector3f FOG = new Vector3f(0.20f, 0.08f, 0.17f);
    private static final Vector3f FOG_WITCHING = new Vector3f(0.29f, 0.11f, 0.12f);
    private static final Vector3f CLOUDS = new Vector3f(0.16f, 0.09f, 0.24f);
    private static final Vector3f MOONLIGHT = new Vector3f(0.80f, 0.62f, 1.0f);
    private static final Vector3f FLAMELIGHT = new Vector3f(1.0f, 0.62f, 0.30f);
    private static final float[] DUSK_GLOW = {0.95f, 0.38f, 0.10f, 0.9f};

    private Atmosphere() {
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Colours

    public static Vec3 skyColor(Vec3 base) {
        if (!sky()) return base;
        return lerp(i() * 0.85f, base, SKY);
    }

    /** Blends the fog colour (r, g, b) in place. */
    public static void fogColor(float[] rgb) {
        if (!fog()) return;
        float w = HallowedClient.witching();
        float t = i() * 0.8f;
        for (int c = 0; c < 3; c++) {
            float target = Mth.lerp(w, FOG.get(c), FOG_WITCHING.get(c));
            rgb[c] = Mth.lerp(t, rgb[c], target);
        }
    }

    /** The terrain fog's far end: the Witching Hour draws it in. */
    public static float fogEnd(float end) {
        return fog() ? end * (1 - 0.3f * HallowedClient.witching() * scale()) : end;
    }

    public static Vec3 cloudColor(Vec3 base) {
        return sky() ? lerp(i() * 0.8f, base, CLOUDS) : base;
    }

    /** Blends the sunrise/sunset glow (r, g, b, a) in place. */
    public static void sunsetColor(@Nullable float[] rgba) {
        if (rgba == null || !sky()) return;
        float t = i() * 0.7f;
        for (int c = 0; c < 4; c++) rgba[c] = Mth.lerp(t, rgba[c], DUSK_GLOW[c]);
    }

    /** What the lightmap's sky colour is drawn toward: white normally, the Harvest Moon's violet on a Hallowed Night. */
    public static Vector3f skyLightTarget(Vector3f white) {
        if (!sky()) return white;
        float t = i() * 0.55f;
        return new Vector3f(white).lerp(MOONLIGHT, t);
    }

    /** One channel (0 red, 1 green, 2 blue) of the lightmap's block light, tinted toward flame orange. */
    public static float blockLight(float value, int channel) {
        if (!sky()) return value;
        return value * Mth.lerp(i() * 0.45f, 1.0f, FLAMELIGHT.get(channel));
    }

    public static float starBrightness(float base) {
        return sky() ? Math.min(1, base + i() * 0.35f) : base;
    }

    /** The Harvest Moon is always full. */
    public static boolean fullMoon() {
        return Configs.client().moon && i() > 0.05f;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Ambience

    /** Called for each block the level picks for ambient particles (as biomes' ambient particles are). */
    public static void ambientParticles(ClientLevel level, BlockPos pos, RandomSource random) {
        float i = i();
        if (i < 0.02f || !Configs.client().ambientParticles) return;
        if (level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)) return;
        particle(level, pos, random, ModContent.EMBER.get(), 0.0016f * i);
        particle(level, pos, random, ModContent.AUTUMN_LEAF.get(), 0.0011f * i);
        particle(level, pos, random, ModContent.SOUL_MOTE.get(), 0.0005f * i + 0.0012f * HallowedClient.witching());
    }

    private static void particle(ClientLevel level, BlockPos pos, RandomSource random, SimpleParticleType type, float chance) {
        if (random.nextFloat() <= chance) {
            level.addParticle(type, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(), 0, 0, 0);
        }
    }

    /** Called every client tick: owls, wind, chimes and whispers, like a biome's additional ambient sounds. */
    public static void ambientSounds(Minecraft minecraft, RandomSource random) {
        float i = i();
        if (i < 0.05f || !Configs.client().ambientSounds || minecraft.isPaused() || minecraft.player == null) return;
        float w = HallowedClient.witching();
        sound(minecraft, random, ModContent.AMBIENT_OWL.get(), 0.0009 * i);
        sound(minecraft, random, ModContent.AMBIENT_WIND.get(), 0.0006 * i);
        sound(minecraft, random, ModContent.AMBIENT_CHIMES.get(), 0.0004 * i);
        sound(minecraft, random, ModContent.AMBIENT_WHISPERS.get(), 0.0002 * i + 0.0010 * w);
    }

    private static void sound(Minecraft minecraft, RandomSource random, SoundEvent sound, double chance) {
        if (random.nextDouble() < chance) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forAmbientAddition(sound));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    private static Vec3 lerp(float t, Vec3 base, Vector3f target) {
        return new Vec3(Mth.lerp(t, base.x, target.x()), Mth.lerp(t, base.y, target.y()), Mth.lerp(t, base.z, target.z()));
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
}
