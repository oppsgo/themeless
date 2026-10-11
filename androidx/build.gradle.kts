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
    // 不传递、不锁版本；AppCompat / RV / ViewPager2 / Material 为可选能力，只参与编译。
    compileOnly(libs.androidx.appcompat)
    compileOnly(libs.androidx.core.ktx)
    compileOnly(libs.androidx.recyclerview)
    compileOnly(libs.androidx.viewpager2)
    compileOnly(libs.androidx.material)

    testImplementation(libs.robolectric)
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.vintage.engine)
    testImplementation(libs.androidx.recyclerview)
    testImplementation(libs.androidx.viewpager2)
    testImplementation(libs.androidx.material)
}
