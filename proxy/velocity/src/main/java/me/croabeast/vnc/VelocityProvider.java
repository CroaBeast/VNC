package me.croabeast.vnc;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.ProxyVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class VelocityProvider implements VNCProvider {

    @Override
    public boolean isAvailable() {
        try {
            return ProxyServer.class.getName() != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    @NotNull
    public VNCProvider.VersionInfo resolve() {
        String version = System.getProperty("minecraft.version");
        if (version == null || version.trim().isEmpty())
            throw new IllegalStateException("Velocity requires an explicit minecraft.version system property.");

        return resolve(version);
    }

    @NotNull
    public VNCProvider.VersionInfo resolve(@NotNull String minecraftVersion) {
        return resolve(null, minecraftVersion);
    }

    @NotNull
    public VNCProvider.VersionInfo resolve(@Nullable ProxyServer proxyServer, @NotNull String minecraftVersion) {
        return VersionResolver.resolve(
                "Velocity",
                implementationVersion(proxyServer),
                minecraftVersion,
                System.getProperty("minecraft.version")
        );
    }

    @Nullable
    public String implementationVersion(@Nullable ProxyServer proxyServer) {
        ProxyVersion version = proxyServer != null ? proxyServer.getVersion() : null;

        return VersionResolver.firstNonBlank(
                version != null ? version.getVersion() : null,
                version != null ? String.valueOf(version) : null,
                implementationVersion(ProxyServer.class)
        );
    }

    @Nullable
    private String implementationVersion(@NotNull Class<?> type) {
        Package pkg = type.getPackage();
        return pkg != null ? pkg.getImplementationVersion() : null;
    }
}
