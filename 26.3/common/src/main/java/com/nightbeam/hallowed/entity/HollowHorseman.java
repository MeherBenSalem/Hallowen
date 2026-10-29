package com.nightbeam.hallowed.entity;

import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.config.Configs;
import com.nightbeam.hallowed.config.HallowedConfig;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.item.Candy;
import com.nightbeam.hallowed.network.FxPayload;
import com.nightbeam.hallowed.night.HallowedNight;
import com.nightbeam.hallowed.night.NightClock;
import com.nightbeam.hallowed.reward.Advancements;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Hollow Horseman, who rides out of a Cursed Pumpkin at the Witching Hour.
 *
 * <p>Mounted, he circles his prey on a burning steed, charges straight through anything in his way (bait the charge
 * into a wall and he is stunned), lobs flaming jack o'lanterns and cuts with his scythe. Below half health the steed
 * falls and he fights on foot: faster swings, three-bomb volleys, spirits called from the ground, and his own head
 * hurled as a homing bomb, during which his headless body takes extra damage. He rides away at dawn if he has not
 * fallen, and when he falls he bursts into candy.
 */
public class HollowHorseman extends Monster {
    private static final EntityDataAccessor<Boolean> MOUNTED = SynchedEntityData.defineId(HollowHorseman.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HEADLESS = SynchedEntityData.defineId(HollowHorseman.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> ACTION = SynchedEntityData.defineId(HollowHorseman.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> ARRIVAL = SynchedEntityData.defineId(HollowHorseman.class, EntityDataSerializers.INT);

    public static final byte NONE = 0;
    public static final byte SWING = 1;
    public static final byte THROW = 2;
    public static final byte WINDUP = 3;
    public static final byte CHARGE = 4;
    public static final byte CALL = 5;
    public static final byte FALL = 6;
    public static final byte HEAD_TOSS = 7;
    public static final byte STUNNED = 8;
    public static final int ARRIVAL_TICKS = 60;

    private static final EntityDimensions MOUNTED_SIZE = EntityDimensions.scalable(1.4f, 2.9f).withEyeHeight(2.6f);
    private static final EntityDimensions FOOT_SIZE = EntityDimensions.scalable(0.75f, 2.35f).withEyeHeight(2.05f);

    private final ServerBossEvent bossEvent = new ServerBossEvent(Mth.createInsecureUUID(random),
            Component.translatable("entity.hallowed.hollow_horseman"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);

    private int actionTicks;
    private int bombCooldown = 60;
    private int chargeCooldown = 80;
    private int callCooldown = 160;
    private int headCooldown = 120;
    private int laughCooldown = 200;
    private Vec3 chargeDir = Vec3.ZERO;
    private final Set<UUID> chargeHits = new HashSet<>();
    private float circleAngle;
    private int circleDir = 1;
    private Vec3 lastPos = Vec3.ZERO;
    private int stuckTicks;
    private boolean configured;
    /** He only rides away at dawn after a night: one summoned by day (an egg, a command) stays until the next one. */
    private boolean sawNight;
    private int headlessTicks;

    /** Client: when the current action started, for animations. */
    private int clientActionStart;

    public HollowHorseman(EntityType<? extends HollowHorseman> type, Level level) {
        super(type, level);
        xpReward = 250;
        setPersistenceRequired();
        if (!level.isClientSide()) {
            setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModContent.HORSEMAN_HEAD.get()));
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModContent.REAPER_SCYTHE.get()));
            setDropChance(EquipmentSlot.HEAD, 0);
            setDropChance(EquipmentSlot.MAINHAND, 0);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 320.0)
                .add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.STEP_HEIGHT, 1.6);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Summoning

    /** Raises a Horseman from a Cursed Pumpkin at {@code pos}. */
    public static void summon(ServerLevel level, BlockPos pos, Direction facing) {
        HollowHorseman horseman = ModContent.HOLLOW_HORSEMAN.get().create(level, EntitySpawnReason.TRIGGERED);
        if (horseman == null) return;
        horseman.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, facing.toYRot(), 0);
        horseman.yHeadRot = horseman.yBodyRot = facing.toYRot();
        horseman.entityData.set(ARRIVAL, ARRIVAL_TICKS);
        level.addFreshEntity(horseman);
        level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 80, 0.8, 1.2, 0.8, 0.08);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 40, 1.2, 0.4, 1.2, 0.05);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 1.0, 0.6, 1.0, 0.03);
        level.playSound(null, pos, ModContent.HORSEMAN_SUMMON.get(), SoundSource.HOSTILE, 4.0f, 1.0f);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(horseman) < 96 * 96) {
                Hallowed.platform().sendToPlayer(player, new FxPayload(FxPayload.Kind.HORSEMAN_BANNER, 1));
                Hallowed.platform().sendToPlayer(player, new FxPayload(FxPayload.Kind.SHAKE, 0.7f));
            }
        }
    }

    public static boolean anyNear(ServerLevel level, BlockPos pos, double radius) {
        return !level.getEntitiesOfClass(HollowHorseman.class, new AABB(pos).inflate(radius), Entity::isAlive).isEmpty();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // State

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MOUNTED, true);
        builder.define(HEADLESS, false);
        builder.define(ACTION, NONE);
        builder.define(ARRIVAL, 0);
    }

    public boolean isMounted() {
        return entityData.get(MOUNTED);
    }

    public boolean isHeadless() {
        return entityData.get(HEADLESS);
    }

    public byte action() {
        return entityData.get(ACTION);
    }

    public int arrivalTicks() {
        return entityData.get(ARRIVAL);
    }

    /** Ticks (with partial) since the current action started, on the client. */
    public float actionAge(float partialTick) {
        return tickCount - clientActionStart + partialTick;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (ACTION.equals(accessor)) clientActionStart = tickCount;
        if (MOUNTED.equals(accessor)) refreshDimensions();
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return isMounted() ? MOUNTED_SIZE : FOOT_SIZE;
    }

    private void setAction(byte action) {
        entityData.set(ACTION, action);
        actionTicks = 0;
    }

    /** The thrown head burst: it comes back to his shoulders. */
    public void headReturned() {
        entityData.set(HEADLESS, false);
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModContent.HORSEMAN_HEAD.get()));
        headCooldown = 160 + random.nextInt(80);
        if (level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.FLAME, getX(), getY() + getBbHeight() - 0.2, getZ(), 20, 0.2, 0.2, 0.2, 0.05);
        }
    }

    private boolean phaseTwo() {
        return !isMounted();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Ticking

    @Override
    protected void registerGoals() {
        targetSelector.addGoal(1, new HurtByTargetGoal(this, Spirit.class, HollowHorseman.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            clientTick();
            return;
        }
        int arrival = entityData.get(ARRIVAL);
        if (arrival > 0) {
            entityData.set(ARRIVAL, arrival - 1);
            if (arrival % 10 == 0 && level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.2, getZ(), 12, 0.7, 0.2, 0.7, 0.03);
            }
            if (arrival == 1) laugh(1.0f);
        }
        bossEvent.setProgress(getHealth() / getMaxHealth());
        if (tickCount % 10 == 0 && isAlive()) ensureEquipment();
    }

    /** He always carries his scythe, and wears his head unless it is flying at someone. */
    private void ensureEquipment() {
        if (!isHeadless() && getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModContent.HORSEMAN_HEAD.get()));
            setDropChance(EquipmentSlot.HEAD, 0);
        }
        if (getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModContent.REAPER_SCYTHE.get()));
            setDropChance(EquipmentSlot.MAINHAND, 0);
        }
    }

    private void clientTick() {
        Level level = level();
        float top = getBbHeight();
        if (isHeadless()) {
            level.addParticle(ParticleTypes.FLAME, getX(), getY() + top - 0.45, getZ(), (random.nextDouble() - 0.5) * 0.03, 0.06, (random.nextDouble() - 0.5) * 0.03);
        } else if (random.nextInt(3) == 0) {
            level.addParticle(random.nextBoolean() ? ParticleTypes.SMALL_FLAME : ModContent.EMBER.get(), getX(), getY() + top + 0.1, getZ(),
                    (random.nextDouble() - 0.5) * 0.02, 0.03, (random.nextDouble() - 0.5) * 0.02);
        }
        if (isMounted() && getDeltaMovement().horizontalDistanceSqr() > 0.02) {
            for (int i = 0; i < (action() == CHARGE ? 4 : 1); i++) {
                level.addParticle(action() == CHARGE ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME,
                        getRandomX(0.8), getY() + 0.1, getRandomZ(0.8), 0, 0.02, 0);
            }
        }
        if (action() == STUNNED && tickCount % 3 == 0) {
            float a = tickCount * 0.35f;
            level.addParticle(ParticleTypes.CRIT, getX() + Mth.cos(a) * 0.6, getY() + top + 0.3, getZ() + Mth.sin(a) * 0.6, 0, 0, 0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        configure();
        if (arrivalTicks() > 0) {
            getNavigation().stop();
            return;
        }
        HallowedConfig c = Configs.common();
        if (tickCount % 20 == 0 && HallowedNight.affects(level)) {
            if (!dawn(level)) {
                sawNight = true;
            } else if (c.horsemanLeavesAtDawn && sawNight) {
                rideAway(level);
                return;
            }
        }
        if (isHeadless() && ++headlessTicks > 140) {
            headReturned();
        } else if (!isHeadless()) {
            headlessTicks = 0;
        }
        tickCooldowns();
        if (isMounted() && action() != FALL && getHealth() < getMaxHealth() * 0.5f) {
            setAction(FALL);
            getNavigation().stop();
        }
        LivingEntity target = getTarget();
        if (action() != NONE) {
            tickAction(level, target);
            return;
        }
        if (target == null || !target.isAlive()) {
            if (tickCount % 80 == 0) {
                Vec3 wander = position().add((random.nextDouble() - 0.5) * 16, 0, (random.nextDouble() - 0.5) * 16);
                getNavigation().moveTo(wander.x, wander.y, wander.z, 0.6);
            }
            return;
        }
        getLookControl().setLookAt(target, 30, 30);
        if (--laughCooldown <= 0) laugh(1.0f);
        antiStuck(level, target);
        double dist = distanceTo(target);
        if (isMounted()) {
            if (chargeCooldown <= 0 && dist > 6 && dist < 30 && hasLineOfSight(target)) {
                setAction(WINDUP);
                getNavigation().stop();
            } else if (dist < 3.8) {
                setAction(SWING);
            } else if (bombCooldown <= 0 && dist > 5) {
                setAction(THROW);
            } else {
                circle(target);
            }
        } else {
            if (headCooldown <= 0 && !isHeadless() && dist > 4 && hasLineOfSight(target)) {
                setAction(HEAD_TOSS);
            } else if (callCooldown <= 0 && countSpirits(level) < 4) {
                setAction(CALL);
            } else if (dist < 3.6) {
                setAction(SWING);
            } else if (bombCooldown <= 0 && dist > 6) {
                setAction(THROW);
            } else if (tickCount % 5 == 0) {
                getNavigation().moveTo(target, 1.15);
            }
        }
    }

    private void tickCooldowns() {
        if (bombCooldown > 0) bombCooldown--;
        if (chargeCooldown > 0) chargeCooldown--;
        if (callCooldown > 0) callCooldown--;
        if (headCooldown > 0 && !isHeadless()) headCooldown--;
    }

    private void tickAction(ServerLevel level, LivingEntity target) {
        actionTicks++;
        byte action = action();
        switch (action) {
            case SWING -> {
                getNavigation().stop();
                if (target != null) getLookControl().setLookAt(target, 60, 30);
                if (actionTicks == 1) swing(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.component.SwingAnimation(net.minecraft.world.item.SwingAnimationType.WHACK, 10), false);
                if (actionTicks == 7) swing(level);
                if (actionTicks >= (phaseTwo() ? 11 : 14)) setAction(NONE);
            }
            case THROW -> {
                getNavigation().stop();
                if (target != null) getLookControl().setLookAt(target, 60, 30);
                if (actionTicks == 9 && target != null) throwBombs(level, target);
                if (actionTicks >= 16) {
                    bombCooldown = phaseTwo() ? 90 + random.nextInt(40) : 70 + random.nextInt(50);
                    setAction(NONE);
                }
            }
            case WINDUP -> {
                getNavigation().stop();
                setDeltaMovement(getDeltaMovement().multiply(0.5, 1, 0.5));
                if (target != null) getLookControl().setLookAt(target, 90, 30);
                if (actionTicks == 2) playSound(ModContent.HORSEMAN_CHARGE.get(), 2.5f, 1.0f);
                if (actionTicks >= 18) {
                    Vec3 aim = target != null ? target.position().add(target.getDeltaMovement().scale(6)) : position().add(getLookAngle());
                    chargeDir = new Vec3(aim.x - getX(), 0, aim.z - getZ()).normalize();
                    setYRot((float) (Mth.atan2(chargeDir.z, chargeDir.x) * Mth.RAD_TO_DEG) - 90);
                    yBodyRot = yHeadRot = getYRot();
                    chargeHits.clear();
                    setAction(CHARGE);
                    shakeNear(level, 16, 0.35f);
                }
            }
            case CHARGE -> charge(level);
            case STUNNED -> {
                getNavigation().stop();
                if (actionTicks >= 50) {
                    chargeCooldown = 140;
                    setAction(NONE);
                }
            }
            case CALL -> {
                getNavigation().stop();
                if (actionTicks == 12) callSpirits(level, 2 + random.nextInt(2));
                if (actionTicks >= 24) {
                    callCooldown = 360 + random.nextInt(160);
                    setAction(NONE);
                }
            }
            case HEAD_TOSS -> {
                getNavigation().stop();
                if (target != null) getLookControl().setLookAt(target, 60, 30);
                if (actionTicks == 8 && target != null) {
                    entityData.set(HEADLESS, true);
                    setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    PumpkinBomb head = PumpkinBomb.head(level, this, target, damage() * 1.1f);
                    Vec3 dir = target.getEyePosition().subtract(getEyePosition()).normalize().scale(0.5);
                    head.setDeltaMovement(dir.add(0, 0.25, 0));
                    level.addFreshEntity(head);
                    playSound(ModContent.PUMPKIN_BOMB_THROW.get(), 2.0f, 0.7f);
                    laugh(1.2f);
                }
                if (actionTicks >= 14) setAction(NONE);
            }
            case FALL -> fall(level);
            default -> setAction(NONE);
        }
    }

    private void circle(LivingEntity target) {
        if (tickCount % 8 != 0) return;
        if (random.nextInt(12) == 0) circleDir = -circleDir;
        circleAngle += circleDir * 0.45f;
        double radius = 9 + Math.sin(tickCount * 0.02) * 2;
        double x = target.getX() + Math.cos(circleAngle) * radius;
        double z = target.getZ() + Math.sin(circleAngle) * radius;
        getNavigation().moveTo(x, target.getY(), z, 1.25);
    }

    private void swing(ServerLevel level) {
        playSound(ModContent.HORSEMAN_SWING.get(), 1.6f, 0.9f + random.nextFloat() * 0.2f);
        Vec3 look = Vec3.directionFromRotation(0, getYRot());
        double reach = isMounted() ? 4.2 : 3.8;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(reach, 1.0, reach),
                e -> e != this && e.isAlive() && !ally(e))) {
            Vec3 to = e.position().subtract(position());
            Vec3 flat = new Vec3(to.x, 0, to.z);
            if (flat.length() > reach + e.getBbWidth() / 2) continue;
            if (flat.lengthSqr() > 0.5 && flat.normalize().dot(look) < 0.2) continue;
            if (doHurtTarget(level, e)) {
                e.knockback(0.8, -look.x, -look.z, damageSources().mobAttack(this), 0);
            }
        }
        for (int i = -2; i <= 2; i++) {
            Vec3 p = position().add(Vec3.directionFromRotation(0, getYRot() + i * 22).scale(2.2));
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, p.x, getY() + (isMounted() ? 1.9 : 1.2), p.z, 1, 0, 0, 0, 0);
        }
    }

    /** Monsters are on his side: his swings, charges and bombs pass them by (and they leave him alone). */
    static boolean ally(Entity e) {
        return e instanceof net.minecraft.world.entity.monster.Enemy && !(e instanceof Player);
    }

    private void throwBombs(ServerLevel level, LivingEntity target) {
        int count = phaseTwo() ? 3 : 1;
        for (int i = 0; i < count; i++) {
            Vec3 aim = target.position().add(target.getDeltaMovement().scale(18));
            if (i > 0) aim = aim.add((random.nextDouble() - 0.5) * 7, 0, (random.nextDouble() - 0.5) * 7);
            PumpkinBomb bomb = PumpkinBomb.bomb(level, this, damage() * 0.85f);
            bomb.lobAt(aim);
            level.addFreshEntity(bomb);
        }
        playSound(ModContent.PUMPKIN_BOMB_THROW.get(), 1.8f, 0.9f + random.nextFloat() * 0.2f);
    }

    private void charge(ServerLevel level) {
        if (actionTicks == 1) playSound(ModContent.HORSEMAN_CHARGE.get(), 3.0f, 1.15f);
        double speed = 0.95;
        setDeltaMovement(chargeDir.x * speed, getDeltaMovement().y, chargeDir.z * speed);
        setYRot((float) (Mth.atan2(chargeDir.z, chargeDir.x) * Mth.RAD_TO_DEG) - 90);
        yBodyRot = yHeadRot = getYRot();
        AABB box = getBoundingBox().inflate(0.6, 0.2, 0.6);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this && e.isAlive() && !ally(e) && !chargeHits.contains(e.getUUID()))) {
            chargeHits.add(e.getUUID());
            if (e.hurtServer(level, damageSources().mobAttack(this), damage() * 1.35f)) {
                e.knockback(1.6, -chargeDir.x, -chargeDir.z, damageSources().mobAttack(this), 0);
                e.push(0, 0.45, 0);
                if (e instanceof ServerPlayer player) {
                    Hallowed.platform().sendToPlayer(player, new FxPayload(FxPayload.Kind.SHAKE, 0.9f));
                }
            }
        }
        if (tickCount % 2 == 0) {
            level.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.2, getZ(), 4, 0.5, 0.1, 0.5, 0.01);
        }
        if (horizontalCollision && actionTicks > 3) {
            playSound(ModContent.HORSEMAN_HURT.get(), 2.0f, 0.6f);
            level.sendParticles(ParticleTypes.EXPLOSION, getX() + chargeDir.x, getY() + 1, getZ() + chargeDir.z, 1, 0, 0, 0, 0);
            shakeNear(level, 12, 0.6f);
            setDeltaMovement(-chargeDir.x * 0.4, 0.2, -chargeDir.z * 0.4);
            setAction(STUNNED);
            return;
        }
        if (actionTicks >= 30) {
            chargeCooldown = 110 + random.nextInt(80);
            setAction(NONE);
        }
    }

    private void fall(ServerLevel level) {
        getNavigation().stop();
        setDeltaMovement(getDeltaMovement().multiply(0.3, 1, 0.3));
        if (actionTicks == 1) {
            playSound(ModContent.HORSEMAN_DISMOUNT.get(), 3.0f, 1.0f);
            shakeNear(level, 24, 0.8f);
        }
        if (actionTicks < 22 && actionTicks % 3 == 0) {
            level.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.8, getZ(), 18, 0.8, 0.5, 0.8, 0.05);
        }
        if (actionTicks == 22) {
            entityData.set(MOUNTED, false);
            refreshDimensions();
            level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.6, getZ(), 30, 0.9, 0.4, 0.9, 0.03);
            level.sendParticles(ModContent.EMBER.get(), getX(), getY() + 0.6, getZ(), 50, 1.0, 0.5, 1.0, 0.1);
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.31);
            getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.7);
        }
        if (actionTicks == 32) {
            laugh(0.85f);
            callSpirits(level, 3);
        }
        if (actionTicks >= 46) {
            callCooldown = 400;
            headCooldown = 60;
            bombCooldown = 40;
            setAction(NONE);
        }
    }

    private void callSpirits(ServerLevel level, int count) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            Spirit spirit = ModContent.SPIRIT.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (spirit == null) continue;
            double x = getX() + Math.cos(angle) * 3, z = getZ() + Math.sin(angle) * 3;
            spirit.snapTo(x, getY() + 0.5, z, random.nextFloat() * 360, 0);
            spirit.setSummoned(900);
            if (getTarget() != null) spirit.setTarget(getTarget());
            level.addFreshEntity(spirit);
            spirit.rise();
            level.sendParticles(ParticleTypes.SOUL, x, getY() + 0.3, z, 10, 0.3, 0.2, 0.3, 0.02);
        }
        playSound(ModContent.SPIRIT_CHARGE.get(), 2.0f, 0.6f);
    }

    private int countSpirits(ServerLevel level) {
        return level.getEntitiesOfClass(Spirit.class, getBoundingBox().inflate(32), Entity::isAlive).size();
    }

    private void antiStuck(ServerLevel level, LivingEntity target) {
        if (tickCount % 20 != 0) return;
        if (position().distanceToSqr(lastPos) < 1.5 && distanceTo(target) > 10) {
            stuckTicks += 20;
        } else {
            stuckTicks = 0;
        }
        lastPos = position();
        if (stuckTicks >= 120) {
            stuckTicks = 0;
            for (int i = 0; i < 10; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                int x = Mth.floor(target.getX() + Math.cos(angle) * 7);
                int z = Mth.floor(target.getZ() + Math.sin(angle) * 7);
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                if (Math.abs(y - target.getY()) < 6) {
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1, getZ(), 30, 0.6, 1.0, 0.6, 0.02);
                    teleportTo(x + 0.5, y, z + 0.5);
                    level.sendParticles(ParticleTypes.FLAME, x + 0.5, y + 0.5, z + 0.5, 30, 0.6, 0.8, 0.6, 0.04);
                    laugh(1.1f);
                    break;
                }
            }
        }
    }

    private static boolean dawn(ServerLevel level) {
        int tod = NightClock.timeOfDay(NightClock.time(level));
        return tod >= NightClock.DAWN || tod < NightClock.DUSK;
    }

    private void rideAway(ServerLevel level) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1, getZ(), 50, 0.8, 1.2, 0.8, 0.03);
        level.sendParticles(ParticleTypes.FLAME, getX(), getY() + 1, getZ(), 40, 0.8, 1.2, 0.8, 0.05);
        laugh(0.9f);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) < 96 * 96) {
                Hallowed.platform().sendToPlayer(player, new FxPayload(FxPayload.Kind.HORSEMAN_LEFT, 1));
            }
        }
        discard();
    }

    private void laugh(float pitch) {
        laughCooldown = 260 + random.nextInt(200);
        playSound(ModContent.HORSEMAN_LAUGH.get(), 3.0f, pitch);
    }

    private float damage() {
        return (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    private void shakeNear(ServerLevel level, double radius, float strength) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) < radius * radius) {
                Hallowed.platform().sendToPlayer(player, new FxPayload(FxPayload.Kind.SHAKE, strength));
            }
        }
    }

    /** Applies the configured stats once, the first time he thinks. */
    private void configure() {
        if (configured) return;
        configured = true;
        HallowedConfig c = Configs.common();
        boolean fresh = getHealth() >= getMaxHealth() - 0.01f;
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(c.horsemanHealth);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(c.horsemanDamage);
        getAttribute(Attributes.ARMOR).setBaseValue(c.horsemanArmor);
        if (!isMounted()) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.31);
            getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.7);
        }
        if (fresh) setHealth(getMaxHealth());
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Damage and death

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (arrivalTicks() > 0 || action() == FALL) return false;
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.DROWN) || source.is(DamageTypes.FALL)) return false;
        if (source.getEntity() instanceof HollowHorseman || source.getEntity() instanceof Spirit) return false;
        if (isHeadless()) damage *= 1.5f;
        if (action() == STUNNED) damage *= 1.35f;
        return super.hurtServer(level, source, damage);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living && random.nextInt(3) == 0) {
            living.addEffect(new MobEffectInstance(ModContent.holder(ModContent.SPOOKED), 100, 0), this);
        }
        return hit;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!(level() instanceof ServerLevel level)) return;
        level.playSound(null, getX(), getY(), getZ(), ModContent.HORSEMAN_DEATH.get(), SoundSource.HOSTILE, 4.0f, 1.0f);
        level.sendParticles(ModContent.CANDY_SPARKLE.get(), getX(), getY() + 1.5, getZ(), 80, 1.0, 1.0, 1.0, 0.25);
        level.sendParticles(ModContent.EMBER.get(), getX(), getY() + 1.5, getZ(), 60, 1.0, 1.0, 1.0, 0.15);
        for (ItemStack candy : Candy.randomStacks(random, 22 + random.nextInt(8))) {
            ItemEntity item = new ItemEntity(level, getX(), getY() + 1.8, getZ(), candy);
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 0.15 + random.nextDouble() * 0.3;
            item.setDeltaMovement(Math.cos(angle) * speed, 0.35 + random.nextDouble() * 0.35, Math.sin(angle) * speed);
            item.setDefaultPickUpDelay();
            level.addFreshEntity(item);
        }
        for (Spirit spirit : level.getEntitiesOfClass(Spirit.class, getBoundingBox().inflate(48), Entity::isAlive)) {
            spirit.fade();
        }
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) < 64 * 64) {
                Hallowed.platform().sendToPlayer(player, new FxPayload(FxPayload.Kind.HORSEMAN_FELLED, 1));
                Advancements.grant(player, "headless_no_more");
            }
        }
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected boolean canRide(Entity vehicle) {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public boolean canUsePortal(boolean ignorePassenger) {
        return false;
    }

    @Override
    public void checkDespawn() {
        if (level().getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) discard();
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModContent.HORSEMAN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModContent.HORSEMAN_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.0f;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("mounted", isMounted());
        output.putBoolean("saw_night", sawNight);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        entityData.set(MOUNTED, input.getBooleanOr("mounted", true));
        sawNight = input.getBooleanOr("saw_night", false);
        refreshDimensions();
    }
}
