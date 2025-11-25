import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
//    `kotlin-dsl-precompiled-script-plugins`
}

dependencies {
//    implementation(libs.build.indra)
//    implementation(libs.build.indra.crossdoc)
    implementation(libs.build.indra.git)
    implementation(libs.build.indra.spotless)
    implementation(libs.build.blossom)
    implementation(libs.build.testLogger)
    implementation(libs.build.spotless)
    implementation(libs.build.shadow)
    implementation(libs.build.idea)
    implementation(libs.build.paperweight)
    implementation(libs.build.run.paper)
    implementation(libs.gradle.plugin.kotlin.withVersion(embeddedKotlinVersion))
}

dependencies {
    compileOnly(files(libs::class.java.protectionDomain.codeSource.location))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    target {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }
}

fun Provider<MinimalExternalModuleDependency>.withVersion(version: String): Provider<String> {
    return map { "${it.module.group}:${it.module.name}:$version" }
}
