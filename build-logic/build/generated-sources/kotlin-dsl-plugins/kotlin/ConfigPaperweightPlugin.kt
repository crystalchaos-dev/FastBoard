/**
 * Precompiled [config-paperweight.gradle.kts][Config_paperweight_gradle] script plugin.
 *
 * @see Config_paperweight_gradle
 */
public
class ConfigPaperweightPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Config_paperweight_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
