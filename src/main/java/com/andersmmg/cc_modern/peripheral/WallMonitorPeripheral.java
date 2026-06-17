package com.andersmmg.cc_modern.peripheral;

import com.andersmmg.cc_modern.block.WallMonitorBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.monitor.MonitorPeripheral;

import java.util.Set;

public class WallMonitorPeripheral extends MonitorPeripheral {
    private static final Set<String> ADDITIONAL_TYPES = Set.of("wall_monitor");

    public WallMonitorPeripheral(WallMonitorBlockEntity monitor) {
        super(monitor);
    }

    @Override
    public Set<String> getAdditionalTypes() {
        return ADDITIONAL_TYPES;
    }

    @Override
    public boolean equals(IPeripheral other) {
        return other instanceof WallMonitorPeripheral && super.equals(other);
    }

    @LuaFunction
    public final MultiBlockInfo getMultiBlockInfo() {
        var target = getTarget();
        if (target instanceof WallMonitorBlockEntity monitor) {
            return new MultiBlockInfo(monitor.getXIndex(), monitor.getYIndex(), monitor.getWidth(), monitor.getHeight());
        }
        return new MultiBlockInfo(0, 0, 0, 0);
    }

    public record MultiBlockInfo(int x, int y, int width, int height) { }
}
