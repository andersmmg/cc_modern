package com.andersmmg.cc_modern.init;

import com.andersmmg.cc_modern.CCModern;
import com.andersmmg.cc_modern.block.WallMonitorBlockEntity;
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WallMonitorBlockEntity>> WALL_MONITOR_BE;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WallMonitorBlockEntity>> WALL_MONITOR_ADVANCED_BE;

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
    }
}
