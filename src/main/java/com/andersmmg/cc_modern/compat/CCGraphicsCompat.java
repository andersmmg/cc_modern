package com.andersmmg.cc_modern.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import dan200.computercraft.client.render.monitor.MonitorRenderState;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.shared.peripheral.monitor.ClientMonitor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Method;

public class CCGraphicsCompat {
    private static boolean checked = false;
    private static boolean available = false;

    private static Method getGraphicsMode;
    private static Method getGraphicsTexture;
    private static Method textureUpdate;

    private static void init() {
        if (checked) return;
        checked = true;
        try {
            getGraphicsMode = Terminal.class.getMethod("ccgraphics$getGraphicsMode");
            getGraphicsTexture = MonitorRenderState.class.getMethod("ccgraphics$getGraphicsTexture");
            available = true;
        } catch (NoSuchMethodException e) {
            available = false;
        }
    }

    public static boolean isAvailable() {
        if (!checked) init();
        return available;
    }

    public static boolean renderGraphicsOverlay(ClientMonitor originTerminal, MonitorRenderState renderState,
                                                PoseStack transform, MultiBufferSource bufferSource,
                                                float screenWidth, float screenHeight) {
        if (!isAvailable()) return false;

        try {
            var terminal = originTerminal.getTerminal();
            if (terminal == null) return false;

            int mode = (int) getGraphicsMode.invoke(terminal);
            if (mode <= 0) return false;

            var texture = getGraphicsTexture.invoke(renderState);
            if (texture == null) return false;

            if (textureUpdate == null) {
                textureUpdate = texture.getClass().getMethod("update", Terminal.class, boolean.class);
            }
            boolean changed = originTerminal.pollTerminalChanged();
            var texLocation = (ResourceLocation) textureUpdate.invoke(texture, terminal, changed);

            var matrix = transform.last().pose();
            int fullbright = 0xF000F0;

            var vertexConsumer = bufferSource.getBuffer(RenderType.text(texLocation));

            vertexConsumer.addVertex(matrix, 0.0f, 0.0f, 0.0f).setColor(-1).setUv(0.0f, 0.0f).setLight(fullbright);
            vertexConsumer.addVertex(matrix, 0.0f, -screenHeight, 0.0f).setColor(-1).setUv(0.0f, 1.0f).setLight(fullbright);
            vertexConsumer.addVertex(matrix, screenWidth, -screenHeight, 0.0f).setColor(-1).setUv(1.0f, 1.0f).setLight(fullbright);
            vertexConsumer.addVertex(matrix, screenWidth, 0.0f, 0.0f).setColor(-1).setUv(1.0f, 0.0f).setLight(fullbright);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
