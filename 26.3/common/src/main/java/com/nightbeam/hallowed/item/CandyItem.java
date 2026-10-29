package com.nightbeam.hallowed.item;

import com.nightbeam.hallowed.content.CandyType;
import com.nightbeam.hallowed.content.ModContent;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** One of the eight candies. Eating it applies its effect on top of the food. */
public class CandyItem extends Item {
    private static final List<Pick> GOOD_HEXES = List.of(
            new Pick(MobEffects.SPEED, 400, 1), new Pick(MobEffects.JUMP_BOOST, 400, 1), new Pick(MobEffects.HASTE, 600, 1),
            new Pick(MobEffects.NIGHT_VISION, 900, 0), new Pick(MobEffects.REGENERATION, 200, 0), new Pick(MobEffects.LUCK, 1200, 0),
            new Pick(MobEffects.WATER_BREATHING, 900, 0), new Pick(MobEffects.FIRE_RESISTANCE, 600, 0));
    private static final List<Pick> MISCHIEF_HEXES = List.of(
            new Pick(MobEffects.LEVITATION, 60, 0), new Pick(MobEffects.NAUSEA, 140, 0), new Pick(MobEffects.GLOWING, 400, 0),
            new Pick(MobEffects.SLOWNESS, 160, 0));

    private final CandyType type;

    public CandyItem(CandyType type, Item.Properties properties) {
        super(properties.food(new FoodProperties.Builder().nutrition(type.nutrition).saturationModifier(type.saturation).alwaysEdible().build(),
                Consumables.defaultFood().consumeSeconds(type.eatSeconds)
                        .sound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModContent.CANDY_EAT.get())).build()));
        this.type = type;
    }

    public CandyType type() {
        return type;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel server) {
            apply(server, entity);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    private void apply(ServerLevel level, LivingEntity entity) {
        RandomSource random = entity.getRandom();
        switch (type) {
            case CANDY_CORN -> Candy.sugarRush(entity, 8);
            case CARAMEL_APPLE -> {
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 0));
                Candy.sugarRush(entity, 4);
            }
            case GHOST_GUMMY -> {
                entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 400, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 400, 0));
                level.sendParticles(ModContent.SOUL_MOTE.get(), entity.getX(), entity.getY() + 0.4, entity.getZ(), 24, 0.45, 0.3, 0.45, 0.02);
            }
            case PUMPKIN_TRUFFLE -> {
                entity.removeEffect(ModContent.holder(ModContent.SPOOKED));
                entity.removeEffect(ModContent.holder(ModContent.SUGAR_CRASH));
                entity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 400, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0));
            }
            case WITCH_LOLLIPOP -> {
                List<Pick> pool = random.nextFloat() < 0.75f ? GOOD_HEXES : MISCHIEF_HEXES;
                Pick pick = pool.get(random.nextInt(pool.size()));
                entity.addEffect(new MobEffectInstance(pick.effect, pick.duration, pick.amplifier));
                level.sendParticles(ParticleTypes.WITCH, entity.getX(), entity.getY() + 0.5, entity.getZ(), 18, 0.5, 0.3, 0.5, 0.05);
            }
            case BAT_WING_LICORICE -> {
                entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
                for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(24),
                        e -> e != entity && e.isAlive() && (e instanceof Enemy))) {
                    other.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                }
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BAT_TAKEOFF, SoundSource.PLAYERS, 0.8f, 1.6f);
            }
            case EYEBALL_JAWBREAKER -> entity.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 900, 0));
            case SOUL_TOFFEE -> {
                entity.addEffect(new MobEffectInstance(ModContent.holder(ModContent.SPIRIT_WARD), 2400, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0));
                level.sendParticles(ParticleTypes.SOUL, entity.getX(), entity.getY() + 0.4, entity.getZ(), 12, 0.5, 0.3, 0.5, 0.02);
            }
        }
        level.sendParticles(ModContent.CANDY_SPARKLE.get(), entity.getX(), entity.getY() + 0.3, entity.getZ(), 6, 0.4, 0.15, 0.4, 0.0);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("item.hallowed." + type.id + ".desc").withStyle(ChatFormatting.GRAY));
    }

    private record Pick(Holder<MobEffect> effect, int duration, int amplifier) {
    }
}
