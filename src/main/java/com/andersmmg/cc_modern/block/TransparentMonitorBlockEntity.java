package com.andersmmg.cc_modern.block;

import dan200.computercraft.shared.peripheral.monitor.MonitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TransparentMonitorBlockEntity extends MonitorBlockEntity {
    private final boolean cc_modern$advanced;

    public TransparentMonitorBlockEntity(BlockEntityType<? extends MonitorBlockEntity> type, BlockPos pos, BlockState state, boolean advanced) {
        super(type, pos, state, advanced);
        this.cc_modern$advanced = advanced;
    }

    public boolean cc_modern$isAdvanced() {
        return cc_modern$advanced;
    }
}
