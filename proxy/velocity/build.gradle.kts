repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    api(project(":core"))
    compileOnly("com.velocitypowered:velocity-api:1.1.9") {
        isTransitive = false
    }
    compileOnly("net.kyori:adventure-api:4.8.0")
}
