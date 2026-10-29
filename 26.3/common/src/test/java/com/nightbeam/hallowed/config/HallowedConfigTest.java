package com.nightbeam.hallowed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.nightbeam.hallowed.night.HallowedNight;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.MonthDay;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HallowedConfigTest {
    private static HallowedConfig with(String json) {
        JsonObject user = CommentedJson.parse(json).getAsJsonObject();
        return HallowedConfig.from(CommentedJson.mergeOver(HallowedConfig.spec().defaults(), user));
    }

    @Test
    void defaults() {
        HallowedConfig c = HallowedConfig.from(HallowedConfig.spec().defaults());
        assertEquals(HallowedConfig.Mode.SEASONAL, c.mode);
        assertEquals(MonthDay.of(10, 15), c.seasonStart);
        assertEquals(MonthDay.of(11, 5), c.seasonEnd);
        assertEquals(12, c.wardRadius);
        assertEquals(20, c.soulWardRadius);
        assertEquals(320, c.horsemanHealth);
        assertFalse(c.horsemanFire);
        assertTrue(c.summonNeedsWitchingHour);
    }

    @Test
    void seasonWindow() {
        HallowedConfig c = HallowedConfig.from(HallowedConfig.spec().defaults());
        assertTrue(HallowedNight.inSeason(MonthDay.of(10, 15), c));
        assertTrue(HallowedNight.inSeason(MonthDay.of(10, 31), c));
        assertTrue(HallowedNight.inSeason(MonthDay.of(11, 5), c));
        assertFalse(HallowedNight.inSeason(MonthDay.of(11, 6), c));
        assertFalse(HallowedNight.inSeason(MonthDay.of(7, 1), c));
    }

    @Test
    void seasonWindowCanWrapTheNewYear() {
        HallowedConfig c = with("{\"season\": {\"start\": \"12-20\", \"end\": \"01-10\"}}");
        assertTrue(HallowedNight.inSeason(MonthDay.of(12, 31), c));
        assertTrue(HallowedNight.inSeason(MonthDay.of(1, 1), c));
        assertFalse(HallowedNight.inSeason(MonthDay.of(10, 31), c));
    }

    @Test
    void badValuesFallBackToDefaults() {
        HallowedConfig c = with("{\"season\": {\"mode\": \"sometimes\", \"start\": \"spooky\"}}");
        assertEquals(HallowedConfig.Mode.SEASONAL, c.mode);
        assertEquals(MonthDay.of(10, 15), c.seasonStart);
        assertEquals(HallowedConfig.Mode.FULL_MOONS, with("{\"season\": {\"mode\": \"Full_Moons\"}}").mode);
        assertEquals(HallowedConfig.Mode.MANUAL, with("{\"season\": {\"mode\": \"manual\"}}").mode);
    }

    @Test
    void fileKeepsUserValuesAndGainsMissingKeys(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("hallowed.json");
        Files.writeString(file, """
                // an old file, with comments and a missing section
                {
                  "season": { "mode": "always" },
                  "horseman": { "health": 500 }
                }
                """);
        JsonObject merged = HallowedConfig.spec().load(file);
        HallowedConfig c = HallowedConfig.from(merged);
        assertEquals(HallowedConfig.Mode.ALWAYS, c.mode);
        assertEquals(500, c.horsemanHealth);
        assertEquals(0.72, c.treatChance, 1e-9);

        String written = Files.readString(file);
        assertTrue(written.startsWith("// Hallowed"), written);
        assertTrue(written.contains("\"trick_or_treat\""), written);
        assertTrue(written.contains("\"mode\": \"always\""), written);
        // Reading the written file back gives the same values.
        assertEquals(merged, CommentedJson.parse(written).getAsJsonObject());
    }
}
