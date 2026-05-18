package me.croabeast.vnc;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Common runtime contract implemented by each platform module.
 */
public interface VNCProvider {

    /**
     * @return true when this provider can resolve the current runtime.
     */
    boolean isAvailable();

    /**
     * Resolves the current runtime version snapshot.
     *
     * @return resolved platform version information
     */
    @NotNull
    VNCProvider.VersionInfo resolve();

    /**
     * Resolves this provider only when it is currently available.
     *
     * @return resolved platform version information, or null when unavailable
     */
    @Nullable
    default VNCProvider.VersionInfo resolveIfAvailable() {
        try {
            return isAvailable() ? resolve() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Alias for {@link #resolve()} for fluent provider-style usage.
     *
     * @return resolved platform version information
     */
    @NotNull
    default VNCProvider.VersionInfo getInfo() {
        return resolve();
    }

    @NotNull
    default String getPlatform() {
        return resolve().getPlatform();
    }

    @NotNull
    default String getImplementationVersion() {
        return resolve().getImplementationVersion();
    }

    @NotNull
    default MinecraftVersion getMinecraftVersion() {
        return resolve().getMinecraftVersion();
    }

    @NotNull
    default String getClassicVersion() {
        return resolve().getClassicVersion();
    }

    @NotNull
    default String getDropVersion() {
        return resolve().getDropVersion();
    }

    default int getProtocol() {
        return resolve().getProtocol();
    }

    default int getJavaVersion() {
        return resolve().getJavaVersion();
    }

    default double getServerVersion() {
        return resolve().getServerVersion();
    }

    default boolean isAtLeast(@NotNull String version) {
        return resolve().isAtLeast(version);
    }

    default boolean isBefore(@NotNull String version) {
        return resolve().isBefore(version);
    }

    default boolean isBetween(@NotNull String minInclusive, @NotNull String maxInclusive) {
        return resolve().isBetween(minInclusive, maxInclusive);
    }

    default int compare(@NotNull MinecraftVersion left, @NotNull String right) {
        return Versioning.compare(left, right);
    }

    default int compare(@NotNull MinecraftVersion left, @NotNull MinecraftVersion right) {
        return Versioning.compare(left, right);
    }

    /**
     * Generic runtime version snapshot shared by the platform-specific modules.
     */
    @Getter
    final class VersionInfo {

        private final String platform;
        private final String implementationVersion;
        private final MinecraftVersion minecraftVersion;
        private final String classicVersion;
        private final String dropVersion;
        private final int protocol;
        private final int javaVersion;
        private final double serverVersion;

        public VersionInfo(
                @NotNull String platform,
                @Nullable String implementationVersion,
                @NotNull MinecraftVersion minecraftVersion
        ) {
            this.platform = Objects.requireNonNull(platform, "platform");
            this.implementationVersion = implementationVersion != null ? implementationVersion : "";
            this.minecraftVersion = Objects.requireNonNull(minecraftVersion, "minecraftVersion");
            this.classicVersion = Versioning.DEFAULT_SCHEME.toClassic(minecraftVersion);
            this.dropVersion = Versioning.DEFAULT_SCHEME.toDrop(minecraftVersion);
            this.protocol = minecraftVersion.getProtocol() != null ? minecraftVersion.getProtocol() : -1;
            this.javaVersion = Versioning.javaVersion();
            this.serverVersion = Versioning.toLegacyServerVersion(classicVersion);
        }

        public boolean isAtLeast(@NotNull String version) {
            return Versioning.isAtLeast(minecraftVersion, version);
        }

        public boolean isBefore(@NotNull String version) {
            return Versioning.isBefore(minecraftVersion, version);
        }

        public boolean isBetween(@NotNull String minInclusive, @NotNull String maxInclusive) {
            return Versioning.isBetween(minecraftVersion, minInclusive, maxInclusive);
        }
    }
}
