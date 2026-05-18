package me.croabeast.vnc;

import lombok.experimental.UtilityClass;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.VersionInfo;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
class NeoForgeRuntime {

    static boolean isAvailable() {
        FMLLoader.versionInfo();
        return true;
    }

    static boolean isLoaded() {
        return FMLLoader.versionInfo() != null;
    }

    @NotNull
    static VNCProvider.VersionInfo resolve() {
        VersionInfo versionInfo = versionInfo();

        return VersionResolver.resolve(
                "NeoForge",
                neoForgeVersion(versionInfo),
                minecraftVersion(versionInfo),
                System.getProperty("minecraft.version"),
                System.getProperty("fml.mcVersion")
        );
    }

    @Nullable
    static String minecraftVersion() {
        return minecraftVersion(versionInfo());
    }

    @Nullable
    static String neoForgeVersion() {
        return neoForgeVersion(versionInfo());
    }

    @Nullable
    private static String minecraftVersion(@Nullable VersionInfo versionInfo) {
        return VersionResolver.firstNonBlank(
                versionInfo != null ? versionInfo.mcVersion() : null,
                System.getProperty("minecraft.version"),
                System.getProperty("fml.mcVersion")
        );
    }

    @Nullable
    private static String neoForgeVersion(@Nullable VersionInfo versionInfo) {
        return VersionResolver.firstNonBlank(
                versionInfo != null ? versionInfo.neoForgeVersion() : null,
                NeoForge.class.getPackage() != null ? NeoForge.class.getPackage().getImplementationVersion() : null
        );
    }

    @Nullable
    private static VersionInfo versionInfo() {
        return FMLLoader.versionInfo();
    }
}
