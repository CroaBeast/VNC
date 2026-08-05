package me.croabeast.vnc;

import lombok.experimental.UtilityClass;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Distinguishes the Fabric Loader distributions that VNC reports as separate platforms.
 *
 * <p>Legacy Fabric, Ornithe, and Babric all expose the same {@code FabricLoader} API: the first
 * runs stock Fabric Loader on old releases, the other two are forks. They cannot be told apart by
 * class presence, so the flavor is resolved from the loaded mod set and, as a last step, from the
 * shape of the reported game version.</p>
 */
@UtilityClass
class FabricFlavor {

    static final String FABRIC = "Fabric";
    static final String LEGACY_FABRIC = "Legacy Fabric";
    static final String ORNITHE = "Ornithe";
    static final String BABRIC = "Babric";
    static final String BABRIC_BTA = "Babric (BTA)";

    /**
     * Every BTA build runs on Beta 1.7.3, whatever its own version number says.
     */
    static final String BTA_GAME_VERSION = "b1.7.3";

    // BTA numbered its builds "1.7.4.x" through "1.7.7.x" until 2023, then switched to "7.x".
    // Minecraft never published a four-segment id, nor a classic major between 2 and 9.
    private static final Pattern BTA_LEGACY = Pattern.compile("^v?1\\.7\\.\\d+\\.\\d+(?:_\\d+)?(?:-[\\w.-]+)?$");
    private static final Pattern BTA_MODERN = Pattern.compile("^v?[2-9]\\.\\d+(?:_\\d+)?(?:-[\\w.-]+)?$");

    // Beta 1.7.3 is the only thing "1.7.3" can mean: it was never a release id.
    private static final String AMBIGUOUS_BETA = "1.7.3";

    /**
     * Resolves the platform name to report for the current runtime.
     *
     * @param gameVersion version reported by the {@code minecraft} mod container
     * @return flavor name, falling back to {@link #FABRIC}
     */
    @NotNull
    static String detect(@Nullable String gameVersion) {
        if (hasMod("osl") || hasMod("ornithe-standard-libraries")) return ORNITHE;
        if (hasMod("legacy-fabric-api-base") || hasMod("legacy-fabric-api")) return LEGACY_FABRIC;

        if (isBta(gameVersion)) return BABRIC_BTA;
        if (isBeta(gameVersion)) return BABRIC;

        return FABRIC;
    }

    /**
     * Rewrites the reported game version into something VNC can parse.
     *
     * <p>BTA reports its own build number, which is not a Minecraft version at all. Babric may
     * report the bare {@code 1.7.3}, which has to be read as Beta rather than as a release that
     * never existed.</p>
     *
     * @param flavor      resolved flavor name
     * @param gameVersion version reported by the {@code minecraft} mod container
     * @return version text to resolve against, or {@code null} when none was reported
     */
    @Nullable
    static String normalizeGameVersion(@NotNull String flavor, @Nullable String gameVersion) {
        if (BABRIC_BTA.equals(flavor)) return BTA_GAME_VERSION;

        if (BABRIC.equals(flavor) && AMBIGUOUS_BETA.equals(trim(gameVersion)))
            return BTA_GAME_VERSION;

        return gameVersion;
    }

    private static boolean isBta(@Nullable String gameVersion) {
        if (hasMod("bta")) return true;

        String value = trim(gameVersion);
        if (value == null) return false;

        return BTA_LEGACY.matcher(value).matches() || BTA_MODERN.matcher(value).matches();
    }

    private static boolean isBeta(@Nullable String gameVersion) {
        String value = trim(gameVersion);
        if (value == null) return false;

        if (AMBIGUOUS_BETA.equals(value)) return true;

        MinecraftVersion version = Versioning.parseMinecraftVersion(value);
        return version != null && version.isPreRelease();
    }

    private static boolean hasMod(@NotNull String modId) {
        try {
            return FabricLoader.getInstance().isModLoaded(modId);
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nullable
    private static String trim(@Nullable String value) {
        return VersionResolver.firstNonBlank(value);
    }
}
