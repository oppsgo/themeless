plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.metalava)
}

android {
    namespace = "io.github.oppsgo.android.theme.core"
    defaultConfig {
        minSdk = 16
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
    testImplementation(libs.robolectric)
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.vintage.engine)
}
