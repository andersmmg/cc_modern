package com.andersmmg.cc_modern.mixin;

import com.andersmmg.cc_modern.block.WallMonitorBlockEntity;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MonitorBlockEntity.class, remap = false)
public class MonitorBlockEntityMixin {

    @Inject(method = "isCompatible", at = @At("HEAD"), cancellable = true)
    private void cc_modern$avoidCrossTypeMerge(MonitorBlockEntity other, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof WallMonitorBlockEntity ^ other instanceof WallMonitorBlockEntity) {
            cir.setReturnValue(false);
        }
    }
}
