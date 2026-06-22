package com.andersmmg.cc_modern.client.render;

import com.andersmmg.cc_modern.mixin.MonitorBlockEntityRendererAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dan200.computercraft.client.FrameInfo;
import dan200.computercraft.client.integration.ShaderMod;
import dan200.computercraft.client.render.RenderTypes;
import dan200.computercraft.client.render.monitor.MonitorBlockEntityRenderer;
import dan200.computercraft.client.render.monitor.MonitorRenderState;
import dan200.computercraft.client.render.text.FixedWidthFontRenderer;
import dan200.computercraft.shared.config.Config;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlock;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_HEIGHT;
import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_WIDTH;

public class AngledMonitorBlockEntityRenderer extends MonitorBlockEntityRenderer {

    private static final float MARGIN = (float) MonitorBlockEntity.RENDER_MARGIN;
    private static final float TILT_PITCH_DEG = -67.5f;
    private static final float DEPTH_OFFSET = 5.0f / 16.0f - 0.5252f;

    public AngledMonitorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
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
        // ORIENTATION is pinned NORTH, so read FACING for the screen rotation instead. no idea why
        var yaw = origin.getBlockState().getValue(MonitorBlock.FACING).toYRot() + 180f;

        transform.pushPose();
        transform.translate(
                originPos.getX() - monitorPos.getX() + 0.5,
                originPos.getY() - monitorPos.getY() + 0.5,
                originPos.getZ() - monitorPos.getZ() + 0.5
        );

        transform.mulPose(Axis.YN.rotationDegrees(yaw));
        transform.mulPose(Axis.XP.rotationDegrees(TILT_PITCH_DEG));
        transform.translate(
                -0.5 + MonitorBlockEntity.RENDER_BORDER + MonitorBlockEntity.RENDER_MARGIN,
                origin.getHeight() - 0.63 - (MonitorBlockEntity.RENDER_BORDER + MonitorBlockEntity.RENDER_MARGIN),
                DEPTH_OFFSET
        );

        var xSize = origin.getWidth() - 2.0 * (MonitorBlockEntity.RENDER_MARGIN + MonitorBlockEntity.RENDER_BORDER);
        var ySize = origin.getHeight() - 2.0 * (MonitorBlockEntity.RENDER_MARGIN + MonitorBlockEntity.RENDER_BORDER);

        var terminal = originTerminal.getTerminal();
        if (terminal != null && !ShaderMod.get().isRenderingShadowPass()) {
            int width = terminal.getWidth();
            int height = terminal.getHeight();
            int pixelWidth = width * FONT_WIDTH;
            int pixelHeight = height * FONT_HEIGHT;
            var xScale = xSize / pixelWidth;
            var yScale = ySize / pixelHeight;
            transform.pushPose();
            transform.scale((float) xScale, (float) -yScale, 1.0f);

            var matrix = transform.last().pose();
            ((MonitorBlockEntityRendererAccess) this).cc_modern$renderTerminal(matrix, originTerminal, renderState, terminal, (float) (MARGIN / xScale), (float) (MARGIN / yScale));

            transform.popPose();
        } else {
            FixedWidthFontRenderer.drawEmptyTerminal(
                    FixedWidthFontRenderer.toVertexConsumer(transform, bufferSource.getBuffer(RenderTypes.TERMINAL)),
                    -MARGIN, MARGIN,
                    (float) (xSize + 2 * MARGIN), (float) -(ySize + MARGIN * 2)
            );
        }

        transform.popPose();
    }

    @Override
    public int getViewDistance() {
        return Config.monitorDistance;
    }

    @Override
    public AABB getRenderBoundingBox(MonitorBlockEntity monitor) {
        return monitor.getRenderBoundingBox();
    }
}
