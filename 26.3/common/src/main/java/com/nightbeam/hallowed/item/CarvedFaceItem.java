package com.nightbeam.hallowed.item;

import com.nightbeam.hallowed.block.CarvedFaceBlock;
import com.nightbeam.hallowed.content.Face;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

/** A carved pumpkin or jack o'lantern that remembers its face (in the block state component). */
public class CarvedFaceItem extends BlockItem {
    public CarvedFaceItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    public static Face face(ItemStack stack) {
        BlockItemStateProperties props = stack.get(DataComponents.BLOCK_STATE);
        if (props != null) {
            Integer value = props.get(CarvedFaceBlock.FACE);
            if (value != null) return Face.byIndex(value);
        }
        return Face.CLASSIC;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("face.hallowed." + face(stack).id).withStyle(ChatFormatting.GOLD));
        builder.accept(Component.translatable("block.hallowed.carved_pumpkin.desc").withStyle(ChatFormatting.GRAY));
    }
}
