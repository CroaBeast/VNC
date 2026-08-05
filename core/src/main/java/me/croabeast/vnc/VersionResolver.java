package me.croabeast.vnc;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Small shared resolver used by platform modules that expose runtime version snapshots.
 */
@UtilityClass
public class VersionResolver {

    @NotNull
    public static VNCProvider.VersionInfo resolve(
            @NotNull String platform,
            @Nullable String implementationVersion,
            @Nullable String... candidates
    ) {
        MinecraftVersion version = firstVersion(candidates);
        if (version == null)
            throw new IllegalStateException("Could not resolve the Minecraft version for " + platform + ".");

        return new VNCProvider.VersionInfo(platform, implementationVersion, version);
    }

    @Nullable
    public static MinecraftVersion firstVersion(@Nullable String... candidates) {
        if (candidates == null) return null;

        for (String candidate : candidates) {
            MinecraftVersion version = Versioning.parseMinecraftVersion(candidate);
            if (version != null) return version;
        }

        return null;
    }

    /**
     * Looks up a runtime class without initializing it.
     *
     * <p>Agent-based loaders can end up on a different class loader than the game, so the lookup
     * also retries through the thread context class loader.</p>
     *
     * @param name fully qualified class name
     * @return the class when present in the current runtime, otherwise null
     */
    @Nullable
    public static Class<?> findClass(@NotNull String name) {
        try {
            return Class.forName(name, false, VersionResolver.class.getClassLoader());
        } catch (Throwable ignored) {}

        try {
            return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Reads the implementation version declared by the manifest of the supplied class.
     *
     * @param type class to inspect, or null
     * @return declared implementation version, or null when unavailable
     */
    @Nullable
    public static String implementationVersion(@Nullable Class<?> type) {
        if (type == null) return null;

        Package pkg = type.getPackage();
        return pkg != null ? pkg.getImplementationVersion() : null;
    }

    @Nullable
    public static String firstNonBlank(@Nullable String... values) {
        if (values == null) return null;

        for (String value : values) {
            if (value != null && !value.trim().isEmpty())
                return value.trim();
        }

        return null;
    }
}
