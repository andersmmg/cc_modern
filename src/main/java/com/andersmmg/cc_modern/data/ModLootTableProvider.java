package com.andersmmg.cc_modern.data;

import com.andersmmg.cc_modern.CCModern;
import dan200.computercraft.shared.data.BlockNamedEntityLootCondition;
import dan200.computercraft.shared.data.HasComputerIdLootCondition;
import dan200.computercraft.shared.data.PlayerCreativeLootCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends LootTableProvider {
    private static final List<Block> KNOWN_BLOCKS = List.of(
            CCModern.WALL_MONITOR_BLOCK.get(),
            CCModern.WALL_MONITOR_ADVANCED_BLOCK.get(),
            CCModern.ANGLED_MONITOR_BLOCK.get(),
            CCModern.ANGLED_MONITOR_ADVANCED_BLOCK.get(),
            CCModern.SERVER_BLOCK.get(),
            CCModern.SERVER_ADVANCED_BLOCK.get()
    );

    public ModLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(ModBlockLootSubProvider::new, LootContextParamSets.BLOCK)
        ), lookup);
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

            add(CCModern.SERVER_BLOCK.get(), computerLootTable(CCModern.SERVER_BLOCK.get()));
            add(CCModern.SERVER_ADVANCED_BLOCK.get(), computerLootTable(CCModern.SERVER_ADVANCED_BLOCK.get()));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return KNOWN_BLOCKS;
        }
    }
}
