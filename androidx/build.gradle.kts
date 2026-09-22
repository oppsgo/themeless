plugins {
    alias(libs.plugins.convention.android.library)
}

android {
    namespace = "io.github.oppsgo.theme.androidx"
    defaultConfig {
        minSdk = 19
    }
}

dependencies {
    implementation(projects.core)
    // 不传递、不锁版本；RecyclerView 是可选能力。
    compileOnly(libs.androidx.appcompat)
    compileOnly(libs.androidx.recyclerview)
}
