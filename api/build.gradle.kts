plugins {
    id("config-common")
    id("config-paper")
    id("config-paperweight")
}

dependencies {
    paperweight.paperDevBundle(libs.versions.minecraft.get() + "-R0.1-SNAPSHOT")
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.text.serializer.legacy)
}
