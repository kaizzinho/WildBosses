import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.zip.ZipFile

plugins {
    id("java")
    id("dev.architectury.loom") version("1.13.469")
    id("architectury-plugin") version("3.4.164")
    kotlin("jvm") version("2.2.20")
}

group = "com.kaizzinho.wildbosses"
version = "1.0.0"

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    silentMojangMappingsLicense()
    splitEnvironmentSourceSets()

    mods {
        register("wildbosses") {
            sourceSet("main")
            sourceSet("client")
        }
    }

    runs {
        named("client") {
            client()
            configName = "WildBosses Client 1.8"
            ideConfigGenerated(true)
            runDir("run-wildbosses-1.8")
            vmArg("-Xmx4G")
        }
    }
}

repositories {
    mavenCentral()
    maven("https://artefacts.cobblemon.com/releases/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.isxander.dev/releases/")
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven") { name = "Modrinth" }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    minecraft("net.minecraft:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:0.17.2")
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:0.116.6+1.21.1")
    modImplementation(fabricApi.module("fabric-command-api-v2", "0.116.6+1.21.1"))
    modImplementation(fabricApi.module("fabric-lifecycle-events-v1", "0.116.6+1.21.1"))
    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.6+kotlin.2.2.20")

    modCompileOnly("com.terraformersmc:modmenu:11.0.3")
    modCompileOnly("dev.isxander:yet-another-config-lib:3.8.1+1.21.1-fabric")

    modImplementation("com.cobblemon:fabric:1.8.0+1.21.1")
}

sourceSets {
    main {
        kotlin.srcDirs("src/main/kotlin")
    }
    named("client") {
        kotlin.srcDirs("src/client/kotlin")
    }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(project.properties)
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    compileJava {
        options.release.set(21)
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}

val devJar = tasks.named<Jar>("jar") {
    archiveClassifier.set("dev")
    destinationDirectory.set(layout.buildDirectory.dir("devlibs"))
}

val productionJar = tasks.named<AbstractArchiveTask>("remapJar") {
    dependsOn(devJar)
    archiveClassifier.set("")
}

val verifyProductionJar = tasks.register("verifyProductionJar") {
    group = "verification"
    description = "Verifies that the installable WildBosses JAR was remapped for production."
    dependsOn(productionJar)

    doLast {
        val jarFile = productionJar.get().archiveFile.get().asFile
        val mixinPath = "com/kaizzinho/wildbosses/mixin/PokemonEntityMixin.class"

        ZipFile(jarFile).use { zip ->
            val entry = zip.getEntry(mixinPath)
                ?: error("Missing $mixinPath in ${jarFile.name}")

            val bytecode = zip.getInputStream(entry).readBytes().toString(Charsets.ISO_8859_1)

            check("defineSynchedData" !in bytecode) {
                "${jarFile.name} is not production-remapped: PokemonEntityMixin still targets defineSynchedData."
            }
            check("method_5693" in bytecode) {
                "${jarFile.name} does not contain the expected Minecraft 1.21.1 intermediary target method_5693."
            }
        }

        logger.lifecycle("Verified production-remapped JAR: ${jarFile.absolutePath}")
    }
}

val releaseJar = tasks.register<Sync>("releaseJar") {
    group = "build"
    description = "Places the verified installable JAR in build/release."
    dependsOn(verifyProductionJar)
    from(productionJar.flatMap { it.archiveFile })
    into(layout.buildDirectory.dir("release"))
}

tasks.named("build") {
    dependsOn(releaseJar)
}
