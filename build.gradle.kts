plugins {
    id("configuration.jar_in_jar")
    id("publishing.florianreuth")
}

dependencies {
    jarInJar(platform(libs.fabric.api.bom))
    jarInJar(libs.fabric.api.base)
    jarInJar(libs.fabric.resource.loader.v0)

    compileOnly(libs.modmenu)
}
