package com.andersmmg.cc_modern.compat;

import com.andersmmg.cc_modern.block.WallTerminalBlock;
import com.andersmmg.cc_modern.block.WallTerminalBlockEntity;
import com.andersmmg.cc_modern.init.HolderRegistryEntry;
import com.sashafiesta.ccterminals.TerminalBlockEntity;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * CC: Terminals references are here, so it can be conditionally loaded
 */
public final class WallTerminalRegistration {
    private static DeferredBlock<WallTerminalBlock> block;
    private static DeferredBlock<WallTerminalBlock> advancedBlock;
    private static DeferredItem<BlockItem> item;
    private static DeferredItem<BlockItem> advancedItem;
    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<TerminalBlockEntity>> blockEntity;
    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<TerminalBlockEntity>> advancedBlockEntity;

    private WallTerminalRegistration() {
    }

    public static void register(DeferredRegister.Blocks blocks, DeferredRegister.Items items,
                                DeferredRegister<BlockEntityType<?>> blockEntityTypes) {
        block = blocks.register(
                "wall_terminal",
                () -> new WallTerminalBlock(
                        properties(MapColor.STONE),
                        new HolderRegistryEntry<>(
                                blockEntity,
                                blockEntity.unwrapKey().orElseThrow().location()
                        ),
                        false
                )
        );
        blockEntity = blockEntityTypes.register(
                "wall_terminal",
                () -> BlockEntityType.Builder.<TerminalBlockEntity>of(
                        (pos, state) -> new WallTerminalBlockEntity(blockEntity.get(), pos, state, false),
                        block.get()
                ).build(null)
        );
        item = items.registerSimpleBlockItem("wall_terminal", block);

        advancedBlock = blocks.register(
                "wall_terminal_advanced",
                () -> new WallTerminalBlock(
                        properties(MapColor.GOLD),
                        new HolderRegistryEntry<>(
                                advancedBlockEntity,
                                advancedBlockEntity.unwrapKey().orElseThrow().location()
                        ),
                        true
                )
        );
        advancedBlockEntity = blockEntityTypes.register(
                "wall_terminal_advanced",
                () -> BlockEntityType.Builder.<TerminalBlockEntity>of(
                        (pos, state) -> new WallTerminalBlockEntity(advancedBlockEntity.get(), pos, state, true),
                        advancedBlock.get()
                ).build(null)
        );
        advancedItem = items.registerSimpleBlockItem("wall_terminal_advanced", advancedBlock);
    }

    private static BlockBehaviour.Properties properties(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .strength(2.5F)
                .requiresCorrectToolForDrops();
    }

    public static Supplier<Block> block() {
        return () -> block.get();
    }

    public static Supplier<Block> advancedBlock() {
        return () -> advancedBlock.get();
    }

    public static DeferredItem<BlockItem> item() {
        return item;
    }

    public static DeferredItem<BlockItem> advancedItem() {
        return advancedItem;
    }

    public static void registerPeripheral(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                PeripheralCapability.get(),
                blockEntity.get(),
                (be, side) -> be.peripheral()
        );
        event.registerBlockEntity(
                PeripheralCapability.get(),
                advancedBlockEntity.get(),
                (be, side) -> be.peripheral()
        );
    }
}
