package me.croabeast.vnc;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

final class QuiltProvider implements VNCProvider {

    @NotNull
    public VNCProvider.VersionInfo resolve() {
        return VersionResolver.resolve(
                flavor(),
                loaderVersion(),
                minecraftVersion(),
                System.getProperty("minecraft.version")
        );
    }

    /**
     * Returns {@code Ornithe} when the Ornithe Standard Libraries are present, otherwise
     * {@code Quilt}. Ornithe supports both the Fabric and the Quilt loader.
     *
     * @return resolved flavor name
     */
    @NotNull
    public String flavor() {
        boolean ornithe = QuiltLoader.getModContainer("osl").isPresent()
                || QuiltLoader.getModContainer("ornithe-standard-libraries").isPresent();

        return ornithe ? "Ornithe" : "Quilt";
    }

    @Override
    public boolean isAvailable() {
        try {
            QuiltLoader.getNormalizedGameVersion();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nullable
    public String minecraftVersion() {
        return VersionResolver.firstNonBlank(
                QuiltLoader.getRawGameVersion(),
                QuiltLoader.getNormalizedGameVersion(),
                modVersion("minecraft")
        );
    }

    @Nullable
    public String loaderVersion() {
        return VersionResolver.firstNonBlank(
                modVersion("quilt_loader"),
                implementationVersion(QuiltLoader.class)
        );
    }

    @Nullable
    private String modVersion(@NotNull String modId) {
        Optional<ModContainer> container = QuiltLoader.getModContainer(modId);
        if (!container.isPresent()) return null;

        Version version = container.get().metadata().version();

        return VersionResolver.firstNonBlank(
                version.raw(),
                version != null ? String.valueOf(version) : null
        );
    }

    @Nullable
    private String implementationVersion(@NotNull Class<?> type) {
        Package pkg = type.getPackage();
        return pkg != null ? pkg.getImplementationVersion() : null;
    }
}
