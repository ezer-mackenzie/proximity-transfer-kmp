import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    `maven-publish`
}

group = providers.gradleProperty("project.group").get()
version = providers.gradleProperty("project.version").get()

kotlin {
    jvm()
    androidLibrary {
        namespace = "io.github.ezer_mackenzie.proximitytransfer"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava()
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.coroutines.core)
            implementation(libs.crypto.sha2)
        }

        commonTest.dependencies {
            implementation(libs.coroutines.test)
            implementation(libs.kotlin.test)
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        artifactId = artifactId.replace("library", "proximity-transfer")
        pom {
            name.set("Proximity Transfer")
            description.set("Kotlin Multiplatform library for offline, proximity-based peer-to-peer data transfer.")
            url.set("https://github.com/ezer-mackenzie/proximity-transfer-kmp")

            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("ezer-mackenzie")
                    name.set("Eli-ezer Reuven Ramirez Ruiz")
                    email.set("ramirez.ruiz.eliezer.reuven@gmail.com")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/ezer-mackenzie/proximity-transfer-kmp.git")
                developerConnection.set("scm:git:ssh://github.com/ezer-mackenzie/proximity-transfer-kmp.git")
                url.set("https://github.com/ezer-mackenzie/proximity-transfer-kmp")
            }
        }
    }
}

