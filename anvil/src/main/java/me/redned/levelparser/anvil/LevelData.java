package me.redned.levelparser.anvil;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;

import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class LevelData {
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
                .putInt("version", 19133)
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

    public record LevelVersion(boolean snapshot, String series, int dataVersion, String name) {
    }

    public record WorldGenSettings(boolean bonusChest, long seed, boolean generateFeatures, Map<String, NbtMap> dimensions) {
    }
}
