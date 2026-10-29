package com.nightbeam.hallowed.block;

import com.nightbeam.hallowed.config.Configs;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.entity.HollowHorseman;
import com.nightbeam.hallowed.night.HallowedNight;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Cursed Pumpkin: the Hollow Horseman's lure. It lies dormant until the Witching Hour of a Hallowed Night, then
 * wakes over a few seconds (four stages, each brighter and louder) and bursts into flame as the Horseman rides out of
 * it. Place it early and wait for midnight.
 */
public class CursedPumpkinBlock extends HorizontalDirectionalBlock {
    public static final int MAX_STAGE = 4;
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, MAX_STAGE);
    private static final int DORMANT_CHECK = 40;
    private static final int STAGE_TICKS = 32;

    public CursedPumpkinBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(STAGE, 0));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STAGE);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) level.scheduleTick(pos, this, 10);
    }

    /** Whether this pumpkin may wake here and now. */
    public static boolean canWake(Level level) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        return Configs.common().summonNeedsWitchingHour ? HallowedNight.witchingHour(level) : level.isDarkOutside() || !HallowedNight.affects(level);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int stage = state.getValue(STAGE);
        if (stage == 0) {
            if (canWake(level) && !HollowHorseman.anyNear(level, pos, 160)) {
                level.setBlock(pos, state.setValue(STAGE, 1), Block.UPDATE_ALL);
                pulse(level, pos, 1);
                level.scheduleTick(pos, this, STAGE_TICKS);
            } else {
                level.scheduleTick(pos, this, DORMANT_CHECK);
            }
            return;
        }
        if (stage < MAX_STAGE) {
            level.setBlock(pos, state.setValue(STAGE, stage + 1), Block.UPDATE_ALL);
            pulse(level, pos, stage + 1);
            level.scheduleTick(pos, this, STAGE_TICKS);
            return;
        }
        level.removeBlock(pos, false);
        HollowHorseman.summon(level, pos, state.getValue(FACING));
    }

    private static void pulse(ServerLevel level, BlockPos pos, int stage) {
        level.playSound(null, pos, ModContent.CURSED_PULSE.get(), SoundSource.HOSTILE, 1.0f + stage * 0.4f, 0.6f + stage * 0.12f);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8 + stage * 6,
                0.4, 0.4, 0.4, 0.03 * stage);
        if (stage == MAX_STAGE) {
            level.playSound(null, pos, ModContent.WITCHING_BELL.get(), SoundSource.HOSTILE, 2.0f, 0.8f);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            String key = state.getValue(STAGE) > 0 ? "block.hallowed.cursed_pumpkin.waking"
                    : level.getDifficulty() == Difficulty.PEACEFUL ? "block.hallowed.cursed_pumpkin.peaceful"
                    : !HallowedNight.affects(level) ? "block.hallowed.cursed_pumpkin.nowhere"
                    : "block.hallowed.cursed_pumpkin.dormant";
            player.sendOverlayMessage(Component.translatable(key).withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int stage = state.getValue(STAGE);
        int count = stage == 0 ? (random.nextInt(6) == 0 ? 1 : 0) : stage * 2;
        for (int i = 0; i < count; i++) {
            float angle = random.nextFloat() * Mth.TWO_PI;
            float r = 0.5f + stage * 0.25f + random.nextFloat() * 0.3f;
            level.addParticle(stage == 0 ? ModContent.SOUL_MOTE.get() : ParticleTypes.SOUL_FIRE_FLAME,
                    pos.getX() + 0.5 + Mth.cos(angle) * r, pos.getY() + 0.1 + random.nextFloat() * (0.6 + stage * 0.3),
                    pos.getZ() + 0.5 + Mth.sin(angle) * r, -Mth.cos(angle) * 0.02 * stage, 0.03, -Mth.sin(angle) * 0.02 * stage);
        }
        if (stage >= 3 && random.nextInt(3) == 0) {
            level.addParticle(ModContent.EMBER.get(), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    (random.nextFloat() - 0.5) * 0.1, 0.12, (random.nextFloat() - 0.5) * 0.1);
        }
    }
}
