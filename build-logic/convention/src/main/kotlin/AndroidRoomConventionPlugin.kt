import com.android.build.api.dsl.LibraryExtension
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Room com esquema exportado: cada versão do banco vira um JSON em `schemas/`,
 * versionado no git. Os mesmos arquivos entram como assets dos testes
 * instrumentados, onde o MigrationTestHelper recria as versões antigas.
 */
class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")

            val schemaDir = layout.projectDirectory.dir("schemas").asFile

            extensions.configure<KspExtension> {
                arg("room.schemaLocation", schemaDir.path)
            }

            extensions.configure<LibraryExtension> {
                sourceSets.getByName("androidTest").assets.srcDir(schemaDir)
            }

            dependencies {
                add("implementation", libs.library("room-runtime"))
                add("implementation", libs.library("room-ktx"))
                add("ksp", libs.library("room-compiler"))
                add("androidTestImplementation", libs.library("room-testing"))
            }
        }
    }
}
