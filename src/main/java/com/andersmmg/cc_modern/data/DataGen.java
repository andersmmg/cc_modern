package com.andersmmg.cc_modern.data;

import com.andersmmg.cc_modern.CCModern;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@EventBusSubscriber(modid = CCModern.MODID)
public class DataGen {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        var gen = event.getGenerator();
        var packOutput = gen.getPackOutput();
        var lookup = event.getLookupProvider();

        gen.addProvider(event.includeClient(), new WallBlockModelProvider(packOutput));

        gen.addProvider(event.includeServer(), new ModLootTableProvider(packOutput, lookup));
        gen.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookup));
    }

    private static class WallBlockModelProvider implements DataProvider {
        private final PackOutput.PathProvider blockStatePath;
        private final PackOutput.PathProvider modelPath;

        public WallBlockModelProvider(PackOutput output) {
            blockStatePath = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
            modelPath = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
        }

        @Override
        public CompletableFuture<?> run(CachedOutput output) {
            Map<Block, BlockStateGenerator> blockStates = new HashMap<>();
            Map<ResourceLocation, Supplier<JsonElement>> models = new HashMap<>();
            Set<Item> explicitItems = new HashSet<>();

            var generators = new BlockModelGenerators(generator -> blockStates.put(generator.getBlock(), generator), models::put, explicitItems::add);
            WallMonitorModels.addBlockModels(generators);
            ServerModels.addBlockModels(generators);

            for (var block : BuiltInRegistries.BLOCK) {
                if (!blockStates.containsKey(block)) continue;
                var item = Item.BY_BLOCK.get(block);
                if (item == null || explicitItems.contains(item)) continue;
                var model = ModelLocationUtils.getModelLocation(item);
                if (!models.containsKey(model)) {
                    models.put(model, new DelegatedModel(ModelLocationUtils.getModelLocation(block)));
                }
            }

            List<CompletableFuture<?>> futures = new ArrayList<>();
            blockStates.forEach((block, generator) -> {
                var path = blockStatePath.json(BuiltInRegistries.BLOCK.getKey(block));
                futures.add(DataProvider.saveStable(output, generator.get(), path));
            });
            models.forEach((location, supplier) -> {
                var path = modelPath.json(location);
                futures.add(DataProvider.saveStable(output, supplier.get(), path));
            });
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        }

        @Override
        public String getName() {
            return "Wall Block Models";
        }
    }
}
