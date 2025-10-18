import com.diffplug.gradle.spotless.FormatExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.kyori.indra.IndraExtension
import net.kyori.indra.licenser.spotless.HeaderFormat
import net.kyori.indra.licenser.spotless.IndraSpotlessLicenserExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.plugins.ide.idea.model.IdeaModel
import java.util.Calendar
import java.util.Date

plugins {
  id("java-library")
  id("com.gradleup.shadow")
  id("net.kyori.indra")
  id("net.kyori.indra.git")
  id("net.kyori.indra.publishing")
  id("org.jetbrains.gradle.plugin.idea-ext")
}

val libs = project.the<LibrariesForLibs>()
val noRelocate = providers.gradleProperty("disable-relocation")
  .map { it.toBoolean() }
  .getOrElse(false)
val disableSpotless: Provider<Boolean> = providers.gradleProperty("disableSpotless")
  .map { it.toBoolean() }
  .orElse(false)

if (noRelocate) {
  project.version = if (version().contains("-SNAPSHOT")) {
    version().substringBefore("-SNAPSHOT") + "-NO-RELOCATE-SNAPSHOT"
  } else {
    version() + "-NO-RELOCATE"
  }
}

dependencies {
  compileOnly(libs.jspecify)
  compileOnly(libs.jetbrainsAnnotations)
}

repositories {
  mavenCentral()
  maven("https://repo.mizule.dev/papermc")
  releasesRepo("https://repo.mizule.dev/releases")
  snapshotRepo("https://repo.mizule.dev/snapshots")
  maven("https://repo.mizule.dev/testing")
  snapshotRepo("https://repo.jpenilla.xyz/snapshots")
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://repo.lucko.me/")
}

extensions.configure<IndraExtension> {
  github("MineLimit", "FastBoard")
  mitLicense()

  javaVersions {
    target(21)
    minimumToolchain(21)
  }

  configurePublications {
    pom {
      developers {
        developer {
          name.set("MrMicky")
          email.set("git[at]mrmicky.fr")
          url.set("https://mrmicky.fr")
        }
      }
      contributors {
        contributor {
          this.
          name.set("powercas_gamer")
          email.set("cas@mizule.dev")
          url.set("https://github.com/powercasgamer")
        }
      }
    }
  }

  if (project.ci()) {
    publishReleasesTo("maven", "https://repo.mizule.dev/releases")
    publishSnapshotsTo("maven", "https://repo.mizule.dev/snapshots")
  } else {
    publishReleasesTo("mizule", "https://repo.mizule.dev/releases")
    publishSnapshotsTo("mizule", "https://repo.mizule.dev/snapshots")
  }
}

// Override Indra’s default archive name
extensions.getByType<BasePluginExtension>().archivesName.set(project.name)

fun ShadowJar.configureStandard() {
  dependencies {
//    exclude(dependency("org.jetbrains.kotlin:.*:.*"))
    exclude(dependency("org.slf4j:.*:.*"))
  }

  exclude(
    "META-INF/*.SF",
    "META-INF/*.DSA",
    "META-INF/*.RSA",
    "OSGI-INF/**",
    "*.profile",
    "module-info.class",
    "ant_tasks/**",
    "OSGI-OPT/**",
    "META-INF/*.pro"
  )

  mergeServiceFiles()
}

tasks.withType<ShadowJar>().configureEach {
  archiveClassifier.set(null as String?)
  configureStandard()

  inputs.property("noRelocate", noRelocate)
  if (!noRelocate) {
    val prefix = "${project.group}.lib"
    listOf(
      "org.intellij.lang",
      "org.jetbrains.annotations",
    ).forEach { pack ->
      relocate(pack, "$prefix.$pack")
    }
  }
}

tasks.named("assemble") {
  dependsOn(tasks.named<ShadowJar>("shadowJar"))
}

tasks.named<Jar>("jar") {
  archiveClassifier.set("slim")
  manifest {
    attributes("Implementation-Version" to project.version)
  }
}

if (!disableSpotless.get()) {
  plugins.apply("com.diffplug.spotless")
  plugins.apply("net.kyori.indra.licenser.spotless")

  extensions.configure<SpotlessExtension> {
    val ktlintVer = "0.50.0"
    val overrides = mapOf(
      "ktlint_standard_no-wildcard-imports" to "disabled",
      "ktlint_standard_filename" to "disabled",
      "ktlint_standard_trailing-comma-on-call-site" to "disabled",
      "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
    )

    fun FormatExtension.applyCommon(spaces: Int = 2) {
      trimTrailingWhitespace()
      leadingTabsToSpaces(spaces)
      endWithNewline()
      encoding("UTF-8")
      toggleOffOn()
      targetExclude(
        "generated/**",
        "run/**",
        "build/generated-sources/**",
        "build/generated/**",
        "build/generated/sources/",
        "build/generated/sources/**",
        "build/generated/sources/blossom/**",
        "build/**",
        "src/*/java/generated/**",
        "src/*/kotlin/generated/**",
      )
    }

    java {
      importOrderFile(rootProject.file("gradle/spotless/mizule.importorder"))
      removeUnusedImports()
      formatAnnotations()
      applyCommon()
    }
    kotlinGradle {
      ktlint(ktlintVer).editorConfigOverride(overrides)
      applyCommon()
    }
    plugins.withId("org.jetbrains.kotlin.jvm") {
      kotlin {
        ktlint(ktlintVer).editorConfigOverride(overrides)
        applyCommon()
      }
    }
    format("configs") {
      target("**/*.yml", "**/*.yaml", "**/*.json", "**/*.json5", "**/*.conf")
      targetExclude("run/**")
      applyCommon(2)
    }
  }

  extensions.configure<IndraSpotlessLicenserExtension> {
    headerFormat(HeaderFormat.starSlash())
    licenseHeaderFile(rootProject.file("HEADER"))

    val currentYear = Calendar.getInstance().apply { time = Date() }.get(Calendar.YEAR)
    val createdYear = providers.gradleProperty("createdYear").map { it.toInt() }.getOrElse(currentYear)
    val year = if (createdYear == currentYear) createdYear.toString() else "$createdYear-$currentYear"

    property("name", providers.gradleProperty("projectName").getOrElse("template"))
    property("year", year)
    property("description", project.description ?: "A template project")
    property("author", providers.gradleProperty("projectAuthor").getOrElse("template"))
  }

  tasks.register("format") {
    group = "formatting"
    description = "Formats source code according to project style"
    dependsOn("spotlessApply")
  }

  tasks.named("spotlessCheck") {
    enabled = false
  }
  tasks.named("spotlessApply") {
    enabled = false
  }
}

extensions.configure(IdeaModel::class) {
  module {
    isDownloadSources = true
    isDownloadJavadoc = true
  }
}

tasks.withType<JavaCompile>().configureEach {
  // -processing: ignore unclaimed annotations
  // -classfile: ignore annotations/annotation methods missing in dependencies
  // -serial: we don't support java serialization
  // -options: ignore java 8 deprecation with jdk 21
  options.let {
    it.compilerArgs.addAll(listOf("-Xlint:-processing,-classfile,-serial,-options", "-parameters"))
    it.isFork = true
    it.isIncremental = true
    it.encoding = "UTF-8"
  }
}

tasks.withType<ProcessResources>().configureEach {
  filteringCharset = "UTF-8"
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
