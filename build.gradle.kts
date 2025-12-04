plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1" // Replaces maven-shade-plugin
}

group = "io.github.blackeden-studios"
version = "0.1-BETA"

repositories {
    mavenCentral() // Required for Fulcrum API
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io") // Required for ItemsAdder & LandsAPI
}

dependencies {
    compileOnly("io.github.blackeden-studios:fulcrum-api:0.1.3-BETA")

    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.16.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.processResources {
    // Replaces Maven resource filtering
    filesMatching("plugin.yml") {
        expand(project.properties)
    }
}

tasks.shadowJar {
    // Configures the output jar name format: vclasses-0.1-BETA.jar
    archiveBaseName.set("vclasses")
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())
}

tasks.build {
    dependsOn(tasks.shadowJar)
}