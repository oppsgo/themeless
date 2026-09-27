plugins {
    alias(libs.plugins.convention.android.library)
}

android {
    namespace = "io.github.oppsgo.theme.androidx"
    defaultConfig {
        minSdk = 19
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(projects.core)
    // 不传递、不锁版本；AppCompat / RV / ViewPager 为可选能力，只参与编译。
    compileOnly(libs.androidx.appcompat)
    compileOnly(libs.androidx.recyclerview)
    compileOnly(libs.androidx.viewpager)
    compileOnly(libs.androidx.viewpager2)

    testImplementation(libs.robolectric)
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.vintage.engine)
    testImplementation(libs.androidx.viewpager)
    testImplementation(libs.androidx.viewpager2)
}
