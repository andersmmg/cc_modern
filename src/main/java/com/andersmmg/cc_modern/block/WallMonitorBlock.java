package com.andersmmg.cc_modern.block;

import com.mojang.serialization.MapCodec;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlock;
import dan200.computercraft.shared.platform.RegistryEntry;
import dan200.computercraft.shared.util.BlockCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallMonitorBlock extends MonitorBlock {
    private static final MapCodec<WallMonitorBlock> CODEC = BlockCodecs.blockWithBlockEntityCodec(
        WallMonitorBlock::new,
        x -> x.typeAccessor
    );

    private static final VoxelShape SHAPE_NORTH = Shapes.box(0, 0, 15.0 / 16, 1, 1, 1);
    private static final VoxelShape SHAPE_SOUTH = Shapes.box(0, 0, 0, 1, 1, 1.0 / 16);
    private static final VoxelShape SHAPE_WEST = Shapes.box(15.0 / 16, 0, 0, 1, 1, 1);
    private static final VoxelShape SHAPE_EAST = Shapes.box(0, 0, 0, 1.0 / 16, 1, 1);

    private final RegistryEntry<? extends BlockEntityType<? extends WallMonitorBlockEntity>> typeAccessor;

    public WallMonitorBlock(Properties properties, RegistryEntry<? extends BlockEntityType<? extends WallMonitorBlockEntity>> type) {
        super(properties, type);
        this.typeAccessor = type;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var facing = context.getClickedFace();
        if (facing.getAxis() == Direction.Axis.Y) return null;
        return defaultBlockState()
            .setValue(FACING, facing)
            .setValue(ORIENTATION, Direction.NORTH);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> Shapes.block();
        };
    }

    @Override
    protected MapCodec<? extends WallMonitorBlock> codec() {
        return CODEC;
    }
}
