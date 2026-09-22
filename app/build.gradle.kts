plugins {
    alias(libs.plugins.convention.android.application)
}

android {
    namespace = "io.github.oppsgo.themeless"

    defaultConfig {
        applicationId = "io.github.oppsgo.themeless"
        versionCode = 1
        versionName = "0.1.0"
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
