import de.florianreuth.baseproject.integration.configureJarInJar
import de.florianreuth.baseproject.integration.fabricApiVersion
import de.florianreuth.baseproject.integration.setupFabric
import de.florianreuth.baseproject.setupProject
import de.florianreuth.baseproject.setupPublishing

plugins {
    id("net.fabricmc.fabric-loom")
    id("de.florianreuth.baseproject")
}

setupProject()
setupFabric()
setupPublishing()

repositories {
    maven("https://maven.terraformersmc.com/releases")
}

val shade = configureJarInJar()

dependencies {
    shade(fabricApi.module("fabric-api-base", fabricApiVersion))
    shade(fabricApi.module("fabric-resource-loader-v0", fabricApiVersion))
    shade(fabricApi.module("fabric-screen-api-v1", fabricApiVersion))
    shade(fabricApi.module("fabric-lifecycle-events-v1", fabricApiVersion))

    compileOnly("com.terraformersmc:modmenu:20.0.0-beta.2")
}
