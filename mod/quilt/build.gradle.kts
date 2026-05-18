repositories {
    maven("https://maven.quiltmc.org/repository/release/")
}

dependencies {
    api(project(":core"))
    compileOnly("org.quiltmc:quilt-loader:0.19.2")
}
