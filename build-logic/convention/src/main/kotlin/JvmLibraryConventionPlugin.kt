import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** Módulo Kotlin puro: o modelo de domínio não conhece o Android. */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            configureKotlinJvm()

            dependencies {
                add("testImplementation", libs.library("junit4"))
            }
        }
    }
}
