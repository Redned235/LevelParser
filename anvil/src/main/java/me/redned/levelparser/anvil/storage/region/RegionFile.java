package me.redned.levelparser.anvil.storage.region;

import lombok.Getter;
import me.redned.levelparser.anvil.AnvilChunk;
import me.redned.levelparser.anvil.util.FastByteArrayOutputStream;
import org.cloudburstmc.nbt.NBTOutputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtUtils;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
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

    public static RegionFile deserialize(Path path, Consumer<RandomAccessFile> chunkDeserializer) throws IOException {
        String fileName = path.getFileName().toString();
        String[] split = fileName.split("\\.");
        if (split.length != 4) {
            throw new IllegalArgumentException("Invalid region file! Name should follow the format r.<x>.<z>.mca!");
        }

        int x = Integer.parseInt(split[1]);
        int z = Integer.parseInt(split[2]);

        RegionFile regionFile = new RegionFile(path, x, z);
        RandomAccessFile file = regionFile.file;

        // Read all sectors (32 * 32)
        for (int i = 0; i < 1024; i++) {
            file.seek(i * 4);

            int offset = file.read() << 16;
            offset |= (file.read() & 0xFF) << 8;
            offset |= file.read() & 0xFF;

            // Check if there is any sectors
            if (file.readByte() == 0) {
                continue;
            }

            file.seek((long) SECTOR_SIZE * offset);
            chunkDeserializer.accept(file);
        }

        return regionFile;
    }

    public void serialize(List<AnvilChunk> chunks, Function<AnvilChunk, NbtMap> chunkSerializer) throws IOException {
        int offset = 2;
        int cursor = 0;

        this.file.seek(0);
        for (AnvilChunk chunk : chunks) {
            this.file.seek((long) offset * SECTOR_SIZE);

            int index = (chunk.getX() & 0x1F) + (chunk.getZ() & 0x1F) * 32;

            NbtMap serializedChunk = chunkSerializer.apply(chunk);

            FastByteArrayOutputStream byteStream = new FastByteArrayOutputStream(SECTOR_SIZE);
            try (BufferedOutputStream outputStream = new BufferedOutputStream(new DeflaterOutputStream(byteStream))) {
                try (NBTOutputStream nbtOutputStream = NbtUtils.createWriter(outputStream)) {
                    nbtOutputStream.writeTag(serializedChunk);
                }
            }

            byte[] bytes = byteStream.getByteArray();
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

    public void close() throws IOException {
        this.file.close();
    }
}
