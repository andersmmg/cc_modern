package com.andersmmg.cc_modern.client.render;

import com.andersmmg.cc_modern.compat.CCGraphicsCompat;
import com.andersmmg.cc_modern.mixin.MonitorBlockEntityRendererAccess;
import com.andersmmg.cc_modern.mixin.PaletteAccess;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;
import dan200.computercraft.client.FrameInfo;
import dan200.computercraft.client.integration.ShaderMod;
import dan200.computercraft.client.render.RenderTypes;
import dan200.computercraft.client.render.monitor.MonitorBlockEntityRenderer;
import dan200.computercraft.client.render.monitor.MonitorRenderState;
import dan200.computercraft.client.render.text.DirectFixedWidthFontRenderer;
import dan200.computercraft.client.render.text.FixedWidthFontRenderer;
import dan200.computercraft.shared.config.Config;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlockEntity;
import dan200.computercraft.shared.util.DirectionUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_HEIGHT;
import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_WIDTH;

public class TransparentMonitorBlockEntityRenderer extends MonitorBlockEntityRenderer {
    private static final float MARGIN = (float) (MonitorBlockEntity.RENDER_MARGIN * 1.1);
    private static final float DEPTH_OFFSET = 1.0f / 16.0f - 0.5f + 0.001f;

    public TransparentMonitorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(MonitorBlockEntity monitor, float partialTicks, PoseStack transform, MultiBufferSource bufferSource, int lightmapCoord, int overlayLight) {
        var originTerminal = monitor.getOriginClientMonitor();
        if (originTerminal == null) return;

        var origin = originTerminal.getOrigin();
        var renderState = originTerminal.getRenderState(MonitorRenderState::new);
        var monitorPos = monitor.getBlockPos();

        var renderFrame = FrameInfo.getRenderFrame();
        if (renderState.lastRenderFrame == renderFrame && !monitorPos.equals(renderState.lastRenderPos)) {
            return;
        }

        renderState.lastRenderFrame = renderFrame;
        renderState.lastRenderPos = monitorPos;

        var originPos = origin.getBlockPos();

        var dir = origin.getDirection();
        var front = origin.getFront();
        var yaw = dir.toYRot();
        var pitch = DirectionUtil.toPitchAngle(front);

        transform.pushPose();
        transform.translate(
                originPos.getX() - monitorPos.getX() + 0.5,
                originPos.getY() - monitorPos.getY() + 0.5,
                originPos.getZ() - monitorPos.getZ() + 0.5
        );

        transform.mulPose(Axis.YN.rotationDegrees(yaw));
        transform.mulPose(Axis.XP.rotationDegrees(pitch));
        transform.translate(
                -0.5 + MonitorBlockEntity.RENDER_BORDER + MonitorBlockEntity.RENDER_MARGIN,
                origin.getHeight() - 0.5 - (MonitorBlockEntity.RENDER_BORDER + MonitorBlockEntity.RENDER_MARGIN),
                DEPTH_OFFSET + 0.47
        );
        var xSize = origin.getWidth() - 2.0 * (MonitorBlockEntity.RENDER_MARGIN + MonitorBlockEntity.RENDER_BORDER);
        var ySize = origin.getHeight() - 2.0 * (MonitorBlockEntity.RENDER_MARGIN + MonitorBlockEntity.RENDER_BORDER);

        var terminal = originTerminal.getTerminal();
        if (terminal != null && !ShaderMod.get().isRenderingShadowPass()) {
            float screenWidth = (float) xSize;
            float screenHeight = (float) ySize;
            if (!CCGraphicsCompat.renderGraphicsOverlay(originTerminal, renderState, transform, bufferSource, screenWidth, screenHeight)) {
                int width = terminal.getWidth(), height = terminal.getHeight();
                int pixelWidth = width * FONT_WIDTH, pixelHeight = height * FONT_HEIGHT;
                var xScale = xSize / pixelWidth;
                var yScale = ySize / pixelHeight;
                transform.pushPose();
                transform.scale((float) xScale, (float) -yScale, 1.0f);

                var matrix = transform.last().pose();
                var margin = (float) (MARGIN / xScale);
                var marginY = (float) (MARGIN / yScale);

                var renderType = MonitorBlockEntityRenderer.currentRenderer();
                var redraw = originTerminal.pollTerminalChanged();
                if (renderState.createBuffer(renderType)) redraw = true;

                switch (renderType) {
                    case TBO -> {
                        // skip opaque
                    }
                    case VBO -> {
                        var backgroundBuffer = renderState.backgroundBuffer;
                        var foregroundBuffer = renderState.foregroundBuffer;
                        if (backgroundBuffer != null && foregroundBuffer != null) {
                            if (redraw) {
                                var size = DirectFixedWidthFontRenderer.getVertexCount(terminal);

                                // Make palette colour 0 (default black background) transparent.
                                var palette = terminal.getPalette();
                                var byteColours = ((PaletteAccess) palette).cc_modern$getByteColours();
                                int savedBlack = byteColours[0];
                                byteColours[0] = savedBlack & 0x00FFFFFF;

                                ((MonitorBlockEntityRendererAccess) this).cc_modern$renderToBuffer(backgroundBuffer, size, sink ->
                                        DirectFixedWidthFontRenderer.drawTerminalBackground(sink, 0, 0, terminal, marginY, marginY, margin, margin));

                                byteColours[0] = savedBlack;

                                ((MonitorBlockEntityRendererAccess) this).cc_modern$renderToBuffer(foregroundBuffer, size, sink -> {
                                    DirectFixedWidthFontRenderer.drawTerminalForeground(sink, 0, 0, terminal);
                                    DirectFixedWidthFontRenderer.drawCursor(sink, 0, 0, terminal);
                                });
                            }

                            var oldFogStart = RenderSystem.getShaderFogStart();
                            RenderSystem.setShaderFogStart(1e4f);

                            RenderTypes.TERMINAL.setupRenderState();

                            var modelView = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(matrix);

                            backgroundBuffer.bind();
                            backgroundBuffer.drawWithShader(modelView, RenderSystem.getProjectionMatrix(), RenderTypes.getTerminalShader());

                            RenderSystem.polygonOffset(-1.0f, -10.0f);
                            RenderSystem.enablePolygonOffset();

                            foregroundBuffer.bind();
                            foregroundBuffer.drawWithShader(
                                    modelView, RenderSystem.getProjectionMatrix(), RenderTypes.getTerminalShader(),
                                    FixedWidthFontRenderer.isCursorVisible(terminal) && !FrameInfo.getGlobalCursorBlink()
                                            ? foregroundBuffer.getIndexCount() - RenderTypes.TERMINAL.mode().indexCount(4)
                                            : foregroundBuffer.getIndexCount()
                            );

                            RenderSystem.polygonOffset(0.0f, -0.0f);
                            RenderSystem.disablePolygonOffset();
                            RenderTypes.TERMINAL.clearRenderState();
                            VertexBuffer.unbind();

                            RenderSystem.setShaderFogStart(oldFogStart);
                        }
                    }
                    case BEST -> throw new IllegalStateException("Impossible");
                }

                transform.popPose();
            }
        }

        transform.popPose();
    }

    @Override
    public int getViewDistance() {
        return Config.monitorDistance;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(MonitorBlockEntity monitor) {
        return monitor.getRenderBoundingBox();
    }
}
