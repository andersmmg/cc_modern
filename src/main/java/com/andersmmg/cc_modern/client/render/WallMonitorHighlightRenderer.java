package com.andersmmg.cc_modern.client.render;

import com.andersmmg.cc_modern.block.WallMonitorBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlockEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

import java.util.EnumSet;

public class WallMonitorHighlightRenderer {
    public static boolean drawHighlight(PoseStack transformStack, MultiBufferSource bufferSource, Camera camera, BlockHitResult hit) {
        if (camera.getEntity().isCrouching()) return false;

        var world = camera.getEntity().getCommandSenderWorld();
        var pos = hit.getBlockPos();
        var state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof WallMonitorBlock)) return false;

        var tile = world.getBlockEntity(pos);
        if (!(tile instanceof MonitorBlockEntity monitor)) return false;

        var faces = EnumSet.allOf(Direction.class);
        faces.remove(monitor.getFront());
        if (monitor.getXIndex() != 0) faces.remove(monitor.getRight().getOpposite());
        if (monitor.getXIndex() != monitor.getWidth() - 1) faces.remove(monitor.getRight());
        if (monitor.getYIndex() != 0) faces.remove(monitor.getDown().getOpposite());
        if (monitor.getYIndex() != monitor.getHeight() - 1) faces.remove(monitor.getDown());

        VoxelShape shape = state.getShape(world, pos);
        var bounds = shape.bounds();

        double minX = bounds.minX, minY = bounds.minY, minZ = bounds.minZ;
        double maxX = bounds.maxX, maxY = bounds.maxY, maxZ = bounds.maxZ;

        var cameraPos = camera.getPosition();
        transformStack.pushPose();
        transformStack.translate(pos.getX() - cameraPos.x(), pos.getY() - cameraPos.y(), pos.getZ() - cameraPos.z());

        var buffer = bufferSource.getBuffer(RenderType.lines());
        var transform = transformStack.last().pose();
        var normal = transformStack.last();

        if (faces.contains(Direction.NORTH) || faces.contains(Direction.WEST))
            line(buffer, transform, normal, minX, minY, minZ, minX, maxY, minZ);
        if (faces.contains(Direction.SOUTH) || faces.contains(Direction.WEST))
            line(buffer, transform, normal, minX, minY, maxZ, minX, maxY, maxZ);
        if (faces.contains(Direction.NORTH) || faces.contains(Direction.EAST))
            line(buffer, transform, normal, maxX, minY, minZ, maxX, maxY, minZ);
        if (faces.contains(Direction.SOUTH) || faces.contains(Direction.EAST))
            line(buffer, transform, normal, maxX, minY, maxZ, maxX, maxY, maxZ);

        if (faces.contains(Direction.NORTH) || faces.contains(Direction.DOWN))
            line(buffer, transform, normal, minX, minY, minZ, maxX, minY, minZ);
        if (faces.contains(Direction.SOUTH) || faces.contains(Direction.DOWN))
            line(buffer, transform, normal, minX, minY, maxZ, maxX, minY, maxZ);
        if (faces.contains(Direction.NORTH) || faces.contains(Direction.UP))
            line(buffer, transform, normal, minX, maxY, minZ, maxX, maxY, minZ);
        if (faces.contains(Direction.SOUTH) || faces.contains(Direction.UP))
            line(buffer, transform, normal, minX, maxY, maxZ, maxX, maxY, maxZ);

        if (faces.contains(Direction.WEST) || faces.contains(Direction.DOWN))
            line(buffer, transform, normal, minX, minY, minZ, minX, minY, maxZ);
        if (faces.contains(Direction.EAST) || faces.contains(Direction.DOWN))
            line(buffer, transform, normal, maxX, minY, minZ, maxX, minY, maxZ);
        if (faces.contains(Direction.WEST) || faces.contains(Direction.UP))
            line(buffer, transform, normal, minX, maxY, minZ, minX, maxY, maxZ);
        if (faces.contains(Direction.EAST) || faces.contains(Direction.UP))
            line(buffer, transform, normal, maxX, maxY, minZ, maxX, maxY, maxZ);

        transformStack.popPose();
        return true;
    }

    private static void line(VertexConsumer buffer, Matrix4f transform, PoseStack.Pose normal, double x1, double y1, double z1, double x2, double y2, double z2) {
        var dx = x2 - x1;
        var dy = y2 - y1;
        var dz = z2 - z1;
        var len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len == 0) return;
        float nx = (float)(dx / len);
        float ny = (float)(dy / len);
        float nz = (float)(dz / len);
        buffer.addVertex(transform, (float)x1, (float)y1, (float)z1).setColor(0, 0, 0, 0.4f).setNormal(normal, nx, ny, nz);
        buffer.addVertex(transform, (float)x2, (float)y2, (float)z2).setColor(0, 0, 0, 0.4f).setNormal(normal, nx, ny, nz);
    }
}
