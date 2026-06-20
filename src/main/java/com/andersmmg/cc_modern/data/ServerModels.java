package com.andersmmg.cc_modern.data;

import com.andersmmg.cc_modern.CCModern;
import com.andersmmg.cc_modern.block.ServerBlock;
import com.google.gson.JsonObject;
import dan200.computercraft.shared.computer.core.ComputerState;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.resources.ResourceLocation;

import static net.minecraft.data.models.model.ModelLocationUtils.getModelLocation;

public final class ServerModels {
    private static final ResourceLocation WALL_BASE =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_base");
    private static final ResourceLocation WALL_SCREEN =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_screen");

    private ServerModels() {
    }

    public static void addBlockModels(BlockModelGenerators generators) {
        registerFamily(generators, CCModern.SERVER_BLOCK.get(), "computer_normal",
                "computercraft:block/computer_on", "computercraft:block/computer_blink");
        registerFamily(generators, CCModern.SERVER_ADVANCED_BLOCK.get(), "computer_advanced",
                "computercraft:block/computer_on", "computercraft:block/computer_blink");
    }

    private static void registerFamily(
            BlockModelGenerators generators, ServerBlock block, String textureBase,
            String cursorOn, String cursorBlink
    ) {
        emitModel(generators, modelLocation(block, ComputerState.OFF), WALL_BASE, boxTextures(textureBase, null));
        emitModel(generators, modelLocation(block, ComputerState.ON), WALL_SCREEN, boxTextures(textureBase, cursorOn));
        emitModel(generators, modelLocation(block, ComputerState.BLINKING), WALL_SCREEN, boxTextures(textureBase, cursorBlink));

        generators.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(WallBlockGeneratorsUtil.facingDispatch())
                .with(WallBlockGeneratorsUtil.modelDispatch(ServerBlock.STATE, state -> modelLocation(block, state)))
        );

        generators.delegateItemModel(block, modelLocation(block, ComputerState.BLINKING));
    }

    private static void emitModel(
            BlockModelGenerators generators, ResourceLocation modelLoc, ResourceLocation parent, TextureMap textures
    ) {
        generators.modelOutput.accept(modelLoc, () -> {
            var json = new JsonObject();
            json.addProperty("parent", parent.toString());
            json.add("textures", textures.toJson());
            return json;
        });
    }


    private static TextureMap boxTextures(String textureBase, String cursor) {
        var map = new TextureMap()
                .put("particle", "computercraft:block/" + textureBase + "_side")
                .put("front", "computercraft:block/" + textureBase + "_front")
                .put("back", "computercraft:block/" + textureBase + "_side")
                .put("side", "computercraft:block/" + textureBase + "_side")
                .put("top", "computercraft:block/" + textureBase + "_top");
        if (cursor != null) map.put("cursor", cursor);
        return map;
    }

    private static ResourceLocation modelLocation(ServerBlock block, ComputerState state) {
        var suffix = state == ComputerState.OFF ? "" : "_" + state.getSerializedName();
        return getModelLocation(block, suffix);
    }

    private static final class TextureMap {
        private final JsonObject json = new JsonObject();

        TextureMap put(String name, String resourcePath) {
            json.addProperty(name, resourcePath);
            return this;
        }

        JsonObject toJson() {
            return json;
        }
    }
}
