package com.nightbeam.hallowed.night;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.config.Configs;
import com.nightbeam.hallowed.config.HallowedConfig;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.network.FxPayload;
import com.nightbeam.hallowed.network.NightPayload;
import com.nightbeam.hallowed.reward.Advancements;
import com.nightbeam.hallowed.util.Feedback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.MonthDay;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.jspecify.annotations.Nullable;

/**
 * Decides which nights are Hallowed and runs their milestones: the dusk banner, the twelve midnight tolls that open
 * the Witching Hour, and the quiet end at dawn. Whether a night is Hallowed is a pure function of the date, the
 * world seed and the night's index, so nothing has to be remembered except commands that force or cancel a night
 * ({@code <world>/hallowed.json}).
 */
public final class HallowedNight {
    private static final int TOLLS = 12;
    private static final int TOLL_INTERVAL = 34;

    private static long forcedNight = Long.MIN_VALUE;
    private static long cancelledNight = Long.MIN_VALUE;
    private static @Nullable Path file;
    private static long seed;

    private static int lastTimeOfDay = -1;
    private static long lastNight = Long.MIN_VALUE;
    private static boolean lastActive;
    private static int tollsLeft;
    private static int tollTimer;
    private static final Map<UUID, Long> SYNCED = new HashMap<>();

    private HallowedNight() {
    }

    public static void load(MinecraftServer server) {
        forcedNight = cancelledNight = Long.MIN_VALUE;
        lastTimeOfDay = -1;
        lastNight = Long.MIN_VALUE;
        lastActive = false;
        tollsLeft = 0;
        SYNCED.clear();
        seed = server.overworld().getSeed();
        file = server.getWorldPath(LevelResource.ROOT).resolve(Hallowed.MOD_ID + ".json");
        try {
            if (Files.exists(file)) {
                JsonObject o = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
                forcedNight = o.has("forced_night") ? o.get("forced_night").getAsLong() : Long.MIN_VALUE;
                cancelledNight = o.has("cancelled_night") ? o.get("cancelled_night").getAsLong() : Long.MIN_VALUE;
            }
        } catch (IOException | RuntimeException e) {
            Hallowed.LOG.warn("Could not read {}: {}", file, e.toString());
        }
    }

    public static void unload() {
        save();
        file = null;
        SYNCED.clear();
    }

    private static void save() {
        if (file == null) return;
        JsonObject o = new JsonObject();
        o.addProperty("forced_night", forcedNight);
        o.addProperty("cancelled_night", cancelledNight);
        try {
            Files.writeString(file, o.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Hallowed.LOG.warn("Could not write {}: {}", file, e.toString());
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Queries

    /** Whether night {@code nightIndex} is (or will be) a Hallowed Night. */
    public static boolean isHallowed(long nightIndex) {
        if (nightIndex == cancelledNight) return false;
        if (nightIndex == forcedNight) return true;
        HallowedConfig c = Configs.common();
        return switch (c.mode) {
            case ALWAYS -> true;
            case MANUAL -> false;
            case FULL_MOONS -> NightClock.fullMoon(nightIndex) && roll(nightIndex) < c.offseasonChance;
            case SEASONAL -> inSeason(MonthDay.now(), c) || (NightClock.fullMoon(nightIndex) && roll(nightIndex) < c.offseasonChance);
        };
    }

    public static boolean inSeason(MonthDay today, HallowedConfig c) {
        if (!c.seasonStart.isAfter(c.seasonEnd)) {
            return !today.isBefore(c.seasonStart) && !today.isAfter(c.seasonEnd);
        }
        return !today.isBefore(c.seasonStart) || !today.isAfter(c.seasonEnd);
    }

    /** A stable 0..1 roll per world and night. */
    private static double roll(long nightIndex) {
        long h = seed ^ (nightIndex * 0x9E3779B97F4A7C15L);
        h = (h ^ (h >>> 33)) * 0xff51afd7ed558ccdL;
        h = (h ^ (h >>> 33)) * 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return (h >>> 11) * 0x1.0p-53;
    }

    /** Levels with the overworld's sky and clock (not the Nether or the End). */
    public static boolean affects(Level level) {
        return !level.dimensionType().hasFixedTime() && level.dimensionType().hasSkyLight();
    }

    /** A Hallowed Night is under way right now in this level. */
    public static boolean active(Level level) {
        if (!affects(level)) return false;
        long time = NightClock.time(level);
        return NightClock.night(NightClock.timeOfDay(time)) && isHallowed(NightClock.dayIndex(time));
    }

    /** The Witching Hour (midnight to 3 AM) of a Hallowed Night is under way in this level. */
    public static boolean witchingHour(Level level) {
        if (!affects(level)) return false;
        long time = NightClock.time(level);
        return NightClock.witching(NightClock.timeOfDay(time)) && isHallowed(NightClock.dayIndex(time));
    }

    public static float intensity(Level level) {
        if (!affects(level)) return 0;
        long time = NightClock.time(level);
        return isHallowed(NightClock.dayIndex(time)) ? NightClock.intensity(NightClock.timeOfDay(time)) : 0;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Commands

    /** Makes the current (or coming) night Hallowed. */
    public static void force(MinecraftServer server) {
        long night = NightClock.nightIndex(NightClock.time(server.overworld()));
        forcedNight = night;
        if (cancelledNight == night) cancelledNight = Long.MIN_VALUE;
        save();
        syncAll(server);
    }

    /** Ends the current (or cancels the coming) Hallowed Night. */
    public static void cancel(MinecraftServer server) {
        long night = NightClock.nightIndex(NightClock.time(server.overworld()));
        cancelledNight = night;
        if (forcedNight == night) forcedNight = Long.MIN_VALUE;
        tollsLeft = 0;
        save();
        syncAll(server);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Ticking

    public static void tick(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        long time = NightClock.time(overworld);
        int tod = NightClock.timeOfDay(time);
        long night = NightClock.nightIndex(time);
        boolean hallowed = isHallowed(NightClock.dayIndex(time));
        boolean active = hallowed && NightClock.night(tod);
        HallowedConfig c = Configs.common();

        if (server.getTickCount() % 20 == 0 || night != lastNight) {
            syncAll(server);
        }
        if (lastTimeOfDay >= 0 && night == lastNight && hallowed) {
            if (crossed(lastTimeOfDay, tod, NightClock.DUSK) && c.announce) {
                broadcastFx(server, new FxPayload(FxPayload.Kind.DUSK_BANNER, 1));
            }
            if (crossed(lastTimeOfDay, tod, NightClock.MIDNIGHT)) {
                beginWitchingHour(server, c);
            }
        }
        if (lastActive && !active && lastNight == night && tod >= NightClock.DAWN) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (affects(player.level())) {
                    Feedback.actionBar(player, net.minecraft.network.chat.Component.translatable("event.hallowed.dawn")
                            .withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC));
                }
            }
        }
        if (tollsLeft > 0 && --tollTimer <= 0) {
            toll(server, TOLLS - tollsLeft);
            tollsLeft--;
            tollTimer = TOLL_INTERVAL;
        }
        lastTimeOfDay = tod;
        lastNight = night;
        lastActive = active;
    }

    private static boolean crossed(int before, int now, int mark) {
        return before < mark && now >= mark && now - before < 1200;
    }

    private static void beginWitchingHour(MinecraftServer server, HallowedConfig c) {
        if (c.bell) {
            tollsLeft = TOLLS;
            tollTimer = 0;
        }
        if (c.announce) {
            broadcastFx(server, new FxPayload(FxPayload.Kind.WITCHING_BANNER, 1));
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (affects(player.level())) {
                Advancements.grant(player, "witching_hour");
            }
        }
    }

    private static void toll(MinecraftServer server, int index) {
        float pitch = index == TOLLS - 1 ? 0.94f : 1.0f;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (affects(player.level())) {
                Feedback.sound(player, ModContent.WITCHING_BELL.get(), SoundSource.AMBIENT, 1.6f, pitch);
            }
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Sync

    public static void syncAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sync(player, false);
        }
    }

    /** Tells a player whether tonight is Hallowed when that changed (or always with {@code force}). */
    public static void sync(ServerPlayer player, boolean force) {
        long night = NightClock.dayIndex(NightClock.time(player.level()));
        boolean hallowed = affects(player.level()) && isHallowed(night);
        long key = hallowed ? night : -night - 1;
        Long previous = SYNCED.get(player.getUUID());
        if (force || previous == null || previous != key) {
            SYNCED.put(player.getUUID(), key);
            Hallowed.platform().sendToPlayer(player, new NightPayload(hallowed, night));
        }
    }

    public static void forget(ServerPlayer player) {
        SYNCED.remove(player.getUUID());
    }

    public static void broadcastFx(MinecraftServer server, FxPayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (affects(player.level())) {
                Hallowed.platform().sendToPlayer(player, payload);
            }
        }
    }
}
