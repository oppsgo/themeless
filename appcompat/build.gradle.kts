plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.metalava)
}

android {
    namespace = "io.github.oppsgo.android.theme.appcompat"
    defaultConfig {
        minSdk = 19
    }
}

metalava {
    filename.set("api/api.txt")
    javaSourceLevel.set(JavaVersion.VERSION_1_8)
    enforceCheck.set(true)
}

dependencies {
    implementation(projects.core)
    // 旧版 Support Library；不传递、不锁版本。AppCompat / RV / ViewPager 为可选能力，只参与编译。
    compileOnly(libs.support.appcompat)
    compileOnly(libs.support.recyclerview)
    compileOnly(libs.support.v4)
}
