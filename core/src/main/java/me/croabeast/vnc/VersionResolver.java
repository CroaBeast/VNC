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
