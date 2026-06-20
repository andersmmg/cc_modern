package com.andersmmg.cc_modern.data;

import com.andersmmg.cc_modern.CCModern;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    private static final ResourceLocation COMPUTER_NORMAL =
            ResourceLocation.fromNamespaceAndPath("computercraft", "computer_normal");
    private static final ResourceLocation COMPUTER_ADVANCED =
            ResourceLocation.fromNamespaceAndPath("computercraft", "computer_advanced");
    private static final ResourceLocation DISK_DRIVE =
            ResourceLocation.fromNamespaceAndPath("computercraft", "disk_drive");
    private static final ResourceLocation WIRELESS_MODEM_NORMAL =
            ResourceLocation.fromNamespaceAndPath("computercraft", "wireless_modem_normal");
    private static final TagKey<Item> GLASS_PANES =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "glass_panes"));

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, CCModern.WALL_MONITOR_BLOCK.get(), 2)
                .pattern("SSS")
                .pattern("SGS")
                .define('G', GLASS_PANES)
                .define('S', Items.STONE)
                .unlockedBy("has_computer_normal", has(BuiltInRegistries.ITEM.get(COMPUTER_NORMAL)))
                .save(output, ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "wall_monitor"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, CCModern.WALL_MONITOR_ADVANCED_BLOCK.get())
                .pattern("III")
                .pattern("IGI")
                .define('G', GLASS_PANES)
                .define('I', Items.GOLD_INGOT)
                .unlockedBy("has_computer_advanced", has(BuiltInRegistries.ITEM.get(COMPUTER_ADVANCED)))
                .save(output, ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "wall_monitor_advanced"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, CCModern.WALL_MONITOR_ADVANCED_BLOCK.get())
                .requires(CCModern.WALL_MONITOR_BLOCK.get())
                .requires(Items.GOLD_INGOT, 5)
                .unlockedBy("has_computer_advanced", has(BuiltInRegistries.ITEM.get(COMPUTER_ADVANCED)))
                .save(output, ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "wall_monitor_advanced_from_normal"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, CCModern.SERVER_BLOCK.get())
                .requires(BuiltInRegistries.ITEM.get(COMPUTER_NORMAL))
                .requires(BuiltInRegistries.ITEM.get(DISK_DRIVE))
                .requires(BuiltInRegistries.ITEM.get(WIRELESS_MODEM_NORMAL))
                .unlockedBy("has_computer_normal", has(BuiltInRegistries.ITEM.get(COMPUTER_NORMAL)))
                .save(output, ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "server"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, CCModern.SERVER_ADVANCED_BLOCK.get())
                .requires(BuiltInRegistries.ITEM.get(COMPUTER_ADVANCED))
                .requires(BuiltInRegistries.ITEM.get(DISK_DRIVE))
                .requires(BuiltInRegistries.ITEM.get(WIRELESS_MODEM_NORMAL))
                .unlockedBy("has_computer_advanced", has(BuiltInRegistries.ITEM.get(COMPUTER_ADVANCED)))
                .save(output, ResourceLocation.fromNamespaceAndPath(CCModern.MODID, "server_advanced"));
    }
}
