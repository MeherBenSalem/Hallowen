package com.nightbeam.hallowed.item;

import com.nightbeam.hallowed.config.Configs;
import com.nightbeam.hallowed.content.CandyType;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.reward.Advancements;
import com.nightbeam.hallowed.util.Feedback;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Candy helpers: picking random candy, Sugar Rush stacking and the crash that follows an overdose. */
public final class Candy {
    private Candy() {
    }

    /** A random candy, weighted by rarity. */
    public static Item random(RandomSource random) {
        int total = 0;
        for (CandyType type : CandyType.values()) total += type.weight;
        int roll = random.nextInt(total);
        for (CandyType type : CandyType.values()) {
            roll -= type.weight;
            if (roll < 0) return ModContent.candy(type);
        }
        return ModContent.candy(CandyType.CANDY_CORN);
    }

    public static List<ItemStack> randomStacks(RandomSource random, int count) {
        java.util.ArrayList<ItemStack> out = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) out.add(new ItemStack(random(random)));
        return out;
    }

    /**
     * Adds {@code seconds} of Sugar Rush, up to the configured cap. Past half the cap it becomes Sugar Rush II, which
     * ends in a Sugar Crash.
     */
    public static void sugarRush(LivingEntity entity, int seconds) {
        int cap = Configs.common().sugarRushCap * 20;
        MobEffectInstance current = entity.getEffect(ModContent.holder(ModContent.SUGAR_RUSH));
        int duration = Math.min(cap, (current == null ? 0 : current.getDuration()) + seconds * 20);
        int amplifier = duration > cap / 2 || (current != null && current.getAmplifier() > 0) ? 1 : 0;
        if (current != null && current.getAmplifier() > amplifier) amplifier = current.getAmplifier();
        entity.removeEffect(ModContent.holder(ModContent.SUGAR_RUSH));
        entity.addEffect(new MobEffectInstance(ModContent.holder(ModContent.SUGAR_RUSH), duration, amplifier, false, true, true));
    }

    /** Called every tick for players: Sugar Rush II running out turns into a Sugar Crash. */
    public static void tickPlayer(ServerPlayer player) {
        MobEffectInstance rush = player.getEffect(ModContent.holder(ModContent.SUGAR_RUSH));
        if (rush != null && rush.getAmplifier() > 0 && rush.getDuration() <= 1 && Configs.common().sugarCrash) {
            player.removeEffect(ModContent.holder(ModContent.SUGAR_RUSH));
            player.addEffect(new MobEffectInstance(ModContent.holder(ModContent.SUGAR_CRASH), 240, 0, false, true, true));
            player.causeFoodExhaustion(6.0f);
            Feedback.sound(player, ModContent.TRICK.get(), SoundSource.PLAYERS, 0.6f, 0.6f);
            Advancements.grant(player, "sugar_crash");
        }
    }
}
