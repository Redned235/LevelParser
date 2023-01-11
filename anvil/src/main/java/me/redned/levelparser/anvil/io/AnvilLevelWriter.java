package me.redned.levelparser.anvil.io;

import me.redned.levelparser.ChunkSection;
import me.redned.levelparser.anvil.AnvilChunk;
import me.redned.levelparser.anvil.AnvilChunkSection;
import me.redned.levelparser.anvil.AnvilLevel;
import me.redned.levelparser.anvil.storage.region.RegionFile;
import me.redned.levelparser.io.LevelWriter;
import org.cloudburstmc.nbt.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnvilLevelWriter implements LevelWriter<AnvilLevel> {
    private static final String ANVIL_EXTENSION = ".mca";

    @Override
    public void write(AnvilLevel level, Path path) throws IOException {
        if (Files.exists(path) && !Files.isDirectory(path)) {
            throw new IllegalArgumentException("Level path was found at location " + path + ", but it was not a directory!");
        } else if (Files.notExists(path)) {
            Files.createDirectory(path);
        }

        // Write level.dat file
        Path levelData = path.resolve("level.dat");
        if (Files.notExists(levelData)) {
            Files.createFile(levelData);
        }

        NbtMap serializedLevelData = level.getLevelData().serialize();
        try (OutputStream outputStream = Files.newOutputStream(levelData)) {
            try (NBTOutputStream nbtOutputStream = NbtUtils.createGZIPWriter(outputStream)) {
                nbtOutputStream.writeTag(serializedLevelData);
            }
        }

        // Create region file directory
        Path regionDir = path.resolve("region");
        if (Files.exists(regionDir) && !Files.isDirectory(regionDir)) {
            throw new IllegalArgumentException("Region path " + regionDir + " is not a directory!");
        } else if (Files.notExists(regionDir)) {
            Files.createDirectory(regionDir);
        }

        // Break chunks into their corresponding region files and save them to disk
        Map<Long, List<AnvilChunk>> chunksByRegion = new HashMap<>();
        for (Map.Entry<Long, AnvilChunk> entry : level.getChunks().entrySet()) {
            AnvilChunk chunk = entry.getValue();
            int regionX = chunk.getX() >> 5;
            int regionZ = chunk.getZ() >> 5;

            long serializedPos = ((long) regionZ << 32) | (regionX & 0xFFFFFFFFL);
            chunksByRegion.computeIfAbsent(serializedPos, e -> new ArrayList<>()).add(chunk);
        }

        for (Map.Entry<Long, List<AnvilChunk>> entry : chunksByRegion.entrySet()) {
            int regionX = ((int) entry.getKey().longValue());
            int regionZ = ((int) (entry.getKey() >>> 32));

            Path regionPath = regionDir.resolve("r." + regionX + "." + regionZ + ANVIL_EXTENSION);
            Files.createFile(regionPath);

            RegionFile regionFile = new RegionFile(regionPath, regionX, regionZ);
            regionFile.serialize(entry.getValue(), this::getSerializedChunk);

            regionFile.close();
        }
    }

    private NbtMap getSerializedChunk(AnvilChunk chunk) {
        AnvilLevel level = chunk.getLevel();
        int minSectionY = level.getMinHeight() >> 4;
        int maxSectionY = level.getMaxHeight() >> 4;

        NbtMapBuilder builder = NbtMap.builder()
                .putInt("DataVersion", level.getLevelData().getDataVersion())
                .putInt("xPos", chunk.getX())
                .putInt("yPos", minSectionY)
                .putInt("zPos", chunk.getZ())
                .putLong("LastUpdate", level.getWorldTime())
                .putLong("InhabitedTime", chunk.getInhabitedTime())
                .putString("Status", chunk.getStatus());

        ChunkSection[] sections = chunk.getSections();

        List<NbtMap> serializedSections = new ArrayList<>();
        for (int i = minSectionY; i < maxSectionY; i++) {
            int arrayIndex = i - minSectionY;

            AnvilChunkSection section = (AnvilChunkSection) sections[arrayIndex];
            if (section == null) {
                // Use empty section if none are set
                section = AnvilChunkSection.EMPTY;
            }

            NbtMapBuilder sectionBuilder = NbtMap.builder()
                    .putByte("Y", (byte) section.getY())
                    .putCompound("block_states", section.getBlockPalette().serialize())
                    .putCompound("biomes", section.getBiomePalette().serialize());

            if (section.getBlockLight() != null) {
                sectionBuilder.putByteArray("BlockLight", section.getBlockLight().getData());
            }

            if (section.getSkyLight() != null) {
                sectionBuilder.putByteArray("SkyLight", section.getSkyLight().getData());
            }

            serializedSections.add(sectionBuilder.build());
        }

        builder.putList("sections", NbtType.COMPOUND, serializedSections);
        builder.putList("block_entities", NbtType.COMPOUND, chunk.getBlockEntities());
        return builder.build();
    }
}
