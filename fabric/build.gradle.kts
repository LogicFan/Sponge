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
	// extendsFrom(configurations.minecraft.get())
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

	val installer = fabricInstallerConfig.get().name
	installer("com.google.code.gson:gson:2.8.0")
	installer("org.spongepowered:configurate-hocon:4.1.1")
	installer("org.spongepowered:configurate-core:4.1.1")
	installer("net.sf.jopt-simple:jopt-simple:5.0.3")
	installer("org.tinylog:tinylog-api:2.2.1")
	installer("org.tinylog:tinylog-impl:2.2.1")
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

	val installerResources = project.layout.buildDirectory.dir("generated/resources/installer")
//	val downloadNotNeeded = configurations.register("downloadNotNeeded") {
//		extendsFrom(configurations.minecraft.get())
//	}
	val emitDependencies by registering(org.spongepowered.gradle.impl.OutputDependenciesToJson::class) {
		group = "sponge"
		this.dependencies(fabricAppLaunchConfig)
		// except what we're providing through the installer
		// this.excludedDependencies(downloadNotNeeded)
		outputFile.set(installerResources.map { it.file("libraries.json") })
	}
	named(fabricInstaller.processResourcesTaskName).configure {
		dependsOn(emitDependencies)
	}

	shadowJar {
		archiveClassifier.set("universal-dev")
		configurations = listOf(project.configurations.getByName(fabricInstaller.runtimeClasspathConfigurationName))
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


