import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar

val bundledProjects = listOf(
    project(":core"),
    project(":bukkit"),
    project(":mod:fabric"),
    project(":mod:quilt"),
    project(":mod:forge"),
    project(":mod:neoforge"),
    project(":mod:sponge"),
    project(":mod:liteloader"),
    project(":proxy:bungee"),
    project(":proxy:velocity")
)

dependencies {
    api(project(":core"))
    api(project(":bukkit"))
    api(project(":mod:fabric"))
    api(project(":mod:quilt"))
    api(project(":mod:forge"))
    api(project(":mod:neoforge"))
    api(project(":mod:sponge"))
    api(project(":mod:liteloader"))
    api(project(":proxy:bungee"))
    api(project(":proxy:velocity"))
}

tasks.withType<Jar>().configureEach {
    archiveBaseName.set("VNC")
}

tasks.named<Jar>("sourcesJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(bundledProjects.map {
        it.extensions.getByType<SourceSetContainer>().named("main").get().allSource
    })
}

tasks.named<Javadoc>("javadoc") {
    source(bundledProjects.map {
        it.extensions.getByType<SourceSetContainer>().named("main").get().allJava
    })

    classpath += files(bundledProjects.map {
        it.extensions.getByType<SourceSetContainer>().named("main").get().compileClasspath
    })
}

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(bundledProjects.map { it.tasks.named("jar") })

    from({
        configurations.getByName("runtimeClasspath")
            .filter { it.isFile && it.extension == "jar" }
            .map { zipTree(it) }
    })
}
