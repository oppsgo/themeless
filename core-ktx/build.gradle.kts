plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.metalava)
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

metalava {
    filename.set("api/api.txt")
    javaSourceLevel.set(JavaVersion.VERSION_1_8)
    enforceCheck.set(true)
}

dependencies {
    implementation(projects.core)
}
