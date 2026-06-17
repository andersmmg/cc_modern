package com.andersmmg.cc_modern.data;

import com.andersmmg.cc_modern.CCModern;
import com.andersmmg.cc_modern.block.WallMonitorBlock;
import dan200.computercraft.shared.peripheral.monitor.MonitorEdgeState;
import net.minecraft.core.Direction;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;
import java.util.function.Function;

import static net.minecraft.data.models.model.ModelLocationUtils.getModelLocation;

public class WallMonitorModels {
    private static final ModelTemplate WALL_MONITOR_BASE = new ModelTemplate(
        Optional.of(ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_monitor_base")),
        Optional.empty(),
        TextureSlot.FRONT, TextureSlot.BACK, TextureSlot.SIDE, TextureSlot.TOP
    );

    private static final int[][] TEXTURE_MAP = {
        {16, 4, 0, 32},   // 0: NONE
        {22, 5, 0, 38},   // 1: U
        {20, 7, 0, 36},   // 2: D
        {21, 6, 0, 37},   // 3: UD
        {19, 4, 1, 33},   // 4: L
        {25, 5, 1, 39},   // 5: LU
        {31, 7, 1, 45},   // 6: LD
        {28, 6, 1, 42},   // 7: LUD
        {17, 4, 3, 35},   // 8: R
        {23, 5, 3, 41},   // 9: RU
        {29, 7, 3, 47},   // 10: RD
        {26, 6, 3, 44},   // 11: RUD
        {18, 4, 2, 34},   // 12: LR
        {24, 5, 2, 40},   // 13: LRU
        {30, 7, 2, 46},   // 14: LRD
        {27, 6, 2, 43},   // 15: LRUD
    };

    private static final int[] ITEM_TEXTURE = {15, 4, 0, 32};

    public static void addBlockModels(BlockModelGenerators generators) {
        registerWallMonitor(generators, CCModern.WALL_MONITOR_BLOCK.get(), "monitor_normal");
        registerWallMonitor(generators, CCModern.WALL_MONITOR_ADVANCED_BLOCK.get(), "monitor_advanced");
    }

    private static void registerWallMonitor(BlockModelGenerators generators, WallMonitorBlock block, String textureBase) {
        for (int i = 0; i < 16; i++) {
            var state = MonitorEdgeState.fromConnections(
                (i & 1) != 0,
                (i & 2) != 0,
                (i & 4) != 0,
                (i & 8) != 0
            );
            var suffix = state == MonitorEdgeState.NONE ? "" : "_" + state.getSerializedName();
            var textures = TEXTURE_MAP[i];
            wallMonitorModel(generators, block, suffix, textureBase, textures[0], textures[1], textures[2], textures[3]);
        }

        generators.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
            .with(createHorizontalFacingDispatch())
            .with(createModelDispatch(block.STATE, edge -> getModelLocation(block, edge == MonitorEdgeState.NONE ? "" : "_" + edge.getSerializedName())))
        );

        generators.delegateItemModel(block, wallMonitorModel(generators, block, "_item", textureBase, ITEM_TEXTURE[0], ITEM_TEXTURE[1], ITEM_TEXTURE[2], ITEM_TEXTURE[3]));
    }

    private static ResourceLocation wallMonitorModel(BlockModelGenerators generators, WallMonitorBlock block, String suffix, String textureBase, int front, int side, int top, int back) {
        return WALL_MONITOR_BASE.create(
            getModelLocation(block, suffix),
            new TextureMapping()
                .put(TextureSlot.FRONT, ResourceLocation.fromNamespaceAndPath("computercraft", "block/" + textureBase + "_" + front))
                .put(TextureSlot.BACK, ResourceLocation.fromNamespaceAndPath("computercraft", "block/" + textureBase + "_" + back))
                .put(TextureSlot.SIDE, ResourceLocation.fromNamespaceAndPath("computercraft", "block/" + textureBase + "_" + side))
                .put(TextureSlot.TOP, ResourceLocation.fromNamespaceAndPath("computercraft", "block/" + textureBase + "_" + top)),
            generators.modelOutput
        );
    }

    private static PropertyDispatch createHorizontalFacingDispatch() {
        var dispatch = PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING);
        for (var direction : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            dispatch.select(direction, Variant.variant().with(VariantProperties.Y_ROT, toYAngle(direction)));
        }
        return dispatch;
    }

    private static VariantProperties.Rotation toYAngle(Direction direction) {
        return switch (direction) {
            case SOUTH -> VariantProperties.Rotation.R0;
            case WEST -> VariantProperties.Rotation.R90;
            case NORTH -> VariantProperties.Rotation.R180;
            case EAST -> VariantProperties.Rotation.R270;
            default -> VariantProperties.Rotation.R0;
        };
    }

    private static <T extends Comparable<T>> PropertyDispatch createModelDispatch(net.minecraft.world.level.block.state.properties.Property<T> property, Function<T, ResourceLocation> makeModel) {
        var variant = PropertyDispatch.property(property);
        for (var value : property.getPossibleValues()) {
            variant.select(value, Variant.variant().with(VariantProperties.MODEL, makeModel.apply(value)));
        }
        return variant;
    }
}
