package com.nightbeam.hallowed.entity;

import com.nightbeam.hallowed.config.Configs;
import com.nightbeam.hallowed.content.ModContent;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;

/** Lit jack o'lanterns as spirit wards, found through their point of interest. */
public final class Wards {
    private Wards() {
    }

    /** The nearest lantern within {@code radius}, if any. */
    public static Optional<BlockPos> nearest(ServerLevel level, BlockPos pos, int radius) {
        if (radius <= 0) return Optional.empty();
        return level.getPoiManager().findClosest(holder -> holder.is(ModContent.LANTERN_WARD_KEY), pos, radius, PoiManager.Occupancy.ANY);
    }

    /** True when a lantern keeps spirits from spawning at {@code pos}: soul lanterns reach farther. */
    public static boolean warded(ServerLevel level, BlockPos pos) {
        int normal = Configs.common().wardRadius;
        int soul = Configs.common().soulWardRadius;
        Optional<BlockPos> near = nearest(level, pos, Math.max(normal, soul));
        if (near.isEmpty()) return false;
        BlockPos lantern = near.get();
        double dist = Math.sqrt(lantern.distSqr(pos));
        boolean isSoul = level.getBlockState(lantern).is(ModContent.SOUL_JACK_O_LANTERN.get());
        return dist <= (isSoul ? soul : normal);
    }
}
