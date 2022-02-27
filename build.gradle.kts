// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.android.tools.build.gradle)
        classpath(libs.android.gradlePlugin)
        classpath(libs.kotlin.gradlePlugin)
    }
}

plugins {
    //alias(libs.plugins.dokka)
    //alias(libs.plugins.ktlint)
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
