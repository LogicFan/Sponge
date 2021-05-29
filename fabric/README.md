# Sponge-Fabric

A support of Fabric MC for Sponge server

## How to build

### Tiny-Remapper setup

1. Clone the tiny-remapper repo from [here](https://github.com/LogicFan/tiny-remapper)
```
git clone https://github.com/LogicFan/tiny-remapper.git
```

2. Checkout to `multiReleaseJar-1` (Not the `multiReleaseJar` one, this is old wrong version)
```
git checkout multiReleaseJar-1
```

3. Build the Tiny-Remapper

```
./gradlew clean build publishToMavenLocal
```

### Fabric-Loom Setup

1. Clone the [Fabric-Loom](https://github.com/FabricMC/fabric-loom) to local

```access transformers
git clone https://github.com/FabricMC/fabric-loom.git
```

2. Checkout to `dev/0.8` branch

```access transformers
git checkout dev/0.8
```

3. In `build.gradle` at root of `fabric-loom` project, add `mavenLocal()` to the repositories section .
Something like:
```access transformers
repositories {
	maven {
		name = 'Fabric'
		url = 'https://maven.fabricmc.net/'
	}
	mavenLocal()
	mavenCentral()
}
```

4. Also in the same `build.gradle` file as in step 3. We change the following line
```
implementation ('net.fabricmc:tiny-remapper:0.3.2')
```
to 
```
implementation ('net.fabricmc:tiny-remapper:0.3.2+local')
```
inside the `dependencies` sectiion.

5. We build the loom project:

```access transformers
./gradlew clean build publishToMavenLocal -x test
```

### Build the Project

1. Clone the sponge repository:

```access transformers
git clone --recursive https://github.com/LogicFan/Sponge.git
```

2. Change to fabric branch and checkout:

```access transformers
git checkout fabric
```

3. Final steps:

- ```git pull```
- ```git submodule update --recursive```
- ```./gradlew build --refresh-dependencies```