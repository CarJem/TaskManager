package wueffi.taskmanager.client.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wueffi.taskmanager.client.TextureUploadProfiler;

import java.lang.reflect.Field;
import java.util.Map;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

@Mixin(TextureManager.class)
public class TextureManagerMixin {

    @Shadow @Final
    private Map<ResourceLocation, AbstractTexture> byPath;

    @Inject(method = "register(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/renderer/texture/AbstractTexture;)V", at = @At("TAIL"), require = 0)
    private void taskmanager$onRegisterTexture(ResourceLocation id, AbstractTexture registered, CallbackInfo ci) {
        if (id == null) {
            return;
        }
        Object texture = registered != null ? registered : (byPath == null ? null : byPath.get(id));
        TextureUploadProfiler.getInstance().recordUpload(id.getNamespace(), taskmanager$estimateTextureBytes(texture), id.toString());
    }

    @Unique
    private static long taskmanager$estimateTextureBytes(Object texture) {
        if (texture == null) {
            return 0L;
        }
        if (texture instanceof DynamicTexture dynamicTexture && dynamicTexture.getPixels() != null) {
            NativeImage pixels = dynamicTexture.getPixels();
            return (long) pixels.getWidth() * pixels.getHeight() * 4L;
        }
        int width = taskmanager$readIntField(texture, "width", "textureWidth", "field_5204");
        int height = taskmanager$readIntField(texture, "height", "textureHeight", "field_5205");
        if (width <= 0 || height <= 0) {
            return 0L;
        }
        return (long) width * height * 4L;
    }

    @Unique
    private static int taskmanager$readIntField(Object texture, String... names) {
        for (String name : names) {
            try {
                Field field = texture.getClass().getDeclaredField(name);
                field.setAccessible(true);
                Object value = field.get(texture);
                if (value instanceof Number number) {
                    int intValue = number.intValue();
                    if (intValue > 0) {
                        return intValue;
                    }
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return 0;
    }
}
