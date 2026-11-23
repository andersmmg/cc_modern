package com.andersmmg.cc_modern.data;

import com.andersmmg.cc_modern.CCModern;
import com.andersmmg.cc_modern.compat.CCTerminalsCompat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dan200.computercraft.shared.data.BlockNamedEntityLootCondition;
import dan200.computercraft.shared.data.HasComputerIdLootCondition;
import dan200.computercraft.shared.data.PlayerCreativeLootCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends LootTableProvider {
    private static final List<Block> KNOWN_BLOCKS = knownBlocks();

    private final CompletableFuture<HolderLookup.Provider> registries;
    private final PackOutput.PathProvider lootTablePath;

    public ModLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(ModBlockLootSubProvider::new, LootContextParamSets.BLOCK)
        ), lookup);
        this.registries = lookup;
        this.lootTablePath = output.createRegistryElementsPathProvider(Registries.LOOT_TABLE);
    }

    private static List<Block> knownBlocks() {
        return List.of(
                CCModern.WALL_MONITOR_BLOCK.get(),
                CCModern.WALL_MONITOR_ADVANCED_BLOCK.get(),
                CCModern.ANGLED_MONITOR_BLOCK.get(),
                CCModern.ANGLED_MONITOR_ADVANCED_BLOCK.get(),
                CCModern.TRANSPARENT_MONITOR_BLOCK.get(),
                CCModern.TRANSPARENT_MONITOR_ADVANCED_BLOCK.get(),
                CCModern.TRANSPARENT_WALL_MONITOR_BLOCK.get(),
                CCModern.TRANSPARENT_WALL_MONITOR_ADVANCED_BLOCK.get(),
                CCModern.SERVER_BLOCK.get(),
                CCModern.SERVER_ADVANCED_BLOCK.get()
        );
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return super.run(output)
                .thenCompose(ignored -> registries.thenCompose(lookup -> writeWallTerminalTables(output, lookup)));
    }

    private CompletableFuture<?> writeWallTerminalTables(CachedOutput output, HolderLookup.Provider lookup) {
        var blockLoot = new ModBlockLootSubProvider(lookup);
        var futures = new ArrayList<CompletableFuture<?>>();
        if (CCModern.WALL_TERMINAL_BLOCK != null) {
            futures.add(writeWallTerminalTable(output, lookup, blockLoot, CCModern.WALL_TERMINAL_BLOCK.get()));
        }
        if (CCModern.WALL_TERMINAL_ADVANCED_BLOCK != null) {
            futures.add(writeWallTerminalTable(output, lookup, blockLoot, CCModern.WALL_TERMINAL_ADVANCED_BLOCK.get()));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> writeWallTerminalTable(CachedOutput output, HolderLookup.Provider lookup,
                                                        ModBlockLootSubProvider blockLoot, Block block) {
        var id = block.getLootTable().location();
        var table = blockLoot.createSingleItemTable(block)
                .setParamSet(LootContextParamSets.BLOCK)
                .setRandomSequence(id)
                .build();
        var ops = RegistryOps.create(JsonOps.INSTANCE, lookup);
        JsonElement json = LootTable.DIRECT_CODEC.encodeStart(ops, table).getOrThrow();
        ICondition.writeConditions(ops, (JsonObject) json,
                List.of(new ModLoadedCondition(CCTerminalsCompat.MOD_ID)));
        return DataProvider.saveStable(output, json, lootTablePath.json(id));
    }

    private static final class ModBlockLootSubProvider extends BlockLootSubProvider {
        ModBlockLootSubProvider(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        private static LootTable.Builder computerLootTable(Block block) {
            return LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(block)
                                    .apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)))
                            .when(AnyOfCondition.anyOf(
                                    BlockNamedEntityLootCondition.BUILDER,
                                    HasComputerIdLootCondition.BUILDER,
                                    PlayerCreativeLootCondition.BUILDER.invert()
                            ))
                    );
        }

        @Override
        protected void generate() {
            dropSelf(CCModern.WALL_MONITOR_BLOCK.get());
            dropSelf(CCModern.WALL_MONITOR_ADVANCED_BLOCK.get());
            dropSelf(CCModern.ANGLED_MONITOR_BLOCK.get());
            dropSelf(CCModern.ANGLED_MONITOR_ADVANCED_BLOCK.get());
            dropSelf(CCModern.TRANSPARENT_MONITOR_BLOCK.get());
            dropSelf(CCModern.TRANSPARENT_MONITOR_ADVANCED_BLOCK.get());
            dropSelf(CCModern.TRANSPARENT_WALL_MONITOR_BLOCK.get());
            dropSelf(CCModern.TRANSPARENT_WALL_MONITOR_ADVANCED_BLOCK.get());

            add(CCModern.SERVER_BLOCK.get(), computerLootTable(CCModern.SERVER_BLOCK.get()));
            add(CCModern.SERVER_ADVANCED_BLOCK.get(), computerLootTable(CCModern.SERVER_ADVANCED_BLOCK.get()));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return KNOWN_BLOCKS;
        }
    }
}
