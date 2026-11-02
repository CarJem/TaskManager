package wueffi.taskmanager.client.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wueffi.taskmanager.client.ProfilerManager;
import wueffi.taskmanager.client.RenderPhaseProfiler;
import wueffi.taskmanager.client.util.GpuTimer;

/**
 * 1.21.1 has no dedicated SkyRenderer; the whole sky (sun, moon, stars, sky dome) is drawn by
 * {@link LevelRenderer#renderSky}. The phase keeps its original name so exported sessions stay comparable.
 */
@Mixin(LevelRenderer.class)
public class SkyRenderingMixin {

    @Inject(method = "renderSky", at = @At("HEAD"), require = 0)
    private void taskmanager$onSkyHead(CallbackInfo ci) {
        if (!ProfilerManager.getInstance().shouldCollectDetailedMetrics()) return;
        RenderPhaseProfiler.getInstance().beginCpuPhase("worldRenderer.renderSky", "shared/render");
        GpuTimer.begin("worldRenderer.renderSky");
    }

    @Inject(method = "renderSky", at = @At("TAIL"), require = 0)
    private void taskmanager$onSkyTail(CallbackInfo ci) {
        if (!ProfilerManager.getInstance().shouldCollectDetailedMetrics()) return;
        GpuTimer.end("worldRenderer.renderSky");
        RenderPhaseProfiler.getInstance().endCpuPhase("worldRenderer.renderSky");
    }
}
