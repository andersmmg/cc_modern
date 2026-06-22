package com.andersmmg.cc_modern.mixin;

import dan200.computercraft.shared.peripheral.monitor.MonitorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MonitorBlockEntity.class)
public interface MonitorBlockEntityAccess {
    @Invoker("monitorTouched")
    void cc_modern$monitorTouched(float x, float y, float z);
}
