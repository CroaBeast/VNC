package me.croabeast.vnc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class LiteLoaderProvider implements VNCProvider {

    @NotNull
    public VNCProvider.VersionInfo resolve() {
        return VersionResolver.resolve(
                "LiteLoader",
                implementationVersion(),
                minecraftVersion(),
                System.getProperty("minecraft.version")
        );
    }

    @Override
    public boolean isAvailable() {
        return implementationVersion() != null || System.getProperty("liteloader.minecraft.version") != null;
    }

    @Nullable
    public String minecraftVersion() {
        return VersionResolver.firstNonBlank(
                System.getProperty("liteloader.minecraft.version"),
                System.getProperty("liteloader.minecraftVersion"),
                System.getProperty("minecraft.version")
        );
    }

    @Nullable
    public String implementationVersion() {
        return VersionResolver.firstNonBlank(
                System.getProperty("liteloader.version"),
                System.getProperty("liteloader.loader.version")
        );
    }
}
