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
    @Nullable
    public final VNCProvider.VersionInfo SERVER = PROVIDER != null ? PROVIDER.resolveIfAvailable() : null;
    @Nullable
    public final MinecraftVersion SERVER_MINECRAFT_VERSION = SERVER != null ? SERVER.getMinecraftVersion() : null;
    public final String SERVER_CLASSIC_VERSION = SERVER != null ? SERVER.getClassicVersion() : "";
    public final String SERVER_DROP_VERSION = SERVER != null ? SERVER.getDropVersion() : "";
    public final int SERVER_PROTOCOL = SERVER != null ? SERVER.getProtocol() : -1;
    public final int JAVA_VERSION = Versioning.javaVersion();
    public final double SERVER_VERSION = SERVER != null ? SERVER.getServerVersion() : -1D;

    @Nullable
    public VNCProvider getProviderOrNull() {
        return PROVIDER;
    }

    @NotNull
    public VNCProvider getProvider() {
        if (PROVIDER != null) return PROVIDER;

        throw new IllegalStateException("No supported VNC runtime implementation was detected.");
    }

    @NotNull
    public VNCProvider provider() {
        return getProvider();
    }

    @Nullable
    public VNCProvider.VersionInfo resolveIfAvailable() {
        return PROVIDER != null ? PROVIDER.resolveIfAvailable() : null;
    }

    @NotNull
    public VNCProvider.VersionInfo resolve() {
        return getProvider().resolve();
    }

    public boolean hasRuntime() {
        return PROVIDER != null;
    }

    @NotNull
    public String platform() {
        return getProvider().getPlatform();
    }

    public boolean isAtLeast(@NotNull String version) {
        return getProvider().isAtLeast(version);
    }

    public boolean isAtLeast(@NotNull MinecraftVersion version, @NotNull String minimum) {
        return Versioning.isAtLeast(version, minimum);
    }

    public boolean isBefore(@NotNull String version) {
        return getProvider().isBefore(version);
    }

    public boolean isBefore(@NotNull MinecraftVersion version, @NotNull String maximum) {
        return Versioning.isBefore(version, maximum);
    }

    public boolean isBetween(@NotNull String minInclusive, @NotNull String maxInclusive) {
        return getProvider().isBetween(minInclusive, maxInclusive);
    }

    public boolean isBetween(
            @NotNull MinecraftVersion version,
            @NotNull String minInclusive,
            @NotNull String maxInclusive
    ) {
        return Versioning.isBetween(version, minInclusive, maxInclusive);
    }

    public int compare(@NotNull MinecraftVersion left, @NotNull String right) {
        return Versioning.compare(left, right);
    }

    public int compare(@NotNull MinecraftVersion left, @NotNull MinecraftVersion right) {
        return Versioning.compare(left, right);
    }

    @Nullable
    public MinecraftVersion parseMinecraftVersion(@Nullable String value) {
        return Versioning.parseMinecraftVersion(value);
    }

    @NotNull
    public MinecraftVersion requireMinecraftVersion(@Nullable String value) {
        return Versioning.requireMinecraftVersion(value);
    }

    public int javaVersion() {
        return Versioning.javaVersion();
    }

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

        return tryLiteLoader();
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
    private VNCProvider tryLiteLoader() {
        try {
            return available(new LiteLoaderProvider());
        } catch (Throwable ignored) {
            return null;
        }
    }
}
