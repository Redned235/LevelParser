package me.redned.levelparser.anvil.storage.region;

import lombok.Getter;
import me.redned.levelparser.anvil.AnvilChunk;
import org.cloudburstmc.nbt.NBTOutputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.zip.DeflaterOutputStream;

@Getter
public class RegionFile {
    private static final int SECTOR_SIZE = 4096;
    private static final int HEADER_SIZE = 5;

    private final int x;
    private final int z;

    private final RandomAccessFile file;

    public RegionFile(Path path, int x, int z) {
        this.x = x;
        this.z = z;

        try {
            this.file = new RandomAccessFile(path.toFile(), "rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(List<AnvilChunk> chunks, Function<AnvilChunk, NbtMap> chunkSerializer) throws IOException {
        int offset = 2;
        int cursor = 0;

        this.file.seek(0);
        for (AnvilChunk chunk : chunks) {
            this.file.seek((long) offset * SECTOR_SIZE);

            int index = (chunk.getX() & 0x1F) + (chunk.getZ() & 0x1F) * 32;

            NbtMap serializedChunk = chunkSerializer.apply(chunk);

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(SECTOR_SIZE);
            try (BufferedOutputStream outputStream = new BufferedOutputStream(new DeflaterOutputStream(byteStream))) {
                try (NBTOutputStream nbtOutputStream = NbtUtils.createWriter(outputStream)) {
                    nbtOutputStream.writeTag(serializedChunk);
                }
            }

            byte[] bytes = byteStream.toByteArray();
            this.file.writeInt(bytes.length + 1);
            this.file.writeByte(2); // Zlib
            this.file.write(bytes, 0, bytes.length);

            cursor = bytes.length + HEADER_SIZE;
            int sectorCount = (cursor >> 12) + (cursor % SECTOR_SIZE != 0 ? 1 : 0);

            this.file.seek(index * 4);
            this.file.writeByte(offset >>> 16);
            this.file.writeByte(offset >> 8 & 0xFF);
            this.file.writeByte(offset & 0xFF);
            this.file.writeByte(sectorCount);

            this.file.seek(index * 4 + SECTOR_SIZE);
            this.file.writeInt((int) (System.currentTimeMillis() / 1000));

            offset += sectorCount;
        }

        // Additional padding at end
        if (cursor % SECTOR_SIZE != 0) {
            this.file.seek((long) offset * SECTOR_SIZE - 1);
            this.file.write(0);
        }
    }
}
