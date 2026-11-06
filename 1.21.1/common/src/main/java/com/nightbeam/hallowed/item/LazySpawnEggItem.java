package com.nightbeam.hallowed.item;

import java.util.function.Supplier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

/**
 * A spawn egg whose entity type is looked up when used, so it does not depend on the order loaders register entity
 * types and items in. The egg's texture is drawn in full colour, so both tint colours are white.
 */
public class LazySpawnEggItem extends SpawnEggItem {
    private final Supplier<? extends EntityType<?>> type;

    public LazySpawnEggItem(Supplier<? extends EntityType<?>> type, Properties properties) {
        super((EntityType<? extends Mob>) null, 0xFFFFFF, 0xFFFFFF, properties);
        this.type = type;
    }

    @Override
    public EntityType<?> getType(ItemStack stack) {
        EntityType<?> fromData = super.getType(stack);
        return fromData != null ? fromData : type.get();
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return type.get().requiredFeatures();
    }
}
