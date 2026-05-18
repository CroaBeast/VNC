repositories {
    maven("https://maven.minecraftforge.net") {
        metadataSources {
            mavenPom()
            artifact()
        }
    }
}

dependencies {
    api(project(":core"))
    compileOnly("net.minecraftforge:forge:1.16.5-36.2.42:universal@jar") {
        isTransitive = false
    }
}
