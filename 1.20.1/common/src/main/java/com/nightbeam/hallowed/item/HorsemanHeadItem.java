package com.nightbeam.hallowed.item;

import com.nightbeam.hallowed.content.ModContent;
import java.util.List;
import javax.annotation.Nullable;
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
import net.minecraft.world.level.Level;

/** The Hollow Horseman's burning head, worn as a helmet: night vision, and spirits take you for one of their own. */
public class HorsemanHeadItem extends HatItem {
    public HorsemanHeadItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity owner, int slotIndex, boolean selected) {
        if (!(world instanceof ServerLevel level) || !(owner instanceof LivingEntity living)) return;
        // Ticked from every slot: only the copy actually worn counts.
        if (living.getItemBySlot(EquipmentSlot.HEAD) != stack) return;
        tickWorn(level, living);
    }

    /** The worn effects; players reach this from {@link #inventoryTick}, the Horseman and armor stands do not tick it. */
    public static void tickWorn(ServerLevel level, LivingEntity owner) {
        if (owner.tickCount % 20 == 0) {
            MobEffectInstance vision = owner.getEffect(MobEffects.NIGHT_VISION);
            if (vision == null || vision.getDuration() < 240) {
                owner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 320, 0, true, false, true));
            }
            owner.addEffect(new MobEffectInstance(ModContent.holder(ModContent.SPIRIT_WARD), 60, 0, true, false, true));
        }
        if (owner.tickCount % 6 == 0 && !owner.isInvisible()) {
            level.sendParticles(owner.tickCount % 12 == 0 ? ParticleTypes.SMALL_FLAME : ModContent.EMBER.get(),
                    owner.getX(), owner.getEyeY() + 0.45, owner.getZ(), 1, 0.12, 0.05, 0.12, 0.01);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hallowed.horseman_head.desc").withStyle(ChatFormatting.GRAY));
    }
}
