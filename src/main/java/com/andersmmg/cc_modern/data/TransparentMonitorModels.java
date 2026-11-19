package com.andersmmg.cc_modern.data;

import com.andersmmg.cc_modern.CCModern;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlock;
import dan200.computercraft.shared.peripheral.monitor.MonitorEdgeState;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public final class TransparentMonitorModels {
    private static final ResourceLocation BASE_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/transparent_monitor_glass");
    private static final ResourceLocation WALL_BASE_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_transparent_monitor_glass");

    private TransparentMonitorModels() {
    }

    public static void addBlockModels(BlockModelGenerators generators) {
        registerFamily(generators, CCModern.TRANSPARENT_MONITOR_BLOCK.get(), "transparent_monitor_glass", BASE_MODEL);
        registerFamily(generators, CCModern.TRANSPARENT_MONITOR_ADVANCED_BLOCK.get(), "transparent_monitor_glass", BASE_MODEL);
        registerFamily(generators, CCModern.TRANSPARENT_WALL_MONITOR_BLOCK.get(), "wall_transparent_monitor_glass", WALL_BASE_MODEL);
        registerFamily(generators, CCModern.TRANSPARENT_WALL_MONITOR_ADVANCED_BLOCK.get(), "wall_transparent_monitor_glass", WALL_BASE_MODEL);
    }

    private static void registerFamily(BlockModelGenerators generators, Block block, String prefix, ResourceLocation baseModel) {
        for (var state : MonitorEdgeState.values()) {
            var loc = modelLocation(prefix, state);
            generators.modelOutput.accept(loc, variantModel(state, baseModel));
        }

        generators.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(WallBlockGeneratorsUtil.facingDispatch())
                .with(WallBlockGeneratorsUtil.modelDispatch(MonitorBlock.STATE, state -> modelLocation(prefix, state)))
        );
        generators.delegateItemModel(block, modelLocation(prefix, MonitorEdgeState.NONE));
    }

    private static ResourceLocation modelLocation(String prefix, MonitorEdgeState state) {
        return ResourceLocation.fromNamespaceAndPath(
                CCModern.MODID, "block/" + prefix + "_" + state.getSerializedName()
        );
    }

    private static Supplier<JsonElement> variantModel(MonitorEdgeState state, ResourceLocation baseModel) {
        return () -> {
            JsonObject json = new JsonObject();
            json.addProperty("parent", baseModel.toString());
            JsonObject textures = new JsonObject();
            textures.addProperty("border_glass",
                    CCModern.MODID + ":block/glass_border_" + state.getSerializedName());
            json.add("textures", textures);
            return json;
        };
    }
}
