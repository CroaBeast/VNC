package me.croabeast.vnc;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
final class ServerVersionInfo {

    private final MinecraftVersion version;
    private final String classicVersion;
    private final String dropVersion;
    private final int protocol;
    private final String craftBukkitPackage;
    private final String bukkitApiVersion;
    private final String serverFork;
    private final boolean paper;
    private final int javaVersion;
    private final double serverVersion;

    ServerVersionInfo(
            @NotNull MinecraftVersion version,
            @NotNull String classicVersion,
            @NotNull String dropVersion,
            int protocol,
            @NotNull String craftBukkitPackage,
            @NotNull String bukkitApiVersion,
            @NotNull String serverFork,
            boolean paper,
            int javaVersion,
            double serverVersion
    ) {
        this.version = Objects.requireNonNull(version, "version");
        this.classicVersion = Objects.requireNonNull(classicVersion, "classicVersion");
        this.dropVersion = Objects.requireNonNull(dropVersion, "dropVersion");
        this.protocol = protocol;
        this.craftBukkitPackage = Objects.requireNonNull(craftBukkitPackage, "craftBukkitPackage");
        this.bukkitApiVersion = Objects.requireNonNull(bukkitApiVersion, "bukkitApiVersion");
        this.serverFork = Objects.requireNonNull(serverFork, "serverFork");
        this.paper = paper;
        this.javaVersion = javaVersion;
        this.serverVersion = serverVersion;
    }

    public boolean isAtLeast(@NotNull String version) {
        return VNC.compare(this.version, version) >= 0;
    }

    public boolean isAtLeast(int minor) {
        return isAtLeast("1." + minor);
    }

    public boolean isAtLeast(int minor, int patch) {
        return isAtLeast("1." + minor + "." + patch);
    }

    public boolean isBefore(@NotNull String version) {
        return VNC.compare(this.version, version) < 0;
    }

    public boolean isBetween(@NotNull String minInclusive, @NotNull String maxInclusive) {
        return VNC.compare(this.version, minInclusive) >= 0 &&
                VNC.compare(this.version, maxInclusive) <= 0;
    }
}
