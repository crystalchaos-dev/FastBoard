import org.gradle.accessors.dm.LibrariesForLibs
import xyz.jpenilla.runpaper.task.RunServer
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

plugins {
  id("xyz.jpenilla.run-paper")
}

val libs = project.the<LibrariesForLibs>()

// --- Helpers ---
fun isServiceUp(url: String): Boolean = try {
  val client = HttpClient.newHttpClient()
  val request = HttpRequest.newBuilder()
    .uri(URI.create(url))
    .header("User-Agent", "${rootProject.name} - ${project.version()} (Build Logic)")
    .GET()
    .build()
  val response = client.send(request, HttpResponse.BodyHandlers.discarding())
  response.statusCode() in 200..299
} catch (_: Exception) {
  false
}

// --- Plugins to fetch if service is up ---
val triniumPlugins = listOf(
  "https://trinium.mizule.dev/plugins/viaversion/download/viaversion",
  "https://trinium.mizule.dev/plugins/placeholderapi/download",
  "https://trinium.mizule.dev/plugins/luckperms/download/bukkit",
  "https://trinium.mizule.dev/plugins/miniplaceholders/download/paper",
  "https://trinium.mizule.dev/plugins/vaultunlocked/download",
  "https://trinium.mizule.dev/plugins/fastasyncworldedit/download",
)

val commonPlugins = runPaper.downloadPluginsSpec {
  if (isServiceUp("https://trinium.mizule.dev/health")) {
    triniumPlugins.forEach(::url)
  }
  // modrinth("ayRaM8J7", "2.15.1")
}

tasks {
  runServer {
    minecraftVersion(libs.versions.minecraft.get())
    configureRunServerTask()
  }
}

private fun RunServer.configureRunServerTask(port: Int = 25562) {
  jvmArguments.add("-Dcom.mojang.eula.agree=true")
  systemProperty("terminal.jline", false)
  systemProperty("terminal.ansi", true)
  systemProperty("file.encoding", "UTF-8")
  args("-p", port)
  downloadPlugins.from(commonPlugins)
}
