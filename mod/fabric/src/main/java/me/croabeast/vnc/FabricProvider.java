package me.croabeast.vnc;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

final class FabricProvider implements VNCProvider {

    @Override
    @NotNull
    public VNCProvider.VersionInfo resolve() {
        return VersionResolver.resolve(
                "Fabric",
                loaderVersion(),
                minecraftVersion(),
                System.getProperty("minecraft.version")
        );
    }

    @Override
    public boolean isAvailable() {
        try {
            FabricLoader.getInstance();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nullable
    public String minecraftVersion() {
        return modVersion("minecraft");
    }

    @Nullable
    public String loaderVersion() {
        return VersionResolver.firstNonBlank(
                modVersion("fabricloader"),
                implementationVersion(FabricLoader.class)
        );
    }

    @Nullable
    private String modVersion(@NotNull String modId) {
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(modId);
        if (!container.isPresent()) return null;

        return container.get().getMetadata().getVersion().getFriendlyString();
    }

    @Nullable
    private String implementationVersion(@NotNull Class<?> type) {
        Package pkg = type.getPackage();
        return pkg != null ? pkg.getImplementationVersion() : null;
    }
}
