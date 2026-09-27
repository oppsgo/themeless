plugins {
    alias(libs.plugins.convention.android.application)
}

android {
    namespace = "io.github.oppsgo.themeless"

    defaultConfig {
        applicationId = "io.github.oppsgo.themeless"
        // versionName 跟根工程 version；versionCode 仅 Demo 安装用，与 JitPack 无关。
        versionCode = 15
        versionName = rootProject.version.toString()
    }
}

dependencies {
    implementation(projects.core)
    implementation(projects.coreKtx)
    implementation(projects.androidx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager)
    implementation(libs.androidx.viewpager2)
}
