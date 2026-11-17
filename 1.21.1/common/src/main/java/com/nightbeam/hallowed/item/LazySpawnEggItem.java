package com.nightbeam.hallowed.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * A spawn egg whose entity type is looked up when used, so it does not depend on the order loaders register entity
 * types and items in. The egg's texture is drawn in full colour, so both tint colours are white.
 *
 * <p>Vanilla wires spawn eggs into dispensers and into {@link SpawnEggItem#byId} at bootstrap, before mod items exist,
 * so {@link #finishSetup()} does both once registries are filled.
 */
public class LazySpawnEggItem extends SpawnEggItem {
    private static final List<LazySpawnEggItem> EGGS = new ArrayList<>();

    private final Supplier<? extends EntityType<?>> type;

    public LazySpawnEggItem(Supplier<? extends EntityType<?>> type, Properties properties) {
        super((EntityType<? extends Mob>) null, 0xFFFFFF, 0xFFFFFF, properties);
        this.type = type;
        EGGS.add(this);
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

    /**
     * Registers every Hallowed egg with dispensers and with the entity-type lookup used by pick block. Call once,
     * after entity types and items are registered.
     */
    @SuppressWarnings("unchecked")
    public static void finishSetup() {
        // The super constructor files each egg under a null type; drop that entry and file them under their real types.
        BY_ID.remove(null);
        for (LazySpawnEggItem egg : EGGS) {
            BY_ID.put((EntityType<? extends Mob>) egg.type.get(), egg);
            DispenserBlock.registerBehavior(egg, DISPENSE);
        }
    }

    /** The vanilla spawn egg dispense behaviour, which bootstrap only gives to the eggs that exist at that point. */
    private static final DispenseItemBehavior DISPENSE = new DefaultDispenseItemBehavior() {
        @Override
        public ItemStack execute(BlockSource source, ItemStack stack) {
            Direction face = source.state().getValue(DispenserBlock.FACING);
            EntityType<?> type = ((SpawnEggItem) stack.getItem()).getType(stack);
            try {
                type.spawn(source.level(), stack, null, source.pos().relative(face), MobSpawnType.DISPENSER, face != Direction.UP, false);
            } catch (Exception e) {
                LOGGER.error("Error while dispensing spawn egg from dispenser at {}", source.pos(), e);
                return ItemStack.EMPTY;
            }
            stack.shrink(1);
            source.level().gameEvent(null, GameEvent.ENTITY_PLACE, source.pos());
            return stack;
        }
    };
}
