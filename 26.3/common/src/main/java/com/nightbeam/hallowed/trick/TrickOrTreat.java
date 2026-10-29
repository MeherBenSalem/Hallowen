package com.nightbeam.hallowed.trick;

import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.config.Configs;
import com.nightbeam.hallowed.config.HallowedConfig;
import com.nightbeam.hallowed.content.CandyType;
import com.nightbeam.hallowed.content.Face;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.content.ModTags;
import com.nightbeam.hallowed.item.Candy;
import com.nightbeam.hallowed.item.TreatPailItem;
import com.nightbeam.hallowed.network.FxPayload;
import com.nightbeam.hallowed.night.HallowedNight;
import com.nightbeam.hallowed.night.NightClock;
import com.nightbeam.hallowed.reward.Advancements;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Trick-or-treating: hold a Treat Pail out to a villager on a Hallowed Night. Each villager answers once per night,
 * with a treat (candy straight into the pail) or, less often, a harmless trick. A costume on your head makes treats
 * likelier. Wandering traders always treat, and generously.
 */
public final class TrickOrTreat {
    private static final String PRANK_TAG = "hallowed_prank_until";
    private static final Map<UUID, Long> ANSWERED = new HashMap<>();
    private static final Map<UUID, Integer> TREATED = new HashMap<>();

    private TrickOrTreat() {
    }

    public static void reset() {
        ANSWERED.clear();
        TREATED.clear();
    }

    /** Called before a villager's own interaction. Returns a result to stop it, or null to carry on as normal. */
    public static @Nullable InteractionResult interact(Player player, AbstractVillager villager, InteractionHand hand) {
        ItemStack pail = player.getItemInHand(hand);
        if (!pail.is(ModContent.TREAT_PAIL.get()) || !Configs.common().trickOrTreat) return null;
        if (villager.isBaby() || villager.isSleeping()) return null;
        if (!(player.level() instanceof ServerLevel level) || !(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
        if (!HallowedNight.active(level)) {
            sp.sendOverlayMessage(Component.translatable("trick.hallowed.not_tonight").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            villager.setUnhappyCounter(20);
            return InteractionResult.SUCCESS;
        }
        long night = NightClock.dayIndex(NightClock.time(level));
        Long answered = ANSWERED.get(villager.getUUID());
        if (answered != null && answered == night) {
            sp.sendOverlayMessage(Component.translatable("trick.hallowed.already").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            villager.setUnhappyCounter(20);
            return InteractionResult.SUCCESS;
        }
        if (ANSWERED.size() > 1024) ANSWERED.values().removeIf(n -> n < night);
        ANSWERED.put(villager.getUUID(), night);
        villager.getLookControl().setLookAt(player);
        RandomSource random = level.getRandom();
        HallowedConfig c = Configs.common();
        boolean costume = wearingCostume(player);
        boolean trader = villager instanceof WanderingTrader;
        double chance = trader ? 1.0 : costume ? c.costumeTreatChance : c.treatChance;
        if (random.nextDouble() < chance || !trick(level, sp, villager, pail, random)) {
            treat(level, sp, villager, pail, random, costume, trader);
        }
        return InteractionResult.SUCCESS;
    }

    public static boolean wearingCostume(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).is(ModTags.COSTUMES);
    }

    private static void treat(ServerLevel level, ServerPlayer player, AbstractVillager villager, ItemStack pail, RandomSource random,
            boolean costume, boolean trader) {
        int count = 1 + random.nextInt(2) + (costume ? 1 : 0) + (trader ? 2 : 0);
        List<ItemStack> candy = new ArrayList<>(Candy.randomStacks(random, count));
        if (trader) candy.add(new ItemStack(ModContent.candy(CandyType.SOUL_TOFFEE)));
        boolean spilled = false;
        for (ItemStack stack : candy) {
            ItemStack rest = TreatPailItem.insert(pail, stack);
            if (!rest.isEmpty()) {
                spilled = true;
                if (!player.getInventory().add(rest)) player.drop(rest, false, net.minecraft.util.Prediction.SERVER_ONLY);
            }
        }
        Vec3 head = villager.getEyePosition();
        level.sendParticles(ModContent.CANDY_SPARKLE.get(), head.x, head.y + 0.3, head.z, 14, 0.3, 0.2, 0.3, 0.08);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, head.x, head.y + 0.4, head.z, 6, 0.3, 0.2, 0.3, 0);
        level.playSound(null, villager.getX(), villager.getY(), villager.getZ(), ModContent.TREAT.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        level.playSound(null, villager.getX(), villager.getY(), villager.getZ(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.8f, 1.1f);
        Component what = Component.translatable(costume ? "trick.hallowed.treat.costume" : "trick.hallowed.treat", candy.size());
        player.sendOverlayMessage(what.copy().withStyle(ChatFormatting.GOLD));
        if (spilled) player.sendSystemMessage(Component.translatable("trick.hallowed.pail_full").withStyle(ChatFormatting.GRAY));
        Hallowed.platform().sendToPlayer(player, new FxPayload(FxPayload.Kind.TREAT, 1));
        Advancements.grant(player, "trick_or_treat");
        int total = TREATED.merge(player.getUUID(), 1, Integer::sum);
        if (total >= 10) Advancements.grant(player, "neighbourhood_round");
        if (costume) Advancements.grant(player, "dressed_for_the_occasion");
    }

    /** Plays a random enabled trick; false when none could play (the villager treats instead). */
    private static boolean trick(ServerLevel level, ServerPlayer player, AbstractVillager villager, ItemStack pail, RandomSource random) {
        HallowedConfig c = Configs.common();
        List<Runnable> tricks = new ArrayList<>();
        if (c.trickBats) tricks.add(() -> batBurst(level, villager));
        if (c.trickPumpkin && player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) tricks.add(() -> pumpkinHead(level, player));
        if (c.trickLevitate) tricks.add(() -> {
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 1), villager);
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 160, 0), villager);
            level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 12, 0.3, 0.1, 0.3, 0.02);
        });
        if (c.trickSpook) tricks.add(() -> {
            player.addEffect(new MobEffectInstance(ModContent.holder(ModContent.SPOOKED), 160, 0, false, false, true), villager);
            Hallowed.platform().sendToPlayer(player, new FxPayload(FxPayload.Kind.SPOOK, 1));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModContent.SPOOK.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        });
        if (c.trickVanish) tricks.add(() -> vanish(level, villager, random));
        if (c.trickSteal && pail.has(DataComponents.BUNDLE_CONTENTS) && !pail.get(DataComponents.BUNDLE_CONTENTS).isEmpty()) {
            tricks.add(() -> {
                ItemStack taken = TreatPailItem.steal(pail);
                if (!taken.isEmpty()) {
                    level.sendParticles(ParticleTypes.SMOKE, villager.getX(), villager.getEyeY(), villager.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
                    player.sendSystemMessage(Component.translatable("trick.hallowed.stolen", taken.getHoverName()).withStyle(ChatFormatting.GRAY));
                }
            });
        }
        if (tricks.isEmpty()) return false;
        tricks.get(random.nextInt(tricks.size())).run();
        level.playSound(null, villager.getX(), villager.getY(), villager.getZ(), ModContent.TRICK.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        player.sendOverlayMessage(Component.translatable("trick.hallowed.trick").withStyle(ChatFormatting.DARK_PURPLE));
        Hallowed.platform().sendToPlayer(player, new FxPayload(FxPayload.Kind.TRICK, 1));
        Advancements.grant(player, "tricked");
        return true;
    }

    private static void batBurst(ServerLevel level, AbstractVillager villager) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE, villager.getX(), villager.getEyeY(), villager.getZ(), 16, 0.3, 0.3, 0.3, 0.03);
        for (int i = 0; i < 5; i++) {
            Bat bat = EntityTypes.BAT.create(level, EntitySpawnReason.EVENT);
            if (bat == null) continue;
            bat.snapTo(villager.getX(), villager.getEyeY(), villager.getZ(), level.getRandom().nextFloat() * 360, 0);
            bat.setResting(false);
            Pranks.expire(bat, 200);
            level.addFreshEntity(bat);
        }
        level.playSound(null, villager.getX(), villager.getY(), villager.getZ(), SoundEvents.BAT_TAKEOFF, SoundSource.NEUTRAL, 1.0f, 1.0f);
    }

    private static void pumpkinHead(ServerLevel level, ServerPlayer player) {
        Face face = Face.byIndex(1 + level.getRandom().nextInt(Face.COUNT - 1));
        ItemStack pumpkin = ModContent.faceStack(ModContent.CARVED_PUMPKIN_ITEM.get(), face);
        CompoundTag tag = new CompoundTag();
        tag.putLong(PRANK_TAG, level.getGameTime() + 400);
        pumpkin.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        player.setItemSlot(EquipmentSlot.HEAD, pumpkin);
        level.sendParticles(ParticleTypes.POOF, player.getX(), player.getEyeY() + 0.3, player.getZ(), 10, 0.2, 0.2, 0.2, 0.02);
    }

    private static void vanish(ServerLevel level, AbstractVillager villager, RandomSource random) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE, villager.getX(), villager.getY() + 1, villager.getZ(), 24, 0.3, 0.6, 0.3, 0.02);
        for (int i = 0; i < 10; i++) {
            double x = villager.getX() + (random.nextDouble() - 0.5) * 16;
            double z = villager.getZ() + (random.nextDouble() - 0.5) * 16;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(x), (int) Math.floor(z));
            if (Math.abs(y - villager.getY()) < 4 && level.getBlockState(BlockPos.containing(x, y - 1, z)).isSolid()) {
                villager.teleportTo(x, y, z);
                break;
            }
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE, villager.getX(), villager.getY() + 1, villager.getZ(), 24, 0.3, 0.6, 0.3, 0.02);
        level.playSound(null, villager.getX(), villager.getY(), villager.getZ(), SoundEvents.WITCH_CELEBRATE, SoundSource.NEUTRAL, 0.8f, 1.6f);
    }

    /** Removes prank pumpkins whose time is up (checked every second for each player). */
    public static void tickPlayer(ServerPlayer player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        CustomData data = head.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        CompoundTag tag = data.copyTag();
        if (!tag.contains(PRANK_TAG)) return;
        if (player.level().getGameTime() >= tag.getLongOr(PRANK_TAG, 0)) {
            player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            if (player.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.POOF, player.getX(), player.getEyeY() + 0.3, player.getZ(), 10, 0.2, 0.2, 0.2, 0.02);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), ModContent.CARVE.get(), SoundSource.PLAYERS, 0.8f, 1.4f);
            }
        }
    }
}
