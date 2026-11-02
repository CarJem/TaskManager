package wueffi.taskmanager.client.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;
import wueffi.taskmanager.client.ProfilerManager;
import wueffi.taskmanager.client.TaskManagerScreen;

public class KeyBindHandler {

    public static final String CATEGORY = "key.categories.taskmanager";

    private static KeyMapping openKey;
    private static KeyMapping sessionKey;
    private static KeyMapping hudToggleKey;

    public static boolean matchesOpenKey(int keyCode, int scanCode) {
        return openKey != null && openKey.matches(keyCode, scanCode);
    }

    public static void register(RegisterKeyMappingsEvent event) {
        sessionKey = new KeyMapping(
                "key.taskmanager.session",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F9,
                CATEGORY
        );
        hudToggleKey = new KeyMapping(
                "key.taskmanager.hud_toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F10,
                CATEGORY
        );
        openKey = new KeyMapping(
                "key.taskmanager.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F12,
                CATEGORY
        );
        event.register(sessionKey);
        event.register(hudToggleKey);
        event.register(openKey);
    }

    public static void onClientTick(Minecraft client) {
        if (openKey == null || sessionKey == null || hudToggleKey == null) {
            return;
        }
        while (openKey.consumeClick()) {
            if (client.screen instanceof TaskManagerScreen) {
                client.setScreen(null);
            } else {
                client.setScreen(new TaskManagerScreen());
            }
        }
        while (sessionKey.consumeClick()) {
            ProfilerManager.getInstance().toggleSessionLogging();
        }
        while (hudToggleKey.consumeClick()) {
            ConfigManager.setHudEnabled(!ConfigManager.isHudEnabled());
        }
    }
}
