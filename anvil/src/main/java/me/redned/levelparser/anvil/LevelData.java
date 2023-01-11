package me.redned.levelparser.anvil;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class LevelData {
    private static final int FORMAT_VERSION = 19133;

    private final LevelVersion version;
    private final String levelName;
    private final int gameType;
    private final int spawnX;
    private final int spawnY;
    private final int spawnZ;
    private final long lastPlayed;
    private final boolean hardcore;
    private final boolean allowCommands;
    private final List<String> enabledDataPacks;
    private final List<String> disabledDataPacks;
    private final WorldGenSettings worldGenSettings;

    public int getDataVersion() {
        return this.version.dataVersion();
    }

    public NbtMap serialize() {
        NbtMapBuilder dimensionsTagBuilder = NbtMap.builder();
        dimensionsTagBuilder.putAll(this.worldGenSettings.dimensions());

        NbtMap dataTag = NbtMap.builder()
                .putString("LevelName", this.levelName)
                .putInt("GameType", this.gameType)
                .putInt("DataVersion", this.version.dataVersion())
                .putInt("SpawnX", this.spawnX)
                .putInt("SpawnY", this.spawnY)
                .putInt("SpawnZ", this.spawnZ)
                .putBoolean("allowCommands", this.allowCommands)
                .putBoolean("hardcore", this.hardcore)
                .putCompound("Version", NbtMap.builder()
                        .putBoolean("Snapshot", this.version.snapshot())
                        .putString("Series", this.version.series())
                        .putInt("Id", this.version.dataVersion())
                        .putString("Name", this.version.name())
                        .build())
                .putInt("version", FORMAT_VERSION)
                .putLong("LastPlayed", this.lastPlayed)
                .putCompound("DataPacks", NbtMap.builder()
                        .putList("Enabled", NbtType.STRING, this.enabledDataPacks)
                        .putList("Disabled", NbtType.STRING, this.disabledDataPacks)
                        .build())
                .putCompound("WorldGenSettings", NbtMap.builder()
                        .putBoolean("bonus_chest", this.worldGenSettings.bonusChest())
                        .putLong("seed", this.worldGenSettings.seed())
                        .putBoolean("generate_features", this.worldGenSettings.generateFeatures())
                        .putCompound("dimensions", dimensionsTagBuilder.build())
                        .build())
                .build();

        return NbtMap.builder()
                .putCompound("Data", dataTag)
                .build();
    }

    public static LevelData deserialize(NbtMap tag) {
        NbtMap dataTag = tag.getCompound("Data");
        NbtMap versionTag = dataTag.getCompound("Version");
        NbtMap datapacksTag = dataTag.getCompound("DataPacks");

        NbtMap worldGenTag = dataTag.getCompound("WorldGenSettings");
        Map<String, NbtMap> dimensions = (Map<String, NbtMap>) worldGenTag.get("dimensions");

        return new LevelData(
                new LevelVersion(
                        versionTag.getBoolean("Snapshot"),
                        versionTag.getString("Series"),
                        versionTag.getInt("Id"),
                        versionTag.getString("Name")
                ),
                dataTag.getString("LevelName"),
                dataTag.getInt("GameType"),
                dataTag.getInt("SpawnX"),
                dataTag.getInt("SpawnY"),
                dataTag.getInt("SpawnZ"),
                dataTag.getLong("LastPlayed"),
                dataTag.getBoolean("hardcore"),
                dataTag.getBoolean("allowCommands"),
                datapacksTag.getList("Enabled", NbtType.STRING),
                datapacksTag.getList("Disabled", NbtType.STRING),
                new WorldGenSettings(
                        worldGenTag.getBoolean("bonus_chest"),
                        worldGenTag.getLong("seed"),
                        worldGenTag.getBoolean("generate_features"),
                        dimensions == null ? Collections.emptyMap() : dimensions
                )
        );
    }

    public record LevelVersion(boolean snapshot, String series, int dataVersion, String name) {
    }

    public record WorldGenSettings(boolean bonusChest, long seed, boolean generateFeatures, Map<String, NbtMap> dimensions) {
    }
}
