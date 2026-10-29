package com.nightbeam.hallowed.night;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * Overworld time and the shape of a Hallowed Night, shared by server and client so both agree on every phase.
 * A night belongs to the day it starts on: it runs from dusk (12500) to dawn (23500) of that day.
 */
public final class NightClock {
    public static final int DAY = 24000;
    public static final int DUSK = 12500;
    public static final int FULL_DARK = 13600;
    public static final int MIDNIGHT = 18000;
    public static final int WITCHING_END = 21000;
    public static final int DAWN = 23500;

    private NightClock() {
    }

    /** Ticks on the overworld clock, including skipped nights. Works on both sides. */
    public static long time(Level level) {
        return level.getOverworldClockTime();
    }

    public static long dayIndex(long time) {
        return Math.floorDiv(time, DAY);
    }

    public static int timeOfDay(long time) {
        return (int) Math.floorMod(time, DAY);
    }

    public static boolean night(int timeOfDay) {
        return timeOfDay >= DUSK && timeOfDay < DAWN;
    }

    public static boolean witching(int timeOfDay) {
        return timeOfDay >= MIDNIGHT && timeOfDay < WITCHING_END;
    }

    /** The night a time belongs to, or the coming night during the day. */
    public static long nightIndex(long time) {
        return dayIndex(time) + (timeOfDay(time) >= DAWN ? 1 : 0);
    }

    /** Vanilla moon phases: a full moon every eighth night. */
    public static boolean fullMoon(long nightIndex) {
        return Math.floorMod(nightIndex, 8) == 0;
    }

    /**
     * How strongly a Hallowed Night shows (0..1): it gathers through dusk, swells toward midnight, holds through the
     * Witching Hour and thins away before dawn.
     */
    public static float intensity(float timeOfDay) {
        float t = timeOfDay;
        if (t < DUSK || t >= DAWN) return 0;
        if (t < FULL_DARK) return 0.6f * smooth((t - DUSK) / (FULL_DARK - DUSK));
        if (t < MIDNIGHT - 900) return 0.6f;
        if (t < MIDNIGHT) return Mth.lerp(smooth((t - (MIDNIGHT - 900)) / 900f), 0.6f, 1.0f);
        if (t < WITCHING_END) return 1.0f;
        if (t < WITCHING_END + 800) return Mth.lerp(smooth((t - WITCHING_END) / 800f), 1.0f, 0.55f);
        return 0.55f * (1 - smooth((t - (WITCHING_END + 800)) / (DAWN - WITCHING_END - 800f)));
    }

    /** The Witching Hour's own weight (0..1), for the effects that only belong to it. */
    public static float witchingWeight(float timeOfDay) {
        float t = timeOfDay;
        if (t < MIDNIGHT - 400 || t >= WITCHING_END + 600) return 0;
        if (t < MIDNIGHT) return smooth((t - (MIDNIGHT - 400)) / 400f);
        if (t < WITCHING_END) return 1;
        return 1 - smooth((t - WITCHING_END) / 600f);
    }

    public static float smooth(float t) {
        t = Mth.clamp(t, 0, 1);
        return t * t * (3 - 2 * t);
    }

    /** "11:42 PM" style clock text for a time of day, for status messages. */
    public static String clockText(int timeOfDay) {
        int minutes = (int) ((timeOfDay + 6000L) % DAY * 1440 / DAY);
        int hour = minutes / 60;
        int minute = minutes % 60;
        int h12 = hour % 12 == 0 ? 12 : hour % 12;
        return String.format("%d:%02d %s", h12, minute, hour < 12 ? "AM" : "PM");
    }
}
