package com.andersmmg.cc_modern.client.render;

import com.andersmmg.cc_modern.block.AngledMonitorBlock;
import com.andersmmg.cc_modern.block.TransparentMonitorBlock;
import com.andersmmg.cc_modern.block.WallMonitorBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlockEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
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
        var block = state.getBlock();
        if (!(block instanceof WallMonitorBlock) && !(block instanceof AngledMonitorBlock) && !(block instanceof TransparentMonitorBlock))
            return false;

        var tile = world.getBlockEntity(pos);
        if (!(tile instanceof MonitorBlockEntity monitor)) return false;

        Direction slabFront = block instanceof AngledMonitorBlock
                ? monitor.getFront().getOpposite()
                : monitor.getFront();
        Direction slabBack = slabFront.getOpposite();

        boolean connectedRight = monitor.getXIndex() != monitor.getWidth() - 1;
        boolean connectedLeft = monitor.getXIndex() != 0;
        boolean connectedUp = monitor.getYIndex() != monitor.getHeight() - 1;
        boolean connectedDown = monitor.getYIndex() != 0;

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

        if (block instanceof AngledMonitorBlock) {
            // Angled: bottom edges + back edges
            line(buffer, transform, normal, minX, minY, minZ, maxX, minY, minZ);
            line(buffer, transform, normal, minX, minY, maxZ, maxX, minY, maxZ);
            line(buffer, transform, normal, minX, minY, minZ, minX, minY, maxZ);
            line(buffer, transform, normal, maxX, minY, minZ, maxX, minY, maxZ);

            if (slabBack.getAxis() == Direction.Axis.Z) {
                double z = slabBack == Direction.SOUTH ? maxZ : minZ;
                line(buffer, transform, normal, minX, minY, z, minX, maxY, z);
                line(buffer, transform, normal, maxX, minY, z, maxX, maxY, z);
                line(buffer, transform, normal, minX, maxY, z, maxX, maxY, z);
            } else {
                double x = slabBack == Direction.EAST ? maxX : minX;
                line(buffer, transform, normal, x, minY, minZ, x, maxY, minZ);
                line(buffer, transform, normal, x, minY, maxZ, x, maxY, maxZ);
                line(buffer, transform, normal, x, maxY, minZ, x, maxY, maxZ);
            }
        } else if (block instanceof TransparentMonitorBlock) {
            double cx = cameraPos.x() - (pos.getX() + 0.5);
            double cz = cameraPos.z() - (pos.getZ() + 0.5);
            boolean viewingFromFront = switch (slabFront) {
                case SOUTH -> cz > 0;
                case NORTH -> cz < 0;
                case EAST -> cx > 0;
                case WEST -> cx < 0;
                default -> true;
            };

            if (viewingFromFront) {
                // From the front: only exterior edges of the merged group
                boolean frontOnZ = slabFront.getAxis() == Direction.Axis.Z;

                if (frontOnZ) {
                    double frontZ = slabFront == Direction.SOUTH ? maxZ : minZ;
                    double backZ = slabFront == Direction.NORTH ? maxZ : minZ;
                    double rightX = monitor.getRight() == Direction.EAST ? maxX : minX;
                    double leftX = monitor.getRight() == Direction.EAST ? minX : maxX;

                    if (!connectedRight) {
                        line(buffer, transform, normal, rightX, minY, frontZ, rightX, maxY, frontZ);
                        line(buffer, transform, normal, rightX, minY, backZ, rightX, maxY, backZ);
                    }
                    if (!connectedLeft) {
                        line(buffer, transform, normal, leftX, minY, frontZ, leftX, maxY, frontZ);
                        line(buffer, transform, normal, leftX, minY, backZ, leftX, maxY, backZ);
                    }
                } else {
                    double frontX = slabFront == Direction.EAST ? maxX : minX;
                    double backX = slabFront == Direction.WEST ? maxX : minX;
                    double rightZ = monitor.getRight() == Direction.SOUTH ? maxZ : minZ;
                    double leftZ = monitor.getRight() == Direction.SOUTH ? minZ : maxZ;

                    if (!connectedRight) {
                        line(buffer, transform, normal, frontX, minY, rightZ, frontX, maxY, rightZ);
                        line(buffer, transform, normal, backX, minY, rightZ, backX, maxY, rightZ);
                    }
                    if (!connectedLeft) {
                        line(buffer, transform, normal, frontX, minY, leftZ, frontX, maxY, leftZ);
                        line(buffer, transform, normal, backX, minY, leftZ, backX, maxY, leftZ);
                    }
                }
                if (!connectedDown) {
                    line(buffer, transform, normal, minX, minY, minZ, maxX, minY, minZ);
                    line(buffer, transform, normal, minX, minY, maxZ, maxX, minY, maxZ);
                    line(buffer, transform, normal, minX, minY, minZ, minX, minY, maxZ);
                    line(buffer, transform, normal, maxX, minY, minZ, maxX, minY, maxZ);
                }
                if (!connectedUp) {
                    line(buffer, transform, normal, minX, maxY, minZ, maxX, maxY, minZ);
                    line(buffer, transform, normal, minX, maxY, maxZ, maxX, maxY, maxZ);
                    line(buffer, transform, normal, minX, maxY, minZ, minX, maxY, maxZ);
                    line(buffer, transform, normal, maxX, maxY, minZ, maxX, maxY, maxZ);
                }
            } else {
                // From the back: full individual block outline
                line(buffer, transform, normal, minX, minY, minZ, minX, maxY, minZ);
                line(buffer, transform, normal, maxX, minY, minZ, maxX, maxY, minZ);
                line(buffer, transform, normal, minX, minY, maxZ, minX, maxY, maxZ);
                line(buffer, transform, normal, maxX, minY, maxZ, maxX, maxY, maxZ);

                line(buffer, transform, normal, minX, minY, minZ, maxX, minY, minZ);
                line(buffer, transform, normal, minX, minY, maxZ, maxX, minY, maxZ);
                line(buffer, transform, normal, minX, minY, minZ, minX, minY, maxZ);
                line(buffer, transform, normal, maxX, minY, minZ, maxX, minY, maxZ);

                line(buffer, transform, normal, minX, maxY, minZ, maxX, maxY, minZ);
                line(buffer, transform, normal, minX, maxY, maxZ, maxX, maxY, maxZ);
                line(buffer, transform, normal, minX, maxY, minZ, minX, maxY, maxZ);
                line(buffer, transform, normal, maxX, maxY, minZ, maxX, maxY, maxZ);
            }
        } else {
            // Wall monitor
            var faces = EnumSet.allOf(Direction.class);
            faces.remove(monitor.getFront());

            if (slabFront != Direction.NORTH && slabFront != Direction.WEST && (faces.contains(Direction.NORTH) || faces.contains(Direction.WEST)))
                line(buffer, transform, normal, minX, minY, minZ, minX, maxY, minZ);
            if (slabFront != Direction.SOUTH && slabFront != Direction.WEST && (faces.contains(Direction.SOUTH) || faces.contains(Direction.WEST)))
                line(buffer, transform, normal, minX, minY, maxZ, minX, maxY, maxZ);
            if (slabFront != Direction.NORTH && slabFront != Direction.EAST && (faces.contains(Direction.NORTH) || faces.contains(Direction.EAST)))
                line(buffer, transform, normal, maxX, minY, minZ, maxX, maxY, minZ);
            if (slabFront != Direction.SOUTH && slabFront != Direction.EAST && (faces.contains(Direction.SOUTH) || faces.contains(Direction.EAST)))
                line(buffer, transform, normal, maxX, minY, maxZ, maxX, maxY, maxZ);

            if ((faces.contains(Direction.NORTH) || faces.contains(Direction.DOWN)) && slabFront != Direction.NORTH)
                line(buffer, transform, normal, minX, minY, minZ, maxX, minY, minZ);
            if ((faces.contains(Direction.SOUTH) || faces.contains(Direction.DOWN)) && slabFront != Direction.SOUTH)
                line(buffer, transform, normal, minX, minY, maxZ, maxX, minY, maxZ);
            if (slabBack == Direction.NORTH && (faces.contains(Direction.NORTH) || faces.contains(Direction.UP)))
                line(buffer, transform, normal, minX, maxY, minZ, maxX, maxY, minZ);
            if (slabBack == Direction.SOUTH && (faces.contains(Direction.SOUTH) || faces.contains(Direction.UP)))
                line(buffer, transform, normal, minX, maxY, maxZ, maxX, maxY, maxZ);

            if ((faces.contains(Direction.WEST) || faces.contains(Direction.DOWN)) && slabFront != Direction.WEST)
                line(buffer, transform, normal, minX, minY, minZ, minX, minY, maxZ);
            if ((faces.contains(Direction.EAST) || faces.contains(Direction.DOWN)) && slabFront != Direction.EAST)
                line(buffer, transform, normal, maxX, minY, minZ, maxX, minY, maxZ);
            if (slabBack == Direction.WEST && (faces.contains(Direction.WEST) || faces.contains(Direction.UP)))
                line(buffer, transform, normal, minX, maxY, minZ, minX, maxY, maxZ);
            if (slabBack == Direction.EAST && (faces.contains(Direction.EAST) || faces.contains(Direction.UP)))
                line(buffer, transform, normal, maxX, maxY, minZ, maxX, maxY, maxZ);

            // Front-face edges with connection checks
            if (slabFront.getAxis() == Direction.Axis.Z) {
                double frontZ = slabFront == Direction.SOUTH ? maxZ : minZ;
                double rightX = monitor.getRight() == Direction.EAST ? maxX : minX;
                double leftX = monitor.getRight() == Direction.EAST ? minX : maxX;

                if (!connectedRight) line(buffer, transform, normal, rightX, minY, frontZ, rightX, maxY, frontZ);
                if (!connectedLeft) line(buffer, transform, normal, leftX, minY, frontZ, leftX, maxY, frontZ);
                if (!connectedDown) line(buffer, transform, normal, leftX, minY, frontZ, rightX, minY, frontZ);
                if (!connectedUp) line(buffer, transform, normal, leftX, maxY, frontZ, rightX, maxY, frontZ);
            } else {
                double frontX = slabFront == Direction.EAST ? maxX : minX;
                double rightZ = monitor.getRight() == Direction.SOUTH ? maxZ : minZ;
                double leftZ = monitor.getRight() == Direction.SOUTH ? minZ : maxZ;

                if (!connectedRight) line(buffer, transform, normal, frontX, minY, rightZ, frontX, maxY, rightZ);
                if (!connectedLeft) line(buffer, transform, normal, frontX, minY, leftZ, frontX, maxY, leftZ);
                if (!connectedDown) line(buffer, transform, normal, frontX, minY, leftZ, frontX, minY, rightZ);
                if (!connectedUp) line(buffer, transform, normal, frontX, maxY, leftZ, frontX, maxY, rightZ);
            }
        }

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
