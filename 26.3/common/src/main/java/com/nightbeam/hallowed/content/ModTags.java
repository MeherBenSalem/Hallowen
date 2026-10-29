package com.nightbeam.hallowed.content;

import com.nightbeam.hallowed.Hallowed;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public final class ModTags {
    /** Everything the Treat Pail accepts. */
    public static final TagKey<Item> CANDY = TagKey.create(Registries.ITEM, Hallowed.id("candy"));
    /** Head items that count as a costume when trick-or-treating. */
    public static final TagKey<Item> COSTUMES = TagKey.create(Registries.ITEM, Hallowed.id("costumes"));
    /** Monsters that may spawn wearing a carved pumpkin on a Hallowed Night. */
    public static final TagKey<EntityType<?>> COSTUME_WEARERS = TagKey.create(Registries.ENTITY_TYPE, Hallowed.id("costume_wearers"));
    /** Monsters that never drop Hallowed Night candy. */
    public static final TagKey<EntityType<?>> NO_CANDY = TagKey.create(Registries.ENTITY_TYPE, Hallowed.id("no_candy"));

    private ModTags() {
    }
}
