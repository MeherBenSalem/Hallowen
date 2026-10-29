package com.nightbeam.hallowed.block;

import com.nightbeam.hallowed.content.Face;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.night.HallowedNight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
 * A carved pumpkin, jack o'lantern or soul jack o'lantern with one of eight faces. Shears carve the next face (sneak
 * for the previous one); a torch or soul torch lights a carved pumpkin where it stands.
 */
public class CarvedFaceBlock extends HorizontalDirectionalBlock {
    public static final IntegerProperty FACE = IntegerProperty.create("face", 0, Face.COUNT - 1);

    public enum Kind {
        CARVED,
        LIT,
        SOUL
    }

    private final Kind kind;

    public CarvedFaceBlock(Kind kind, BlockBehaviour.Properties properties) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FACE, Face.GRIN.ordinal()));
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        InteractionResult result = Carving.use(stack, state, level, pos, player, hand, hit);
        return result != null ? result : super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (kind == Kind.CARVED) return;
        Direction facing = state.getValue(FACING);
        if (kind == Kind.LIT && random.nextInt(HallowedNight.affects(level) && level.isDarkOutside() ? 5 : 14) == 0) {
            level.addParticle(ModContent.EMBER.get(), pos.getX() + 0.5 + (random.nextFloat() - 0.5) * 0.3, pos.getY() + 1.02,
                    pos.getZ() + 0.5 + (random.nextFloat() - 0.5) * 0.3, 0, 0.02, 0);
        }
        if (kind == Kind.SOUL && random.nextInt(9) == 0) {
            double x = pos.getX() + 0.5 + facing.getStepX() * 0.52 + (random.nextFloat() - 0.5) * 0.4;
            double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.52 + (random.nextFloat() - 0.5) * 0.4;
            level.addParticle(random.nextBoolean() ? ModContent.SOUL_MOTE.get() : ParticleTypes.SOUL_FIRE_FLAME, x,
                    pos.getY() + 0.35 + random.nextFloat() * 0.3, z, facing.getStepX() * 0.01, 0.01, facing.getStepZ() * 0.01);
        }
    }
}
