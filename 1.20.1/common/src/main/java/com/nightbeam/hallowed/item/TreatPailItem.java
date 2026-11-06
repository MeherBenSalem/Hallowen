package com.nightbeam.hallowed.item;

import com.nightbeam.hallowed.content.ModTags;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * A jack o'lantern pail for trick-or-treating: a bundle that only holds candy. Treats from villagers land in it
 * directly. Held out to a villager on a Hallowed Night it asks "trick or treat?" (see {@code TrickOrTreat}).
 */
public class TreatPailItem extends BundleItem {
    private static final String ITEMS = "Items";
    private static final int CAPACITY = 64;

    public TreatPailItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction clickAction, Player player) {
        ItemStack other = slot.getItem();
        if (clickAction == ClickAction.SECONDARY && !other.isEmpty() && !other.is(ModTags.CANDY)) {
            player.playSound(SoundEvents.BUNDLE_INSERT, 1.0f, 0.5f);
            return true;
        }
        return super.overrideStackedOnOther(self, slot, clickAction, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess carriedItem) {
        if (clickAction == ClickAction.SECONDARY && !other.isEmpty() && !other.is(ModTags.CANDY)) {
            player.playSound(SoundEvents.BUNDLE_INSERT, 1.0f, 0.5f);
            return true;
        }
        return super.overrideOtherStackedOnMe(self, other, slot, clickAction, player, carriedItem);
    }

    /** Whether the pail holds anything. */
    public static boolean hasContents(ItemStack pail) {
        CompoundTag tag = pail.getTag();
        return tag != null && !tag.getList(ITEMS, Tag.TAG_COMPOUND).isEmpty();
    }

    /** Puts {@code candy} into the pail (the same {@code Items} list a bundle keeps); returns what did not fit. */
    public static ItemStack insert(ItemStack pail, ItemStack candy) {
        if (candy.isEmpty() || !(pail.getItem() instanceof TreatPailItem)) return candy;
        ItemStack rest = candy.copy();
        CompoundTag tag = pail.getOrCreateTag();
        if (!tag.contains(ITEMS)) tag.put(ITEMS, new ListTag());
        ListTag list = tag.getList(ITEMS, Tag.TAG_COMPOUND);
        int weight = 0;
        for (int i = 0; i < list.size(); i++) {
            ItemStack inside = ItemStack.of(list.getCompound(i));
            weight += weight(inside) * inside.getCount();
        }
        int fits = Math.min(rest.getCount(), (CAPACITY - weight) / weight(rest));
        if (fits <= 0) return rest;
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ItemStack inside = ItemStack.of(entry);
            if (ItemStack.isSameItemSameTags(inside, rest) && inside.getCount() < inside.getMaxStackSize()) {
                int add = Math.min(fits, inside.getMaxStackSize() - inside.getCount());
                inside.grow(add);
                list.remove(i);
                list.add(0, inside.save(new CompoundTag()));
                rest.shrink(add);
                fits -= add;
                break;
            }
        }
        if (fits > 0) {
            list.add(0, rest.copyWithCount(fits).save(new CompoundTag()));
            rest.shrink(fits);
        }
        return rest;
    }

    /** Takes one candy out of the pail (for the Sticky Fingers trick), or an empty stack. */
    public static ItemStack steal(ItemStack pail) {
        CompoundTag tag = pail.getTag();
        if (tag == null) return ItemStack.EMPTY;
        ListTag list = tag.getList(ITEMS, Tag.TAG_COMPOUND);
        if (list.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = ItemStack.of(list.getCompound(0));
        list.remove(0);
        ItemStack one = taken.split(1);
        if (!taken.isEmpty()) list.add(0, taken.save(new CompoundTag()));
        if (list.isEmpty()) pail.removeTagKey(ITEMS);
        return one;
    }

    private static int weight(ItemStack stack) {
        return Math.max(1, 64 / Math.max(1, stack.getMaxStackSize()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.hallowed.treat_pail.desc").withStyle(ChatFormatting.GRAY));
    }
}
