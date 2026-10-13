plugins {
    id("configuration.jar_in_jar")
    id("publishing.florianreuth")
}

dependencies {
    jarInJar(platform(libs.fabric.api.bom))
    jarInJar(libs.fabric.api.base)
    jarInJar(libs.fabric.resource.loader.v0)
    jarInJar(libs.fabric.screen.api.v1)
    jarInJar(libs.fabric.lifecycle.events.v1)

    compileOnly(libs.modmenu)
}
