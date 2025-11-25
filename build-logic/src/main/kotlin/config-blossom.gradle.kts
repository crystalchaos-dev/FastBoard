import net.kyori.blossom.BlossomExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.time.Instant

plugins {
  id("net.kyori.blossom")
}

val libs = project.the<LibrariesForLibs>()

extensions.configure(SourceSetContainer::class) {
  this.named("main") {
    extensions.configure(BlossomExtension::class) {
      javaSources {
        this.property("version", project.version())
        this.property(
          "projectVersion",
          project.extraProperties["projectVersion"]?.toString() ?: project.version()
        )
        this.property("git_branch", "project.currentBranch()")
        this.property("git_commit", "project.lastCommitHash()")
        this.property("git_commit_long", "project.lastCommitHash(length = -1)")
        this.property("git_tag", "project.latestTag()")
        this.property(
          "git_url",
          "https://github.com/${project.gradleProperty("githubOrg")}/${project.gradleProperty("githubRepo")}"
        )
        this.property("git_user", project.gradleProperty("githubOrg"))
        this.property("git_repo", project.gradleProperty("githubRepo"))
        this.property("author", project.gradleProperty("projectAuthor", ""))
        this.property("description", project.description ?: "No description provided")
        this.property("name", project.gradleProperty("projectName", ""))
        this.property("url", project.gradleProperty("projectUrl", ""))
        this.property("build_time", Instant.now().toString()) // good enough
      }
    }
  }
}

if (plugins.hasPlugin("com.diffplug.spotless")) {
//  if (tasks.named("generateTemplates").configure {
//    dependsOn(tasks.named("spotlessApply"))
//  }
}
