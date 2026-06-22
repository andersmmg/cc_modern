package com.andersmmg.cc_modern;

import com.andersmmg.cc_modern.block.AngledMonitorBlock;
import com.andersmmg.cc_modern.block.ServerBlock;
import com.andersmmg.cc_modern.block.WallMonitorBlock;
import com.andersmmg.cc_modern.init.HolderRegistryEntry;
import com.andersmmg.cc_modern.init.ModBlockEntities;
import com.andersmmg.cc_modern.init.ModPeripheralProviders;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CCModern.MODID)
public class CCModern {
    public static final String MODID = "cc_modern";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<WallMonitorBlock> WALL_MONITOR_BLOCK = BLOCKS.register(
        "wall_monitor",
        () -> new WallMonitorBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.5F)
                .requiresCorrectToolForDrops(),
            new HolderRegistryEntry<>(
                ModBlockEntities.WALL_MONITOR_BE,
                ModBlockEntities.WALL_MONITOR_BE.unwrapKey().orElseThrow().location()
            )
        )
    );
    public static final DeferredItem<BlockItem> WALL_MONITOR_BLOCK_ITEM =
        ITEMS.registerSimpleBlockItem("wall_monitor", WALL_MONITOR_BLOCK);

    public static final DeferredBlock<WallMonitorBlock> WALL_MONITOR_ADVANCED_BLOCK = BLOCKS.register(
        "wall_monitor_advanced",
        () -> new WallMonitorBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.GOLD)
                .strength(2.5F)
                .requiresCorrectToolForDrops(),
            new HolderRegistryEntry<>(
                ModBlockEntities.WALL_MONITOR_ADVANCED_BE,
                ModBlockEntities.WALL_MONITOR_ADVANCED_BE.unwrapKey().orElseThrow().location()
            )
        )
    );
    public static final DeferredItem<BlockItem> WALL_MONITOR_ADVANCED_BLOCK_ITEM =
        ITEMS.registerSimpleBlockItem("wall_monitor_advanced", WALL_MONITOR_ADVANCED_BLOCK);

    public static final DeferredBlock<ServerBlock> SERVER_BLOCK = BLOCKS.register(
            "server",
            () -> new ServerBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(2.5F)
                            .requiresCorrectToolForDrops(),
                    new HolderRegistryEntry<>(
                            ModBlockEntities.SERVER_BE,
                            ModBlockEntities.SERVER_BE.unwrapKey().orElseThrow().location()
                    )
            )
    );
    public static final DeferredItem<BlockItem> SERVER_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("server", SERVER_BLOCK);

    public static final DeferredBlock<ServerBlock> SERVER_ADVANCED_BLOCK = BLOCKS.register(
            "server_advanced",
            () -> new ServerBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GOLD)
                            .strength(2.5F)
                            .requiresCorrectToolForDrops(),
                    new HolderRegistryEntry<>(
                            ModBlockEntities.SERVER_ADVANCED_BE,
                            ModBlockEntities.SERVER_ADVANCED_BE.unwrapKey().orElseThrow().location()
                    )
            )
    );
    public static final DeferredItem<BlockItem> SERVER_ADVANCED_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("server_advanced", SERVER_ADVANCED_BLOCK);

    public static final DeferredBlock<AngledMonitorBlock> ANGLED_MONITOR_BLOCK = BLOCKS.register(
            "angled_monitor",
            () -> new AngledMonitorBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(2.5F)
                            .requiresCorrectToolForDrops(),
                    new HolderRegistryEntry<>(
                            ModBlockEntities.ANGLED_MONITOR_BE,
                            ModBlockEntities.ANGLED_MONITOR_BE.unwrapKey().orElseThrow().location()
                    )
            )
    );
    public static final DeferredItem<BlockItem> ANGLED_MONITOR_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("angled_monitor", ANGLED_MONITOR_BLOCK);

    public static final DeferredBlock<AngledMonitorBlock> ANGLED_MONITOR_ADVANCED_BLOCK = BLOCKS.register(
            "angled_monitor_advanced",
            () -> new AngledMonitorBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GOLD)
                            .strength(2.5F)
                            .requiresCorrectToolForDrops(),
                    new HolderRegistryEntry<>(
                            ModBlockEntities.ANGLED_MONITOR_ADVANCED_BE,
                            ModBlockEntities.ANGLED_MONITOR_ADVANCED_BE.unwrapKey().orElseThrow().location()
                    )
            )
    );
    public static final DeferredItem<BlockItem> ANGLED_MONITOR_ADVANCED_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("angled_monitor_advanced", ANGLED_MONITOR_ADVANCED_BLOCK);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MOD_TAB = CREATIVE_TABS.register("tab", () -> CreativeModeTab.builder()
        .title(Component.translatable("creativetab." + MODID + ".tab"))
            .icon(() -> new ItemStack(SERVER_BLOCK_ITEM.get()))
        .displayItems((parameters, output) -> {
            output.accept(WALL_MONITOR_BLOCK_ITEM.get());
            output.accept(WALL_MONITOR_ADVANCED_BLOCK_ITEM.get());
            output.accept(ANGLED_MONITOR_BLOCK_ITEM.get());
            output.accept(ANGLED_MONITOR_ADVANCED_BLOCK_ITEM.get());
            output.accept(SERVER_BLOCK_ITEM.get());
            output.accept(SERVER_ADVANCED_BLOCK_ITEM.get());
        })
        .build());

    public CCModern(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);

        ModPeripheralProviders.register(modEventBus);
    }
}
