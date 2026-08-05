package me.croabeast.vnc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class RiftProvider implements VNCProvider {

    private static final String LOADER_CLASS = "org.dimdev.riftloader.RiftLoader";

    @NotNull
    public VNCProvider.VersionInfo resolve() {
        return VersionResolver.resolve(
                "Rift",
                loaderVersion(),
                minecraftVersion(),
                System.getProperty("minecraft.version")
        );
    }

    @Override
    public boolean isAvailable() {
        return VersionResolver.findClass(LOADER_CLASS) != null;
    }

    @Nullable
    public String minecraftVersion() {
        return GameVersionLookup.detect();
    }

    @Nullable
    public String loaderVersion() {
        return VersionResolver.implementationVersion(VersionResolver.findClass(LOADER_CLASS));
    }
}
