package me.croabeast.vnc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class NeoForgeProvider implements VNCProvider {

    static final int MINIMUM_JAVA_VERSION = 21;

    public boolean isJavaSupported() {
        return Versioning.javaVersion() >= MINIMUM_JAVA_VERSION;
    }

    public boolean isNeoForgePresent() {
        if (!isJavaSupported()) return false;

        try {
            return NeoForgeRuntime.isAvailable();
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public boolean isAvailable() {
        if (!isNeoForgePresent()) return false;

        try {
            return NeoForgeRuntime.isLoaded();
        } catch (Throwable ignored) {
            return false;
        }
    }

    @NotNull
    public VNCProvider.VersionInfo resolve() {
        if (!isJavaSupported())
            throw new IllegalStateException("NeoForge requires Java " + MINIMUM_JAVA_VERSION + " or newer.");

        if (!isNeoForgePresent())
            throw new IllegalStateException("NeoForge is not available in the current runtime.");

        return NeoForgeRuntime.resolve();
    }

    @Nullable
    public String minecraftVersion() {
        if (!isNeoForgePresent())
            return VersionResolver.firstNonBlank(
                    System.getProperty("minecraft.version"),
                    System.getProperty("fml.mcVersion")
            );

        try {
            return NeoForgeRuntime.minecraftVersion();
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    public String neoForgeVersion() {
        if (!isNeoForgePresent()) return null;

        try {
            return NeoForgeRuntime.neoForgeVersion();
        } catch (Throwable ignored) {
            return null;
        }
    }
}
