package com.andersmmg.cc_modern.data;

import com.andersmmg.cc_modern.CCModern;
import com.andersmmg.cc_modern.block.TransparentMonitorBlock;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlock;
import dan200.computercraft.shared.peripheral.monitor.MonitorEdgeState;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public final class TransparentMonitorModels {
    private static final ResourceLocation BASE_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/transparent_monitor_glass");

    private TransparentMonitorModels() {
    }

    public static void addBlockModels(BlockModelGenerators generators) {
        registerFamily(generators, CCModern.TRANSPARENT_MONITOR_BLOCK.get());
        registerFamily(generators, CCModern.TRANSPARENT_MONITOR_ADVANCED_BLOCK.get());
    }

    private static void registerFamily(BlockModelGenerators generators, TransparentMonitorBlock block) {
        for (var state : MonitorEdgeState.values()) {
            var loc = modelLocation(state);
            generators.modelOutput.accept(loc, variantModel(state));
        }

        generators.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(WallBlockGeneratorsUtil.facingDispatch())
                .with(WallBlockGeneratorsUtil.modelDispatch(MonitorBlock.STATE, TransparentMonitorModels::modelLocation))
        );
        generators.delegateItemModel(block, modelLocation(MonitorEdgeState.NONE));
    }

    private static ResourceLocation modelLocation(MonitorEdgeState state) {
        return ResourceLocation.fromNamespaceAndPath(
                CCModern.MODID, "block/transparent_monitor_glass_" + state.getSerializedName()
        );
    }

    private static Supplier<JsonElement> variantModel(MonitorEdgeState state) {
        return () -> {
            JsonObject json = new JsonObject();
            json.addProperty("parent", BASE_MODEL.toString());
            JsonObject textures = new JsonObject();
            textures.addProperty("border_glass",
                    CCModern.MODID + ":block/glass_border_" + state.getSerializedName());
            json.add("textures", textures);
            return json;
        };
    }
}
