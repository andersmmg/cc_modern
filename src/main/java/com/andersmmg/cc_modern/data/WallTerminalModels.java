package com.andersmmg.cc_modern.data;

import com.andersmmg.cc_modern.CCModern;
import com.google.gson.JsonObject;
import com.sashafiesta.ccterminals.TerminalBlock;
import com.sashafiesta.ccterminals.TerminalDisplayState;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import static net.minecraft.data.models.model.ModelLocationUtils.getModelLocation;

public final class WallTerminalModels {
    private static final ResourceLocation WALL_SCREEN =
            ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "block/wall_screen");
    private static final String TERMINAL_TEXTURES = "ccterminals:block/";

    private WallTerminalModels() {
    }

    /**
     * @param textures the CC: Terminals texture set to borrow, either {@code terminal_normal} or
     *                 {@code terminal_advanced}.
     */
    public static void addBlockModels(BlockModelGenerators generators, Block block, String textures) {
        emitModel(generators, modelLocation(block, TerminalDisplayState.ON),
                "ccterminals:block/terminal_on", textures);
        emitModel(generators, modelLocation(block, TerminalDisplayState.BLINKING),
                "ccterminals:block/terminal_blink", textures);

        generators.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(WallBlockGeneratorsUtil.facingDispatch())
                .with(WallBlockGeneratorsUtil.modelDispatch(TerminalBlock.STATE, state -> modelLocation(block, state)))
        );

        generators.delegateItemModel(block, modelLocation(block, TerminalDisplayState.ON));
    }

    private static void emitModel(BlockModelGenerators generators, ResourceLocation modelLocation, String cursor,
                                  String textures) {
        generators.modelOutput.accept(modelLocation, () -> {
            var json = new JsonObject();
            json.addProperty("parent", WALL_SCREEN.toString());
            var texturesJson = new JsonObject();
            texturesJson.addProperty("particle", TERMINAL_TEXTURES + textures + "_side");
            texturesJson.addProperty("front", TERMINAL_TEXTURES + textures + "_front");
            texturesJson.addProperty("back", TERMINAL_TEXTURES + textures + "_side");
            texturesJson.addProperty("side", TERMINAL_TEXTURES + textures + "_side");
            texturesJson.addProperty("top", TERMINAL_TEXTURES + textures + "_top");
            texturesJson.addProperty("cursor", cursor);
            json.add("textures", texturesJson);
            return json;
        });
    }

    private static ResourceLocation modelLocation(Block block, TerminalDisplayState state) {
        return getModelLocation(block, state == TerminalDisplayState.ON ? "" : "_" + state.getSerializedName());
    }
}
