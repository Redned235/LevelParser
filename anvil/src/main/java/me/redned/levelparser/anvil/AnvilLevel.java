package me.redned.levelparser.anvil;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.redned.levelparser.Biome;
import me.redned.levelparser.BlockState;
import me.redned.levelparser.Chunk;
import me.redned.levelparser.Level;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class AnvilLevel implements Level {
    private final int minHeight;
    private final int maxHeight;
    private final long worldTime;
    private final boolean skyLight;
    private final LevelData levelData;

    private final Map<Long, AnvilChunk> chunks = new HashMap<>();

    @Override
    public boolean chunkExists(int x, int z) {
        return this.chunks.containsKey(((long) z << 32) | (x & 0xFFFFFFFFL));
    }

    @Override
    public AnvilChunk getChunk(int x, int z) {
        long key = ((long) z << 32) | (x & 0xFFFFFFFFL);
        AnvilChunk chunk = this.chunks.get(key);
        if (chunk == null) {
            this.chunks.put(key, chunk = new AnvilChunk(this, x, z));
        }

        return chunk;
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        return this.getChunk(x >> 4, z >> 4).getBlockState(x, y, z);
    }

    @Override
    public void setBlockState(int x, int y, int z, BlockState state) {
        AnvilChunk chunk = this.getChunk(x >> 4, z >> 4);
        chunk.setBlockState(x, y, z, state);
    }

    @Override
    public Biome getBiome(int x, int y, int z) {
        return this.getChunk(x >> 4, z >> 4).getBiome(x, y, z);
    }

    @Override
    public void setBiome(int x, int y, int z, Biome biome) {
        AnvilChunk chunk = this.getChunk(x >> 4, z >> 4);
        chunk.setBiome(x, y, z, biome);
    }

    @Override
    public boolean hasSkyLight() {
        return this.skyLight;
    }

    public LevelData getLevelData() {
        return this.levelData;
    }
}
