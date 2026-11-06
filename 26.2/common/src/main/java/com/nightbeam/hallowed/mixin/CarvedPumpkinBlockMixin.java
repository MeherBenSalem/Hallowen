package com.nightbeam.hallowed.mixin;

import com.nightbeam.hallowed.block.Carving;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;

/** Vanilla carved pumpkins and jack o'lanterns can be re-carved with shears and lit in place with a torch. */
@Mixin(CarvedPumpkinBlock.class)
public abstract class CarvedPumpkinBlockMixin extends HorizontalDirectionalBlock {
    protected CarvedPumpkinBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        InteractionResult result = Carving.use(stack, state, level, pos, player, hand, hit);
        return result != null ? result : super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
}
