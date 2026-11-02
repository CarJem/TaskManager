package wueffi.taskmanager.client.mixin;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import wueffi.taskmanager.client.EntityCostProfiler;
import wueffi.taskmanager.client.ProfilerManager;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderManagerMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private <E extends Entity> void taskmanager$beginEntityRenderPrep(E entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (!ProfilerManager.getInstance().shouldCollectDetailedMetrics()) {
            return;
        }
        EntityCostProfiler.getInstance().beginEntityRenderPrep(entity);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void taskmanager$endEntityRenderPrep(CallbackInfo ci) {
        if (!ProfilerManager.getInstance().shouldCollectDetailedMetrics()) {
            return;
        }
        EntityCostProfiler.getInstance().endEntityRenderPrep();
    }
}
