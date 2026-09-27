plugins {
    alias(libs.plugins.convention.android.library)
}

android {
    namespace = "io.github.oppsgo.theme.appcompat"
    defaultConfig {
        minSdk = 19
    }
}

dependencies {
    implementation(projects.core)
    // 旧版 Support Library；不传递、不锁版本。AppCompat / RV / ViewPager 为可选能力，只参与编译。
    compileOnly(libs.support.appcompat)
    compileOnly(libs.support.recyclerview)
    compileOnly(libs.support.v4)
}
