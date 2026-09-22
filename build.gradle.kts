plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
}

// 格式：SemVer + 可选 Maven 预发布后缀（必须大写 SNAPSHOT）。
group = "io.github.oppsgo"
version = "0.1.1-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version
}
