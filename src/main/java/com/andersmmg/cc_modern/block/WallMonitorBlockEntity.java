package com.andersmmg.cc_modern.block;

import dan200.computercraft.shared.peripheral.monitor.MonitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class WallMonitorBlockEntity extends MonitorBlockEntity {
    public WallMonitorBlockEntity(BlockEntityType<? extends MonitorBlockEntity> type, BlockPos pos, BlockState state, boolean advanced) {
        super(type, pos, state, advanced);
    }
}
