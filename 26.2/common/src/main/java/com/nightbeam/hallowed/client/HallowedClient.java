package com.nightbeam.hallowed.client;

import com.nightbeam.hallowed.config.ClientConfig;
import com.nightbeam.hallowed.config.Configs;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.network.FxPayload;
import com.nightbeam.hallowed.network.NightPayload;
import com.nightbeam.hallowed.night.HallowedNight;
import com.nightbeam.hallowed.night.NightClock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * The client's view of tonight: whether it is Hallowed (from the server), and from that and the clock how strongly the
 * night shows right now. Also routes one-off effects to the overlay and the camera.
 */
public final class HallowedClient {
    private static boolean hallowed;
    private static long night = Long.MIN_VALUE;
    private static float intensity;
    private static float previousIntensity;
    private static float witching;
    private static int heartbeat;
    private static ClientLevel lastLevel;

    private HallowedClient() {
    }

    public static void handleNight(NightPayload payload) {
        hallowed = payload.hallowed();
        night = payload.nightIndex();
    }

    public static void handleFx(FxPayload payload) {
        ClientConfig c = Configs.client();
        switch (payload.kind()) {
            case DUSK_BANNER -> NightOverlay.banner(NightOverlay.Banner.DUSK);
            case WITCHING_BANNER -> NightOverlay.banner(NightOverlay.Banner.WITCHING);
            case HORSEMAN_BANNER -> NightOverlay.banner(NightOverlay.Banner.HORSEMAN);
            case HORSEMAN_FELLED -> NightOverlay.banner(NightOverlay.Banner.FELLED);
            case HORSEMAN_LEFT -> NightOverlay.banner(NightOverlay.Banner.LEFT);
            case SPOOK -> {
                if (c.jumpscares) NightOverlay.spook();
                ScreenShake.add(0.6f);
            }
            case SHAKE -> ScreenShake.add(payload.strength());
            case TREAT -> NightOverlay.sparkle();
            case TRICK -> ScreenShake.add(0.15f);
        }
    }

    public static void reset() {
        hallowed = false;
        night = Long.MIN_VALUE;
        intensity = previousIntensity = witching = 0;
        lastLevel = null;
        NightOverlay.reset();
        ScreenShake.reset();
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        previousIntensity = intensity;
        if (level == null) {
            intensity = witching = 0;
            return;
        }
        long time = NightClock.time(level);
        boolean tonight = hallowed && NightClock.dayIndex(time) == night && HallowedNight.affects(level);
        int tod = NightClock.timeOfDay(time);
        float target = tonight ? NightClock.intensity(tod) * Configs.client().intensity : 0;
        if (level != lastLevel) {
            // A new world or dimension: no fading over from the last one.
            lastLevel = level;
            intensity = previousIntensity = target;
        }
        intensity = Mth.approach(intensity, target, 0.01f);
        witching = tonight ? NightClock.witchingWeight(tod) : Mth.approach(witching, 0, 0.02f);
        NightOverlay.tick();
        ScreenShake.tick();
        tickSpooked(minecraft.player);
    }

    private static void tickSpooked(LocalPlayer player) {
        if (player == null) return;
        MobEffectInstance spooked = player.getEffect(ModContent.holder(ModContent.SPOOKED));
        if (spooked == null) {
            heartbeat = 0;
            return;
        }
        if (--heartbeat <= 0) {
            heartbeat = 22;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModContent.HEARTBEAT.get(), 1.0f, 0.6f));
            if (Configs.client().cameraShake) ScreenShake.atLeast(0.12f);
        }
    }

    /** How strongly tonight shows (0..1), smoothed. */
    public static float intensity(float partialTick) {
        return Mth.lerp(partialTick, previousIntensity, intensity);
    }

    public static float intensity() {
        return intensity;
    }

    /** The Witching Hour's weight (0..1). */
    public static float witching() {
        return witching;
    }
}
