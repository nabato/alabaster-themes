import com.jetbrains.plugin.structure.base.utils.contentBuilder.buildDirectory
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.d8.D8RootPlugin.Companion.kotlinD8Extension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


fun properties(key: String) = project.findProperty(key).toString()
fun environment(key: String) = providers.environmentVariable(key).toString()

plugins {
    id("java")

    kotlin("jvm") version "2.0.0-RC3"

    id("dev.clojurephant.clojure") version "0.8.0-beta.7"

    id("org.jetbrains.intellij") version "1.17.3"

    id("org.jetbrains.changelog") version "2.0.0"

    id("org.jetbrains.qodana") version "0.1.13"

    id("org.jetbrains.kotlinx.kover") version "0.6.1"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()
    maven {
        name = "Clojars"
        url = uri("https://repo.clojars.org")
    }
}


dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.clojure:clojure:1.12.0-alpha11")
    implementation("http-kit:http-kit:2.5.3")
}


// Useful to override another IC platforms from env
val platformVersion = System.getenv("PLATFORM_VERSION") ?: properties("platformVersion")
val platformPlugins = System.getenv("PLATFORM_PLUGINS") ?: properties("platformPlugins")


// Set the JVM language level used to build a project. Use Java 11 for 2020.3+, and Java 17 for 2022.2+.
kotlin {
    jvmToolchain(17)
}

// Configure Gradle IntelliJ Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName.set(properties("pluginName"))
    version.set(platformVersion)
    type.set(properties("platformType"))

    // Plugin Dependencies. Use `platformPlugins` property from the gradle.properties file.
    plugins.set(platformPlugins.split(',').map(String::trim).filter(String::isNotEmpty))
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
qodana {
    cachePath.set(file(".qodana").canonicalPath)
    reportPath.set(file("build/reports/inspections").canonicalPath)
    saveReport.set(true)
    showReport.set(System.getenv("QODANA_SHOW_REPORT")?.toBoolean() ?: false)
}

// Configure Gradle Kover Plugin - read more: https://github.com/Kotlin/kotlinx-kover#configuration
kover.xmlReport {
    onCheck.set(true)
}

tasks.withType(KotlinCompile::class).all {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
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
        gradleVersion = properties("gradleVersion")
    }

    checkClojure {
        isEnabled = false
    }

    compileClojure {
        dependsOn(compileKotlin)
//        to get all output paths use: sourceSets.main.get().output.asPath
        classpath.from(sourceSets.main.get().kotlin.destinationDirectory.get())
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            file("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").let { markdownToHTML(it) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider {
            with(changelog) {
                renderItem(
                    getOrNull(properties("pluginVersion")) ?: getLatest(),
                    Changelog.OutputType.HTML,
                )
            }
        })
    }

    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    signPlugin {
        certificateChain = environment("CERTIFICATE_CHAIN")
        privateKey = environment("PRIVATE_KEY")
        password = environment("PRIVATE_KEY_PASSWORD")
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token = environment("PUBLISH_TOKEN")
    }

    buildSearchableOptions {
        enabled = false
    }
}

clojure.builds.named("main") {
    aotAll()
    checkAll()
    reflection.set("fail")
}