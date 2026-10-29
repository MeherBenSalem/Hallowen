package com.nightbeam.hallowed.block;

import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.reward.Advancements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A Hallowed Cache: the treasure a will-o'-the-wisp leads you to. Use it (or break it) and it bursts open, spilling its
 * loot table ({@code hallowed:blocks/hallowed_cache}).
 */
public class HallowedCacheBlock extends HorizontalDirectionalBlock {
    /** Left by a cursed wisp: guarded by spirits, and its loot table rolls richer. */
    public static final BooleanProperty CURSED = BooleanProperty.create("cursed");
    private static final VoxelShape SHAPE = Block.box(1, 0, 2, 15, 12, 14);
    private static final VoxelShape SHAPE_X = Block.box(2, 0, 1, 14, 12, 15);

    public HallowedCacheBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(CURSED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CURSED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel server) {
            burst(server, pos);
            level.destroyBlock(pos, true, player);
            if (player instanceof ServerPlayer sp) Advancements.grant(sp, "follow_the_light");
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel server) {
            burst(server, pos);
            if (player instanceof ServerPlayer sp) Advancements.grant(sp, "follow_the_light");
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private static void burst(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5, y = pos.getY() + 0.6, z = pos.getZ() + 0.5;
        level.sendParticles(ModContent.CANDY_SPARKLE.get(), x, y, z, 30, 0.35, 0.3, 0.35, 0.12);
        level.sendParticles(ModContent.EMBER.get(), x, y, z, 16, 0.3, 0.2, 0.3, 0.06);
        level.sendParticles(ParticleTypes.POOF, x, y, z, 8, 0.25, 0.1, 0.25, 0.02);
        level.playSound(null, pos, ModContent.CACHE_OPEN.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            level.addParticle(state.getValue(CURSED) ? net.minecraft.core.particles.ParticleTypes.WITCH : ModContent.WISP_FLAME.get(), pos.getX() + 0.5 + (random.nextFloat() - 0.5) * 0.7, pos.getY() + 0.8,
                    pos.getZ() + 0.5 + (random.nextFloat() - 0.5) * 0.7, 0, 0.015, 0);
        }
    }
}
