package me.croabeast.vnc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class NilLoaderProvider implements VNCProvider {

    private static final String LOADER_CLASS = "nilloader.NilLoader";

    @NotNull
    public VNCProvider.VersionInfo resolve() {
        return VersionResolver.resolve(
                "NilLoader",
                loaderVersion(),
                minecraftVersion(),
                System.getProperty("minecraft.version")
        );
    }

    @Override
    public boolean isAvailable() {
        return VersionResolver.findClass(LOADER_CLASS) != null;
    }

    // NilLoader carries no Minecraft-specific code, so the version comes from the game itself.
    @Nullable
    public String minecraftVersion() {
        return GameVersionLookup.detect();
    }

    @Nullable
    public String loaderVersion() {
        return VersionResolver.implementationVersion(VersionResolver.findClass(LOADER_CLASS));
    }
}
