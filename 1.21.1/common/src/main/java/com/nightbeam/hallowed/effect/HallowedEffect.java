package com.nightbeam.hallowed.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * The mod's status effects. Their behaviour lives where it is used: Sugar Rush's attributes here, the crash that
 * follows it in {@code Candy}, Spooked's heartbeat on the client and Spirit Ward in the spirits' targeting.
 */
public class HallowedEffect extends MobEffect {
    public HallowedEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
