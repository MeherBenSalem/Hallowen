package com.nightbeam.hallowed.night;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NightClockTest {
    @Test
    void nightBelongsToTheDayItStartsOn() {
        assertEquals(0, NightClock.nightIndex(NightClock.MIDNIGHT));
        assertEquals(0, NightClock.nightIndex(NightClock.DAWN - 1));
        // From dawn on, the coming night is the next day's.
        assertEquals(1, NightClock.nightIndex(NightClock.DAWN));
        assertEquals(1, NightClock.nightIndex(NightClock.DAY + 6000));
        assertEquals(3, NightClock.nightIndex(3L * NightClock.DAY + NightClock.DUSK));
    }

    @Test
    void phases() {
        assertFalse(NightClock.night(6000));
        assertTrue(NightClock.night(NightClock.DUSK));
        assertTrue(NightClock.night(NightClock.DAWN - 1));
        assertFalse(NightClock.night(NightClock.DAWN));
        assertTrue(NightClock.witching(NightClock.MIDNIGHT));
        assertTrue(NightClock.witching(NightClock.WITCHING_END - 1));
        assertFalse(NightClock.witching(NightClock.WITCHING_END));
        assertFalse(NightClock.witching(NightClock.MIDNIGHT - 1));
    }

    @Test
    void fullMoonEveryEighthNight() {
        assertTrue(NightClock.fullMoon(0));
        assertTrue(NightClock.fullMoon(8));
        assertTrue(NightClock.fullMoon(-8));
        assertFalse(NightClock.fullMoon(4));
    }

    @Test
    void intensityRisesThroughTheNightAndPeaksInTheWitchingHour() {
        assertEquals(0, NightClock.intensity(6000), 1e-6);
        assertEquals(0, NightClock.intensity(NightClock.DUSK), 1e-6);
        assertEquals(0.6f, NightClock.intensity(NightClock.FULL_DARK), 1e-6);
        assertEquals(1.0f, NightClock.intensity(NightClock.MIDNIGHT), 1e-6);
        assertEquals(1.0f, NightClock.intensity(NightClock.WITCHING_END - 1), 1e-6);
        assertEquals(0, NightClock.intensity(NightClock.DAWN), 1e-6);
        // Never jumps: every tick of the night is within a small step of the one before.
        float previous = 0;
        for (int t = NightClock.DUSK; t <= NightClock.DAWN; t++) {
            float now = NightClock.intensity(t);
            assertTrue(Math.abs(now - previous) < 0.01f, "jump at " + t);
            assertTrue(now >= 0 && now <= 1);
            previous = now;
        }
    }

    @Test
    void witchingWeightFadesInAndOut() {
        assertEquals(0, NightClock.witchingWeight(NightClock.MIDNIGHT - 400), 1e-6);
        assertEquals(1, NightClock.witchingWeight(NightClock.MIDNIGHT), 1e-6);
        assertEquals(1, NightClock.witchingWeight(NightClock.WITCHING_END - 1), 1e-6);
        assertEquals(0, NightClock.witchingWeight(NightClock.WITCHING_END + 600), 1e-6);
    }
}
