package com.nightbeam.hallowed.trick;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.Entity;

/** Short-lived prank entities (the bats of a Bat Burst): they vanish when their time is up. */
public final class Pranks {
    private static final Map<Entity, Integer> EXPIRING = new WeakHashMap<>();

    private Pranks() {
    }

    public static void expire(Entity entity, int ticks) {
        EXPIRING.put(entity, ticks);
    }

    public static void tick() {
        Iterator<Map.Entry<Entity, Integer>> it = EXPIRING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Entity, Integer> e = it.next();
            Entity entity = e.getKey();
            if (entity.isRemoved()) {
                it.remove();
                continue;
            }
            int left = e.getValue() - 1;
            if (left <= 0) {
                entity.discard();
                it.remove();
            } else {
                e.setValue(left);
            }
        }
    }

    public static void reset() {
        EXPIRING.clear();
    }
}
