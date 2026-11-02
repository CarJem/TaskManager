package wueffi.taskmanager.client.util;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Thin wrapper around the NeoForge mod loader APIs. Everything that used to go through
 * {@code FabricLoader} goes through here so loader specifics stay in one place.
 */
public final class LoaderCompat {

    public static final String LOADER_MOD_ID = "neoforge";
    public static final String LOADER_NAME = "NeoForge";

    private LoaderCompat() {
    }

    public static boolean isModLoaded(String modId) {
        ModList modList = ModList.get();
        return modList != null && modList.isLoaded(modId);
    }

    public static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static Optional<IModInfo> getModInfo(String modId) {
        ModList modList = ModList.get();
        if (modList == null || modId == null) {
            return Optional.empty();
        }
        return modList.getModContainerById(modId).map(ModContainer::getModInfo);
    }

    public static Optional<String> getModName(String modId) {
        return getModInfo(modId).map(IModInfo::getDisplayName);
    }

    public static Optional<String> getModVersion(String modId) {
        return getModInfo(modId).map(info -> info.getVersion().toString());
    }

    public static List<IModInfo> getAllMods() {
        ModList modList = ModList.get();
        return modList == null ? List.of() : new ArrayList<>(modList.getMods());
    }

    public static List<String> getAllModIds() {
        List<String> ids = new ArrayList<>();
        for (IModInfo info : getAllMods()) {
            ids.add(info.getModId());
        }
        return ids;
    }

    public static Optional<IModFile> getModFile(IModInfo info) {
        try {
            return Optional.ofNullable(info.getOwningFile()).map(owning -> owning.getFile());
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }

    /**
     * Resolves a file packaged inside a mod's jar, e.g. its logo.
     */
    public static Optional<Path> findModResource(String modId, String path) {
        return getModInfo(modId).flatMap(LoaderCompat::getModFile).map(file -> file.findResource(path));
    }
}
