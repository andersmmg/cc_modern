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

public class AngledMonitorBlock extends MonitorBlock {
    private static final MapCodec<AngledMonitorBlock> CODEC = BlockCodecs.blockWithBlockEntityCodec(
            AngledMonitorBlock::new,
            x -> x.typeAccessor
    );

    private static final VoxelShape OCCUPIED = Shapes.box(0, 0, 0, 1, 7.2 / 16, 1);

    private static final VoxelShape INTERACTION = Shapes.box(0, 0, 0, 1, 1, 1);

    private final RegistryEntry<? extends BlockEntityType<? extends AngledMonitorBlockEntity>> typeAccessor;

    public AngledMonitorBlock(Properties properties, RegistryEntry<? extends BlockEntityType<? extends AngledMonitorBlockEntity>> type) {
        super(properties, type);
        this.typeAccessor = type;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(ORIENTATION, Direction.NORTH);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OCCUPIED;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return INTERACTION;
    }

    @Override
    protected MapCodec<? extends AngledMonitorBlock> codec() {
        return CODEC;
    }
}
