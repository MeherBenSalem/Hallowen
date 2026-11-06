package com.nightbeam.hallowed.client;

import com.nightbeam.hallowed.config.ClientConfig;
import com.nightbeam.hallowed.config.Configs;
import com.nightbeam.hallowed.content.ModContent;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * How a Hallowed Night looks and sounds. Each piece blends the vanilla value toward the Hallowed look by tonight's
 * intensity: the sky, fog, clouds, sunset glow, star brightness, moon phase and lightmap through small client mixins,
 * the ambient motes through the level's animate tick, and the ambient additions from the client tick.
 */
public final class Atmosphere {
    private static final float[] SKY = {0.13f, 0.05f, 0.21f};
    private static final float[] FOG = {0.20f, 0.08f, 0.17f};
    private static final float[] FOG_WITCHING = {0.29f, 0.11f, 0.12f};
    private static final float[] CLOUDS = {0.16f, 0.09f, 0.24f};
    private static final float[] MOONLIGHT = {0.80f, 0.62f, 1.0f};
    private static final float[] FLAMELIGHT = {1.0f, 0.62f, 0.30f};
    private static final float[] DUSK_GLOW = {0.95f, 0.38f, 0.10f, 0.9f};

    private Atmosphere() {
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Sky

    public static Vec3 skyColor(Vec3 base) {
        return sky() ? lerp(i() * 0.85f, base, SKY) : base;
    }

    public static Vec3 cloudColor(Vec3 base) {
        return sky() ? lerp(i() * 0.8f, base, CLOUDS) : base;
    }

    /** The sunrise/sunset fan: a dusky ember glow that stays low on the horizon all night. */
    public static float @Nullable [] sunrise(float @Nullable [] base) {
        if (!sky()) return base;
        float t = i() * 0.7f;
        float[] from = base != null ? base : new float[]{DUSK_GLOW[0], DUSK_GLOW[1], DUSK_GLOW[2], 0};
        return new float[]{Mth.lerp(t, from[0], DUSK_GLOW[0]), Mth.lerp(t, from[1], DUSK_GLOW[1]), Mth.lerp(t, from[2], DUSK_GLOW[2]),
                Mth.lerp(t, from[3], DUSK_GLOW[3])};
    }

    public static float stars(float base) {
        return sky() ? Math.min(1, base + i() * 0.35f) : base;
    }

    /** A Harvest Moon: always full on a Hallowed Night. */
    public static int moonPhase(int base) {
        return Configs.client().moon && i() > 0.05f ? 0 : base;
    }

    /** How much the moon swells (0..1). */
    public static float moon() {
        return Configs.client().moon ? Math.min(1, HallowedClient.intensity()) : 0;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Fog

    public static void fogColor(float[] rgb) {
        if (!fog()) return;
        float w = HallowedClient.witching();
        float t = i() * 0.8f;
        for (int c = 0; c < 3; c++) {
            float target = Mth.lerp(w, FOG[c], FOG_WITCHING[c]);
            rgb[c] = Mth.lerp(t, rgb[c], target);
        }
    }

    public static float fogEnd(float end) {
        return fog() ? end * (1 - 0.3f * HallowedClient.witching() * scale()) : end;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Light

    /**
     * Tints one lightmap pixel: moonlight toward violet where sky light dominates, flame light toward orange where
     * block light does. {@code abgr} is the pixel as the lightmap stores it.
     */
    public static int lightmap(int block, int skyLight, int abgr) {
        if (!sky() || (block == 0 && skyLight == 0)) return abgr;
        float i = i();
        float ws = skyLight / 15f, wb = block / 15f;
        float sum = ws + wb;
        float sm = i * 0.55f, bm = i * 0.45f;
        float r = (ws * Mth.lerp(sm, 1, MOONLIGHT[0]) + wb * Mth.lerp(bm, 1, FLAMELIGHT[0])) / sum;
        float g = (ws * Mth.lerp(sm, 1, MOONLIGHT[1]) + wb * Mth.lerp(bm, 1, FLAMELIGHT[1])) / sum;
        float b = (ws * Mth.lerp(sm, 1, MOONLIGHT[2]) + wb * Mth.lerp(bm, 1, FLAMELIGHT[2])) / sum;
        int red = Math.min(255, (int) ((abgr & 0xFF) * r));
        int green = Math.min(255, (int) ((abgr >> 8 & 0xFF) * g));
        int blue = Math.min(255, (int) ((abgr >> 16 & 0xFF) * b));
        return abgr & 0xFF000000 | blue << 16 | green << 8 | red;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Ambience

    /** Called for each random spot the client level animates around the camera (like biome ambient particles). */
    public static void ambientParticles(ClientLevel level, RandomSource random, BlockPos pos) {
        float i = i();
        if (i < 0.02f || !Configs.client().ambientParticles) return;
        spawn(level, random, pos, ModContent.EMBER, 0.0016f * i);
        spawn(level, random, pos, ModContent.AUTUMN_LEAF, 0.0011f * i);
        spawn(level, random, pos, ModContent.SOUL_MOTE, 0.0005f * i + 0.0012f * HallowedClient.witching());
    }

    private static void spawn(ClientLevel level, RandomSource random, BlockPos pos, Supplier<SimpleParticleType> type, float chance) {
        if (random.nextFloat() <= chance) {
            level.addParticle(type.get(), pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(),
                    0, 0, 0);
        }
    }

    /** Called every client tick: owls, wind, chimes and whispers, like a biome's ambient additions. */
    public static void ambientSounds(RandomSource random) {
        float i = i();
        Minecraft minecraft = Minecraft.getInstance();
        if (i < 0.05f || !Configs.client().ambientSounds || minecraft.player == null || minecraft.isPaused()) return;
        float w = HallowedClient.witching();
        play(minecraft, random, ModContent.AMBIENT_OWL.get(), 0.0009 * i);
        play(minecraft, random, ModContent.AMBIENT_WIND.get(), 0.0006 * i);
        play(minecraft, random, ModContent.AMBIENT_CHIMES.get(), 0.0004 * i);
        play(minecraft, random, ModContent.AMBIENT_WHISPERS.get(), 0.0002 * i + 0.0010 * w);
    }

    private static void play(Minecraft minecraft, RandomSource random, SoundEvent sound, double chance) {
        if (random.nextDouble() < chance) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forAmbientAddition(sound));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    private static Vec3 lerp(float t, Vec3 base, float[] to) {
        return new Vec3(Mth.lerp(t, base.x, to[0]), Mth.lerp(t, base.y, to[1]), Mth.lerp(t, base.z, to[2]));
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
