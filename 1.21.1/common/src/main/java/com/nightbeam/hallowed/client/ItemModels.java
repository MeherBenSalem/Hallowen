package com.nightbeam.hallowed.client;

import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.block.CarvedFaceBlock;
import com.nightbeam.hallowed.content.Face;
import com.nightbeam.hallowed.content.ModContent;
import java.util.function.Function;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BlockItemStateProperties;
import org.jetbrains.annotations.Nullable;

/**
 * Item model switches: which face a carved pumpkin item shows ({@code hallowed:face}), whether the Treat Pail has
 * candy in it ({@code hallowed:filled}), and the 3D witch hat drawn when the hat is worn.
 */
public final class ItemModels {
    /** The witch hat as worn: a standalone model each loader loads alongside the item models. */
    public static final ResourceLocation WITCH_HAT_WORN = Hallowed.id("item/witch_hat_worn");
    /** Looks up a standalone baked model; set by each loader's client entry point. */
    public static Function<ResourceLocation, @Nullable BakedModel> lookup = id -> null;

    private ItemModels() {
    }

    /** Registers the item properties; call once on the client after items are registered. */
    public static void registerProperties() {
        for (Item item : new Item[]{ModContent.CARVED_PUMPKIN_ITEM.get(), ModContent.JACK_O_LANTERN_ITEM.get(),
                ModContent.SOUL_JACK_O_LANTERN_ITEM.get()}) {
            ItemProperties.register(item, Hallowed.id("face"), (stack, level, entity, seed) -> {
                BlockItemStateProperties props = stack.get(DataComponents.BLOCK_STATE);
                Integer face = props != null ? props.get(CarvedFaceBlock.FACE) : null;
                // Like the block, an item without a face shows the grin.
                return (face != null ? Face.byIndex(face).ordinal() : Face.GRIN.ordinal()) / (float) (Face.COUNT - 1);
            });
        }
        ItemProperties.register(ModContent.TREAT_PAIL.get(), Hallowed.id("filled"),
                (stack, level, entity, seed) -> BundleItem.getFullnessDisplay(stack));
    }

    public static @Nullable BakedModel wornHat() {
        return lookup.apply(WITCH_HAT_WORN);
    }
}
