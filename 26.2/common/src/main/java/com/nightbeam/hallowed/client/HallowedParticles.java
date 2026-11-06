package com.nightbeam.hallowed.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Hallowed's particles: rising embers, falling autumn leaves, the wisps' foxfire, pale soul motes and candy sparkles.
 * The glowing ones are full-bright; leaves are lit like the world.
 */
public final class HallowedParticles {
    private HallowedParticles() {
    }

    /**
     * Ambient motes are scattered around the camera; one that lands right in front of the lens would fill the screen,
     * so those are skipped.
     */
    private static boolean nearCamera(double x, double y, double z, double distance) {
        Vec3 eye = Minecraft.getInstance().gameRenderer.mainCamera().position();
        return eye.distanceToSqr(x, y, z) < distance * distance;
    }

    /** A drifting, fading, optionally glowing quad. */
    private static class Mote extends SingleQuadParticle {
        private final SpriteSet sprites;
        private final float baseSize;
        private final float sway;
        private final boolean glow;
        private final boolean animate;
        private final float spin;
        private final float fadeIn;

        Mote(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites, int life, float size,
                float sway, boolean glow, boolean animate, float spin, RandomSource random) {
            super(level, x, y, z, sprites.first());
            this.xd = dx;
            this.yd = dy;
            this.zd = dz;
            this.sprites = sprites;
            this.lifetime = life;
            this.baseSize = size;
            this.quadSize = size;
            this.sway = sway;
            this.glow = glow;
            this.animate = animate;
            this.spin = spin;
            this.fadeIn = 5;
            this.hasPhysics = false;
            this.friction = 0.96f;
            this.alpha = 0;
            this.roll = this.oRoll = random.nextFloat() * Mth.TWO_PI;
            if (animate) setSpriteFromAge(sprites);
            else setSprite(sprites.get(random));
        }

        @Override
        public void tick() {
            oRoll = roll;
            super.tick();
            if (removed) return;
            if (animate) setSpriteFromAge(sprites);
            float life = age / (float) lifetime;
            alpha = Math.min(1, age / fadeIn) * (1 - life * life);
            quadSize = baseSize * (1 - life * 0.35f);
            xd += Mth.sin(age * 0.21f + (float) y) * sway;
            zd += Mth.cos(age * 0.17f + (float) x) * sway;
            roll += spin;
        }

        @Override
        protected Layer getLayer() {
            return Layer.TRANSLUCENT;
        }

        @Override
        protected int getLightCoords(float partialTick) {
            return glow ? 0xF000F0 : super.getLightCoords(partialTick);
        }
    }

    /** Falls with gravity, swaying side to side and turning, then settles on the ground. */
    private static final class Leaf extends Mote {
        private final float phase;

        Leaf(ClientLevel level, double x, double y, double z, SpriteSet sprites, RandomSource random) {
            super(level, x, y, z, 0, -0.02, 0, sprites, 160 + random.nextInt(80), 0.08f + random.nextFloat() * 0.05f, 0, false, false,
                    (random.nextFloat() - 0.5f) * 0.12f, random);
            this.hasPhysics = true;
            this.gravity = 0.06f;
            this.phase = random.nextFloat() * 10;
        }

        @Override
        public void tick() {
            super.tick();
            if (removed) return;
            if (onGround) {
                xd = zd = 0;
                if (lifetime - age > 30) age = lifetime - 30;
            } else {
                xd += Mth.sin(age * 0.09f + phase) * 0.0035;
                zd += Mth.cos(age * 0.07f + phase) * 0.0025;
                yd = Math.max(yd, -0.045);
            }
        }
    }

    public static final class EmberProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public EmberProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy,
                double dz, RandomSource random) {
            if (nearCamera(x, y, z, 1.5)) return null;
            Mote p = new Mote(level, x, y, z, dx, dy + 0.012 + random.nextFloat() * 0.01, dz, sprites, 40 + random.nextInt(50),
                    0.045f + random.nextFloat() * 0.04f, 0.0009f, true, true, 0, random);
            float warm = random.nextFloat() * 0.25f;
            p.setColor(1.0f, 0.52f + warm, 0.18f + warm * 0.4f);
            return p;
        }
    }

    public static final class LeafProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public LeafProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy,
                double dz, RandomSource random) {
            if (nearCamera(x, y, z, 4.0)) return null;
            return new Leaf(level, x, y, z, sprites, random);
        }
    }

    public static final class WispFlameProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public WispFlameProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz,
                RandomSource random) {
            Mote p = new Mote(level, x, y, z, dx, dy + 0.008, dz, sprites, 18 + random.nextInt(14), 0.09f + random.nextFloat() * 0.06f,
                    0.0005f, true, true, 0, random);
            float t = random.nextFloat();
            p.setColor(Mth.lerp(t, 0.55f, 1.0f), Mth.lerp(t, 1.0f, 0.78f), Mth.lerp(t, 0.45f, 0.30f));
            return p;
        }
    }

    public static final class SoulMoteProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public SoulMoteProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy,
                double dz, RandomSource random) {
            if (nearCamera(x, y, z, 1.5)) return null;
            Mote p = new Mote(level, x, y, z, dx, dy + 0.004, dz, sprites, 50 + random.nextInt(50), 0.04f + random.nextFloat() * 0.04f,
                    0.0007f, true, true, 0, random);
            float t = random.nextFloat() * 0.2f;
            p.setColor(0.62f + t, 0.92f, 1.0f);
            return p;
        }
    }

    public static final class CandySparkleProvider implements ParticleProvider<SimpleParticleType> {
        private static final float[][] COLORS = {
                {1.0f, 0.55f, 0.18f}, {0.98f, 0.42f, 0.70f}, {0.66f, 0.42f, 1.0f}, {0.45f, 0.95f, 0.70f}, {1.0f, 0.88f, 0.35f}};
        private final SpriteSet sprites;

        public CandySparkleProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz,
                RandomSource random) {
            Mote p = new Mote(level, x, y, z, dx * 2, dy * 2 + 0.03, dz * 2, sprites, 16 + random.nextInt(16), 0.07f + random.nextFloat() * 0.05f,
                    0, true, true, (random.nextFloat() - 0.5f) * 0.3f, random);
            float[] c = COLORS[random.nextInt(COLORS.length)];
            p.setColor(c[0], c[1], c[2]);
            return p;
        }
    }
}
