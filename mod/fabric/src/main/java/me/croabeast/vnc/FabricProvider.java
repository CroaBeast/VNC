package me.croabeast.vnc;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

final class FabricProvider implements VNCProvider {

    @NotNull
    public VNCProvider.VersionInfo resolve() {
        String gameVersion = minecraftVersion();
        String flavor = FabricFlavor.detect(gameVersion);

        return VersionResolver.resolve(
                flavor,
                loaderVersion(),
                FabricFlavor.normalizeGameVersion(flavor, gameVersion),
                System.getProperty("minecraft.version")
        );
    }

    /**
     * Returns the Fabric distribution running the game: {@code Fabric}, {@code Legacy Fabric},
     * {@code Ornithe}, {@code Babric}, or {@code Babric (BTA)}.
     *
     * @return resolved flavor name
     */
    @NotNull
    public String flavor() {
        return FabricFlavor.detect(minecraftVersion());
    }

    @Override
    public boolean isAvailable() {
        try {
            FabricLoader.getInstance();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nullable
    public String minecraftVersion() {
        return modVersion("minecraft");
    }

    @Nullable
    public String loaderVersion() {
        return VersionResolver.firstNonBlank(
                modVersion("fabricloader"),
                implementationVersion()
        );
    }

    @Nullable
    private String modVersion(@NotNull String modId) {
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(modId);
        return container.map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString()).orElse(null);
    }

    @Nullable
    private String implementationVersion() {
        Package pkg = FabricLoader.class.getPackage();
        return pkg != null ? pkg.getImplementationVersion() : null;
    }
}
