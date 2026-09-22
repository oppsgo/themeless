import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")
            apply(plugin = "convention.android.lint")

            extensions.configure<LibraryExtension> {
                compileSdk = 36
                // Libraries publish minSdk only; targetSdk belongs on the consuming app.
                defaultConfig.minSdk = 21
                compileOptions {
                    // Publish Java 8 bytecode for broad consumer reach (OkHttp/Coil-style).
                    // Building this repo still requires JDK 17 because AGP 8 does.
                    sourceCompatibility = JavaVersion.VERSION_1_8
                    targetCompatibility = JavaVersion.VERSION_1_8
                }
                testOptions.animationsDisabled = true
            }

            // JDK 17+ javac warns that -source/-target 8 are obsolete; keep Java 8 bytecode.
            tasks.withType<JavaCompile>().configureEach {
                options.compilerArgs.add("-Xlint:-options")
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            dependencies {
                add("implementation",libs.findLibrary("androidx-annotation").get())
                "testImplementation"(platform(libs.findLibrary("junit-bom").get()))
                "testImplementation"(libs.findLibrary("junit-jupiter").get())
                "testRuntimeOnly"(libs.findLibrary("junit-platform-launcher").get())
            }

            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
            }
        }
    }
}
