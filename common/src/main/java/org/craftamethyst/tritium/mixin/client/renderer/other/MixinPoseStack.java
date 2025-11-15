package org.craftamethyst.tritium.mixin.client.renderer.other;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(PoseStack.class)
public class MixinPoseStack {

    private final Deque<PoseStack.Pose> pool = new ArrayDeque<>();
    @Shadow
    @Final
    private Deque<PoseStack.Pose> poseStack;

    private static PoseStack.Pose createPose(Matrix4f pose, Matrix3f normal) {
        try {
            java.lang.reflect.Constructor<PoseStack.Pose> ctr =
                    PoseStack.Pose.class.getDeclaredConstructor(Matrix4f.class, Matrix3f.class);
            ctr.setAccessible(true);
            return ctr.newInstance(pose, normal);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PoseStack.Pose", e);
        }
    }

    @Inject(method = "pushPose", at = @At("HEAD"), cancellable = true)
    private void onPush(CallbackInfo ci) {
        PoseStack.Pose top = this.poseStack.getLast();
        this.pool.pollLast();

        // Always create fresh copies for 1.19.2 Mojmap matrices
        Matrix4f newPos = top.pose().copy();
        Matrix3f newNorm = top.normal().copy();
        PoseStack.Pose reused = createPose(newPos, newNorm);

        this.poseStack.addLast(reused);
        ci.cancel();
    }

    @Inject(method = "popPose", at = @At("HEAD"), cancellable = true)
    private void onPop(CallbackInfo ci) {
        this.pool.addLast(this.poseStack.removeLast());
        ci.cancel();
    }
}