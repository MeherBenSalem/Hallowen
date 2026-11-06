package com.nightbeam.hallowed.item;

import com.nightbeam.hallowed.content.ModContent;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The Hollow Horseman's scythe. Right-click to Reap: a full circle that cuts every monster within reach and harvests
 * (and replants) every ripe crop around you. Killing blows heal you (see {@code HallowedEvents}).
 */
public class ReaperScytheItem extends Item {
    public static final double REAP_RADIUS = 3.6;
    public static final int REAP_COOLDOWN = 50;

    public ReaperScytheItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            reap(server, player, stack, hand);
        }
        player.getCooldowns().addCooldown(stack, REAP_COOLDOWN);
        return InteractionResult.SUCCESS;
    }

    private static void reap(ServerLevel level, Player player, ItemStack stack, InteractionHand hand) {
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75f;
        int hits = 0;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(REAP_RADIUS, 1.0, REAP_RADIUS),
                e -> e != player && e.isAlive() && !(e instanceof Player) && !(e instanceof ArmorStand)
                        && !(e instanceof OwnableEntity owned && owned.getOwner() == player)
                        && e.distanceToSqr(player) <= REAP_RADIUS * REAP_RADIUS + 1)) {
            if (target.hurtServer(level, level.damageSources().playerAttack(player), damage)) {
                Vec3 away = target.position().subtract(player.position()).normalize();
                target.knockback(0.45, -away.x, -away.z, level.damageSources().playerAttack(player), 0);
                hits++;
            }
        }
        int crops = 0;
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, -1, -3), center.offset(3, 1, 3))) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                Block.dropResources(state, level, pos, null, player, stack);
                level.setBlock(pos, crop.getStateForAge(0), Block.UPDATE_ALL);
                level.sendParticles(ModContent.EMBER.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.01);
                crops++;
            }
        }
        for (int i = 0; i < 24; i++) {
            float angle = i / 24f * Mth.TWO_PI;
            double x = player.getX() + Mth.cos(angle) * 2.4, z = player.getZ() + Mth.sin(angle) * 2.4;
            level.sendParticles(i % 3 == 0 ? ParticleTypes.SWEEP_ATTACK : ParticleTypes.SOUL_FIRE_FLAME, x, player.getY() + 0.9, z, 1, 0, 0, 0, 0);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModContent.SCYTHE_REAP.get(), SoundSource.PLAYERS, 1.0f,
                0.9f + level.getRandom().nextFloat() * 0.2f);
        if (hits + crops > 0) {
            stack.hurtAndBreak(1 + hits / 3, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
        if (crops >= 9 && player instanceof net.minecraft.server.level.ServerPlayer sp) {
            com.nightbeam.hallowed.reward.Advancements.grant(sp, "grim_harvest");
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("item.hallowed.reaper_scythe.desc").withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("item.hallowed.reaper_scythe.desc2").withStyle(ChatFormatting.DARK_AQUA));
    }
}
