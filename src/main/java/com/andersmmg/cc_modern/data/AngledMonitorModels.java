package com.andersmmg.cc_modern.data;

import com.andersmmg.cc_modern.CCModern;
import com.andersmmg.cc_modern.block.AngledMonitorBlock;
import com.google.gson.JsonObject;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class AngledMonitorModels {
    private static final ResourceLocation ANGLED_MONITOR_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/angled_monitor");
    private static final ResourceLocation ANGLED_MONITOR_NORMAL_MODEL =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/angled_monitor_normal");

    private AngledMonitorModels() {
    }

    public static void addBlockModels(BlockModelGenerators generators) {
        emitBasicModel(generators);

        var facingDispatch = PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING);
        facingDispatch.select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R0));
        facingDispatch.select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));
        facingDispatch.select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180));
        facingDispatch.select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));

        registerFamily(generators, CCModern.ANGLED_MONITOR_BLOCK.get(), ANGLED_MONITOR_NORMAL_MODEL, facingDispatch);
        registerFamily(generators, CCModern.ANGLED_MONITOR_ADVANCED_BLOCK.get(), ANGLED_MONITOR_MODEL, facingDispatch);
    }

    private static void emitBasicModel(BlockModelGenerators generators) {
        generators.modelOutput.accept(ANGLED_MONITOR_NORMAL_MODEL, () -> {
            var json = new JsonObject();
            json.addProperty("parent", "cc_modern:block/angled_monitor");
            var textures = new JsonObject();
            textures.addProperty("0", "computercraft:block/monitor_normal_4");
            textures.addProperty("1", "computercraft:block/monitor_normal_15");
            textures.addProperty("2", "computercraft:block/monitor_normal_0");
            textures.addProperty("particle", "computercraft:block/monitor_normal_4");
            json.add("textures", textures);
            return json;
        });
    }

    private static void registerFamily(
            BlockModelGenerators generators, AngledMonitorBlock block,
            ResourceLocation modelRef, PropertyDispatch facingDispatch
    ) {
        var modelDispatch = WallBlockGeneratorsUtil.modelDispatch(MonitorBlock.STATE, state -> modelRef);
        generators.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(facingDispatch)
                .with(modelDispatch)
        );
        generators.delegateItemModel(block, modelRef);
    }
}
