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
    // 旧版 Support Library；不传递、不锁版本。
    compileOnly(libs.support.appcompat)
    compileOnly(libs.support.recyclerview)
}
