package com.nightbeam.hallowed.client;

import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.content.Face;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.item.CarvedFaceItem;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Item model properties that stand in for newer versions' item model definitions: a carved pumpkin's face
 * ({@code hallowed:face}), whether the Treat Pail holds anything ({@code hallowed:filled}) and whether the Witch Hat is
 * being drawn on a head ({@code hallowed:worn}).
 */
public final class HallowedItemModels {
    /** The display context of the item being rendered through {@code ItemRenderer#renderStatic}, if any. */
    private static @Nullable ItemDisplayContext rendering;

    private HallowedItemModels() {
    }

    public interface Sink {
        void register(Item item, ResourceLocation id, ClampedItemPropertyFunction function);
    }

    public static void register(Sink sink) {
        ClampedItemPropertyFunction face = (stack, level, entity, seed) -> {
            Face stored = CarvedFaceItem.storedFace(stack);
            return (stored != null ? stored : Face.GRIN).ordinal() / 10f;
        };
        sink.register(ModContent.CARVED_PUMPKIN_ITEM.get(), Hallowed.id("face"), face);
        sink.register(ModContent.JACK_O_LANTERN_ITEM.get(), Hallowed.id("face"), face);
        sink.register(ModContent.SOUL_JACK_O_LANTERN_ITEM.get(), Hallowed.id("face"), face);
        sink.register(ModContent.TREAT_PAIL.get(), Hallowed.id("filled"), (stack, level, entity, seed) -> BundleItem.getFullnessDisplay(stack));
        sink.register(ModContent.WITCH_HAT.get(), Hallowed.id("worn"), (stack, level, entity, seed) -> rendering == ItemDisplayContext.HEAD ? 1 : 0);
    }

    public static void beginRender(ItemDisplayContext context) {
        rendering = context;
    }

    public static void endRender() {
        rendering = null;
    }
}
