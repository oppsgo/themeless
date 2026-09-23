plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
}

group = "com.github.oppsgo"
version = "0.1.2-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version
}
