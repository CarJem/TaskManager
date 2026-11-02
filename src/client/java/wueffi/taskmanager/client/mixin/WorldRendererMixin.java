package wueffi.taskmanager.client.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Unique;
import wueffi.taskmanager.client.RenderPhaseProfiler;
import wueffi.taskmanager.client.util.GpuTimer;
import org.spongepowered.asm.mixin.Mixin;
import wueffi.taskmanager.client.ProfilerManager;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin {

    @Unique
    private static void taskmanager$beginPhase(String phase) {
        if (!ProfilerManager.getInstance().shouldCollectDetailedMetrics()) {
            return;
        }
        RenderPhaseProfiler.getInstance().beginCpuPhase(phase, "shared/render");
        GpuTimer.begin(phase);
    }

    @Unique
    private static void taskmanager$endPhase(String phase) {
        if (!ProfilerManager.getInstance().shouldCollectDetailedMetrics()) {
            return;
        }
        GpuTimer.end(phase);
        RenderPhaseProfiler.getInstance().endCpuPhase(phase);
    }

    @Unique
    private static void taskmanager$beginCpuOnlyPhase(String phase) {
        if (!ProfilerManager.getInstance().shouldCollectDetailedMetrics()) {
            return;
        }
        RenderPhaseProfiler.getInstance().beginCpuPhase(phase, "shared/render");
    }

    @Unique
    private static void taskmanager$endCpuOnlyPhase(String phase) {
        if (!ProfilerManager.getInstance().shouldCollectDetailedMetrics()) {
            return;
        }
        RenderPhaseProfiler.getInstance().endCpuPhase(phase);
    }

    @Inject(method = "doEntityOutline", at = @At("HEAD"))
    private void taskmanager$onOutlinesHead(CallbackInfo ci) {
        taskmanager$beginPhase("worldRenderer.entityOutlines");
    }

    @Inject(method = "doEntityOutline", at = @At("TAIL"))
    private void taskmanager$onOutlinesTail(CallbackInfo ci) {
        taskmanager$endPhase("worldRenderer.entityOutlines");
    }

    @Inject(method = "renderSnowAndRain", at = @At("HEAD"), require = 0)
    private void taskmanager$onRenderWeatherHead(CallbackInfo ci) {
        taskmanager$beginPhase("worldRenderer.renderWeather");
    }

    @Inject(method = "renderSnowAndRain", at = @At("TAIL"), require = 0)
    private void taskmanager$onRenderWeatherTail(CallbackInfo ci) {
        taskmanager$endPhase("worldRenderer.renderWeather");
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), require = 0)
    private void taskmanager$onRenderCloudsHead(CallbackInfo ci) {
        taskmanager$beginPhase("worldRenderer.renderClouds");
    }

    @Inject(method = "renderClouds", at = @At("TAIL"), require = 0)
    private void taskmanager$onRenderCloudsTail(CallbackInfo ci) {
        taskmanager$endPhase("worldRenderer.renderClouds");
    }

    // Terrain is drawn one RenderType layer at a time (solid, cutout, translucent, ...).
    @Inject(method = "renderSectionLayer", at = @At("HEAD"), require = 0)
    private void taskmanager$onRenderSectionLayerHead(CallbackInfo ci) {
        taskmanager$beginPhase("worldRenderer.renderMain");
    }

    @Inject(method = "renderSectionLayer", at = @At("TAIL"), require = 0)
    private void taskmanager$onRenderSectionLayerTail(CallbackInfo ci) {
        taskmanager$endPhase("worldRenderer.renderMain");
    }

    // Called once per visible entity, so only CPU time is tracked to avoid flooding GPU timer queries.
    @Inject(method = "renderEntity", at = @At("HEAD"), require = 0)
    private void taskmanager$onRenderEntityHead(CallbackInfo ci) {
        taskmanager$beginCpuOnlyPhase("worldRenderer.pushEntityRenders");
    }

    @Inject(method = "renderEntity", at = @At("TAIL"), require = 0)
    private void taskmanager$onRenderEntityTail(CallbackInfo ci) {
        taskmanager$endCpuOnlyPhase("worldRenderer.pushEntityRenders");
    }
}
