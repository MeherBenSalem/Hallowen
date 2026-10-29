package com.nightbeam.hallowed.item;

import com.nightbeam.hallowed.content.ModContent;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

/** The Hollow Horseman's burning head, worn as a helmet: night vision, and spirits take you for one of their own. */
public class HorsemanHeadItem extends Item {
    public HorsemanHeadItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (slot != EquipmentSlot.HEAD || !(owner instanceof LivingEntity living)) return;
        if (owner.tickCount % 20 == 0) {
            MobEffectInstance vision = living.getEffect(MobEffects.NIGHT_VISION);
            if (vision == null || vision.getDuration() < 240) {
                living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 320, 0, true, false, true));
            }
            living.addEffect(new MobEffectInstance(ModContent.holder(ModContent.SPIRIT_WARD), 60, 0, true, false, true));
        }
        if (owner.tickCount % 6 == 0 && !owner.isInvisible()) {
            level.sendParticles(owner.tickCount % 12 == 0 ? ParticleTypes.SMALL_FLAME : ModContent.EMBER.get(),
                    owner.getX(), owner.getEyeY() + 0.45, owner.getZ(), 1, 0.12, 0.05, 0.12, 0.01);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("item.hallowed.horseman_head.desc").withStyle(ChatFormatting.GRAY));
    }
}
