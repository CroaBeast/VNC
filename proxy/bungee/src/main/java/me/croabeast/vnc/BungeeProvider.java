package me.croabeast.vnc;

import net.md_5.bungee.api.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class BungeeProvider implements VNCProvider {

    @Override
    @NotNull
    public VNCProvider.VersionInfo resolve() {
        return resolve(ProxyServer.getInstance());
    }

    @NotNull
    VNCProvider.VersionInfo resolve(@Nullable ProxyServer proxyServer) {
        return VersionResolver.resolve(
                "BungeeCord",
                implementationVersion(proxyServer),
                proxyServer != null ? proxyServer.getGameVersion() : null,
                proxyServer != null ? proxyServer.getVersion() : null,
                System.getProperty("minecraft.version")
        );
    }

    @Override
    public boolean isAvailable() {
        try {
            return ProxyServer.getInstance() != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nullable
    public String implementationVersion(@Nullable ProxyServer proxyServer) {
        return VersionResolver.firstNonBlank(
                proxyServer != null ? proxyServer.getVersion() : null,
                implementationVersion(ProxyServer.class)
        );
    }

    @Nullable
    private String implementationVersion(@NotNull Class<?> type) {
        Package pkg = type.getPackage();
        return pkg != null ? pkg.getImplementationVersion() : null;
    }
}
