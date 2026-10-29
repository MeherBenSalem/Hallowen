package com.nightbeam.hallowed.reward;

import com.nightbeam.hallowed.Hallowed;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Grants the advancements that code decides ({@code minecraft:impossible} criteria named "granted"). */
public final class Advancements {
    private Advancements() {
    }

    public static void grant(ServerPlayer player, String name) {
        MinecraftServer server = player.level().getServer();
        if (server == null || Hallowed.platform().isFakePlayer(player)) return;
        AdvancementHolder holder = server.getAdvancements().get(Hallowed.id(name));
        if (holder != null) {
            player.getAdvancements().award(holder, "granted");
        }
    }
}
