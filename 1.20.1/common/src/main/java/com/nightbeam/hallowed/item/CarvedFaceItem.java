package com.nightbeam.hallowed.item;

import com.nightbeam.hallowed.block.CarvedFaceBlock;
import com.nightbeam.hallowed.content.Face;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** A carved pumpkin or jack o'lantern that remembers its face (in the {@code BlockStateTag}, applied on placement). */
public class CarvedFaceItem extends BlockItem {
    public CarvedFaceItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    /** The face stored on the stack, or null when it has none (it then places with the block's default face). */
    public static @Nullable Face storedFace(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(BLOCK_STATE_TAG, CompoundTag.TAG_COMPOUND)) return null;
        CompoundTag state = tag.getCompound(BLOCK_STATE_TAG);
        String key = CarvedFaceBlock.FACE.getName();
        if (!state.contains(key)) return null;
        return CarvedFaceBlock.FACE.getValue(state.getString(key)).map(Face::byIndex).orElse(null);
    }

    public static Face face(ItemStack stack) {
        Face face = storedFace(stack);
        return face != null ? face : Face.CLASSIC;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("face.hallowed." + face(stack).id).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("block.hallowed.carved_pumpkin.desc").withStyle(ChatFormatting.GRAY));
    }
}
