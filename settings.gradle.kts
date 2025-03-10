pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {

    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://android-sdk.is.com/")
        }
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://unity3ddist.jfrog.io/artifactory/unity-mediation-nvn-prod-local/")
        }
        maven { url = uri("https://maven.google.com/androidx/opengl/opengl/1.0.0/opengl-1.0.0.pom") }


    }
}

rootProject.name = "Vision"
include(":app")
 