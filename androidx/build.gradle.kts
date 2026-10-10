plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.metalava)
}

android {
    namespace = "io.github.oppsgo.android.theme.androidx"
    defaultConfig {
        // appcompat-resources 1.7.0 要求 minSdk 21；core 仍保持 19。
        minSdk = 21
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

metalava {
    filename.set("api/api.txt")
    javaSourceLevel.set(JavaVersion.VERSION_1_8)
    enforceCheck.set(true)
}

dependencies {
    api(projects.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.material)

    testImplementation(libs.robolectric)
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.vintage.engine)
}
