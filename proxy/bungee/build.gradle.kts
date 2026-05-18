repositories {
    maven("https://repo.md-5.net/content/repositories/snapshots/")
}

dependencies {
    api(project(":core"))
    compileOnly("net.md-5:bungeecord-api:1.16-R0.4") {
        isTransitive = false
    }
}
