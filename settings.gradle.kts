@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenLocal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                // Support Library 传递依赖（:appcompat → appcompat-v7 → android.arch.lifecycle）
                includeGroupByRegex("android\\.arch.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // 仅作兜底：官方源没有或拉不到时再试腾讯云镜像。
        maven {
            name = "TencentMavenPublic"
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenLocal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                // Support Library 传递依赖（:appcompat → appcompat-v7 → android.arch.lifecycle）
                includeGroupByRegex("android\\.arch.*")
            }
        }
        mavenCentral()
        // 仅作兜底：官方源没有或拉不到时再试（:appcompat 的 support 包也走这里）。
        maven {
            name = "TencentMavenPublic"
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        }
    }
}

rootProject.name = "Themeless"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":core")
include(":core-ktx")
include(":androidx")
include(":androidx-ktx")
include(":appcompat")
include(":appcompat-ktx")

println(
    """
    |Gradle JVM:
    |  version = ${JavaVersion.current()} (${System.getProperty("java.version")})
    |  vendor  = ${System.getProperty("java.vendor")}
    |  home    = ${System.getProperty("java.home")}
    """.trimMargin()
)

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    Project requires JDK 17+ but it is currently using JDK ${JavaVersion.current()}.
    Java Home: [${System.getProperty("java.home")}]
    https://developer.android.com/build/jdks#jdk-config-in-studio
    """.trimIndent()
}
