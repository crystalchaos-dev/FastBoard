import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.attributes.plugin.GradlePluginApiVersion
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.named

fun Configuration.compatibilityAttributes(objects: ObjectFactory) {
  attributes {
    attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 21)
    attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named("8.14"))
  }
}

fun Project.version() = project.version as String

fun Project.libs() = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class.java)

fun Project.ci() = System.getenv("CI")?.toBoolean() ?: false

fun String.toMinecraft() = "$this-R0.1-SNAPSHOT"

/**
 * Gets a system property with safe handling of missing properties and errors.
 *
 * @param key the system property key to look up
 * @return the property value or "Unknown" if not found or error occurs
 */
fun systemProperty(key: String): String = try {
  System.getProperty(key).takeUnless { it.isNullOrEmpty() } ?: "Unknown"
} catch (e: SecurityException) {
  "Unknown"
}

fun Project.gradleProperty(prop: String, orElse: String = "Unknown"): String {
  val property = this.providers.gradleProperty(prop)
  return if (property.isPresent) {
    property.get()
  } else {
    orElse
  }
}

/**
 * Generates a safe repository name from a URL
 */
private fun String.toRepositoryName(prefix: String): String {
  return (
    "$prefix-" + this
      .removePrefix("http://")
      .removePrefix("https://")
      .removeSuffix("/")
      .replace(Regex("[^a-zA-Z0-9-]"), "-")
      .lowercase()
    )
}

fun RepositoryHandler.snapshotRepo(url: String) = snapshotRepo(url) {}
fun RepositoryHandler.snapshotRepo(url: String, configure: MavenArtifactRepository.() -> Unit) {
  maven(url) {
    configureCommon(url.toRepositoryName("snapshots")) {
      mavenContent { snapshotsOnly() }
      configure()
    }
  }
}

fun RepositoryHandler.releasesRepo(url: String) = releasesRepo(url) {}
fun RepositoryHandler.releasesRepo(url: String, configure: MavenArtifactRepository.() -> Unit) {
  maven(url) {
    configureCommon(url.toRepositoryName("releases")) {
      mavenContent { releasesOnly() }
      configure()
    }
  }
}

private fun MavenArtifactRepository.configureCommon(
  name: String,
  configure: MavenArtifactRepository.() -> Unit = {}
) {
  this.name = name
  configure()
}

fun ModuleDependency.excludeCommon(): ModuleDependency {
  exclude(mapOf("group" to "net.kyori"))
  exclude(mapOf("group" to "org.ow2.asm"))
  exclude(mapOf("group" to "org.bukkit", "module" to "bukkit"))
  exclude(mapOf("group" to "org.spigotmc", "module" to "spigot-api"))
  exclude(mapOf("group" to "io.papermc.paper", "module" to "paper-api"))
  exclude(mapOf("group" to "com.destroystokyo.paper", "module" to "paper-api"))
  return this
}

fun Dependency?.excludeCommon(): Dependency? {
  if (this is ModuleDependency) {
    return this.excludeCommon()
  }
  return this
}

fun ModuleDependency.excludeGuice(): ModuleDependency {
  exclude(mapOf("group" to "com.google.inject"))
  return this
}

fun Dependency?.excludeGuice(): Dependency? {
  if (this is ModuleDependency) {
    return this.excludeGuice()
  }
  return this
}
