package me.croabeast.vnc;

import org.spongepowered.api.Sponge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class SpongeProvider implements VNCProvider {

    @Override
    @NotNull
    public VNCProvider.VersionInfo resolve() {
        return VersionResolver.resolve(
                "Sponge",
                spongeVersion(),
                minecraftVersion(),
                System.getProperty("minecraft.version")
        );
    }

    @Override
    public boolean isAvailable() {
        try {
            Sponge.platform();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nullable
    public String minecraftVersion() {
        org.spongepowered.api.MinecraftVersion version = Sponge.platform().minecraftVersion();

        return VersionResolver.firstNonBlank(
                version.name(),
                version != null ? String.valueOf(version) : null,
                System.getProperty("minecraft.version")
        );
    }

    @Nullable
    public String spongeVersion() {
        Package pkg = Sponge.class.getPackage();
        return pkg != null ? pkg.getImplementationVersion() : null;
    }
}
