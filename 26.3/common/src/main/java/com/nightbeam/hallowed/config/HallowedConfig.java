package com.nightbeam.hallowed.config;

import com.google.gson.JsonObject;
import java.time.MonthDay;
import java.util.Locale;

import static com.nightbeam.hallowed.config.Json.obj;

/** {@code config/hallowed/hallowed.json}: when Hallowed Nights happen and how they play. Read on the server. */
public final class HallowedConfig {
    public enum Mode {
        /** Every night inside the season window; outside it, full moons may still turn Hallowed. */
        SEASONAL,
        /** Every night is a Hallowed Night. */
        ALWAYS,
        /** Only full moons, with the off-season chance, whatever the date. */
        FULL_MOONS,
        /** Only nights started with {@code /hallowed night start}. */
        MANUAL
    }

    public final Mode mode;
    public final MonthDay seasonStart;
    public final MonthDay seasonEnd;
    public final double offseasonChance;
    public final boolean announce;
    public final boolean bell;

    public final double costumedChance;
    public final boolean spiritsEnabled;
    public final int spiritsPerPlayer;
    public final int spiritInterval;
    public final boolean wispsEnabled;
    public final int wispInterval;
    public final double wispChance;
    public final double cursedWispChance;
    public final int wardRadius;
    public final int soulWardRadius;

    public final double candyDropChance;
    public final double costumedCandyChance;
    public final int sugarRushCap;
    public final boolean sugarCrash;

    public final boolean trickOrTreat;
    public final double treatChance;
    public final double costumeTreatChance;
    public final boolean trickBats;
    public final boolean trickPumpkin;
    public final boolean trickLevitate;
    public final boolean trickSpook;
    public final boolean trickVanish;
    public final boolean trickSteal;

    public final double horsemanHealth;
    public final double horsemanDamage;
    public final double horsemanArmor;
    public final boolean horsemanFire;
    public final boolean horsemanLeavesAtDawn;
    public final boolean summonNeedsWitchingHour;

    private HallowedConfig(JsonObject o) {
        mode = parseMode(Json.str(o, "season.mode", "seasonal"));
        seasonStart = parseDay(Json.str(o, "season.start", "10-15"), MonthDay.of(10, 15));
        seasonEnd = parseDay(Json.str(o, "season.end", "11-05"), MonthDay.of(11, 5));
        offseasonChance = clamp(Json.num(o, "season.offseason_full_moon_chance", 0.25), 0, 1);
        announce = Json.bool(o, "night.announce", true);
        bell = Json.bool(o, "night.midnight_bell", true);

        costumedChance = clamp(Json.num(o, "spawns.costumed_mob_chance", 0.35), 0, 1);
        spiritsEnabled = Json.bool(o, "spawns.spirits.enabled", true);
        spiritsPerPlayer = (int) clamp(Json.integer(o, "spawns.spirits.max_per_player", 4), 0, 16);
        spiritInterval = (int) clamp(Json.integer(o, "spawns.spirits.interval_seconds", 20), 2, 600);
        wispsEnabled = Json.bool(o, "spawns.wisps.enabled", true);
        wispInterval = (int) clamp(Json.integer(o, "spawns.wisps.interval_seconds", 120), 10, 3600);
        wispChance = clamp(Json.num(o, "spawns.wisps.chance", 0.65), 0, 1);
        cursedWispChance = clamp(Json.num(o, "spawns.wisps.cursed_chance", 0.15), 0, 1);
        wardRadius = (int) clamp(Json.integer(o, "spawns.lantern_ward_radius", 12), 0, 48);
        soulWardRadius = (int) clamp(Json.integer(o, "spawns.soul_lantern_ward_radius", 20), 0, 64);

        candyDropChance = clamp(Json.num(o, "candy.drop_chance", 0.2), 0, 1);
        costumedCandyChance = clamp(Json.num(o, "candy.costumed_drop_chance", 1.0), 0, 1);
        sugarRushCap = (int) clamp(Json.integer(o, "candy.sugar_rush_max_seconds", 60), 5, 600);
        sugarCrash = Json.bool(o, "candy.sugar_crash", true);

        trickOrTreat = Json.bool(o, "trick_or_treat.enabled", true);
        treatChance = clamp(Json.num(o, "trick_or_treat.treat_chance", 0.72), 0, 1);
        costumeTreatChance = clamp(Json.num(o, "trick_or_treat.costume_treat_chance", 0.9), 0, 1);
        trickBats = Json.bool(o, "trick_or_treat.tricks.bat_burst", true);
        trickPumpkin = Json.bool(o, "trick_or_treat.tricks.pumpkin_head", true);
        trickLevitate = Json.bool(o, "trick_or_treat.tricks.levitation", true);
        trickSpook = Json.bool(o, "trick_or_treat.tricks.spook", true);
        trickVanish = Json.bool(o, "trick_or_treat.tricks.vanishing_act", true);
        trickSteal = Json.bool(o, "trick_or_treat.tricks.sticky_fingers", true);

        horsemanHealth = clamp(Json.num(o, "horseman.health", 320), 20, 4096);
        horsemanDamage = clamp(Json.num(o, "horseman.damage", 11), 1, 200);
        horsemanArmor = clamp(Json.num(o, "horseman.armor", 8), 0, 30);
        horsemanFire = Json.bool(o, "horseman.sets_fires", false);
        horsemanLeavesAtDawn = Json.bool(o, "horseman.rides_away_at_dawn", true);
        summonNeedsWitchingHour = Json.bool(o, "horseman.summon_needs_witching_hour", true);
    }

    public static HallowedConfig from(JsonObject json) {
        return new HallowedConfig(json);
    }

    public static CommentedJson spec() {
        JsonObject d = obj(
                "season", obj("mode", "seasonal", "start", "10-15", "end", "11-05", "offseason_full_moon_chance", 0.25),
                "night", obj("announce", true, "midnight_bell", true),
                "spawns", obj("costumed_mob_chance", 0.35,
                        "spirits", obj("enabled", true, "max_per_player", 4, "interval_seconds", 20),
                        "wisps", obj("enabled", true, "interval_seconds", 120, "chance", 0.65, "cursed_chance", 0.15),
                        "lantern_ward_radius", 12, "soul_lantern_ward_radius", 20),
                "candy", obj("drop_chance", 0.2, "costumed_drop_chance", 1.0, "sugar_rush_max_seconds", 60, "sugar_crash", true),
                "trick_or_treat", obj("enabled", true, "treat_chance", 0.72, "costume_treat_chance", 0.9,
                        "tricks", obj("bat_burst", true, "pumpkin_head", true, "levitation", true, "spook", true,
                                "vanishing_act", true, "sticky_fingers", true)),
                "horseman", obj("health", 320, "damage", 11, "armor", 8, "sets_fires", false, "rides_away_at_dawn", true,
                        "summon_needs_witching_hour", true));
        return new CommentedJson(d)
                .header("Hallowed 1.0: Hallowed Nights, trick-or-treating and the Hollow Horseman.\n"
                        + "Changes apply with /hallowed reload. Lines starting with // are comments.")
                .comment("season", "mode: \"seasonal\" makes every night between start and end (month-day, real date of the server)\n"
                        + "a Hallowed Night, and outside that window a full moon turns Hallowed with offseason_full_moon_chance.\n"
                        + "\"always\": every night. \"full_moons\": only full moons, with that chance, whatever the date.\n"
                        + "\"manual\": only nights started with /hallowed night start.")
                .comment("night", "announce: the banners at dusk and at midnight. midnight_bell: twelve tolls when the Witching Hour begins.")
                .comment("spawns", "costumed_mob_chance: zombies and skeletons that spawn on a Hallowed Night wearing a carved pumpkin.\n"
                        + "Spirits haunt Hallowed Nights (most of all the Witching Hour); wisps lead players to Hallowed Caches,\n"
                        + "and cursed_chance of them lead into an ambush with a better cache.\n"
                        + "Lit jack o'lanterns stop spirits from spawning within lantern_ward_radius blocks (soul ones: soul_lantern_ward_radius).")
                .comment("candy", "drop_chance: monsters killed by a player on a Hallowed Night drop candy; costumed ones use\n"
                        + "costumed_drop_chance. Sugar Rush stacks up to sugar_rush_max_seconds; above half of it the rush\n"
                        + "turns into Sugar Rush II, and sugar_crash ends that with a Sugar Crash.")
                .comment("trick_or_treat", "Right-click a villager with a Treat Pail on a Hallowed Night. Wearing a costume on your head (any\n"
                        + "carved pumpkin, jack o'lantern, mob head or the Witch Hat) uses costume_treat_chance. Tricks are harmless;\n"
                        + "turn any of them off here.")
                .comment("horseman", "The Hollow Horseman. sets_fires: his pumpkin bombs leave fire on the ground (never in protected spawn).\n"
                        + "summon_needs_witching_hour: the Cursed Pumpkin only wakes between midnight and 3 AM of a Hallowed Night.");
    }

    private static Mode parseMode(String value) {
        return switch (value.toLowerCase(Locale.ROOT).trim()) {
            case "always" -> Mode.ALWAYS;
            case "full_moons", "full_moon", "moons" -> Mode.FULL_MOONS;
            case "manual", "never", "off" -> Mode.MANUAL;
            default -> Mode.SEASONAL;
        };
    }

    private static MonthDay parseDay(String value, MonthDay fallback) {
        try {
            String[] parts = value.trim().split("[-/.]");
            return MonthDay.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
