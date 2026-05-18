repositories {
    maven("https://maven.fabricmc.net/")
}

dependencies {
    api(project(":core"))
    compileOnly("net.fabricmc:fabric-loader:0.14.25")
}
