plugins {
    kotlin("jvm") version "2.3.0-Beta1"
    id("java-library")
    id("io.freefair.lombok") version "8.10"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "me.croabeast"
version = "1.0"

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.2")
    annotationProcessor("org.jetbrains:annotations:26.0.2")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
}