import xyz.jpenilla.runpaper.task.RunServer

plugins {
  id("config-paper")
  id("io.papermc.paperweight.userdev")
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
paperweight.injectPaperRepository.set(true)

tasks {
  runDevBundleServer {
    configureRunServerTask()
  }
}

private fun RunServer.configureRunServerTask(port: Int = 25562) {
  jvmArguments.add("-Dcom.mojang.eula.agree=true")
  systemProperty("terminal.jline", false)
  systemProperty("terminal.ansi", true)
  systemProperty("file.encoding", "UTF-8")
  args("-p", port)
}
