pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("base.fabric_settings")
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.terraformersmc.com/releases")
    }
}

rootProject.name = "secondchat"
