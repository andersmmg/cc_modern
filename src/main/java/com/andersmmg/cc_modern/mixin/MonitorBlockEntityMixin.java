package com.andersmmg.cc_modern.mixin;

import com.andersmmg.cc_modern.block.AngledMonitorBlockEntity;
import com.andersmmg.cc_modern.block.WallMonitorBlockEntity;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MonitorBlockEntity.class, remap = false)
public class MonitorBlockEntityMixin {
    @Inject(method = "isCompatible", at = @At("HEAD"), cancellable = true)
    private void cc_modern$avoidCrossTypeMerge(MonitorBlockEntity other, CallbackInfoReturnable<Boolean> cir) {
        MonitorBlockEntity self = (MonitorBlockEntity) (Object) this;
        boolean selfAngled = self instanceof AngledMonitorBlockEntity;
        boolean otherAngled = other instanceof AngledMonitorBlockEntity;

        if (selfAngled || otherAngled) {
            if (selfAngled != otherAngled || self.getBlockPos().getY() != other.getBlockPos().getY()) {
                cir.setReturnValue(false);
            }
        } else if (self instanceof WallMonitorBlockEntity ^ other instanceof WallMonitorBlockEntity) {
            cir.setReturnValue(false);
        }
    }

    @ModifyArg(
            method = "monitorTouched",
            at = @At(value = "INVOKE", target = "Ldan200/computercraft/shared/peripheral/monitor/XYPair;add(FF)Ldan200/computercraft/shared/peripheral/monitor/XYPair;"),
            index = 0,
            remap = false
    )
    private float cc_modern$fixMonitorTouchXIndex(float xIndex) {
        MonitorBlockEntity self = (MonitorBlockEntity) (Object) this;
        if (!(self instanceof AngledMonitorBlockEntity)) return xIndex;
        return self.getWidth() - 1 - (int) xIndex;
    }
}
