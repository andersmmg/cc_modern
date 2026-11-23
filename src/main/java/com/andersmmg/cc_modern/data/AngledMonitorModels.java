package com.andersmmg.cc_modern.data;

import com.andersmmg.cc_modern.CCModern;
import com.andersmmg.cc_modern.block.AngledMonitorBlock;
import com.google.gson.JsonObject;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlock;
import dan200.computercraft.shared.peripheral.monitor.MonitorEdgeState;
import net.minecraft.core.Direction;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class AngledMonitorModels {
    private static final ResourceLocation ANGLED_MONITOR_BASE =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/angled_monitor_base");
    private static final ResourceLocation WALL_ANGLED_MONITOR_UP_BASE =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_angled_monitor_up_base");
    private static final ResourceLocation WALL_ANGLED_MONITOR_DOWN_BASE =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_angled_monitor_down_base");

    // Floor variants
    private static final ResourceLocation ANGLED_MONITOR_MODEL = ANGLED_MONITOR_BASE;
    private static final ResourceLocation ANGLED_MONITOR_NORMAL_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/angled_monitor_normal");
    private static final ResourceLocation ANGLED_MONITOR_L_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/angled_monitor_l");
    private static final ResourceLocation ANGLED_MONITOR_R_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/angled_monitor_r");
    private static final ResourceLocation ANGLED_MONITOR_LR_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/angled_monitor_lr");
    private static final ResourceLocation ANGLED_MONITOR_L_NORMAL_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/angled_monitor_l_normal");
    private static final ResourceLocation ANGLED_MONITOR_R_NORMAL_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/angled_monitor_r_normal");
    private static final ResourceLocation ANGLED_MONITOR_LR_NORMAL_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/angled_monitor_lr_normal");

    // Wall UP variants
    private static final ResourceLocation WALL_ANGLED_MONITOR_UP_MODEL = WALL_ANGLED_MONITOR_UP_BASE;
    private static final ResourceLocation WALL_ANGLED_MONITOR_UP_NORMAL_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_angled_monitor_up_normal");
    private static final ResourceLocation WALL_ANGLED_MONITOR_UP_L_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_angled_monitor_up_l");
    private static final ResourceLocation WALL_ANGLED_MONITOR_UP_R_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_angled_monitor_up_r");
    private static final ResourceLocation WALL_ANGLED_MONITOR_UP_LR_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_angled_monitor_up_lr");
    private static final ResourceLocation WALL_ANGLED_MONITOR_UP_L_NORMAL_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_angled_monitor_up_l_normal");
    private static final ResourceLocation WALL_ANGLED_MONITOR_UP_R_NORMAL_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_angled_monitor_up_r_normal");
    private static final ResourceLocation WALL_ANGLED_MONITOR_UP_LR_NORMAL_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_angled_monitor_up_lr_normal");

    // Wall DOWN variants
    private static final ResourceLocation WALL_ANGLED_MONITOR_DOWN_MODEL = WALL_ANGLED_MONITOR_DOWN_BASE;
    private static final ResourceLocation WALL_ANGLED_MONITOR_DOWN_NORMAL_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_angled_monitor_down_normal");
    private static final ResourceLocation WALL_ANGLED_MONITOR_DOWN_L_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_angled_monitor_down_l");
    private static final ResourceLocation WALL_ANGLED_MONITOR_DOWN_R_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_angled_monitor_down_r");
    private static final ResourceLocation WALL_ANGLED_MONITOR_DOWN_LR_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_angled_monitor_down_lr");
    private static final ResourceLocation WALL_ANGLED_MONITOR_DOWN_L_NORMAL_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_angled_monitor_down_l_normal");
    private static final ResourceLocation WALL_ANGLED_MONITOR_DOWN_R_NORMAL_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_angled_monitor_down_r_normal");
    private static final ResourceLocation WALL_ANGLED_MONITOR_DOWN_LR_NORMAL_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_angled_monitor_down_lr_normal");

    private AngledMonitorModels() {
    }

    public static void addBlockModels(BlockModelGenerators generators) {
        var advancedPrefix = "computercraft:block/monitor_advanced";
        var basicPrefix = "computercraft:block/monitor_normal";

        // Floor
        emitVariant(generators, ANGLED_MONITOR_L_MODEL, ANGLED_MONITOR_BASE, advancedPrefix, "19", "35");
        emitVariant(generators, ANGLED_MONITOR_R_MODEL, ANGLED_MONITOR_BASE, advancedPrefix, "17", "33");
        emitVariant(generators, ANGLED_MONITOR_LR_MODEL, ANGLED_MONITOR_BASE, advancedPrefix, "18", "34");

        emitVariant(generators, ANGLED_MONITOR_NORMAL_MODEL, ANGLED_MONITOR_BASE, basicPrefix, "15", "32");
        emitVariant(generators, ANGLED_MONITOR_L_NORMAL_MODEL, ANGLED_MONITOR_BASE, basicPrefix, "19", "35");
        emitVariant(generators, ANGLED_MONITOR_R_NORMAL_MODEL, ANGLED_MONITOR_BASE, basicPrefix, "17", "33");
        emitVariant(generators, ANGLED_MONITOR_LR_NORMAL_MODEL, ANGLED_MONITOR_BASE, basicPrefix, "18", "34");

        // Wall UP
        emitVariant(generators, WALL_ANGLED_MONITOR_UP_NORMAL_MODEL, WALL_ANGLED_MONITOR_UP_BASE, basicPrefix, "15", "32");
        emitVariant(generators, WALL_ANGLED_MONITOR_UP_L_NORMAL_MODEL, WALL_ANGLED_MONITOR_UP_BASE, basicPrefix, "19", "35");
        emitVariant(generators, WALL_ANGLED_MONITOR_UP_R_NORMAL_MODEL, WALL_ANGLED_MONITOR_UP_BASE, basicPrefix, "17", "33");
        emitVariant(generators, WALL_ANGLED_MONITOR_UP_LR_NORMAL_MODEL, WALL_ANGLED_MONITOR_UP_BASE, basicPrefix, "18", "34");

        emitVariant(generators, WALL_ANGLED_MONITOR_UP_L_MODEL, WALL_ANGLED_MONITOR_UP_BASE, advancedPrefix, "19", "35");
        emitVariant(generators, WALL_ANGLED_MONITOR_UP_R_MODEL, WALL_ANGLED_MONITOR_UP_BASE, advancedPrefix, "17", "33");
        emitVariant(generators, WALL_ANGLED_MONITOR_UP_LR_MODEL, WALL_ANGLED_MONITOR_UP_BASE, advancedPrefix, "18", "34");

        // Wall DOWN
        emitVariant(generators, WALL_ANGLED_MONITOR_DOWN_NORMAL_MODEL, WALL_ANGLED_MONITOR_DOWN_BASE, basicPrefix, "15", "32");
        emitVariant(generators, WALL_ANGLED_MONITOR_DOWN_L_NORMAL_MODEL, WALL_ANGLED_MONITOR_DOWN_BASE, basicPrefix, "19", "35");
        emitVariant(generators, WALL_ANGLED_MONITOR_DOWN_R_NORMAL_MODEL, WALL_ANGLED_MONITOR_DOWN_BASE, basicPrefix, "17", "33");
        emitVariant(generators, WALL_ANGLED_MONITOR_DOWN_LR_NORMAL_MODEL, WALL_ANGLED_MONITOR_DOWN_BASE, basicPrefix, "18", "34");

        emitVariant(generators, WALL_ANGLED_MONITOR_DOWN_L_MODEL, WALL_ANGLED_MONITOR_DOWN_BASE, advancedPrefix, "19", "35");
        emitVariant(generators, WALL_ANGLED_MONITOR_DOWN_R_MODEL, WALL_ANGLED_MONITOR_DOWN_BASE, advancedPrefix, "17", "33");
        emitVariant(generators, WALL_ANGLED_MONITOR_DOWN_LR_MODEL, WALL_ANGLED_MONITOR_DOWN_BASE, advancedPrefix, "18", "34");

        var facingDispatch = facingDispatch();

        registerFamily(generators, CCModern.ANGLED_MONITOR_BLOCK.get(),
                ANGLED_MONITOR_NORMAL_MODEL, ANGLED_MONITOR_L_NORMAL_MODEL,
                ANGLED_MONITOR_R_NORMAL_MODEL, ANGLED_MONITOR_LR_NORMAL_MODEL,
                WALL_ANGLED_MONITOR_UP_NORMAL_MODEL, WALL_ANGLED_MONITOR_UP_L_NORMAL_MODEL,
                WALL_ANGLED_MONITOR_UP_R_NORMAL_MODEL, WALL_ANGLED_MONITOR_UP_LR_NORMAL_MODEL,
                WALL_ANGLED_MONITOR_DOWN_NORMAL_MODEL, WALL_ANGLED_MONITOR_DOWN_L_NORMAL_MODEL,
                WALL_ANGLED_MONITOR_DOWN_R_NORMAL_MODEL, WALL_ANGLED_MONITOR_DOWN_LR_NORMAL_MODEL,
                facingDispatch);

        registerFamily(generators, CCModern.ANGLED_MONITOR_ADVANCED_BLOCK.get(),
                ANGLED_MONITOR_MODEL, ANGLED_MONITOR_L_MODEL,
                ANGLED_MONITOR_R_MODEL, ANGLED_MONITOR_LR_MODEL,
                WALL_ANGLED_MONITOR_UP_MODEL, WALL_ANGLED_MONITOR_UP_L_MODEL,
                WALL_ANGLED_MONITOR_UP_R_MODEL, WALL_ANGLED_MONITOR_UP_LR_MODEL,
                WALL_ANGLED_MONITOR_DOWN_MODEL, WALL_ANGLED_MONITOR_DOWN_L_MODEL,
                WALL_ANGLED_MONITOR_DOWN_R_MODEL, WALL_ANGLED_MONITOR_DOWN_LR_MODEL,
                facingDispatch);
    }

    private static PropertyDispatch facingDispatch() {
        var dispatch = PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING);
        dispatch.select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R0));
        dispatch.select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));
        dispatch.select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180));
        dispatch.select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
        return dispatch;
    }

    private static void emitVariant(
            BlockModelGenerators generators, ResourceLocation modelLoc,
            ResourceLocation parentLoc,
            String texturePrefix, String screenTile, String backTile
    ) {
        generators.modelOutput.accept(modelLoc, () -> {
            var json = new JsonObject();
            json.addProperty("parent", parentLoc.toString());
            var textures = new JsonObject();
            textures.addProperty("0", texturePrefix + "_4");
            textures.addProperty("1", texturePrefix + "_" + screenTile);
            textures.addProperty("2", texturePrefix + "_32");
            textures.addProperty("3", texturePrefix + "_" + backTile);
            textures.addProperty("particle", texturePrefix + "_4");
            json.add("textures", textures);
            return json;
        });
    }

    private static ResourceLocation modelForState(
            ResourceLocation noneRef, ResourceLocation lRef,
            ResourceLocation rRef, ResourceLocation lrRef, MonitorEdgeState state
    ) {
        return switch (state) {
            case NONE -> noneRef;
            case L -> lRef;
            case R -> rRef;
            case LR -> lrRef;
            default -> noneRef;
        };
    }

    private static void registerFamily(
            BlockModelGenerators generators, AngledMonitorBlock block,
            ResourceLocation floorNone, ResourceLocation floorL,
            ResourceLocation floorR, ResourceLocation floorLR,
            ResourceLocation upNone, ResourceLocation upL,
            ResourceLocation upR, ResourceLocation upLR,
            ResourceLocation downNone, ResourceLocation downL,
            ResourceLocation downR, ResourceLocation downLR,
            PropertyDispatch facingDispatch
    ) {
        var modelDispatch = PropertyDispatch.properties(MonitorBlock.ORIENTATION, MonitorBlock.STATE);
        for (var state : MonitorBlock.STATE.getPossibleValues()) {
            modelDispatch.select(Direction.NORTH, state, Variant.variant().with(VariantProperties.MODEL, modelForState(floorNone, floorL, floorR, floorLR, state)));
            modelDispatch.select(Direction.UP, state, Variant.variant().with(VariantProperties.MODEL, modelForState(upNone, upR, upL, upLR, state)));
            modelDispatch.select(Direction.DOWN, state, Variant.variant().with(VariantProperties.MODEL, modelForState(downNone, downR, downL, downLR, state)));

            for (var otherOri : MonitorBlock.ORIENTATION.getPossibleValues()) {
                if (otherOri != Direction.NORTH && otherOri != Direction.UP && otherOri != Direction.DOWN) {
                    modelDispatch.select(otherOri, state, Variant.variant().with(VariantProperties.MODEL, modelForState(floorNone, floorL, floorR, floorLR, state)));
                }
            }
        }

        generators.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(facingDispatch)
                .with(modelDispatch)
        );
        generators.delegateItemModel(block, floorNone);
    }
}
