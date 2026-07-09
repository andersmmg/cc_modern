package com.andersmmg.cc_modern.mixin;

import com.andersmmg.cc_modern.block.AngledMonitorBlock;
import com.andersmmg.cc_modern.config.CCModernConfig;
import com.mojang.math.Axis;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlock;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

/**
 * Route clicks on the front and top faces of angled monitor using raycast
 */
@Mixin(MonitorBlock.class)
public class MonitorBlockMixin {

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void cc_modern$angledMonitorTouch(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (!((Object) this instanceof AngledMonitorBlock)) return;

        if (player.isCrouching()) return;

        Direction facing = state.getValue(MonitorBlock.FACING);
        if (hit.getDirection() == Direction.DOWN || hit.getDirection() == facing) return;

        if (!(level.getBlockEntity(pos) instanceof MonitorBlockEntity monitor)) return;

        if (level.isClientSide()) {
            cir.setReturnValue(InteractionResult.sidedSuccess(true));
            return;
        }

        float yaw = facing.toYRot() + 180f;

        Matrix4f pose = new Matrix4f();
        pose.translate(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
        pose.rotate(Axis.YN.rotationDegrees(yaw));
        pose.rotate(Axis.XP.rotationDegrees(-67.5f));
        pose.invert();

        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookDir = player.getViewVector(1.0F);

        Vector4f eyeLocal = new Vector4f((float) eyePos.x, (float) eyePos.y, (float) eyePos.z, 1.0f);
        pose.transform(eyeLocal);
        Vector4f lookLocal = new Vector4f((float) lookDir.x, (float) lookDir.y, (float) lookDir.z, 0.0f);
        pose.transform(lookLocal);

        if (Math.abs(lookLocal.z()) < 1.0E-5) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        float depthOffset = 5.0f / 16.0f - 0.5252f;
        float t = (depthOffset - eyeLocal.z()) / lookLocal.z();

        if (t < 0.0f || t > 6.0f) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        double rx = eyeLocal.x() + t * lookLocal.x();
        double ry = eyeLocal.y() + t * lookLocal.y();

        double quadSize = 1.0 - 2.0 * (MonitorBlockEntity.RENDER_BORDER + MonitorBlockEntity.RENDER_MARGIN);
        double screenX0 = -0.5 + MonitorBlockEntity.RENDER_BORDER + MonitorBlockEntity.RENDER_MARGIN;
        double screenY1 = 1.0 - 0.63 - (MonitorBlockEntity.RENDER_BORDER + MonitorBlockEntity.RENDER_MARGIN);

        double fracX = (rx - screenX0) / quadSize;
        double fracY = (screenY1 - ry) / quadSize;
        if (fracY < 0.0 || fracY >= 1.0) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        double interiorOffset = MonitorBlockEntity.RENDER_BORDER + MonitorBlockEntity.RENDER_MARGIN;

        float synthX, synthZ;
        switch (facing) {
            case NORTH -> {
                synthX = (float) (1.0 - interiorOffset - fracX * quadSize);
                synthZ = 1.0f;
            }
            case SOUTH -> {
                synthX = (float) (interiorOffset + fracX * quadSize);
                synthZ = 0.0f;
            }
            case WEST -> {
                synthX = 0.0f;
                synthZ = (float) (interiorOffset + fracX * quadSize);
            }
            case EAST -> {
                synthX = 1.0f;
                synthZ = (float) (interiorOffset + fracX * quadSize);
            }
            default -> {
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }
        }
        float synthY = (float) (interiorOffset + (1.0 - fracY) * quadSize);

        ((MonitorBlockEntityAccess) monitor).cc_modern$monitorTouched(synthX, synthY, synthZ);
        cir.setReturnValue(InteractionResult.sidedSuccess(false));
    }

    @Inject(method = "setPlacedBy", at = @At("HEAD"), cancellable = true)
    private void cc_modern$preventMergeWhenSneaking(Level world, BlockPos pos, BlockState state,
                                                    @Nullable LivingEntity placer, ItemStack stack,
                                                    CallbackInfo ci) {
        if (CCModernConfig.PREVENT_MERGE_WHEN_SNEAKING.get() && placer != null && placer.isShiftKeyDown()) {
            ci.cancel();
        }
    }
}
