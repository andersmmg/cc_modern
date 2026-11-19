package com.andersmmg.cc_modern.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sashafiesta.ccterminals.TerminalBlock;
import com.sashafiesta.ccterminals.TerminalBlockEntity;
import dan200.computercraft.shared.platform.RegistryEntry;
import dan200.computercraft.shared.util.BlockCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A CC: Terminals terminal, but flat on teh wall!
 */
public class WallTerminalBlock extends TerminalBlock {
    private static final VoxelShape SHAPE_NORTH = Shapes.box(0, 0, 15.0 / 16, 1, 1, 1);
    private static final VoxelShape SHAPE_SOUTH = Shapes.box(0, 0, 0, 1, 1, 1.0 / 16);
    private static final VoxelShape SHAPE_WEST = Shapes.box(15.0 / 16, 0, 0, 1, 1, 1);
    private static final VoxelShape SHAPE_EAST = Shapes.box(0, 0, 0, 1.0 / 16, 1, 1);

    private final RegistryEntry<BlockEntityType<TerminalBlockEntity>> typeAccessor;
    private final boolean colour;

    public WallTerminalBlock(Properties properties, RegistryEntry<BlockEntityType<TerminalBlockEntity>> type,
                             boolean colour) {
        super(properties, type, colour);
        this.typeAccessor = type;
        this.colour = colour;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WallTerminalBlockEntity(typeAccessor.get(), pos, state, colour);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var facing = context.getClickedFace();
        if (facing.getAxis() == Direction.Axis.Y) return null;
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> Shapes.block();
        };
    }

    @Override
    protected MapCodec<? extends WallTerminalBlock> codec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockCodecs.propertiesCodec(),
                BlockCodecs.blockEntityCodec(x -> x.typeAccessor),
                Codec.BOOL.fieldOf("colour").orElse(false).forGetter(block -> block.colour)
        ).apply(instance, WallTerminalBlock::new));
    }
}
