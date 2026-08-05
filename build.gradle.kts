import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.external.javadoc.StandardJavadocDocletOptions

plugins {
    id("io.freefair.lombok") version "9.4.0" apply false
}

allprojects {
    group = "me.croabeast.vnc"
    version = "1.3.0"

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

val javaProjects = subprojects.filter { it.path != ":mod" && it.path != ":proxy" }

configure(javaProjects) {
    apply(plugin = "java-library")
    apply(plugin = "io.freefair.lombok")

    extensions.configure<JavaPluginExtension>("java") {
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<Javadoc>().configureEach {
        isFailOnError = false

        (options as StandardJavadocDocletOptions).apply {
            addStringOption("Xdoclint:none", "-quiet")
            encoding = "UTF-8"
            charSet = "UTF-8"
            docEncoding = "UTF-8"
            memberLevel = JavadocMemberLevel.PACKAGE

            if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_1_9))
                addBooleanOption("html5", true)
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        options.compilerArgs.add("-Xlint:-options")

        if (project.name != "neoforge" && JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_1_9))
            options.release.set(8)
    }

    dependencies {
        "compileOnly"("org.jetbrains:annotations:26.0.2")
        "annotationProcessor"("org.jetbrains:annotations:26.0.2")

        "compileOnly"("org.projectlombok:lombok:1.18.44")
        "annotationProcessor"("org.projectlombok:lombok:1.18.44")
    }

}
