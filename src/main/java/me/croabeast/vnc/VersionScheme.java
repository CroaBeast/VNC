package me.croabeast.vnc;

import org.jetbrains.annotations.NotNull;

public interface VersionScheme {

    VersionScheme MOJANG = new VersionScheme() {

        @NotNull
        String classicToDrop(@NotNull MinecraftVersion v) {
            int minor = v.getMinor(), patch = v.getPatch(), y, d, base;

            if (minor < 22) {
                switch (minor) {
                    case 0:  y = 11; d = 1; base = 0; break; // 1.0.x  -> 11.1.x
                    case 1:  y = 12; d = 1; base = 0; break; // 1.1.x  -> 12.1.x
                    case 2:  y = 12; d = 2; base = 0; break; // 1.2.x  -> 12.2.x
                    case 3:  y = 12; d = 3; base = 0; break; // 1.3.x  -> 12.3.x
                    case 4:  y = 12; d = 4; base = 0; break; // 1.4.x  -> 12.4.x

                    case 5:  y = 13; d = 1; base = 0; break; // 1.5.x  -> 13.1.x
                    case 6:  y = 13; d = 2; base = 0; break; // 1.6.x  -> 13.2.x
                    case 7:  y = 13; d = 3; base = 0; break; // 1.7.x  -> 13.3.x

                    case 8:  y = 14; d = 1; base = 0; break; // 1.8.x  -> 14.1.x

                    case 9:  y = 16; d = 1; base = 0; break; // 1.9.x  -> 16.1.x
                    case 10: y = 16; d = 2; base = 0; break; // 1.10.x -> 16.2.x
                    case 11: y = 16; d = 3; base = 0; break; // 1.11.x -> 16.3.x

                    case 12: y = 17; d = 1; base = 0; break; // 1.12.x -> 17.1.x
                    case 13: y = 18; d = 1; base = 0; break; // 1.13.x -> 18.1.x

                    case 14: y = 19; d = 1; base = 0; break; // 1.14.x -> 19.1.x
                    case 15: y = 19; d = 2; base = 0; break; // 1.15.x -> 19.2.x

                    case 16: y = 20; d = 1; base = 0; break; // 1.16.x -> 20.1.x

                    case 17: y = 21; d = 1; base = 0; break; // 1.17.x -> 21.1.x
                    case 18: y = 21; d = 2; base = 0; break; // 1.18.x -> 21.2.x

                    case 19: y = 22; d = 1; base = 0; break; // 1.19.x -> 22.1.x
                    case 20: y = 23; d = 1; base = 0; break; // 1.20.x -> 23.1.x

                    case 21:
                        switch (patch) {
                            case 0: case 1: case 2: case 3: case 4:
                                y = 24;
                                d = 1;
                                base = 0;
                                break;

                            case 5:
                                y = 25;
                                d = 1;
                                base = 5;
                                break;

                            case 6: case 7: case 8:
                                y = 25;
                                d = 2;
                                base = 6;
                                break;

                            case 9: case 10:
                                y = 25;
                                d = 3;
                                base = 9;
                                break;

                            case 11: default:
                                y = 25;
                                d = 4;
                                base = 11;
                                break;
                        }
                        break;

                    default:
                        throw new IllegalStateException("No year-drop mapping defined for 1." + minor + "." + patch);
                }
            } else {
                y = 26;
                d = minor - 21;
                base = 0;
            }

            int h = Math.max(patch - base, 0);
            return y + "." + d + (h == 0 ? "" : ("." + h));
        }

        private String dropToClassic(@NotNull MinecraftVersion v) {
            int year = v.getMajor(), drop = v.getMinor(), h = v.getPatch(), minor, patch;

            switch (year) {
                case 11:
                    if (drop != 1) return null;
                    minor = 0;
                    patch = h;
                    break;

                case 12:
                    switch (drop) {
                        case 1: minor = 1; break;
                        case 2: minor = 2; break;
                        case 3: minor = 3; break;
                        case 4: minor = 4; break;
                        default: return null;
                    }
                    patch = h;
                    break;

                case 13:
                    switch (drop) {
                        case 1: minor = 5; break;
                        case 2: minor = 6; break;
                        case 3: minor = 7; break;
                        default: return null;
                    }
                    patch = h;
                    break;

                case 14:
                    if (drop != 1) return null;
                    minor = 8;
                    patch = h;
                    break;

                case 16:
                    switch (drop) {
                        case 1: minor = 9;  break;
                        case 2: minor = 10; break;
                        case 3: minor = 11; break;
                        default: return null;
                    }
                    patch = h;
                    break;

                case 17:
                    if (drop != 1) return null;
                    minor = 12;
                    patch = h;
                    break;

                case 18:
                    if (drop != 1) return null;
                    minor = 13;
                    patch = h;
                    break;

                case 19:
                    switch (drop) {
                        case 1: minor = 14; break;
                        case 2: minor = 15; break;
                        default: return null;
                    }
                    patch = h;
                    break;

                case 20:
                    if (drop != 1) return null;
                    minor = 16;
                    patch = h;
                    break;

                case 21:
                    switch (drop) {
                        case 1: minor = 17; break;
                        case 2: minor = 18; break;
                        default: return null;
                    }
                    patch = h;
                    break;

                case 22:
                    if (drop != 1) return null;
                    minor = 19;
                    patch = h;
                    break;

                case 23:
                    if (drop != 1) return null;
                    minor = 20;
                    patch = h;
                    break;

                case 24:
                    if (drop != 1 || h > 4) return null;
                    minor = 21;
                    patch = h;
                    break;

                case 25:
                    minor = 21;
                    switch (drop) {
                        case 1:
                            if (h != 0) return null;
                            patch = 5 + h;
                            break;

                        case 2:
                            switch (h) {
                                case 0: patch = 6; break;
                                case 1: patch = 7; break;
                                case 2: patch = 8; break;
                                default: return null;
                            }
                            break;

                        case 3:
                            switch (h) {
                                case 0: patch = 9; break;
                                case 1: patch = 10; break;
                                default: return null;
                            }
                            break;

                        case 4:
                            patch = 11 + h;
                            break;

                        default: return null;
                    }
                    break;

                default: return null;
            }

            return 1 + "." + minor + (patch > 0 || minor == 0 ? ("." + patch) : "");
        }

        private String formatClassic(@NotNull MinecraftVersion v) {
            if (v.isClassic()) {
                int maj = v.getMajor(), min = v.getMinor(), pat = v.getPatch();
                return maj + "." + min + (pat > 0 || (maj == 1 && min == 0) ? ("." + pat) : "");
            }

            int year = v.getMajor();

            if (year >= 11 && year <= 25) {
                String classic = dropToClassic(v);
                if (classic != null) return classic;
            }

            if (year >= 26) {
                int drop = v.getMinor(), h = v.getPatch(), aliasMajor = 1, aliasMinor = 21 + drop;
                return aliasMajor + "." + aliasMinor + (h > 0 ? ("." + h) : "");
            }

            return formatDrop(v);
        }

        private String formatDrop(@NotNull MinecraftVersion v) {
            if (!v.isClassic()) {
                int y = v.getMajor(), d = v.getMinor(), h = v.getPatch();
                return y + "." + d + (h > 0 ? ("." + h) : "");
            }

            return classicToDrop(v);
        }

        @NotNull
        public String toClassic(@NotNull MinecraftVersion version) {
            return formatClassic(version);
        }

        @NotNull
        public String toDrop(@NotNull MinecraftVersion version) {
            return formatDrop(version);
        }
    };

    VersionScheme CROA_CUSTOM = new VersionScheme() {
        @NotNull
        public String toClassic(@NotNull MinecraftVersion v) {
            if (v.isClassic()) {
                int min = v.getMinor(), pat = v.getPatch();
                if (min == 21 && pat >= 11) {
                    int aliasPatch = pat - 11;
                    return "1.22" + (aliasPatch > 0 ? ("." + aliasPatch) : "");
                }

                return MOJANG.toClassic(v);
            }

            int year = v.getMajor(), drop = v.getMinor(), aliasPatch = v.getPatch();
            if (year == 25 && drop == 4)
                return "1.22" + (aliasPatch > 0 ? ("." + aliasPatch) : "");

            if (year >= 11 && year <= 25) return MOJANG.toClassic(v);
            if (year >= 26)
                return "1." + (22 + drop) + (aliasPatch > 0 ? ("." + aliasPatch) : "");

            return toDrop(v);
        }

        @NotNull
        public String toDrop(@NotNull MinecraftVersion version) {
            if (version.isClassic() && version.getMinor() == 22)
                version = new MinecraftVersion(true, 1, 21, 11 + version.getPatch());

            return MOJANG.toDrop(version);
        }
    };

    @NotNull
    String toClassic(@NotNull MinecraftVersion version);

    @NotNull
    default String toClassic(@NotNull String version) {
        return toClassic(MinecraftVersion.parse(version));
    }

    @NotNull
    String toDrop(@NotNull MinecraftVersion version);

    @NotNull
    default String toDrop(@NotNull String version) {
        return toDrop(MinecraftVersion.parse(version));
    }
}
