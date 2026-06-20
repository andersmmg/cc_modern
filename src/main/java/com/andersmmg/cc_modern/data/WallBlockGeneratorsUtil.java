package com.andersmmg.cc_modern.data;

import net.minecraft.core.Direction;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.function.Function;

public final class WallBlockGeneratorsUtil {
    private WallBlockGeneratorsUtil() {
    }

    public static VariantProperties.Rotation yAngle(Direction direction) {
        return switch (direction) {
            case WEST -> VariantProperties.Rotation.R90;
            case NORTH -> VariantProperties.Rotation.R180;
            case EAST -> VariantProperties.Rotation.R270;
            default -> VariantProperties.Rotation.R0;
        };
    }

    public static PropertyDispatch facingDispatch() {
        var dispatch = PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING);
        for (var direction : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            dispatch.select(direction, Variant.variant().with(VariantProperties.Y_ROT, yAngle(direction)));
        }
        return dispatch;
    }

    public static <T extends Comparable<T>> PropertyDispatch modelDispatch(Property<T> property, Function<T, ResourceLocation> makeModel) {
        var dispatch = PropertyDispatch.property(property);
        for (var value : property.getPossibleValues()) {
            dispatch.select(value, Variant.variant().with(VariantProperties.MODEL, makeModel.apply(value)));
        }
        return dispatch;
    }
}
