package com.nightbeam.hallowed.entity;

import com.nightbeam.hallowed.config.Configs;
import com.nightbeam.hallowed.content.Face;
import com.nightbeam.hallowed.content.ModContent;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Hollow Horseman's flaming jack o'lantern. As a bomb it arcs and bursts where it lands; as his own head it flies
 * without gravity, hunting its target for a few seconds before it bursts and goes back to his shoulders. Neither
 * breaks blocks.
 */
public class PumpkinBomb extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> HEAD = SynchedEntityData.defineId(PumpkinBomb.class, EntityDataSerializers.BOOLEAN);
    private static final float RADIUS = 3.2f;

    private @Nullable Entity target;
    private int life;
    private float damage = 9;

    public PumpkinBomb(EntityType<? extends PumpkinBomb> type, Level level) {
        super(type, level);
    }

    public static PumpkinBomb bomb(Level level, LivingEntity owner, float damage) {
        PumpkinBomb bomb = new PumpkinBomb(ModContent.PUMPKIN_BOMB.get(), level);
        bomb.setOwner(owner);
        bomb.setPos(owner.getX(), owner.getEyeY() - 0.2, owner.getZ());
        Face face = Face.byIndex(1 + level.getRandom().nextInt(Face.COUNT - 1));
        bomb.setItem(ModContent.faceStack(ModContent.JACK_O_LANTERN_ITEM.get(), face));
        bomb.damage = damage;
        return bomb;
    }

    public static PumpkinBomb head(Level level, LivingEntity owner, Entity target, float damage) {
        PumpkinBomb head = new PumpkinBomb(ModContent.PUMPKIN_BOMB.get(), level);
        head.setOwner(owner);
        head.setPos(owner.getX(), owner.getEyeY(), owner.getZ());
        head.setItem(new ItemStack(ModContent.HORSEMAN_HEAD.get()));
        head.entityData.set(HEAD, true);
        head.target = target;
        head.damage = damage;
        return head;
    }

    /** Lobs the bomb so it lands on {@code at}, with a higher arc for farther throws. */
    public void lobAt(Vec3 at) {
        Vec3 from = position();
        double dx = at.x - from.x, dz = at.z - from.z;
        double flat = Math.sqrt(dx * dx + dz * dz);
        double flight = Mth.clamp(flat / 0.75, 14, 38);
        double g = getDefaultGravity();
        double vy = (at.y - from.y + 0.5 * g * flight * flight) / flight;
        setDeltaMovement(dx / flight, vy, dz / flight);
    }

    public boolean isHead() {
        return entityData.get(HEAD);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HEAD, false);
    }

    @Override
    protected Item getDefaultItem() {
        return ModContent.JACK_O_LANTERN_ITEM.get();
    }

    @Override
    protected double getDefaultGravity() {
        return isHead() ? 0 : 0.05;
    }

    @Override
    public void tick() {
        super.tick();
        if (isRemoved()) return;
        Level level = level();
        if (level.isClientSide()) {
            Vec3 v = getDeltaMovement();
            level.addParticle(ParticleTypes.FLAME, getX() - v.x * 0.5, getY() + 0.3 - v.y * 0.5, getZ() - v.z * 0.5, 0, 0.01, 0);
            if (tickCount % 2 == 0) level.addParticle(ModContent.EMBER.get(), getX(), getY() + 0.3, getZ(), 0, 0.02, 0);
            if (isHead()) level.addParticle(ParticleTypes.SMALL_FLAME, getX(), getY() + 0.6, getZ(), 0, 0.04, 0);
            return;
        }
        life++;
        if (isHead()) {
            if (target != null && target.isAlive() && life < 70) {
                Vec3 to = target.getEyePosition().subtract(position()).normalize().scale(0.55);
                setDeltaMovement(getDeltaMovement().scale(0.86).add(to.scale(0.14)).normalize().scale(0.55));
            }
            if (life > 90) burst();
        } else if (life > 200) {
            burst();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity instanceof HollowHorseman || entity instanceof Spirit || entity instanceof PumpkinBomb) return false;
        return super.canHitEntity(entity);
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!level().isClientSide()) burst();
    }

    private void burst() {
        if (!(level() instanceof ServerLevel level) || isRemoved()) return;
        Entity owner = getOwner();
        List<LivingEntity> hit = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(RADIUS),
                e -> e.isAlive() && !HollowHorseman.ally(e) && e.distanceToSqr(this) < RADIUS * RADIUS);
        for (LivingEntity e : hit) {
            float falloff = 1 - (float) Math.sqrt(e.distanceToSqr(this)) / (RADIUS + 0.5f);
            if (e.hurtServer(level, damageSources().explosion(this, owner), damage * Math.max(0.35f, falloff))) {
                e.igniteForSeconds(3);
                Vec3 push = e.position().subtract(position()).normalize();
                e.push(push.x * 0.6, 0.35, push.z * 0.6);
            }
        }
        level.sendParticles(ModContent.EMBER.get(), getX(), getY() + 0.3, getZ(), 36, 0.5, 0.4, 0.5, 0.12);
        level.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.3, getZ(), 24, 0.6, 0.3, 0.6, 0.06);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.3, getZ(), 10, 0.4, 0.2, 0.4, 0.02);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PUMPKIN.defaultBlockState()), getX(), getY() + 0.3, getZ(),
                30, 0.4, 0.3, 0.4, 0.15);
        level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.3, getZ(), 1, 0, 0, 0, 0);
        level.playSound(null, getX(), getY(), getZ(), ModContent.PUMPKIN_BOMB_EXPLODE.get(), SoundSource.HOSTILE, 1.4f,
                0.9f + random.nextFloat() * 0.2f);
        if (Configs.common().horsemanFire && level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            for (int i = 0; i < 4; i++) {
                BlockPos pos = BlockPos.containing(getX() + random.nextInt(5) - 2, getY(), getZ() + random.nextInt(5) - 2);
                if (level.getBlockState(pos).isAir() && BaseFireBlock.canBePlacedAt(level, pos, net.minecraft.core.Direction.UP)) {
                    level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
                }
            }
        }
        if (isHead() && owner instanceof HollowHorseman horseman) {
            horseman.headReturned();
        }
        discard();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("head", isHead());
        output.putFloat("damage", damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        entityData.set(HEAD, input.getBooleanOr("head", false));
        damage = input.getFloatOr("damage", 9);
    }
}
