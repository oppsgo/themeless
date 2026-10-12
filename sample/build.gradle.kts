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

// 皮肤包是独立 APK，不走依赖合并：构建时拷进 sample assets（*.apk 已被 gitignore），运行时复制到 filesDir 按路径加载。
val copyPurpleSkinApk = tasks.register<Copy>("copyPurpleSkinApk") {
    dependsOn(":skin:purple:assembleDebug")
    from("../skin/purple/build/outputs/apk/debug") {
        include("*.apk")
        rename { "themeless-skin-purple.apk" }
    }
    into("src/main/assets/skin")
}

tasks.named("preBuild") {
    dependsOn(copyPurpleSkinApk)
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
    implementation(libs.androidx.viewpager)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.material)
}
