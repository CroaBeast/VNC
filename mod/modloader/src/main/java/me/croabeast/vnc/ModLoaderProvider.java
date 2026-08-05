package me.croabeast.vnc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

final class ModLoaderProvider implements VNCProvider {

    private static final String LOADER_CLASS = "ModLoader";
    private static final String MOD_CLASS = "BaseMod";

    @NotNull
    public VNCProvider.VersionInfo resolve() {
        return VersionResolver.resolve(
                "ModLoader",
                loaderVersion(),
                minecraftVersion(),
                System.getProperty("minecraft.version")
        );
    }

    @Override
    public boolean isAvailable() {
        // Both classes live in the default package, so a single hit is not a reliable signal.
        return VersionResolver.findClass(LOADER_CLASS) != null && VersionResolver.findClass(MOD_CLASS) != null;
    }

    @Nullable
    public String minecraftVersion() {
        return GameVersionLookup.detect();
    }

    @Nullable
    public String loaderVersion() {
        Class<?> type = VersionResolver.findClass(LOADER_CLASS);
        if (type == null) return null;

        try {
            Field field = type.getDeclaredField("VERSION");
            field.setAccessible(true);

            Object value = field.get(null);
            if (value != null) return String.valueOf(value);
        } catch (Throwable ignored) {}

        return VersionResolver.implementationVersion(type);
    }
}
