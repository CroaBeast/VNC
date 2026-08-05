package me.croabeast.vnc;

import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class ForgeProvider implements VNCProvider {

    @NotNull
    public VNCProvider.VersionInfo resolve() {
        return VersionResolver.resolve(
                "Forge",
                forgeVersion(),
                minecraftVersion(),
                System.getProperty("minecraft.version"),
                System.getProperty("fml.mcVersion")
        );
    }

    @Override
    public boolean isAvailable() {
        try {
            return MinecraftForge.class.getName() != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nullable
    public String minecraftVersion() {
        return VersionResolver.firstNonBlank(
                System.getProperty("minecraft.version"),
                System.getProperty("fml.mcVersion")
        );
    }

    @Nullable
    public String forgeVersion() {
        return VersionResolver.firstNonBlank(
                System.getProperty("forge.version"),
                implementationVersion(MinecraftForge.class)
        );
    }

    @Nullable
    private String implementationVersion(@NotNull Class<?> type) {
        Package pkg = type.getPackage();
        return pkg != null ? pkg.getImplementationVersion() : null;
    }
}
