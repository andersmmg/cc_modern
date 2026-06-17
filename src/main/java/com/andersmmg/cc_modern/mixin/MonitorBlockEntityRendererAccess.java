package com.andersmmg.cc_modern.mixin;

import dan200.computercraft.client.render.monitor.MonitorBlockEntityRenderer;
import dan200.computercraft.client.render.monitor.MonitorRenderState;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.shared.peripheral.monitor.ClientMonitor;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MonitorBlockEntityRenderer.class)
public interface MonitorBlockEntityRendererAccess {
    @Invoker("renderTerminal")
    void cc_modern$renderTerminal(Matrix4f matrix, ClientMonitor monitor, MonitorRenderState renderState, Terminal terminal, float xMargin, float yMargin);
}
