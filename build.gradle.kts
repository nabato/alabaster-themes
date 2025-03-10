import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.intellij.platform.gradle.TestFrameworkType


plugins {
    id("java")

    kotlin("jvm") version "2.0.0-RC3"

//    id("dev.clojurephant.clojure") version "0.8.0-beta.7"

    id("org.jetbrains.intellij.platform") version "2.3.0"

    id("org.jetbrains.changelog") version "2.2.1"

    id("org.jetbrains.qodana") version "2024.3.4"

    id("org.jetbrains.kotlinx.kover") version "0.9.1"
}

kotlin {
    jvmToolchain(21)
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

// Configure project's dependencies
repositories {
    mavenCentral()
    maven {
        name = "Clojars"
        url = uri("https://repo.clojars.org")
    }
    intellijPlatform {
        defaultRepositories()
    }
}


dependencies {
    implementation(kotlin("stdlib-jdk8"))
//    implementation("org.clojure:clojure:1.12.0-alpha11")
//    implementation("http-kit:http-kit:2.5.3")
    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))

        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        pluginVerifier()
        zipSigner()
    }
}

intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = providers.gradleProperty("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}

//kover {
//    reports {
//        total {
//            xml {
//                onCheck = true
//            }
//        }
//    }
//}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
//qodana {
//    cachePath.set(file(".qodana").canonicalPath)
//    reportPath.set(file("build/reports/inspections").canonicalPath)
//    saveReport.set(true)
//    showReport.set(System.getenv("QODANA_SHOW_REPORT")?.toBoolean() ?: false)
//}

tasks.withType(KotlinCompile::class).all {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        // For creation of default methods in interfaces
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

tasks.register("saveClassPathToFile") {
    doFirst {
        File("classpath.txt").writeText(sourceSets["main"].runtimeClasspath.asPath)
    }
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }

//    checkClojure {
//        isEnabled = false
//    }

//    compileClojure {
//        dependsOn(compileKotlin)
////        to get all output paths use: sourceSets.main.get().output.asPath
//        classpath.from(sourceSets.main.get().kotlin.destinationDirectory.get())
//    }

    publishPlugin {
        dependsOn(patchChangelog)
    }

    buildSearchableOptions {
        enabled = false
    }
}

//clojure.builds.named("main") {
////    aotAll()
////    checkAll()
////    reflection.set("fail")
////}

intellijPlatformTesting {
    runIde {
        register("runIdeForUiTests") {
            task {
                jvmArgumentProviders += CommandLineArgumentProvider {
                    listOf(
                        "-Drobot-server.port=8082",
                        "-Dide.mac.message.dialogs.as.sheets=false",
                        "-Djb.privacy.policy.text=<!--999.999-->",
                        "-Djb.consents.confirmation.enabled=false",
                    )
                }
            }

            plugins {
                robotServerPlugin()
            }
        }
    }
}