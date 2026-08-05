package me.croabeast.vnc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.management.ManagementFactory;

final class JavaAgentProvider implements VNCProvider {

    private static final String AGENT_PREFIX = "-javaagent:";

    @NotNull
    public VNCProvider.VersionInfo resolve() {
        return VersionResolver.resolve(
                "Java Agent",
                agentName(),
                minecraftVersion(),
                System.getProperty("minecraft.version")
        );
    }

    /**
     * This provider is the last detection fallback, so it only claims the runtime when both an
     * agent and a readable game version are present.
     */
    @Override
    public boolean isAvailable() {
        return agentArgument() != null && Versioning.parseMinecraftVersion(minecraftVersion()) != null;
    }

    @Nullable
    public String minecraftVersion() {
        return GameVersionLookup.detect();
    }

    @Nullable
    public String agentName() {
        String argument = agentArgument();
        if (argument == null) return null;

        int options = argument.indexOf('=');
        String path = options >= 0 ? argument.substring(0, options) : argument;

        int separator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return separator >= 0 ? path.substring(separator + 1) : path;
    }

    @Nullable
    private String agentArgument() {
        try {
            for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                if (argument.startsWith(AGENT_PREFIX))
                    return argument.substring(AGENT_PREFIX.length());
            }
        } catch (Throwable ignored) {}

        return null;
    }
}
