plugins {
	id("fabric-loom") version "0.6-SNAPSHOT"
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

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:$minecraftVersion")
	// mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"
	mappings(minecraft.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

	// Fabric API. This is technically optional, but you probably want it anyway.
	// modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")

	// PSA: Some older mods, compiled on Loom 0.2.1, might have outdated Maven POMs.
	// You may need to force-disable transitiveness on them.
}


tasks {
	withType(ProcessResources::class) {
		inputs.properties(Pair("version", project.version))
		filesMatching("fabric.mod.json") {
			expand(Pair("version", project.version))
		}
	}
	withType(JavaCompile::class).configureEach {
		// ensure that the encoding is set to UTF-8, no matter what the system default is
		// this fixes some edge cases with special characters not displaying correctly
		// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
		// If Javadoc is generated, this must be specified in that task too.
		this.options.encoding = "UTF-8"
		// The Minecraft launcher currently installs Java 8 for users, so your mod probably wants to target Java 8 too
		// JDK 9 introduced a new way of specifying this that will make sure no newer classes or methods are used.
		// We'll use that if it's available, but otherwise we'll use the older option.
		val targetVersion = 8
		if (JavaVersion.current().isJava9Compatible) {
			this.options.release.set(targetVersion)
		}
	}
	withType(Jar::class) {
		from("LICENSE.txt") {
			rename {
				"${it}_${name}"
			}
		}
	}
}

sourceSets {
	main {
		java {
			setSrcDirs(listOf("src/mod/java"))
		}
		resources {
			setSrcDirs(listOf("src/mod/resources"))
		}
	}
}


java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
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


