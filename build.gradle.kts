plugins {
    id("base.java")
    id("base.fabric")
    id("configuration.jar_in_jar")
    id("base.maven_publish")
    id("publishing.reposilite")
    id("publishing.maven_central")
}

dependencies {
    jarInJar(platform(libs.fabric.api.bom))
    jarInJar(libs.fabric.api.base)
    jarInJar(libs.fabric.resource.loader.v0)
    jarInJar(libs.fabric.screen.api.v1)
    jarInJar(libs.fabric.lifecycle.events.v1)

    compileOnly(libs.modmenu)
}
