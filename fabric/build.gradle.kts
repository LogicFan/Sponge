import org.jetbrains.gradle.ext.TaskTriggersConfig
import org.spongepowered.gradle.impl.OutputDependenciesToJson
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
	id("fabric-loom")
	id("com.github.johnrengelman.shadow")
	id("implementation-structure")
	id("templated-resources")
	eclipse
}

// variable for Sponge
val commonProject = parent!!
val apiVersion: String by project
val minecraftVersion: String by project
val recommendedVersion: String by project
val organization: String by project
val projectUrl: String by project

val testplugins: Project? = rootProject.subprojects.find { "testplugins".equals(it.name) }

// variable for Fabric
val name : String by project
val loaderVersion : String by project
val modVersion : String by project

description = "The SpongeAPI implementation for FabricMC"
version = spongeImpl.generatePlatformBuildVersionString(apiVersion, minecraftVersion, recommendedVersion)

// Fabric extra configurations
val fabricLibrariesConfig: NamedDomainObjectProvider<Configuration> = configurations.register("libraries")
val fabricAppLaunchConfig: NamedDomainObjectProvider<Configuration> = configurations.register("applaunch") {
	extendsFrom(fabricLibrariesConfig.get())
	// add mod dependencies (i.e. fabric-loader)
	extendsFrom(configurations.modImplementationMapped.get())
	// add dependencies of fabric-loader
	extendsFrom(configurations.named("loaderLibraries").get())
}
val fabricInstallerConfig: NamedDomainObjectProvider<Configuration> = configurations.register("installer")

// Common source sets and configurations
val launchConfig: NamedDomainObjectProvider<Configuration> = commonProject.configurations.named("launch")
val accessors: NamedDomainObjectProvider<SourceSet> = commonProject.sourceSets.named("accessors")
val launch: NamedDomainObjectProvider<SourceSet> = commonProject.sourceSets.named("launch")
val applaunch: NamedDomainObjectProvider<SourceSet> = commonProject.sourceSets.named("applaunch")
val mixins: NamedDomainObjectProvider<SourceSet> = commonProject.sourceSets.named("mixins")
val main: NamedDomainObjectProvider<SourceSet> = commonProject.sourceSets.named("main")

// Fabric source sets
val fabricInstaller by sourceSets.register("installer")
val fabricMain by sourceSets.named("main") {
	// implementation (compile) dependencies
	spongeImpl.applyNamedDependencyOnOutput(commonProject, accessors.get(), this, project, this.implementationConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), this, project, this.implementationConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
	configurations.named(implementationConfigurationName) {
		extendsFrom(fabricLibrariesConfig.get())
	}
}
val fabricLaunch by sourceSets.register("launch") {
	// implementation (compile) dependencies
	spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), this, project, this.implementationConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(commonProject, main.get(), this, project, this.implementationConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(project, this, fabricMain, project, fabricMain.implementationConfigurationName)

	configurations.named(implementationConfigurationName) {
		extendsFrom(fabricAppLaunchConfig.get())
	}
}
val fabricMixins by sourceSets.register("mixins") {
	// implementation (compile) dependencies
	spongeImpl.applyNamedDependencyOnOutput(commonProject, mixins.get(), this, project, this.implementationConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(commonProject, accessors.get(), this, project, this.implementationConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), this, project, this.implementationConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(commonProject, main.get(), this, project, this.implementationConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(project, fabricMain, this, project, this.implementationConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(project, fabricLaunch, this, project, this.implementationConfigurationName)
}
val fabricAppLaunch by sourceSets.register("applaunch") {
	// implementation (compile) dependencies
	spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), fabricLaunch, project, this.implementationConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(project, fabricInstaller, this, project, this.implementationConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(project, this, fabricLaunch, project, fabricLaunch.implementationConfigurationName)
	// runtime dependencies - literally add the rest of the project, because we want to launch the game
	spongeImpl.applyNamedDependencyOnOutput(project, fabricMixins, this, project, this.runtimeOnlyConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(project, fabricLaunch, this, project, this.runtimeOnlyConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(commonProject, mixins.get(), this, project, this.runtimeOnlyConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(commonProject, main.get(), this, project, this.runtimeOnlyConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(commonProject, accessors.get(), this, project, this.runtimeOnlyConfigurationName)
	spongeImpl.applyNamedDependencyOnOutput(project, fabricMain, this, project, this.runtimeOnlyConfigurationName)
}
val fabricMixinsImplementation by configurations.named(fabricMixins.implementationConfigurationName) {
	extendsFrom(fabricAppLaunchConfig.get())
}
configurations.named(fabricInstaller.implementationConfigurationName) {
	extendsFrom(fabricInstallerConfig.get())
}
configurations.named(fabricAppLaunch.implementationConfigurationName) {
	extendsFrom(fabricAppLaunchConfig.get())
	// remove this line, since minecraft itself is not needed
	// extendsFrom(launchConfig.get())
}
val vanillaAppLaunchRuntime by configurations.named(fabricAppLaunch.runtimeOnlyConfigurationName)

val mixinConfigs: MutableSet<String> = spongeImpl.mixinConfigurations

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
	// val log4jVersion: String by project
	val log4jVersion: String = "2.8.1"
	val mixinVersion: String by project
	val modlauncherVersion: String by project
	val pluginSpiVersion: String by project
	val timingsVersion: String by project

	val installer = fabricInstallerConfig.get().name
	installer("com.google.code.gson:gson:2.8.0")
	installer("org.spongepowered:configurate-hocon:4.1.1")
	installer("org.spongepowered:configurate-core:4.1.1")
	installer("net.sf.jopt-simple:jopt-simple:5.0.3")
	installer("org.tinylog:tinylog-api:2.2.1")
	installer("org.tinylog:tinylog-impl:2.2.1")
	installer("org.ow2.asm:asm-commons:$asmVersion")

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

	testplugins?.also {
		vanillaAppLaunchRuntime(project(it.path)) {
			exclude(group = "org.spongepowered")
		}
	}
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
		archiveClassifier.set("dev")
		manifest.from(fabricManifest)
	}

	val fabricInstallerJar by registering(Jar::class) {
		archiveClassifier.set("installer-dev")
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
	}

	// copy and convert installer/templates/** into src
	val installerTemplateSource = project.file("src/installer/templates")
	val installerTemplateDest = project.layout.buildDirectory.dir("generated/sources/installerTemplates")
	val generateInstallerTemplates by registering(Copy::class) {
		group = "sponge"
		description = "Generate classes from templates for the SpongeFabric installer"
		val properties = mutableMapOf(
				"minecraftVersion" to minecraftVersion
		)
		inputs.properties(properties)

		// Copy template
		from(installerTemplateSource)
		into(installerTemplateDest)
		expand(properties)
	}
	fabricInstaller.java.srcDir(generateInstallerTemplates.map { it.outputs })

	// Generate templates on IDE import as well
	(rootProject.idea.project as? ExtensionAware)?.also {
		(it.extensions["settings"] as ExtensionAware).extensions.getByType(TaskTriggersConfig::class).afterSync(generateInstallerTemplates)
	}
	project.eclipse {
		synchronizationTasks(generateInstallerTemplates)
	}

	val installerResources = project.layout.buildDirectory.dir("generated/resources/installer")
	fabricInstaller.resources.srcDir(installerResources)

	val downloadNotNeeded = configurations.register("downloadNotNeeded") {
		extendsFrom(fabricInstallerConfig.get())
		// exclude mod dependencies (i.e. fabric-loader)
		extendsFrom(configurations.modImplementationMapped.get())
		// exclude dependencies of fabric-loader
		extendsFrom(configurations.named("loaderLibraries").get())
	}

	val emitDependencies by registering(OutputDependenciesToJson::class) {
		group = "sponge"
		this.dependencies(fabricAppLaunchConfig)
		this.excludedDependencies(downloadNotNeeded)
		outputFile.set(installerResources.map { it.file("libraries.json") })
	}
	named(fabricInstaller.processResourcesTaskName).configure {
		dependsOn(emitDependencies)
	}

	shadowJar {
		archiveClassifier.set("universal-dev")

		configurations = listOf(project.configurations.getByName(fabricInstaller.runtimeClasspathConfigurationName))

		transform(Log4j2PluginsCacheFileTransformer::class.java)

		manifest {
			attributes(mapOf(
					// "Access-Widener" to "common.accesswidener",
					// "Main-Class" to "org.spongepowered.fabric.installer.InstallerMain",
					"Launch-Target" to "sponge_server_prod",
					"Multi-Release" to true,
					"Premain-Class" to "org.spongepowered.fabric.installer.Agent",
					"Agent-Class" to "org.spongepowered.fabric.installer.Agent",
					"Launcher-Agent-Class" to "org.spongepowered.fabric.installer.Agent"
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
		from(fabricAppLaunch.output)
		from(fabricLaunch.output)

		// We cannot have modules in a shaded jar
		exclude("META-INF/versions/*/module-info.class")
		exclude("module-info.class")
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

val shadowJar by tasks.existing
val remapJar by tasks.existing
val fabricInstallerJar by tasks.existing
//val vanillaAppLaunchJar by tasks.existing
//val vanillaLaunchJar by tasks.existing
//val vanillaMixinsJar by tasks.existing

// configure the maven publication
publishing {
	publications {
		register("sponge", MavenPublication::class) {
			artifact(shadowJar.get());
			artifact(remapJar.get())
			artifact(fabricInstallerJar.get())
//			artifact(vanillaAppLaunchJar.get())
//			artifact(vanillaLaunchJar.get())
//			artifact(vanillaMixinsJar.get())
//			artifact(tasks["applaunchSourceJar"])
//			artifact(tasks["launchSourceJar"])
//			artifact(tasks["mixinsSourceJar"])
			pom {
				artifactId = project.name.toLowerCase()
				this.name.set(project.name)
				this.description.set(project.description)
				this.url.set(projectUrl)

				licenses {
					license {
						this.name.set("MIT")
						this.url.set("https://opensource.org/licenses/MIT")
					}
				}
				scm {
					connection.set("scm:git:git://github.com/SpongePowered/Sponge.git")
					developerConnection.set("scm:git:ssh://github.com/SpongePowered/Sponge.git")
					this.url.set(projectUrl)
				}
			}
		}
	}
}


