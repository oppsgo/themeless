plugins {
    alias(libs.plugins.convention.android.application)
}

android {
    namespace = "io.github.oppsgo.themeless"

    defaultConfig {
        applicationId = "io.github.oppsgo.themeless"
        // versionName 跟库版本；versionCode 仅给 Demo APK，与 Maven 坐标无关。
        versionCode = 101
        versionName = rootProject.version.toString()
    }
}

dependencies {
    implementation(projects.core)
    implementation(projects.androidx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.recyclerview)
}
