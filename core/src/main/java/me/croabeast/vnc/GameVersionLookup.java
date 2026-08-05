package me.croabeast.vnc;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loader-independent lookup for the running Minecraft version.
 *
 * <p>Some runtimes bridged by VNC know nothing about Minecraft itself. Generic Java agents,
 * NilLoader, and the oldest jar-mod loaders all run inside the game without exposing any version
 * accessor, so their providers have to read the version from the game instead of from the loader.</p>
 *
 * <p>The lookup only reads sources that survive obfuscated production jars: launcher-supplied
 * system properties, the {@code version.json} descriptor bundled since the {@code 1.14} line, and
 * the {@code --version} launch argument.</p>
 */
@UtilityClass
public class GameVersionLookup {

    private static final Pattern MANIFEST_NAME = Pattern.compile("\"(?:id|name)\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern LAUNCH_VERSION = Pattern.compile("--version\\s+(\\S+)");

    /**
     * Resolves the running Minecraft version from every supported generic source.
     *
     * @return raw version text, or {@code null} when no source was readable
     */
    @Nullable
    public static String detect() {
        return VersionResolver.firstNonBlank(
                System.getProperty("minecraft.version"),
                System.getProperty("fml.mcVersion"),
                versionManifest(),
                launchArgument()
        );
    }

    /**
     * Reads the version name from the {@code version.json} descriptor on the classpath.
     *
     * <p>The descriptor is only bundled by the {@code 1.14} line and newer, so older runtimes
     * always fall through to the remaining sources.</p>
     *
     * @return version name declared by the descriptor, or {@code null} when absent or unreadable
     */
    @Nullable
    public static String versionManifest() {
        try (InputStream stream = GameVersionLookup.class.getResourceAsStream("/version.json")) {
            if (stream == null) return null;

            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));

            String line;
            while ((line = reader.readLine()) != null) builder.append(line);

            Matcher matcher = MANIFEST_NAME.matcher(builder);
            return matcher.find() ? matcher.group(1) : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Reads the {@code --version} argument the launcher passed to the game entry point.
     *
     * <p>Launchers often pass an instance name instead of a plain version, so the returned text
     * still has to go through {@link Versioning#parseMinecraftVersion(String)}.</p>
     *
     * @return raw {@code --version} argument, or {@code null} when the command line is unavailable
     */
    @Nullable
    public static String launchArgument() {
        String command = System.getProperty("sun.java.command");
        if (command == null) return null;

        Matcher matcher = LAUNCH_VERSION.matcher(command);
        return matcher.find() ? matcher.group(1) : null;
    }
}
