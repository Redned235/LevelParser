# LevelParser

A Minecraft level parser.

**Currently Supporting: Minecraft: Java Edition 1.19.x**

This project only supports Anvil world files at the moment, but has been designed in such a way that alternative formats can also be used.

## Usage
To start, you will need to create a `LevelParser` object like so:
```java
LevelParser<AnvilLevel> parser = LevelParser.<AnvilLevel>builder()
        .input(Paths.get("input"))
        .output(Paths.get("output"))
        .reader(new AnvilLevelReader())
        .writer(new AnvilLevelWriter())
        .build();
```

Afterward, create a new `AnvilLevel` which you can do like so:
```java
AnvilLevel level = new AnvilLevel(
        0, // minHeight
        256, // maxHeight
        0, // worldTime
        new LevelData( // levelData
                new LevelData.LevelVersion( // levelVersion
                        false, // snapshot
                        "main", // series
                        3120, // dataVersion
                        "1.19.2" // name
                ),
                "My World Name", // worldName
                1, // gameType (1 = creative mode)
                10, // spawnX
                0, // spawnY
                10, // spawnZ
                System.currentTimeMillis(), // lastPlayed
                false, // hardcore
                true, // allowCommands
                List.of("vanilla"), // enabledDataPacks
                List.of(), // disabledDataPacks
                new LevelData.WorldGenSettings( // worldGenSettings
                        false, // bonusChest
                        0, // seed
                        false, // generateFeatures
                        ... // dimensions
                )
        )
);
```

With this level, you can now manipulate it in any way. Once you are done modifying your level, you can write it by doing the following using your parser:

```java
parser.writeLevel(level);
```

## Repository

### Gradle
```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.Redned235.LevelParser:anvil:master-SNAPSHOT")
}
```

### Maven:
```xml
<repositories>
    <repository>
        <id>jitpack</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.Redned235.LevelParser</groupId>
        <artifactId>anvil</artifactId>
        <version>master-SNAPSHOT</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```
