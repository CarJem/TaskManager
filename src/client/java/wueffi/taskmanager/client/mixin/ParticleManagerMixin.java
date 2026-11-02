package wueffi.taskmanager.client.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;
import wueffi.taskmanager.client.ProfilerManager;
import wueffi.taskmanager.client.RenderPhaseProfiler;
import wueffi.taskmanager.client.util.GpuTimer;

@Mixin(ParticleEngine.class)
public class ParticleManagerMixin {

    private static final String RENDER = "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V";

    // NeoForge patches ParticleEngine#render to take a frustum and a render-type filter; vanilla's
    // 3-arg overload only delegates to it, so hooking the patched variant covers both.
    @Inject(method = RENDER, at = @At("HEAD"), require = 0)
    private void taskmanager$onParticlesHead(LightTexture lightTexture, Camera camera, float partialTick, Frustum frustum, Predicate<ParticleRenderType> renderTypePredicate, CallbackInfo ci) {
        if (!ProfilerManager.getInstance().shouldCollectDetailedMetrics()) return;
        RenderPhaseProfiler.getInstance().beginCpuPhase("particles.render", "shared/render");
        GpuTimer.begin("particles.render");
    }

    @Inject(method = RENDER, at = @At("TAIL"), require = 0)
    private void taskmanager$onParticlesTail(LightTexture lightTexture, Camera camera, float partialTick, Frustum frustum, Predicate<ParticleRenderType> renderTypePredicate, CallbackInfo ci) {
        if (!ProfilerManager.getInstance().shouldCollectDetailedMetrics()) return;
        GpuTimer.end("particles.render");
        RenderPhaseProfiler.getInstance().endCpuPhase("particles.render");
    }
}
