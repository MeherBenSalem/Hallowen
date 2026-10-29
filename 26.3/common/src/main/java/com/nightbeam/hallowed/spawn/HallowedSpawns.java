package com.nightbeam.hallowed.spawn;

import com.nightbeam.hallowed.config.Configs;
import com.nightbeam.hallowed.config.HallowedConfig;
import com.nightbeam.hallowed.content.Face;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.content.ModTags;
import com.nightbeam.hallowed.entity.Spirit;
import com.nightbeam.hallowed.entity.Wards;
import com.nightbeam.hallowed.entity.Wisp;
import com.nightbeam.hallowed.night.HallowedNight;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * What a Hallowed Night brings: spirits drifting in from the dark (most of all in the Witching Hour), wisps that
 * wait to be followed, and costumed monsters. Spirits and wisps are spawned here around each player, like phantoms,
 * rather than through biome spawn lists.
 */
public final class HallowedSpawns {
    private static final Map<UUID, int[]> TIMERS = new HashMap<>();

    private HallowedSpawns() {
    }

    public static void reset() {
        TIMERS.clear();
    }

    /** Runs once a second. */
    public static void tick(MinecraftServer server) {
        HallowedConfig c = Configs.common();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator() || !(player.level() instanceof ServerLevel level) || !HallowedNight.active(level)) continue;
            int[] timers = TIMERS.computeIfAbsent(player.getUUID(), id -> new int[]{10, 40});
            boolean witching = HallowedNight.witchingHour(level);
            if (c.spiritsEnabled && level.getDifficulty() != Difficulty.PEACEFUL && --timers[0] <= 0) {
                timers[0] = witching ? Math.max(4, c.spiritInterval / 2) : c.spiritInterval;
                spawnSpirits(level, player, witching ? c.spiritsPerPlayer : Math.max(1, c.spiritsPerPlayer / 2), witching ? 2 : 1);
            }
            if (c.wispsEnabled && --timers[1] <= 0) {
                timers[1] = c.wispInterval;
                if (level.getRandom().nextDouble() < c.wispChance) spawnWisp(level, player, c);
            }
        }
    }

    private static void spawnSpirits(ServerLevel level, ServerPlayer player, int max, int count) {
        int near = level.getEntitiesOfClass(Spirit.class, player.getBoundingBox().inflate(48), Entity::isAlive).size();
        RandomSource random = level.getRandom();
        for (int i = 0; i < count && near < max; i++) {
            BlockPos pos = surfaceSpot(level, player, 18, 34, random);
            if (pos == null || Wards.warded(level, pos)) continue;
            Spirit spirit = ModContent.SPIRIT.get().create(level, EntitySpawnReason.NATURAL);
            if (spirit == null) continue;
            spirit.snapTo(pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, random.nextFloat() * 360, 0);
            level.addFreshEntity(spirit);
            spirit.rise();
            near++;
        }
    }

    private static void spawnWisp(ServerLevel level, ServerPlayer player, HallowedConfig c) {
        if (!level.getEntitiesOfClass(Wisp.class, player.getBoundingBox().inflate(80), Entity::isAlive).isEmpty()) return;
        BlockPos pos = surfaceSpot(level, player, 20, 32, level.getRandom());
        if (pos == null) return;
        Wisp.spawnNear(level, new Vec3(pos.getX() + 0.5, pos.getY() + 1.6, pos.getZ() + 0.5), level.getRandom().nextDouble() < c.cursedWispChance);
    }

    /** A spot under the open sky, {@code min}-{@code max} blocks from the player, near their height. */
    private static BlockPos surfaceSpot(ServerLevel level, ServerPlayer player, int min, int max, RandomSource random) {
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = min + random.nextDouble() * (max - min);
            int x = Mth.floor(player.getX() + Math.cos(angle) * dist);
            int z = Mth.floor(player.getZ() + Math.sin(angle) * dist);
            BlockPos column = new BlockPos(x, 0, z);
            if (!level.isLoaded(column)) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (Math.abs(y - player.getY()) > 24) continue;
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos).isAir() && level.getFluidState(pos.below()).isEmpty() && level.canSeeSky(pos)) return pos;
        }
        return null;
    }

    /** Called when a mob finishes spawning: on a Hallowed Night some monsters come in costume. */
    public static void onFinalizeSpawn(Mob mob, ServerLevel level, EntitySpawnReason reason) {
        if (!mob.is(ModTags.COSTUME_WEARERS) || !mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) return;
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.SPAWNER && reason != EntitySpawnReason.STRUCTURE
                && reason != EntitySpawnReason.CHUNK_GENERATION && reason != EntitySpawnReason.REINFORCEMENT) return;
        if (!HallowedNight.active(level) || level.getRandom().nextDouble() >= Configs.common().costumedChance) return;
        RandomSource random = level.getRandom();
        Face face = Face.byIndex(1 + random.nextInt(Face.COUNT - 1));
        int pick = random.nextInt(10);
        ItemStack head = ModContent.faceStack(pick < 5 ? ModContent.JACK_O_LANTERN_ITEM.get()
                : pick < 8 ? ModContent.CARVED_PUMPKIN_ITEM.get() : ModContent.SOUL_JACK_O_LANTERN_ITEM.get(), face);
        mob.setItemSlot(EquipmentSlot.HEAD, head);
        mob.setDropChance(EquipmentSlot.HEAD, 0.1f);
    }

    public static boolean costumed(Mob mob) {
        ItemStack head = mob.getItemBySlot(EquipmentSlot.HEAD);
        return head.is(ModContent.CARVED_PUMPKIN_ITEM.get()) || head.is(ModContent.JACK_O_LANTERN_ITEM.get())
                || head.is(ModContent.SOUL_JACK_O_LANTERN_ITEM.get()) || head.is(net.minecraft.world.item.Items.CARVED_PUMPKIN)
                || head.is(net.minecraft.world.item.Items.JACK_O_LANTERN);
    }
}
