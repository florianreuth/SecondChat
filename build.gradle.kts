import de.florianreuth.baseproject.*

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

val jij = configureJij()

dependencies {
    jij(fabricApi.module("fabric-api-base", fabricApiVersion))
    jij(fabricApi.module("fabric-resource-loader-v0", fabricApiVersion))
    jij(fabricApi.module("fabric-screen-api-v1", fabricApiVersion))
    jij(fabricApi.module("fabric-lifecycle-events-v1", fabricApiVersion))

    compileOnly("com.terraformersmc:modmenu:18.0.0-alpha.8")
}
