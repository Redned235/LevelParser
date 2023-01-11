package me.redned.levelparser.anvil.io;

import me.redned.levelparser.Biome;
import me.redned.levelparser.BlockState;
import me.redned.levelparser.ChunkSection;
import me.redned.levelparser.anvil.AnvilChunk;
import me.redned.levelparser.anvil.AnvilChunkSection;
import me.redned.levelparser.anvil.AnvilLevel;
import me.redned.levelparser.anvil.LevelData;
import me.redned.levelparser.anvil.storage.NibbleArray;
import me.redned.levelparser.anvil.storage.PaletteStorage;
import me.redned.levelparser.anvil.storage.palette.Palette;
import me.redned.levelparser.anvil.storage.region.RegionFile;
import me.redned.levelparser.io.LevelReader;
import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.nbt.NbtUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.InflaterInputStream;

public class AnvilLevelReader implements LevelReader<AnvilLevel> {

    @Override
    public AnvilLevel read(Path path, int minHeight, int maxHeight) throws IOException {
        if (Files.notExists(path) || !Files.isDirectory(path)) {
            throw new IllegalArgumentException("Level path " + path + " did not exist or was not a directory!");
        }

        // Read level.dat file
        Path levelDataPath = path.resolve("level.dat");
        if (Files.notExists(levelDataPath)) {
            throw new IllegalArgumentException("No level.dat file found in directory " + path + "!");
        }

        // Read region dir
        Path regionDir = path.resolve("region");
        if (Files.notExists(regionDir) || !Files.isDirectory(regionDir)) {
            throw new IllegalArgumentException("Region path " + path + " did not exist or was not a directory!");
        }

        NbtMap serializedLevelData;
        try (InputStream inputStream = Files.newInputStream(levelDataPath)) {
            try (NBTInputStream nbtInputStream = NbtUtils.createGZIPReader(inputStream)) {
                serializedLevelData = (NbtMap) nbtInputStream.readTag();
            }
        }

        LevelData levelData = LevelData.deserialize(serializedLevelData);

        Map<Long, AnvilChunk> chunks = new HashMap<>();
        AnvilLevel level = new AnvilLevel(
                minHeight,
                maxHeight,
                0, // TODO - world time
                levelData,
                chunks
        );

        try (Stream<Path> regionPaths = Files.walk(regionDir)) {
            regionPaths.forEach(regionPath -> {
                if (Files.isDirectory(regionPath)) {
                    return;
                }

                try {
                    RegionFile regionFile = RegionFile.deserialize(regionPath, file -> {
                        try {
                            AnvilChunk chunk = deserializeChunk(level, file);
                            chunks.put(((long) chunk.getZ() << 32) | (chunk.getX() & 0xFFFFFFFFL), chunk);
                        } catch (IOException ex) {
                            throw new RuntimeException("Failed to deserialize chunk!", ex);
                        }
                    });

                    regionFile.close();
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to deserialize RegionFile", ex);
                }
            });
        }

        return level;
    }

    private static AnvilChunk deserializeChunk(AnvilLevel level, RandomAccessFile file) throws IOException {
        int length = file.readInt();
        byte compressionType = file.readByte();

        // TODO: Support other compression algorithms
        if (compressionType != 2) {
            throw new IllegalArgumentException("Only Zlib compression is supported!");
        }

        FileInputStream fileStream = new FileInputStream(file.getFD());
        BufferedInputStream inputStream = new BufferedInputStream(new InflaterInputStream(fileStream));
        NBTInputStream nbtInputStream = NbtUtils.createReader(inputStream);
        NbtMap tag = (NbtMap) nbtInputStream.readTag();

        List<ChunkSection> sections = new ArrayList<>();
        for (NbtMap sectionTag : tag.getList("sections", NbtType.COMPOUND)) {
            byte y = sectionTag.getByte("Y");

            NibbleArray blockLight = null;
            if (sectionTag.containsKey("BlockLight", NbtType.BYTE_ARRAY)) {
                blockLight = new NibbleArray(sectionTag.getByteArray("BlockLight"));
            }

            NibbleArray skyLight = null;
            if (sectionTag.containsKey("SkyLight", NbtType.BYTE_ARRAY)) {
                skyLight = new NibbleArray(sectionTag.getByteArray("SkyLight"));
            }

            PaletteStorage<BlockState> blockPalette = PaletteStorage.deserialize(Palette.BLOCKS, sectionTag.getCompound("block_states"));
            PaletteStorage<Biome> biomePalette = PaletteStorage.deserialize(Palette.BIOMES, sectionTag.getCompound("biomes"));

            sections.add(new AnvilChunkSection(
                    y,
                    blockPalette,
                    biomePalette,
                    skyLight,
                    blockLight
            ));
        }

        return new AnvilChunk(
                level,
                tag.getInt("xPos"),
                tag.getInt("zPos"),
                tag.getLong("InhabitedTime"),
                tag.getString("Status"),
                tag.getList("block_entities", NbtType.COMPOUND),
                sections.toArray(ChunkSection[]::new)
        );
    }
}
