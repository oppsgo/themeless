plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.github.oppsgo.theme.core.ktx"
    defaultConfig {
        minSdk = 19
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
}

dependencies {
    implementation(projects.core)
}
