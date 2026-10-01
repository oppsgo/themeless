plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.metalava)
}

android {
    namespace = "io.github.oppsgo.android.theme.androidx"
    defaultConfig {
        minSdk = 19
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
    implementation(projects.core)
    // 不传递、不锁版本；AppCompat / RV / ViewPager 为可选能力，只参与编译。
    compileOnly(libs.androidx.appcompat)
    compileOnly(libs.androidx.recyclerview)
    compileOnly(libs.androidx.viewpager)
    compileOnly(libs.androidx.viewpager2)
    compileOnly(libs.androidx.material)

    testImplementation(libs.robolectric)
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.vintage.engine)
    testImplementation(libs.androidx.viewpager)
    testImplementation(libs.androidx.viewpager2)
    testImplementation(libs.androidx.material)
}
