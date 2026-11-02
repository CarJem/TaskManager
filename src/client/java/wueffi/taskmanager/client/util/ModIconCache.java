package wueffi.taskmanager.client.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforgespi.language.IModInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ModIconCache {

    private static final ModIconCache INSTANCE = new ModIconCache();
    public static ModIconCache getInstance() { return INSTANCE; }
    public static final Logger LOGGER = LoggerFactory.getLogger("taskmanager");

    private static final ResourceLocation DEFAULT_ICON   = ResourceLocation.fromNamespaceAndPath("taskmanager", "default.png");
    private static final ResourceLocation MINECRAFT_ICON = ResourceLocation.fromNamespaceAndPath("taskmanager", "minecraft.png");

    private final Map<String, ResourceLocation> cache = new HashMap<>();

    public ResourceLocation getIcon(String modId) {
        return cache.computeIfAbsent(modId, this::loadIcon);
    }

    private ResourceLocation loadIcon(String modId) {
        if (modId.equals("minecraft")) return MINECRAFT_ICON;

        try {
            IModInfo mod = LoaderCompat.getModInfo(modId).orElse(null);
            if (mod == null) return DEFAULT_ICON;

            String iconPath = mod.getLogoFile().orElse(null);
            if (iconPath == null || iconPath.isBlank()) {
                return DEFAULT_ICON;
            }

            Path path = LoaderCompat.findModResource(modId, iconPath).orElse(null);
            if (path == null || !Files.exists(path)) {
                return DEFAULT_ICON;
            }

            try (InputStream is = Files.newInputStream(path)) {
                NativeImage img = NativeImage.read(is);
                DynamicTexture tex = new DynamicTexture(img);

                ResourceLocation id = ResourceLocation.fromNamespaceAndPath("taskmanager", "modicon/" + sanitizePath(modId));
                Minecraft.getInstance().getTextureManager().register(id, tex);
                return id;
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to load icon for {}: {}", modId, e.getMessage());
        }

        return DEFAULT_ICON;
    }

    private static String sanitizePath(String modId) {
        StringBuilder builder = new StringBuilder(modId.length());
        for (char c : modId.toLowerCase(java.util.Locale.ROOT).toCharArray()) {
            builder.append(ResourceLocation.validPathChar(c) ? c : '_');
        }
        return builder.toString();
    }
}
