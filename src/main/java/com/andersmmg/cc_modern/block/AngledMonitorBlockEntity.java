package com.andersmmg.cc_modern.block;

import dan200.computercraft.shared.peripheral.monitor.MonitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AngledMonitorBlockEntity extends MonitorBlockEntity {
    public AngledMonitorBlockEntity(BlockEntityType<? extends MonitorBlockEntity> type, BlockPos pos, BlockState state, boolean advanced) {
        super(type, pos, state, advanced);
    }
}
