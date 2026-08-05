package me.croabeast.vnc;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Runtime-neutral VNC entry point.
 *
 * <p>The bootstrap module owns this class and delegates to the platform module that is present in
 * the current runtime. Core parsing/comparison helpers stay in {@link Versioning}; platform-specific
 * bridges stay in their own modules.</p>
 */
@UtilityClass
public class VNC {

    @Nullable
    private final VNCProvider PROVIDER = detectProvider();

    /**
     * Resolved server version information for the current runtime, or {@code null} when no
     * supported platform was detected.
     */
    @Nullable
    public final VNCProvider.VersionInfo SERVER = PROVIDER != null ? PROVIDER.resolveIfAvailable() : null;

    /**
     * Parsed {@link MinecraftVersion} for the running server, or {@code null} when unavailable.
     */
    @Nullable
    public final MinecraftVersion SERVER_MINECRAFT_VERSION = SERVER != null ? SERVER.getMinecraftVersion() : null;

    /**
     * Raw version string reported by the server (e.g. {@code 1.21.4}), or an empty string when
     * no runtime was detected.
     */
    public final String SERVER_RAW_VERSION = SERVER != null ? SERVER.getVersion() : "";

    /**
     * Classic {@code 1.x.y} version string for the running server, or an empty string when
     * no runtime was detected.
     */
    public final String SERVER_CLASSIC_VERSION = SERVER != null ? SERVER.getClassicVersion() : "";

    /**
     * Drop-style version string for the running server, or an empty string when no runtime was
     * detected.
     */
    public final String SERVER_DROP_VERSION = SERVER != null ? SERVER.getDropVersion() : "";

    /**
     * Network protocol number reported by the server, or {@code -1} when no runtime was detected.
     */
    public final int SERVER_PROTOCOL = SERVER != null ? SERVER.getProtocol() : -1;

    /**
     * Major version of the JVM that is currently running (e.g. {@code 17}, {@code 21}).
     */
    public final int JAVA_VERSION = Versioning.javaVersion();

    /**
     * Legacy double representation of the server version (e.g. {@code 20.4} for 1.20.4), or
     * {@code -1} when no runtime was detected.
     */
    public final double SERVER_VERSION = SERVER != null ? SERVER.getServerVersion() : -1D;

    /**
     * Explicit alias for {@link #SERVER_VERSION} when call sites prefer a descriptive name.
     */
    public final double SERVER_VERSION_DOUBLE = SERVER_VERSION;

    /**
     * Returns the detected provider, or {@code null} when no supported runtime was found.
     *
     * @return active provider, or {@code null}
     */
    @Nullable
    public VNCProvider getProviderOrNull() {
        return PROVIDER;
    }

    /**
     * Returns the detected provider, throwing when none is available.
     *
     * @return active provider
     * @throws IllegalStateException when no supported VNC runtime was detected
     */
    @NotNull
    public VNCProvider getProvider() {
        if (PROVIDER != null) return PROVIDER;

        throw new IllegalStateException("No supported VNC runtime implementation was detected.");
    }

    /**
     * Alias for {@link #getProvider()} for fluent call-site style.
     *
     * @return active provider
     * @throws IllegalStateException when no supported VNC runtime was detected
     */
    @NotNull
    public VNCProvider provider() {
        return getProvider();
    }

    /**
     * Resolves the current runtime version snapshot only when a provider is available.
     *
     * @return resolved version information, or {@code null} when no runtime was detected
     */
    @Nullable
    public VNCProvider.VersionInfo resolveIfAvailable() {
        return PROVIDER != null ? PROVIDER.resolveIfAvailable() : null;
    }

    /**
     * Resolves the current runtime version snapshot, throwing when no provider is available.
     *
     * @return resolved version information
     * @throws IllegalStateException when no supported VNC runtime was detected
     */
    @NotNull
    public VNCProvider.VersionInfo resolve() {
        return getProvider().resolve();
    }

    /**
     * Returns whether a supported VNC provider was detected for the current runtime.
     *
     * @return {@code true} when a provider is present
     */
    public boolean hasRuntime() {
        return PROVIDER != null;
    }

    /**
     * Returns the platform name reported by the active provider (e.g. {@code "Bukkit"},
     * {@code "Velocity"}).
     *
     * @return platform identifier string
     * @throws IllegalStateException when no supported VNC runtime was detected
     */
    @NotNull
    public String platform() {
        return getProvider().getPlatform();
    }

    /**
     * Returns whether the server version is at least the given version string.
     *
     * @param version minimum version to test against
     * @return {@code true} when the server version is equal to or newer than {@code version}
     */
    public boolean isAtLeast(@NotNull String version) {
        return getProvider().isAtLeast(version);
    }

    /**
     * Returns whether the given parsed version is at least the specified minimum.
     *
     * @param version parsed version to test
     * @param minimum minimum version string (classic or drop notation)
     * @return {@code true} when {@code version} is equal to or newer than {@code minimum}
     */
    public boolean isAtLeast(@NotNull MinecraftVersion version, @NotNull String minimum) {
        return Versioning.isAtLeast(version, minimum);
    }

    /**
     * Returns whether the server version is strictly before the given version string.
     *
     * @param version exclusive upper bound to test against
     * @return {@code true} when the server version is older than {@code version}
     */
    public boolean isBefore(@NotNull String version) {
        return getProvider().isBefore(version);
    }

    /**
     * Returns whether the given parsed version is strictly before the specified maximum.
     *
     * @param version parsed version to test
     * @param maximum exclusive upper bound (classic or drop notation)
     * @return {@code true} when {@code version} is older than {@code maximum}
     */
    public boolean isBefore(@NotNull MinecraftVersion version, @NotNull String maximum) {
        return Versioning.isBefore(version, maximum);
    }

    /**
     * Returns whether the server version falls within the given inclusive range.
     *
     * @param minInclusive lower bound, inclusive
     * @param maxInclusive upper bound, inclusive
     * @return {@code true} when the server version is within the range
     */
    public boolean isBetween(@NotNull String minInclusive, @NotNull String maxInclusive) {
        return getProvider().isBetween(minInclusive, maxInclusive);
    }

    /**
     * Returns whether the given parsed version falls within the specified inclusive range.
     *
     * @param version      parsed version to test
     * @param minInclusive lower bound, inclusive
     * @param maxInclusive upper bound, inclusive
     * @return {@code true} when {@code version} is within the range
     */
    public boolean isBetween(
            @NotNull MinecraftVersion version,
            @NotNull String minInclusive,
            @NotNull String maxInclusive
    ) {
        return Versioning.isBetween(version, minInclusive, maxInclusive);
    }

    /**
     * Compares a parsed version against a textual version identifier.
     *
     * @param left  parsed left-hand version
     * @param right textual right-hand version
     * @return negative when {@code left < right}, zero when equal, positive when {@code left > right}
     */
    public int compare(@NotNull MinecraftVersion left, @NotNull String right) {
        return Versioning.compare(left, right);
    }

    /**
     * Compares two parsed versions after normalizing both to classic numbering.
     *
     * @param left  left-hand version
     * @param right right-hand version
     * @return negative when {@code left < right}, zero when equal, positive when {@code left > right}
     */
    public int compare(@NotNull MinecraftVersion left, @NotNull MinecraftVersion right) {
        return Versioning.compare(left, right);
    }

    /**
     * Parses the first Minecraft-looking version token from arbitrary runtime text.
     *
     * @param value runtime version text
     * @return parsed version, or {@code null} when no supported token is present
     */
    @Nullable
    public MinecraftVersion parseMinecraftVersion(@Nullable String value) {
        return Versioning.parseMinecraftVersion(value);
    }

    /**
     * Parses a runtime version token and fails with a clear error if it cannot be resolved.
     *
     * @param value runtime version text
     * @return parsed Minecraft version
     * @throws IllegalArgumentException when no version token could be found in {@code value}
     */
    @NotNull
    public MinecraftVersion requireMinecraftVersion(@Nullable String value) {
        return Versioning.requireMinecraftVersion(value);
    }

    /**
     * Parses the current JVM major version.
     *
     * @return Java major version, for example {@code 8}, {@code 17}, or {@code 21}
     */
    public int javaVersion() {
        return Versioning.javaVersion();
    }

    /**
     * Converts a classic {@code 1.x.y} version to the legacy double shape used by old plugins.
     *
     * @param classicVersion classic version text
     * @return legacy double representation
     */
    public double toLegacyServerVersion(@NotNull String classicVersion) {
        return Versioning.toLegacyServerVersion(classicVersion);
    }

    @Nullable
    private VNCProvider detectProvider() {
        VNCProvider provider;

        provider = tryBukkit();
        if (provider != null) return provider;

        provider = tryBungee();
        if (provider != null) return provider;

        provider = tryVelocity();
        if (provider != null) return provider;

        provider = tryQuilt();
        if (provider != null) return provider;

        provider = tryFabric();
        if (provider != null) return provider;

        provider = tryForge();
        if (provider != null) return provider;

        provider = tryNeoForge();
        if (provider != null) return provider;

        provider = trySponge();
        if (provider != null) return provider;

        provider = tryRift();
        if (provider != null) return provider;

        provider = tryLiteLoader();
        if (provider != null) return provider;

        provider = tryModLoader();
        if (provider != null) return provider;

        provider = tryNilLoader();
        if (provider != null) return provider;

        // Generic agents match any instrumented runtime, so they stay last.
        return tryJavaAgent();
    }

    @Nullable
    private VNCProvider available(@NotNull VNCProvider provider) {
        return provider.isAvailable() ? provider : null;
    }

    @Nullable
    private VNCProvider tryBukkit() {
        try {
            return available(new BukkitProvider());
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private VNCProvider tryBungee() {
        try {
            return available(new BungeeProvider());
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private VNCProvider tryVelocity() {
        try {
            return available(new VelocityProvider());
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private VNCProvider tryQuilt() {
        try {
            return available(new QuiltProvider());
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private VNCProvider tryFabric() {
        try {
            return available(new FabricProvider());
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private VNCProvider tryForge() {
        try {
            return available(new ForgeProvider());
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private VNCProvider tryNeoForge() {
        try {
            return available(new NeoForgeProvider());
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private VNCProvider trySponge() {
        try {
            return available(new SpongeProvider());
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private VNCProvider tryRift() {
        try {
            return available(new RiftProvider());
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private VNCProvider tryLiteLoader() {
        try {
            return available(new LiteLoaderProvider());
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private VNCProvider tryModLoader() {
        try {
            return available(new ModLoaderProvider());
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private VNCProvider tryNilLoader() {
        try {
            return available(new NilLoaderProvider());
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private VNCProvider tryJavaAgent() {
        try {
            return available(new JavaAgentProvider());
        } catch (Throwable ignored) {
            return null;
        }
    }
}
