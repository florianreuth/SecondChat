import de.florianreuth.baseproject.*

plugins {
    id("net.fabricmc.fabric-loom")
    id("de.florianreuth.baseproject")
}

setupProject()
setupFabric()
setupPublishing()
includeFabricApiModules("fabric-api-base", "fabric-resource-loader-v0", "fabric-screen-api-v1", "fabric-lifecycle-events-v1")

repositories {
    maven("https://maven.terraformersmc.com/releases")
}

dependencies {
    compileOnly("com.terraformersmc:modmenu:18.0.0-alpha.8")
}
