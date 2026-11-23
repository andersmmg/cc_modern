package com.andersmmg.cc_modern.block;

import com.mojang.serialization.MapCodec;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlock;
import dan200.computercraft.shared.platform.RegistryEntry;
import dan200.computercraft.shared.util.BlockCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

public class AngledMonitorBlock extends MonitorBlock {
    private static final MapCodec<AngledMonitorBlock> CODEC = BlockCodecs.blockWithBlockEntityCodec(
            AngledMonitorBlock::new,
            x -> x.typeAccessor
    );

    private static final Map<Direction, VoxelShape> FLOOR_SHAPES = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> WALL_UP_SHAPES = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> WALL_DOWN_SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            FLOOR_SHAPES.put(direction, makeFloorShape(direction));
            WALL_UP_SHAPES.put(direction, makeWallUpShape(direction));
            WALL_DOWN_SHAPES.put(direction, makeWallDownShape(direction));
        }
    }

    private static final VoxelShape INTERACTION = Shapes.box(0, 0, 0, 1, 1, 1);

    private final RegistryEntry<? extends BlockEntityType<? extends AngledMonitorBlockEntity>> typeAccessor;

    public AngledMonitorBlock(Properties properties, RegistryEntry<? extends BlockEntityType<? extends AngledMonitorBlockEntity>> type) {
        super(properties, type);
        this.typeAccessor = type;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis() == Direction.Axis.Y) {
            return defaultBlockState()
                    .setValue(FACING, context.getHorizontalDirection())
                    .setValue(ORIENTATION, Direction.NORTH);
        }

        double hitY = context.getClickLocation().y - (double) context.getClickedPos().getY();
        Direction tilt = (hitY > 0.5) ? Direction.DOWN : Direction.UP;

        return defaultBlockState()
                .setValue(FACING, clickedFace)
                .setValue(ORIENTATION, tilt);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction orientation = state.getValue(ORIENTATION);
        Direction facing = state.getValue(FACING);
        if (orientation == Direction.NORTH) {
            return FLOOR_SHAPES.getOrDefault(facing, Shapes.block());
        } else if (orientation == Direction.DOWN) {
            return WALL_DOWN_SHAPES.getOrDefault(facing, Shapes.block());
        } else {
            return WALL_UP_SHAPES.getOrDefault(facing, Shapes.block());
        }
    }

    private static VoxelShape makeFloorShape(Direction facing) {
        double[][] steps = {
            {0.0, 2.0, 1.0},
            {2.0, 3.0, 1.75},
            {3.0, 5.0, 2.15},
            {5.0, 7.0, 3.05},
            {7.0, 9.0, 3.75},
            {9.0, 11.0, 4.65},
            {11.0, 13.0, 5.35},
            {13.0, 15.0, 6.25},
            {15.0, 16.0, 7.41}
        };
        VoxelShape result = Shapes.empty();
        for (double[] step : steps) {
            double x0 = step[0], x1 = step[1], h = step[2];
            VoxelShape box = switch (facing) {
                case EAST -> Block.box(x0, 0, 0, x1, h, 16);
                case SOUTH -> Block.box(0, 0, x0, 16, h, x1);
                case WEST -> Block.box(16 - x1, 0, 0, 16 - x0, h, 16);
                case NORTH -> Block.box(0, 0, 16 - x1, 16, h, 16 - x0);
                default -> Block.box(0, 0, 0, 16, h, 16);
            };
            result = Shapes.or(result, box);
        }
        return result.optimize();
    }

    private static VoxelShape makeWallUpShape(Direction facing) {
        double[][] slices = {
            {0.0, 1.0, 7.41},
            {1.0, 3.0, 6.25},
            {3.0, 5.0, 5.35},
            {5.0, 7.0, 4.65},
            {7.0, 9.0, 3.75},
            {9.0, 11.0, 3.05},
            {11.0, 13.0, 2.15},
            {13.0, 14.0, 1.75},
            {14.0, 16.0, 1.0}
        };
        VoxelShape result = Shapes.empty();
        for (double[] slice : slices) {
            double y0 = slice[0], y1 = slice[1], d = slice[2];
            VoxelShape box = switch (facing) {
                case EAST -> Block.box(0, y0, 0, d, y1, 16);
                case SOUTH -> Block.box(0, y0, 0, 16, y1, d);
                case WEST -> Block.box(16 - d, y0, 0, 16, y1, 16);
                case NORTH -> Block.box(0, y0, 16 - d, 16, y1, 16);
                default -> Block.box(0, y0, 0, 16, y1, 16);
            };
            result = Shapes.or(result, box);
        }
        return result.optimize();
    }

    private static VoxelShape makeWallDownShape(Direction facing) {
        double[][] slices = {
            {0.0, 1.0, 7.41},
            {1.0, 3.0, 6.25},
            {3.0, 5.0, 5.35},
            {5.0, 7.0, 4.65},
            {7.0, 9.0, 3.75},
            {9.0, 11.0, 3.05},
            {11.0, 13.0, 2.15},
            {13.0, 14.0, 1.75},
            {14.0, 16.0, 1.0}
        };
        VoxelShape result = Shapes.empty();
        for (double[] slice : slices) {
            double y0 = 16.0 - slice[1], y1 = 16.0 - slice[0], d = slice[2];
            VoxelShape box = switch (facing) {
                case EAST -> Block.box(0, y0, 0, d, y1, 16);
                case SOUTH -> Block.box(0, y0, 0, 16, y1, d);
                case WEST -> Block.box(16 - d, y0, 0, 16, y1, 16);
                case NORTH -> Block.box(0, y0, 16 - d, 16, y1, 16);
                default -> Block.box(0, y0, 0, 16, y1, 16);
            };
            result = Shapes.or(result, box);
        }
        return result.optimize();
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
