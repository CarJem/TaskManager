package wueffi.taskmanager.client.mixin;

import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Program;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wueffi.taskmanager.client.ProfilerManager;
import wueffi.taskmanager.client.ShaderCompilationProfiler;

import java.io.InputStream;

/**
 * In 1.21.1 every GLSL stage is compiled through {@link Program#compileShader}.
 */
@Mixin(Program.class)
public abstract class ShaderProgramMixin {

    @Inject(method = "compileShader", at = @At("HEAD"), require = 0)
    private static void taskmanager$beginShaderCompile(Program.Type type, String name, InputStream shaderData, String sourceName, GlslPreprocessor preprocessor, CallbackInfoReturnable<Program> cir) {
        if (!ProfilerManager.getInstance().shouldCollectDetailedMetrics()) {
            return;
        }
        ShaderCompilationProfiler.getInstance().beginCompile(name + " (" + type.getName() + ")");
    }

    @Inject(method = "compileShader", at = @At("RETURN"), require = 0)
    private static void taskmanager$endShaderCompile(Program.Type type, String name, InputStream shaderData, String sourceName, GlslPreprocessor preprocessor, CallbackInfoReturnable<Program> cir) {
        if (!ProfilerManager.getInstance().shouldCollectDetailedMetrics()) {
            return;
        }
        ShaderCompilationProfiler.getInstance().endCompile();
    }
}
