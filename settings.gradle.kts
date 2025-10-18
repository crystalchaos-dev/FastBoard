pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.mizule.dev/papermc")
        maven("https://repo.stellardrift.ca/repository/snapshots/")
        maven("https://repo.mizule.dev/releases")
        maven("https://repo.mizule.dev/snapshots")
        maven("https://repo.jpenilla.xyz/snapshots")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val name = "fastboard"

rootProject.name = "$name-parent"

sequenceOf(
  "api",
  "test-plugin"
).forEach {
  val projectName = "$name-${it.replace("/", "-")}"
  include(projectName)
  project(":$projectName").projectDir = file(it)
}
