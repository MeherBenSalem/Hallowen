package com.nightbeam.hallowed.entity;

import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.night.HallowedNight;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A spirit: a pale, drifting thing that haunts Hallowed Nights. It flies through walls like a vex, swoops at players
 * and leaves them Spooked, flees from lit jack o'lanterns and fades away at dawn. Spirit Ward (and the Horseman's
 * Head) makes it ignore you.
 */
public class Spirit extends Monster {
    private static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(Spirit.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> RISE = SynchedEntityData.defineId(Spirit.class, EntityDataSerializers.INT);
    public static final int RISE_TICKS = 30;

    private @Nullable BlockPos bound;
    private boolean natural = true;
    private int lifeLimit = -1;
    private int fleeCooldown;

    public Spirit(EntityType<? extends Spirit> type, Level level) {
        super(type, level);
        moveControl = new SpiritMoveControl(this);
        xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.FLYING_SPEED, 0.6);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHARGING, false);
        builder.define(RISE, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FleeLanternGoal());
        goalSelector.addGoal(4, new ChargeGoal());
        goalSelector.addGoal(8, new WanderGoal());
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0f, 1.0f));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, Spirit.class, HollowHorseman.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false, (target, level) -> canHaunt(target)));
    }

    public static boolean canHaunt(LivingEntity target) {
        return !target.hasEffect(ModContent.holder(ModContent.SPIRIT_WARD));
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.EntitySpawnReason reason,
            net.minecraft.world.entity.SpawnGroupData groupData) {
        // Hand-placed spirits (eggs, commands, spawners) keep to their posts instead of fading with the night.
        if (reason != net.minecraft.world.entity.EntitySpawnReason.NATURAL && reason != net.minecraft.world.entity.EntitySpawnReason.EVENT) {
            natural = false;
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    /** Wander around (and return to) this spot, as the guards of a cursed cache do. */
    public void setBound(BlockPos pos) {
        this.bound = pos;
    }

    /** Spirits called by the Horseman: they last a while and do not care for dawn. */
    public void setSummoned(int lifeTicks) {
        natural = false;
        lifeLimit = lifeTicks;
    }

    /** Plays the rising-from-the-ground animation. */
    public void rise() {
        entityData.set(RISE, RISE_TICKS);
    }

    public int riseTicks() {
        return entityData.get(RISE);
    }

    public boolean isCharging() {
        return entityData.get(CHARGING);
    }

    void setCharging(boolean value) {
        entityData.set(CHARGING, value);
    }

    @Override
    public void tick() {
        noPhysics = true;
        super.tick();
        noPhysics = false;
        setNoGravity(true);
        int rise = entityData.get(RISE);
        if (rise > 0) entityData.set(RISE, rise - 1);
        if (level().isClientSide()) {
            if (random.nextInt(3) == 0) {
                level().addParticle(ModContent.SOUL_MOTE.get(), getRandomX(0.6), getY() + random.nextDouble() * 0.9, getRandomZ(0.6),
                        0, -0.01, 0);
            }
            return;
        }
        if (lifeLimit > 0 && --lifeLimit == 0) {
            fade();
            return;
        }
        if (natural && tickCount % 40 == 0 && !HallowedNight.active(level()) && !hasCustomName()) {
            fade();
        }
        if (fleeCooldown > 0) fleeCooldown--;
    }

    /** Dissolves into motes (dawn, or a summon running out). */
    public void fade() {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ModContent.SOUL_MOTE.get(), getX(), getY() + 0.8, getZ(), 20, 0.3, 0.5, 0.3, 0.02);
            server.sendParticles(ParticleTypes.POOF, getX(), getY() + 0.8, getZ(), 6, 0.2, 0.4, 0.2, 0.01);
            playSound(ModContent.SPIRIT_DEATH.get(), 0.6f, 1.4f);
        }
        discard();
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(ModContent.holder(ModContent.SPOOKED), 120, 0, false, false, true), this);
        }
        return hit;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL) || source.is(DamageTypes.DROWN)) return false;
        return super.hurtServer(level, source, damage);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, net.minecraft.world.level.block.state.BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return natural && !hasCustomName();
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModContent.SPIRIT_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModContent.SPIRIT_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModContent.SPIRIT_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        bound = input.read("bound", BlockPos.CODEC).orElse(null);
        natural = input.getBooleanOr("natural", true);
        lifeLimit = input.getIntOr("life", -1);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.storeNullable("bound", BlockPos.CODEC, bound);
        output.putBoolean("natural", natural);
        output.putInt("life", lifeLimit);
    }

    // ----------------------------------------------------------------------------------------------------------------

    private final class ChargeGoal extends Goal {
        ChargeGoal() {
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            return target != null && target.isAlive() && !getMoveControl().hasWanted() && riseTicks() == 0
                    && random.nextInt(reducedTickDelay(9)) == 0 && distanceToSqr(target) > 4.0;
        }

        @Override
        public boolean canContinueToUse() {
            return getMoveControl().hasWanted() && isCharging() && getTarget() != null && getTarget().isAlive();
        }

        @Override
        public void start() {
            LivingEntity target = getTarget();
            if (target != null) {
                Vec3 eye = target.getEyePosition();
                moveControl.setWantedPosition(eye.x, eye.y - 0.3, eye.z, 1.0);
            }
            setCharging(true);
            playSound(ModContent.SPIRIT_CHARGE.get(), 1.0f, 0.9f + random.nextFloat() * 0.2f);
        }

        @Override
        public void stop() {
            setCharging(false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = getTarget();
            if (target == null) return;
            if (getBoundingBox().intersects(target.getBoundingBox()) && level() instanceof ServerLevel server) {
                doHurtTarget(server, target);
                setCharging(false);
                Vec3 back = position().subtract(target.position()).normalize();
                moveControl.setWantedPosition(getX() + back.x * 5, getY() + 2.5, getZ() + back.z * 5, 0.8);
            } else if (distanceToSqr(target) < 9.0) {
                Vec3 eye = target.getEyePosition();
                moveControl.setWantedPosition(eye.x, eye.y - 0.3, eye.z, 1.0);
            }
        }
    }

    private final class WanderGoal extends Goal {
        WanderGoal() {
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !getMoveControl().hasWanted() && random.nextInt(reducedTickDelay(7)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void tick() {
            BlockPos origin = bound != null ? bound : blockPosition();
            for (int i = 0; i < 3; i++) {
                BlockPos pos = origin.offset(random.nextInt(15) - 7, random.nextInt(7) - 2, random.nextInt(15) - 7);
                if (level().isEmptyBlock(pos)) {
                    moveControl.setWantedPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.25);
                    if (getTarget() == null) getLookControl().setLookAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 180, 20);
                    break;
                }
            }
        }
    }

    /** Lit jack o'lanterns frighten spirits: they turn and flee from any within eight blocks. */
    private final class FleeLanternGoal extends Goal {
        private @Nullable BlockPos lantern;

        FleeLanternGoal() {
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (fleeCooldown > 0 || !(level() instanceof ServerLevel server) || tickCount % 10 != 0) return false;
            Optional<BlockPos> near = Wards.nearest(server, blockPosition(), 8);
            lantern = near.orElse(null);
            return lantern != null;
        }

        @Override
        public void start() {
            if (lantern == null) return;
            Vec3 away = position().subtract(Vec3.atCenterOf(lantern));
            away = new Vec3(away.x, 0, away.z).normalize().scale(10);
            moveControl.setWantedPosition(getX() + away.x, getY() + 1.5, getZ() + away.z, 1.1);
            setTarget(null);
            setCharging(false);
            fleeCooldown = 30;
            if (random.nextInt(3) == 0) playSound(ModContent.SPIRIT_HURT.get(), 0.6f, 1.5f);
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    private static final class SpiritMoveControl extends MoveControl<Spirit> {
        SpiritMoveControl(Spirit spirit) {
            super(spirit);
        }

        @Override
        public void tick() {
            if (operation != MoveControl.Operation.MOVE_TO) return;
            Vec3 delta = new Vec3(wantedX - mob.getX(), wantedY - mob.getY(), wantedZ - mob.getZ());
            double length = delta.length();
            if (length < mob.getBoundingBox().getSize()) {
                operation = MoveControl.Operation.WAIT;
                mob.setDeltaMovement(mob.getDeltaMovement().scale(0.5));
                return;
            }
            mob.setDeltaMovement(mob.getDeltaMovement().add(delta.scale(speedModifier * 0.045 / length)));
            Vec3 movement = mob.getDeltaMovement();
            LivingEntity target = mob.getTarget();
            double lx = target != null ? target.getX() - mob.getX() : movement.x;
            double lz = target != null ? target.getZ() - mob.getZ() : movement.z;
            mob.setYRot(-((float) Mth.atan2(lx, lz)) * Mth.RAD_TO_DEG);
            mob.yBodyRot = mob.getYRot();
        }
    }

    @SuppressWarnings("unused")
    private static boolean isMob(Entity e) {
        return e instanceof Mob;
    }
}
