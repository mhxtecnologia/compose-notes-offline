import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Módulo de feature: tela em Compose + ViewModel com Hilt. Depende só das
 * camadas de core, nunca de outra feature; quem liga as features é o :app.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("notes.android.library.compose")
            pluginManager.apply("notes.android.hilt")

            dependencies {
                add("implementation", project(":core:model"))
                add("implementation", project(":core:data"))
                add("implementation", project(":core:designsystem"))

                add("implementation", libs.library("androidx-lifecycle-runtime-compose"))
                add("implementation", libs.library("androidx-lifecycle-viewmodel-compose"))
                add("implementation", libs.library("androidx-navigation-compose"))
                add("implementation", libs.library("androidx-hilt-navigation-compose"))

                add("testImplementation", project(":core:testing"))

                add("androidTestImplementation", libs.library("androidx-compose-ui-test-junit4"))
                add("debugImplementation", libs.library("androidx-compose-ui-test-manifest"))
            }
        }
    }
}
