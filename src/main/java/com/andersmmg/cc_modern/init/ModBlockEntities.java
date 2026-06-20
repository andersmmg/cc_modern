package com.andersmmg.cc_modern.init;

import com.andersmmg.cc_modern.CCModern;
import com.andersmmg.cc_modern.block.ServerBlockEntity;
import com.andersmmg.cc_modern.block.WallMonitorBlockEntity;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CCModern.MODID);

    private static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WallMonitorBlockEntity>>[] BE_REF =
        new DeferredHolder[1];
    private static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WallMonitorBlockEntity>>[] BE_ADV_REF =
        new DeferredHolder[1];
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ServerBlockEntity>> SERVER_BE;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ServerBlockEntity>> SERVER_ADVANCED_BE;

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WallMonitorBlockEntity>> WALL_MONITOR_BE;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WallMonitorBlockEntity>> WALL_MONITOR_ADVANCED_BE;
    private static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ServerBlockEntity>>[] BE_SERVER_REF =
            new DeferredHolder[1];
    private static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ServerBlockEntity>>[] BE_SERVER_ADV_REF =
            new DeferredHolder[1];

    static {
        BE_REF[0] = BLOCK_ENTITY_TYPES.register(
            "wall_monitor",
            () -> BlockEntityType.Builder.of(
                (pos, state) -> new WallMonitorBlockEntity(BE_REF[0].get(), pos, state, false),
                CCModern.WALL_MONITOR_BLOCK.get()
            ).build(null)
        );
        WALL_MONITOR_BE = BE_REF[0];

        BE_ADV_REF[0] = BLOCK_ENTITY_TYPES.register(
            "wall_monitor_advanced",
            () -> BlockEntityType.Builder.of(
                (pos, state) -> new WallMonitorBlockEntity(BE_ADV_REF[0].get(), pos, state, true),
                CCModern.WALL_MONITOR_ADVANCED_BLOCK.get()
            ).build(null)
        );
        WALL_MONITOR_ADVANCED_BE = BE_ADV_REF[0];

        BE_SERVER_REF[0] = BLOCK_ENTITY_TYPES.register(
                "server",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ServerBlockEntity(BE_SERVER_REF[0].get(), pos, state, ComputerFamily.NORMAL),
                        CCModern.SERVER_BLOCK.get()
                ).build(null)
        );
        SERVER_BE = BE_SERVER_REF[0];

        BE_SERVER_ADV_REF[0] = BLOCK_ENTITY_TYPES.register(
                "server_advanced",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ServerBlockEntity(BE_SERVER_ADV_REF[0].get(), pos, state, ComputerFamily.ADVANCED),
                        CCModern.SERVER_ADVANCED_BLOCK.get()
                ).build(null)
        );
        SERVER_ADVANCED_BE = BE_SERVER_ADV_REF[0];
    }
}
