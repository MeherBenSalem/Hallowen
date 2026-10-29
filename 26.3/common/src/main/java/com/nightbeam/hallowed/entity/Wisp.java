package com.nightbeam.hallowed.entity;

import com.nightbeam.hallowed.block.HallowedCacheBlock;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.night.HallowedNight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A will-o'-the-wisp. It waits in the dark until someone comes close, then drifts away, always a few steps ahead,
 * pausing when its follower falls behind. Where it stops it sinks into the ground and leaves a Hallowed Cache. Cursed
 * wisps (violet) lead into an ambush of spirits, beside a richer cache. Hit it and it is gone.
 */
public class Wisp extends Entity {
    private static final EntityDataAccessor<Boolean> CURSED = SynchedEntityData.defineId(Wisp.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> STATE = SynchedEntityData.defineId(Wisp.class, EntityDataSerializers.BYTE);
    public static final byte IDLE = 0;
    public static final byte LEADING = 1;
    public static final byte WAITING = 2;
    public static final byte ARRIVED = 3;

    private @Nullable BlockPos destination;
    private boolean natural = true;
    private int lonely;
    private int arriveTicks;
    private int hum;
    /** Failed searches for somewhere to lead; the wisp gives up and fades after a few. */
    private int searches;
    private int searchCooldown;
    private double hoverY = Double.NaN;

    public Wisp(EntityType<? extends Wisp> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public static Wisp spawnNear(ServerLevel level, Vec3 at, boolean cursed) {
        Wisp wisp = new Wisp(ModContent.WISP.get(), level);
        wisp.setPos(at.x, at.y, at.z);
        wisp.entityData.set(CURSED, cursed);
        level.addFreshEntity(wisp);
        return wisp;
    }

    public boolean isCursed() {
        return entityData.get(CURSED);
    }

    public byte state() {
        return entityData.get(STATE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CURSED, false);
        builder.define(STATE, IDLE);
    }

    /** Spawn eggs and commands make wisps that do not fade at dawn. */
    public void setNatural(boolean natural) {
        this.natural = natural;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            clientTick();
            return;
        }
        ServerLevel level = (ServerLevel) level();
        // Natural wisps only ever appear on a Hallowed Night: one that wakes up outside one came from an egg or a command.
        if (tickCount == 1 && !HallowedNight.active(level)) natural = false;
        if (natural && !HallowedNight.active(level) && state() != ARRIVED) {
            vanish(level, false);
            return;
        }
        if (Double.isNaN(hoverY)) hoverY = getY();
        Player player = level.getNearestPlayer(this, 64);
        if (player != null && (player.isSpectator() || !player.isAlive())) player = null;
        if (player == null && state() != ARRIVED) {
            if (++lonely > 1200) {
                vanish(level, false);
                return;
            }
            hover(Vec3.ZERO);
            return;
        }
        lonely = 0;
        if (++hum >= 90) {
            hum = random.nextInt(30);
            playSound(ModContent.WISP_AMBIENT.get(), 0.5f, 0.9f + random.nextFloat() * 0.2f);
        }
        switch (state()) {
            case IDLE -> {
                hover(Vec3.ZERO);
                if (distanceTo(player) < 14 && --searchCooldown <= 0) {
                    destination = chooseDestination(level, player);
                    if (destination != null) {
                        entityData.set(STATE, LEADING);
                        playSound(ModContent.WISP_CHIME.get(), 1.0f, 1.0f);
                    } else if (++searches >= 6) {
                        vanish(level, false);
                    } else {
                        searchCooldown = 100;
                    }
                }
            }
            case LEADING, WAITING -> lead(level, player);
            case ARRIVED -> arrive(level, player);
            default -> {
            }
        }
    }

    private void lead(ServerLevel level, Player player) {
        if (destination == null) {
            entityData.set(STATE, IDLE);
            return;
        }
        Vec3 goal = Vec3.atBottomCenterOf(destination);
        double flat = Math.sqrt(Mth.square(goal.x - getX()) + Mth.square(goal.z - getZ()));
        if (flat < 1.2) {
            entityData.set(STATE, ARRIVED);
            arriveTicks = 0;
            playSound(ModContent.WISP_CHIME.get(), 1.0f, 1.25f);
            return;
        }
        double gap = distanceTo(player);
        if (gap > 15) {
            if (state() != WAITING) {
                entityData.set(STATE, WAITING);
                playSound(ModContent.WISP_CHIME.get(), 0.8f, 0.8f);
            } else if (tickCount % 50 == 0) {
                playSound(ModContent.WISP_CHIME.get(), 0.7f, 0.9f);
            }
            hover(Vec3.ZERO);
            return;
        }
        entityData.set(STATE, LEADING);
        double speed = gap < 5 ? 0.24 : gap < 9 ? 0.18 : 0.12;
        Vec3 dir = new Vec3(goal.x - getX(), 0, goal.z - getZ()).normalize().scale(Math.min(speed, flat));
        double ground = groundY(level, Mth.floor(getX() + dir.x * 4), Mth.floor(getZ() + dir.z * 4), hoverY);
        hoverY = Mth.lerp(0.08, hoverY, Math.max(ground + 1.7, Math.min(hoverY, ground + 6)));
        hover(dir);
    }

    private void arrive(ServerLevel level, @Nullable Player player) {
        if (destination == null) {
            vanish(level, false);
            return;
        }
        arriveTicks++;
        Vec3 goal = Vec3.atBottomCenterOf(destination);
        double targetY = goal.y + Mth.lerp(Math.min(1, arriveTicks / 50f), 1.8, 0.3);
        Vec3 move = new Vec3((goal.x - getX()) * 0.2, (targetY - getY()) * 0.25, (goal.z - getZ()) * 0.2);
        setDeltaMovement(move);
        move(MoverType.SELF, getDeltaMovement());
        if (arriveTicks % 6 == 0) {
            level.sendParticles(ModContent.WISP_FLAME.get(), getX(), getY(), getZ(), 4, 0.3, 0.1, 0.3, 0.02);
        }
        if (arriveTicks >= 56) {
            placeCache(level, player);
            vanish(level, true);
        }
    }

    private void hover(Vec3 drift) {
        double bob = Math.sin(tickCount * 0.12) * 0.25;
        double dy = (hoverY + bob - getY()) * 0.12;
        setDeltaMovement(drift.x, dy, drift.z);
        move(MoverType.SELF, getDeltaMovement());
        if (drift.lengthSqr() > 1.0E-4) {
            setYRot((float) (Mth.atan2(drift.z, drift.x) * Mth.RAD_TO_DEG) - 90);
        }
    }

    /** Somewhere on open ground ahead of the wisp, near the player's height; null when there is nowhere fitting. */
    private @Nullable BlockPos chooseDestination(ServerLevel level, Player player) {
        Vec3 away = new Vec3(getX() - player.getX(), 0, getZ() - player.getZ());
        if (away.lengthSqr() < 1.0E-3) away = new Vec3(1, 0, 0);
        double base = Math.atan2(away.z, away.x);
        for (int attempt = 0; attempt < 24; attempt++) {
            double angle = base + (random.nextDouble() - 0.5) * Math.toRadians(attempt < 8 ? 100 : attempt < 16 ? 220 : 360);
            double dist = (attempt < 10 ? 38 : attempt < 18 ? 22 : 10) + random.nextDouble() * (attempt < 18 ? 22 : 12);
            int x = Mth.floor(getX() + Math.cos(angle) * dist);
            int z = Mth.floor(getZ() + Math.sin(angle) * dist);
            if (!level.isLoaded(new BlockPos(x, 0, z))) continue;
            int y = groundY(level, x, z, player.getY());
            if (Math.abs(y - player.getY()) > 20) continue;
            BlockPos pos = new BlockPos(x, y, z);
            BlockState below = level.getBlockState(pos.below());
            if (below.getFluidState().isEmpty() && below.isFaceSturdy(level, pos.below(), Direction.UP) && level.getBlockState(pos).canBeReplaced()) {
                return pos;
            }
        }
        return null;
    }

    /**
     * The first walkable surface in a column: the heightmap under an open sky, otherwise (under a ceiling, as in the
     * Nether) the floor found by scanning down from a little above {@code nearY}.
     */
    private static int groundY(ServerLevel level, int x, int z, double nearY) {
        if (!level.dimensionType().hasCeiling()) {
            return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        }
        int start = Mth.floor(nearY) + 6;
        for (int y = start; y > start - 24 && y > level.getMinY(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
                return y;
            }
        }
        return Mth.floor(nearY);
    }

    private void placeCache(ServerLevel level, @Nullable Player player) {
        BlockPos pos = destination;
        for (int i = 0; i < 4 && pos != null && !level.getBlockState(pos).canBeReplaced(); i++) pos = pos.above();
        if (pos == null || !level.getBlockState(pos).canBeReplaced()) return;
        Direction facing = player != null ? Direction.fromYRot(Mth.atan2(player.getZ() - pos.getZ(), player.getX() - pos.getX()) * Mth.RAD_TO_DEG - 90)
                : Direction.NORTH;
        level.setBlock(pos, ModContent.HALLOWED_CACHE.get().defaultBlockState().setValue(HallowedCacheBlock.FACING, facing)
                .setValue(HallowedCacheBlock.CURSED, isCursed()), Block.UPDATE_ALL);
        level.sendParticles(ModContent.CANDY_SPARKLE.get(), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 20, 0.4, 0.3, 0.4, 0.05);
        if (isCursed()) {
            level.playSound(null, pos, ModContent.SPIRIT_CHARGE.get(), SoundSource.HOSTILE, 1.5f, 0.7f);
            for (int i = 0; i < 3; i++) {
                double angle = i / 3.0 * Math.PI * 2;
                Spirit spirit = ModContent.SPIRIT.get().create(level, EntitySpawnReason.EVENT);
                if (spirit != null) {
                    spirit.setPos(pos.getX() + 0.5 + Math.cos(angle) * 3, pos.getY() + 1.5, pos.getZ() + 0.5 + Math.sin(angle) * 3);
                    spirit.setBound(pos);
                    level.addFreshEntity(spirit);
                    spirit.rise();
                }
            }
        }
    }

    private void vanish(ServerLevel level, boolean quiet) {
        level.sendParticles(isCursed() ? ParticleTypes.WITCH : ModContent.WISP_FLAME.get(), getX(), getY(), getZ(), 16, 0.25, 0.25, 0.25, 0.04);
        if (!quiet) playSound(ModContent.WISP_VANISH.get(), 0.8f, 1.0f);
        else playSound(ModContent.WISP_VANISH.get(), 1.0f, 0.7f);
        discard();
    }

    private void clientTick() {
        Level level = level();
        if (tickCount % 2 == 0) {
            level.addParticle(isCursed() ? ParticleTypes.WITCH : ModContent.WISP_FLAME.get(), getX() + (random.nextDouble() - 0.5) * 0.2,
                    getY() + (random.nextDouble() - 0.5) * 0.2, getZ() + (random.nextDouble() - 0.5) * 0.2, 0, 0.01, 0);
        }
        if (random.nextInt(6) == 0) {
            level.addParticle(ModContent.EMBER.get(), getX(), getY(), getZ(), (random.nextDouble() - 0.5) * 0.02, 0.02, (random.nextDouble() - 0.5) * 0.02);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (isInvulnerableToBase(source)) return false;
        vanish(level, false);
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 96 * 96;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        entityData.set(CURSED, input.getBooleanOr("cursed", false));
        natural = input.getBooleanOr("natural", true);
        hoverY = input.getDoubleOr("hover_y", Double.NaN);
        destination = input.read("destination", BlockPos.CODEC).orElse(null);
        entityData.set(STATE, destination != null ? LEADING : IDLE);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putBoolean("cursed", isCursed());
        output.putBoolean("natural", natural);
        output.putDouble("hover_y", Double.isNaN(hoverY) ? getY() : hoverY);
        output.storeNullable("destination", BlockPos.CODEC, destination);
    }
}
