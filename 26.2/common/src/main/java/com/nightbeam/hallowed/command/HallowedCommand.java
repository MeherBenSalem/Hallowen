package com.nightbeam.hallowed.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.config.Configs;
import com.nightbeam.hallowed.config.HallowedConfig;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.entity.HollowHorseman;
import com.nightbeam.hallowed.entity.Spirit;
import com.nightbeam.hallowed.entity.Wisp;
import com.nightbeam.hallowed.night.HallowedNight;
import com.nightbeam.hallowed.night.NightClock;
import java.time.MonthDay;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.phys.Vec3;

/** {@code /hallowed}: status for everyone, the rest for operators. */
public final class HallowedCommand {
    private HallowedCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        java.util.function.Predicate<CommandSourceStack> op = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);
        dispatcher.register(Commands.literal("hallowed")
                .executes(HallowedCommand::status)
                .then(Commands.literal("status").executes(HallowedCommand::status))
                .then(Commands.literal("night").requires(op)
                        .then(Commands.literal("start").executes(c -> night(c, true)))
                        .then(Commands.literal("stop").executes(c -> night(c, false))))
                .then(Commands.literal("witching").requires(op).executes(HallowedCommand::witching))
                .then(Commands.literal("summon").requires(op)
                        .then(Commands.literal("horseman").executes(c -> summon(c, "horseman")))
                        .then(Commands.literal("spirit").executes(c -> summon(c, "spirit")))
                        .then(Commands.literal("wisp").executes(c -> summon(c, "wisp"))
                                .then(Commands.literal("cursed").executes(c -> summon(c, "cursed_wisp")))))
                .then(Commands.literal("reload").requires(op).executes(HallowedCommand::reload)));
    }

    private static int status(CommandContext<CommandSourceStack> c) {
        MinecraftServer server = c.getSource().getServer();
        ServerLevel overworld = server.overworld();
        long time = NightClock.time(overworld);
        int tod = NightClock.timeOfDay(time);
        long night = NightClock.nightIndex(time);
        boolean hallowed = HallowedNight.isHallowed(night);
        HallowedConfig cfg = Configs.common();
        String phase = !NightClock.night(tod) ? "day" : NightClock.witching(tod) ? "witching" : "night";
        Component state = Component.translatable(hallowed ? "commands.hallowed.status.hallowed" : "commands.hallowed.status.ordinary")
                .withStyle(hallowed ? ChatFormatting.GOLD : ChatFormatting.GRAY);
        c.getSource().sendSuccess(() -> Component.translatable("commands.hallowed.status", state,
                Component.translatable("commands.hallowed.phase." + phase), NightClock.clockText(tod)), false);
        boolean season = HallowedNight.inSeason(MonthDay.now(), cfg);
        c.getSource().sendSuccess(() -> Component.translatable("commands.hallowed.status.season", cfg.mode.name().toLowerCase(java.util.Locale.ROOT),
                Component.translatable(season ? "commands.hallowed.status.in_season" : "commands.hallowed.status.off_season")).withStyle(ChatFormatting.GRAY), false);
        return hallowed ? 1 : 0;
    }

    private static int night(CommandContext<CommandSourceStack> c, boolean start) {
        MinecraftServer server = c.getSource().getServer();
        if (start) HallowedNight.force(server);
        else HallowedNight.cancel(server);
        c.getSource().sendSuccess(() -> Component.translatable(start ? "commands.hallowed.night.start" : "commands.hallowed.night.stop"), true);
        return 1;
    }

    private static int witching(CommandContext<CommandSourceStack> c) {
        MinecraftServer server = c.getSource().getServer();
        long time = NightClock.time(server.overworld());
        long day = NightClock.dayIndex(time);
        if (NightClock.timeOfDay(time) >= NightClock.MIDNIGHT - 200) day++;
        long target = day * NightClock.DAY + NightClock.MIDNIGHT - 200;
        server.registryAccess().get(WorldClocks.OVERWORLD).ifPresent(clock -> server.clockManager().setTotalTicks(clock, target));
        HallowedNight.force(server);
        c.getSource().sendSuccess(() -> Component.translatable("commands.hallowed.witching"), true);
        return 1;
    }

    private static int summon(CommandContext<CommandSourceStack> c, String what) {
        CommandSourceStack source = c.getSource();
        ServerLevel level = source.getLevel();
        Vec3 at = source.getPosition();
        Vec3 look = source.getEntity() != null ? source.getEntity().getLookAngle() : new Vec3(0, 0, 1);
        Vec3 ahead = at.add(look.x * 6, 0, look.z * 6);
        switch (what) {
            case "horseman" -> HollowHorseman.summon(level, BlockPos.containing(ahead), Direction.fromYRot(
                    source.getEntity() != null ? source.getEntity().getYRot() + 180 : 0));
            case "spirit" -> {
                Spirit spirit = ModContent.SPIRIT.get().create(level, EntitySpawnReason.COMMAND);
                if (spirit != null) {
                    spirit.snapTo(ahead.x, ahead.y + 0.5, ahead.z, 0, 0);
                    spirit.setPersistenceRequired();
                    level.addFreshEntity(spirit);
                    spirit.rise();
                }
            }
            default -> {
                Wisp wisp = Wisp.spawnNear(level, ahead.add(0, 1.6, 0), what.equals("cursed_wisp"));
                wisp.setNatural(false);
            }
        }
        source.sendSuccess(() -> Component.translatable("commands.hallowed.summon", Component.translatable("commands.hallowed.summon." + what)), true);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> c) {
        Configs.load(Hallowed.platform().configDir(), false);
        HallowedNight.syncAll(c.getSource().getServer());
        c.getSource().sendSuccess(() -> Component.translatable("commands.hallowed.reload"), true);
        return 1;
    }
}
