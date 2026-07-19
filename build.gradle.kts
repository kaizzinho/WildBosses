import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("java")
	id("dev.architectury.loom") version("1.11-SNAPSHOT")
	id("architectury-plugin") version("3.4-SNAPSHOT")
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
}
repositories {
	mavenCentral()
	maven("https://artefacts.cobblemon.com/releases/")
}

dependencies {
	minecraft("net.minecraft:minecraft:1.21.1")
	mappings(loom.officialMojangMappings())

	modImplementation("net.fabricmc:fabric-loader:0.17.2")
	modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:0.116.6+1.21.1")
	modImplementation(fabricApi.module("fabric-command-api-v2", "0.116.6+1.21.1"))
	modImplementation(fabricApi.module("fabric-lifecycle-events-v1", "0.116.6+1.21.1"))
	modImplementation("net.fabricmc:fabric-language-kotlin:1.13.6+kotlin.2.2.20")

	// Configuração oficial do Cobblemon via Architectury
	modCompileOnly("com.cobblemon:mod:1.7.3+1.21.1") { isTransitive = false }
	modImplementation("com.cobblemon:fabric:1.7.3+1.21.1")
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