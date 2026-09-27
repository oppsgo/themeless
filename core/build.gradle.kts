plugins {
    alias(libs.plugins.convention.android.library)
}

android {
    namespace = "io.github.oppsgo.theme.core"
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
    testImplementation(libs.robolectric)
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.vintage.engine)
}
