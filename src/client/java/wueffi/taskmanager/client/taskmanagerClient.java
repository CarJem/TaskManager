package wueffi.taskmanager.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wueffi.taskmanager.client.util.ConfigManager;
import wueffi.taskmanager.client.util.KeyBindHandler;
import wueffi.taskmanager.client.util.ModClassIndex;

@Mod(value = taskmanagerClient.MOD_ID, dist = Dist.CLIENT)
public class taskmanagerClient {

    public static final String MOD_ID = "taskmanager";
    public static final Logger LOGGER = LoggerFactory.getLogger("taskmanager");
    private boolean startupClosed = false;

    public taskmanagerClient(IEventBus modBus, ModContainer container) {
        ModClassIndex.build();
        ConfigManager.loadConfig();
        modBus.addListener(FMLClientSetupEvent.class, event -> ProfilerManager.getInstance().initialize());

        modBus.addListener(RegisterKeyMappingsEvent.class, KeyBindHandler::register);
        modBus.addListener(RegisterGuiLayersEvent.class, event -> event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "hud"),
                (ctx, deltaTracker) -> HudOverlayRenderer.render(ctx)
        ));

        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parent) -> new TaskManagerScreen(11));

        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> onClientTickEnd());
    }

    private void onClientTickEnd() {
        Minecraft client = Minecraft.getInstance();
        if (!startupClosed) {
            startupClosed = true;
            recordStartupRegistrations();
            StartupTimingProfiler.getInstance().close();
        }

        KeyBindHandler.onClientTick(client);
        ProfilerManager.getInstance().onClientTickEnd(client);
    }

    /**
     * NeoForge has no equivalent of Fabric's array-backed event listeners to hook, so startup
     * registrations are attributed by counting registry entries per namespace once loading is done.
     */
    private static void recordStartupRegistrations() {
        try {
            StartupTimingProfiler profiler = StartupTimingProfiler.getInstance();
            for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
                for (ResourceLocation key : registry.keySet()) {
                    profiler.recordRegistration(key.getNamespace());
                }
            }
        } catch (Throwable throwable) {
            LOGGER.debug("Failed to record startup registrations", throwable);
        }
    }
}
