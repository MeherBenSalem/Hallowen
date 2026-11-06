package com.nightbeam.hallowed.client.render;

import com.nightbeam.hallowed.Hallowed;
import net.minecraft.client.model.geom.ModelLayerLocation;

/** The mod's model layers; each loader registers their definitions. */
public final class ModelLayersHallowed {
    public static final ModelLayerLocation SPIRIT = new ModelLayerLocation(Hallowed.id("spirit"), "main");
    public static final ModelLayerLocation HORSEMAN = new ModelLayerLocation(Hallowed.id("hollow_horseman"), "main");

    private ModelLayersHallowed() {
    }
}
