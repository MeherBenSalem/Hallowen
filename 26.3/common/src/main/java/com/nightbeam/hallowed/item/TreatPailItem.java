package com.nightbeam.hallowed.item;

import com.nightbeam.hallowed.content.ModTags;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * A jack o'lantern pail for trick-or-treating: a bundle that only holds candy. Treats from villagers land in it
 * directly. Held out to a villager on a Hallowed Night it asks "trick or treat?" (see {@code TrickOrTreat}).
 */
public class TreatPailItem extends BundleItem {
    public TreatPailItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction clickAction, Player player) {
        ItemStack other = slot.getItem();
        if (clickAction == ClickAction.PRIMARY && !other.isEmpty() && !other.is(ModTags.CANDY)) {
            player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0f, 1.0f);
            return true;
        }
        return super.overrideStackedOnOther(self, slot, clickAction, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess carriedItem) {
        if (clickAction == ClickAction.PRIMARY && !other.isEmpty() && !other.is(ModTags.CANDY)) {
            player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0f, 1.0f);
            return true;
        }
        return super.overrideOtherStackedOnMe(self, other, slot, clickAction, player, carriedItem);
    }

    /** Puts {@code candy} into the pail; returns what did not fit. */
    public static ItemStack insert(ItemStack pail, ItemStack candy) {
        BundleContents contents = pail.get(DataComponents.BUNDLE_CONTENTS);
        if (contents == null || candy.isEmpty()) return candy;
        BundleContents.Mutable mutable = contents.asMutable();
        ItemStack rest = candy.copy();
        mutable.tryInsert(rest);
        pail.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
        return rest;
    }

    /** Takes one random candy out of the pail (for the Sticky Fingers trick), or an empty stack. */
    public static ItemStack steal(ItemStack pail) {
        BundleContents contents = pail.get(DataComponents.BUNDLE_CONTENTS);
        if (contents == null || contents.isEmpty()) return ItemStack.EMPTY;
        BundleContents.Mutable mutable = contents.asMutable();
        ItemStack taken = mutable.removeOne();
        if (taken == null) return ItemStack.EMPTY;
        ItemStack one = taken.split(1);
        if (!taken.isEmpty()) mutable.tryInsert(taken);
        pail.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
        return one;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        builder.accept(Component.translatable("item.hallowed.treat_pail.desc").withStyle(ChatFormatting.GRAY));
    }
}
