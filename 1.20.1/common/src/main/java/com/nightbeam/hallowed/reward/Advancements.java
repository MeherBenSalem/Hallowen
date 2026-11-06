package com.nightbeam.hallowed.reward;

import com.nightbeam.hallowed.Hallowed;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Grants the advancements that code decides ({@code minecraft:impossible} criteria named "granted"). */
public final class Advancements {
    private Advancements() {
    }

    public static void grant(ServerPlayer player, String name) {
        MinecraftServer server = player.getServer();
        if (server == null || Hallowed.platform().isFakePlayer(player)) return;
        Advancement advancement = server.getAdvancements().getAdvancement(Hallowed.id(name));
        if (advancement != null) {
            player.getAdvancements().award(advancement, "granted");
        }
    }
}
