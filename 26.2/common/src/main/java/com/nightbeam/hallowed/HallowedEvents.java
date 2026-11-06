package com.nightbeam.hallowed;

import com.mojang.brigadier.CommandDispatcher;
import com.nightbeam.hallowed.command.HallowedCommand;
import com.nightbeam.hallowed.config.Configs;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.content.ModTags;
import com.nightbeam.hallowed.item.Candy;
import com.nightbeam.hallowed.night.HallowedNight;
import com.nightbeam.hallowed.spawn.HallowedSpawns;
import com.nightbeam.hallowed.trick.Pranks;
import com.nightbeam.hallowed.trick.TrickOrTreat;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;

/** Server hooks, called by each loader's event wiring. */
public final class HallowedEvents {
    private HallowedEvents() {
    }

    public static void onServerStarted(MinecraftServer server) {
        HallowedNight.load(server);
        TrickOrTreat.reset();
        HallowedSpawns.reset();
        Pranks.reset();
    }

    public static void onServerStopping(MinecraftServer server) {
        HallowedNight.unload();
        Pranks.reset();
    }

    public static void onServerTick(MinecraftServer server) {
        HallowedNight.tick(server);
        Pranks.tick();
        boolean second = server.getTickCount() % 20 == 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Candy.tickPlayer(player);
            if (second) TrickOrTreat.tickPlayer(player);
        }
        if (second) HallowedSpawns.tick(server);
    }

    public static void onPlayerJoin(ServerPlayer player) {
        HallowedNight.sync(player, true);
    }

    public static void onPlayerLeave(ServerPlayer player) {
        HallowedNight.forget(player);
    }

    /** Hallowed Night candy from monsters a player kills, and the Reaper Scythe's soul harvest. */
    public static void onLivingDeath(LivingEntity entity, DamageSource source) {
        if (!(entity.level() instanceof ServerLevel level) || !(source.getEntity() instanceof ServerPlayer player)) return;
        if (Hallowed.platform().isFakePlayer(player)) return;
        if (player.getMainHandItem().is(ModContent.REAPER_SCYTHE.get()) && entity instanceof Enemy) {
            player.heal(3.0f);
            level.sendParticles(ParticleTypes.SOUL, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 6, 0.2, 0.3, 0.2, 0.03);
        }
        if (!(entity instanceof Enemy) || entity.is(ModTags.NO_CANDY) || !HallowedNight.active(level)) return;
        boolean costumed = entity instanceof Mob mob && HallowedSpawns.costumed(mob);
        double chance = costumed ? Configs.common().costumedCandyChance : Configs.common().candyDropChance;
        if (level.getRandom().nextDouble() >= chance) return;
        int count = costumed ? 1 + level.getRandom().nextInt(2) : 1;
        for (ItemStack candy : Candy.randomStacks(level.getRandom(), count)) {
            ItemEntity item = new ItemEntity(level, entity.getX(), entity.getY() + 0.5, entity.getZ(), candy);
            item.setDefaultPickUpDelay();
            level.addFreshEntity(item);
        }
        level.sendParticles(ModContent.CANDY_SPARKLE.get(), entity.getX(), entity.getY() + 0.8, entity.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        HallowedCommand.register(dispatcher, context);
    }
}
