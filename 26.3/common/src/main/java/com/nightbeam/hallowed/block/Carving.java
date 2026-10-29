package com.nightbeam.hallowed.block;

import com.nightbeam.hallowed.content.Face;
import com.nightbeam.hallowed.content.ModContent;
import com.nightbeam.hallowed.reward.Advancements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * Carving and lighting, for vanilla carved pumpkins and jack o'lanterns as well as Hallowed ones. Vanilla's own face
 * is face 0 ("Classic"): shears turn a vanilla pumpkin into a Hallowed one with the next face, and carving past the
 * last face comes back round to the vanilla block.
 */
public final class Carving {
    private Carving() {
    }

    /** Handles shears and torches on a carved pumpkin or lantern; null when the item does nothing here. */
    public static @Nullable InteractionResult use(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!isCarved(state)) return null;
        if (stack.is(Items.SHEARS)) {
            if (level instanceof ServerLevel server) {
                int step = player.isSecondaryUseActive() ? -1 : 1;
                BlockState next = withFace(state, Face.byIndex(face(state).ordinal() + step));
                level.setBlock(pos, next, Block.UPDATE_ALL);
                effects(server, pos, state.getValue(HorizontalDirectionalBlock.FACING));
                stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
                level.gameEvent(player, GameEvent.SHEAR, pos);
                if (player instanceof ServerPlayer sp) Advancements.grant(sp, "carve_a_face");
            }
            return InteractionResult.SUCCESS;
        }
        boolean torch = stack.is(Items.TORCH);
        boolean soul = stack.is(Items.SOUL_TORCH);
        if ((torch || soul) && kind(state) == CarvedFaceBlock.Kind.CARVED) {
            if (level instanceof ServerLevel) {
                BlockState lit = soul ? copy(state, ModContent.SOUL_JACK_O_LANTERN.get())
                        : face(state) == Face.CLASSIC ? Blocks.JACK_O_LANTERN.defaultBlockState()
                                .setValue(HorizontalDirectionalBlock.FACING, state.getValue(HorizontalDirectionalBlock.FACING))
                        : copy(state, ModContent.JACK_O_LANTERN.get());
                level.setBlock(pos, lit, Block.UPDATE_ALL);
                level.playSound(null, pos, soul ? SoundEvents.SOUL_ESCAPE.value() : SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.7f, 1.2f);
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8f, 1.0f);
                stack.consume(1, player);
            }
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    public static boolean isCarved(BlockState state) {
        return state.is(Blocks.CARVED_PUMPKIN) || state.is(Blocks.JACK_O_LANTERN) || state.getBlock() instanceof CarvedFaceBlock;
    }

    public static Face face(BlockState state) {
        return state.getBlock() instanceof CarvedFaceBlock ? Face.byIndex(state.getValue(CarvedFaceBlock.FACE)) : Face.CLASSIC;
    }

    public static CarvedFaceBlock.Kind kind(BlockState state) {
        if (state.getBlock() instanceof CarvedFaceBlock block) return block.kind();
        return state.is(Blocks.JACK_O_LANTERN) ? CarvedFaceBlock.Kind.LIT : CarvedFaceBlock.Kind.CARVED;
    }

    /** The same pumpkin with another face; the vanilla blocks stand in for the classic face. */
    public static BlockState withFace(BlockState state, Face face) {
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        CarvedFaceBlock.Kind kind = kind(state);
        if (face == Face.CLASSIC && kind != CarvedFaceBlock.Kind.SOUL) {
            Block vanilla = kind == CarvedFaceBlock.Kind.LIT ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN;
            return vanilla.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
        }
        Block target = switch (kind) {
            case CARVED -> ModContent.CARVED_PUMPKIN.get();
            case LIT -> ModContent.JACK_O_LANTERN.get();
            case SOUL -> ModContent.SOUL_JACK_O_LANTERN.get();
        };
        return target.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing).setValue(CarvedFaceBlock.FACE, face.ordinal());
    }

    private static BlockState copy(BlockState from, Block to) {
        return to.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, from.getValue(HorizontalDirectionalBlock.FACING))
                .setValue(CarvedFaceBlock.FACE, face(from).ordinal());
    }

    private static void effects(ServerLevel level, BlockPos pos, Direction facing) {
        double x = pos.getX() + 0.5 + facing.getStepX() * 0.55;
        double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.55;
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PUMPKIN.defaultBlockState()), x, pos.getY() + 0.5, z,
                14, 0.2, 0.25, 0.2, 0.05);
        level.playSound(null, pos, ModContent.CARVE.get(), SoundSource.BLOCKS, 1.0f, 0.9f + level.getRandom().nextFloat() * 0.2f);
    }
}
