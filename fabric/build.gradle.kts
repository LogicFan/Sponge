plugins {
	id("fabric-loom")
	id("com.github.johnrengelman.shadow")
	id("implementation-structure")
	eclipse
}

val commonProject = parent!!

val apiVersion: String by project
val minecraftVersion: String by project
val recommendedVersion: String by project
val organization: String by project
val projectUrl: String by project

val name : String by project
val loaderVersion : String by project
val modVersion : String by project

// Fabric extra configurations
val fabricLibrariesConfig: NamedDomainObjectProvider<Configuration> = configurations.register("libraries")
val fabricAppLaunchConfig: NamedDomainObjectProvider<Configuration> = configurations.register("applaunch") {
	extendsFrom(fabricLibrariesConfig.get())
}
val fabricInstallerConfig: Provider<Configuration> = configurations.register("installer")

// Fabric source sets
val fabricInstaller by sourceSets.register("installer")
val fabricInstallerJava9 by sourceSets.register("installerJava9") {
	this.java.setSrcDirs(setOf("src/installer/java9"))
	compileClasspath += fabricInstaller.compileClasspath
	compileClasspath += fabricInstaller.runtimeClasspath

	tasks.named(compileJavaTaskName, JavaCompile::class) {
		options.release.set(9)
		if (JavaVersion.current() < JavaVersion.VERSION_11) {
			javaCompiler.set(javaToolchains.compilerFor { languageVersion.set(JavaLanguageVersion.of(11)) })
		}
	}

	dependencies.add(implementationConfigurationName, objects.fileCollection().from(fabricInstaller.output.classesDirs))
}

// Configuration from VanillaGradle
// val spongeMinecraftConfig: Configuration? = commonProject.configurations["minecraft"];

configurations.named(fabricInstaller.implementationConfigurationName) {
	extendsFrom(fabricInstallerConfig.get())
}

dependencies {
	// fabric dependencies
	minecraft("com.mojang:minecraft:$minecraftVersion")
	mappings(minecraft.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

	// sponge dependencies
	implementation(project(commonProject.path))

	val apiAdventureVersion: String by project
	val apiConfigurateVersion: String by project
	val asmVersion: String by project
	val guavaVersion: String by project
	val jlineVersion: String by project
	val log4jVersion: String by project
	val mixinVersion: String by project
	val modlauncherVersion: String by project
	val pluginSpiVersion: String by project
	val timingsVersion: String by project

	val installer = fabricInstallerConfig.get().name
	installer("net.fabricmc:fabric-loader:$loaderVersion")
	installer("com.google.code.gson:gson:2.8.0")
	installer("org.spongepowered:configurate-hocon:4.1.1")
	installer("org.spongepowered:configurate-core:4.1.1")
	installer("net.sf.jopt-simple:jopt-simple:5.0.3")
	installer("org.tinylog:tinylog-api:2.2.1")
	installer("org.tinylog:tinylog-impl:2.2.1")
	// Override ASM versions, and explicitly declare dependencies so ASM is excluded from the manifest.
	val asmExclusions = sequenceOf("-commons", "-tree", "-analysis", "")
			.map { "asm$it" }
			.onEach {
				installer("org.ow2.asm:$it:$asmVersion")
			}.toSet()
	installer("org.cadixdev:atlas:0.2.1") {
		asmExclusions.forEach { exclude(group = "org.ow2.asm", module = it) } // Use our own ASM version
	}
	installer("org.cadixdev:lorenz-asm:0.5.6") {
		asmExclusions.forEach { exclude(group = "org.ow2.asm", module = it) } // Use our own ASM version
	}
	installer("org.cadixdev:lorenz-io-proguard:0.5.6")

	// Add the API as a runtime dependency, just so it gets shaded into the jar
	add(fabricInstaller.runtimeOnlyConfigurationName, "org.spongepowered:spongeapi:$apiVersion") {
		isTransitive = false
	}

	val appLaunch = fabricAppLaunchConfig.name
	appLaunch("org.spongepowered:spongeapi:$apiVersion")
	appLaunch(platform("net.kyori:adventure-bom:$apiAdventureVersion"))
	appLaunch("net.kyori:adventure-serializer-configurate4")
	appLaunch("org.spongepowered:mixin:$mixinVersion")
	appLaunch("org.ow2.asm:asm-util:$asmVersion")
	appLaunch("org.ow2.asm:asm-tree:$asmVersion")
	appLaunch("com.google.guava:guava:$guavaVersion")
	appLaunch("org.spongepowered:plugin-spi:$pluginSpiVersion")
	appLaunch("javax.inject:javax.inject:1")
	appLaunch("org.apache.logging.log4j:log4j-api:$log4jVersion")
	appLaunch("org.apache.logging.log4j:log4j-core:$log4jVersion")
	appLaunch("com.lmax:disruptor:3.4.2")
	appLaunch("com.zaxxer:HikariCP:2.6.3")
	appLaunch("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
	appLaunch(platform("org.spongepowered:configurate-bom:$apiConfigurateVersion"))
	appLaunch("org.spongepowered:configurate-core") {
		exclude(group = "org.checkerframework", module = "checker-qual")
	}
	appLaunch("org.spongepowered:configurate-hocon") {
		exclude(group = "org.spongepowered", module = "configurate-core")
		exclude(group = "org.checkerframework", module = "checker-qual")
	}
	appLaunch("org.spongepowered:configurate-jackson") {
		exclude(group = "org.spongepowered", module = "configurate-core")
		exclude(group = "org.checkerframework", module = "checker-qual")
	}

	val libraries = fabricLibrariesConfig.name
	libraries("net.minecrell:terminalconsoleappender:1.3.0-SNAPSHOT")
	libraries("org.jline:jline-terminal:$jlineVersion")
	libraries("org.jline:jline-reader:$jlineVersion")
	libraries("org.jline:jline-terminal-jansi:$jlineVersion")
	libraries("org.spongepowered:timings:$timingsVersion")
}

val fabricManifest = the<JavaPluginConvention>().manifest {
	attributes(
			"Specification-Title" to "SpongeFabric",
			"Specification-Vendor" to "SpongePowered",
			"Specification-Version" to apiVersion,
			"Implementation-Title" to project.name,
			"Implementation-Version" to spongeImpl.generatePlatformBuildVersionString(apiVersion, minecraftVersion, recommendedVersion),
			"Implementation-Vendor" to "SpongePowered"
	)
}

tasks {
	withType(ProcessResources::class) {
		inputs.properties(Pair("version", project.version))
		filesMatching("fabric.mod.json") {
			expand(Pair("version", project.version))
		}
	}
	withType(JavaCompile::class).configureEach {
		this.options.encoding = "UTF-8"
	}

	jar {
		manifest.from(fabricManifest)
	}
	val fabricInstallerJar by registering(Jar::class) {
		archiveClassifier.set("installer")
		manifest{
			from(fabricManifest)
			attributes(
					"Premain-Class" to "org.spongepowered.fabric.installer.Agent",
					"Agent-Class" to "org.spongepowered.fabric.installer.Agent",
					"Launcher-Agent-Class" to "org.spongepowered.fabric.installer.Agent",
					"Multi-Release" to true
			)
		}
		from(fabricInstaller.output)
		into("META-INF/versions/9/") {
			from(fabricInstallerJava9.output)
		}
	}


	val installerResources = project.layout.buildDirectory.dir("generated/resources/installer")
	val emitDependencies by registering(org.spongepowered.gradle.impl.OutputDependenciesToJson::class) {
		group = "sponge"
		this.dependencies(fabricAppLaunchConfig)
		outputFile.set(installerResources.map { it.file("libraries.json") })
	}
	named(fabricInstaller.processResourcesTaskName).configure {
		dependsOn(emitDependencies)
	}

	shadowJar {
		archiveClassifier.set("universal-dev")

		configurations = listOf(project.configurations.getByName(fabricInstaller.runtimeClasspathConfigurationName))

		manifest {
			attributes(mapOf(
					// "Access-Widener" to "common.accesswidener",
					// "Main-Class" to "org.spongepowered.vanilla.installer.InstallerMain",
					"Launch-Target" to "sponge_server_prod",
					"Multi-Release" to true,
					"Premain-Class" to "org.spongepowered.vanilla.installer.Agent",
					"Agent-Class" to "org.spongepowered.vanilla.installer.Agent",
					"Launcher-Agent-Class" to "org.spongepowered.vanilla.installer.Agent"
			))
			from(fabricManifest)
		}

		from(commonProject.sourceSets.main.map { it.output })
		from(commonProject.sourceSets.named("mixins").map { it.output })
		from(commonProject.sourceSets.named("accessors").map { it.output })
		from(commonProject.sourceSets.named("launch").map { it.output })
		from(commonProject.sourceSets.named("applaunch").map { it.output })
		from(sourceSets.main.map { it.output })
		from(fabricInstaller.output)
		from(fabricInstallerJava9.output) {
			into("META-INF/versions/9/")
		}

		// We cannot have modules in a shaded jar
		exclude("META-INF/versions/*/module-info.class")
		exclude("module-info.class")
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	}
	assemble {
		dependsOn(shadowJar)
	}
	remapJar {
		dependsOn(shadowJar)
		archiveClassifier.set("universal")
		input.fileValue(shadowJar.get().outputs.files.singleFile)
	}
}

license {
	properties {
		this["name"] = "Sponge"
		this["organization"] = organization
		this["url"] = projectUrl
	}
	header(rootProject.file("HEADER.txt"))

	include("**/*.java")
	newLine(false)
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
	withSourcesJar()
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			// add all the jars that should be included when publishing to maven
			artifact(tasks["remapJar"]) {
				builtBy(tasks["remapJar"])
			}
			artifact(tasks["sourcesJar"]) {
				builtBy(tasks["remapSourcesJar"])
			}
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
		mavenCentral()
	}
}


