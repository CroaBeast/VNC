import org.gradle.api.attributes.java.TargetJvmVersion

val neoForgeVersion = "21.1.230"
val fmlLoaderVersion = "4.0.42"

repositories {
    maven("https://maven.neoforged.net/releases") {
        metadataSources {
            artifact()
        }
    }
}

dependencies {
    api(project(":core"))

    // Gradle resolution shim only; the actual NeoForge jars still require Java 21.
    components {
        withModule("net.neoforged:neoforge") {
            allVariants {
                attributes {
                    attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
                }
            }
        }

        withModule("net.neoforged.fancymodloader:loader") {
            allVariants {
                attributes {
                    attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
                }
            }
        }
    }

    compileOnly("net.neoforged:neoforge:$neoForgeVersion:universal@jar") {
        isTransitive = false
    }
    compileOnly("net.neoforged.fancymodloader:loader:$fmlLoaderVersion@jar") {
        isTransitive = false
    }
}
