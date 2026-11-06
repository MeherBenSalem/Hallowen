package com.nightbeam.hallowed.item;

import com.nightbeam.hallowed.block.CarvedFaceBlock;
import com.nightbeam.hallowed.content.Face;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;

/** A carved pumpkin or jack o'lantern that remembers its face (in the block state component). */
public class CarvedFaceItem extends BlockItem implements Equipable {
    public CarvedFaceItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    /** Worn on the head like a vanilla carved pumpkin (shift-click or a dispenser; right-click places it). */
    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> builder, TooltipFlag flag) {
        builder.add(Component.translatable("face.hallowed." + face(stack).id).withStyle(ChatFormatting.GOLD));
        builder.add(Component.translatable("block.hallowed.carved_pumpkin.desc").withStyle(ChatFormatting.GRAY));
    }
}
