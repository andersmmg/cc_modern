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

public class TransparentMonitorBlock extends MonitorBlock {
    private static final MapCodec<TransparentMonitorBlock> CODEC = BlockCodecs.blockWithBlockEntityCodec(
            TransparentMonitorBlock::new,
            x -> x.typeAccessor
    );

    private static final VoxelShape SHAPE_NS = Shapes.box(0, 0, 7.5 / 16, 1, 1, 8.5 / 16);
    private static final VoxelShape SHAPE_EW = Shapes.box(7.5 / 16, 0, 0, 8.5 / 16, 1, 1);

    private final RegistryEntry<? extends BlockEntityType<? extends TransparentMonitorBlockEntity>> typeAccessor;

    public TransparentMonitorBlock(Properties properties, RegistryEntry<? extends BlockEntityType<? extends TransparentMonitorBlockEntity>> type) {
        super(properties, type);
        this.typeAccessor = type;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var clickedFace = context.getClickedFace();
        Direction facing;
        if (clickedFace.getAxis() == Direction.Axis.Y) {
            facing = context.getHorizontalDirection().getOpposite();
        } else {
            facing = clickedFace;
        }
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(ORIENTATION, Direction.NORTH);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH, SOUTH -> SHAPE_NS;
            case EAST, WEST -> SHAPE_EW;
            default -> Shapes.block();
        };
    }

    @Override
    protected MapCodec<? extends TransparentMonitorBlock> codec() {
        return CODEC;
    }
}
