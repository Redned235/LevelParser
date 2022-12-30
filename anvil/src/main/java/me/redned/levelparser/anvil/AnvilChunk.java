package me.redned.levelparser.anvil;

import lombok.Getter;
import lombok.Setter;
import me.redned.levelparser.Biome;
import me.redned.levelparser.BlockState;
import me.redned.levelparser.Chunk;
import me.redned.levelparser.ChunkSection;
import org.cloudburstmc.nbt.NbtMap;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AnvilChunk implements Chunk {
    private final AnvilLevel level;
    private final int x;
    private final int z;
    private long inhabitedTime;
    private final String status;
    private final List<NbtMap> blockEntities = new ArrayList<>();

    private final ChunkSection[] sections;

    public AnvilChunk(AnvilLevel level, int x, int z) {
        this(level, x, z, "full");
    }

    public AnvilChunk(AnvilLevel level, int x, int z, String status) {
        this.level = level;
        this.x = x;
        this.z = z;
        this.status = status;
        this.sections = new ChunkSection[(level.getMaxHeight() >> 4) - (level.getMinHeight() >> 4)];
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        int sectionY = y >> 4;

        ChunkSection section = this.getSection(sectionY);
        if (section == null) {
            return BlockState.AIR;
        }

        return section.getBlockState(x, y, z);
    }

    @Override
    public void setBlockState(int x, int y, int z, BlockState state) {
        int sectionY = y >> 4;

        ChunkSection section = this.getSection(sectionY);
        if (section == null) {
            int arrayY = sectionY - (this.level.getMinHeight() >> 4);
            section = this.sections[arrayY] = new AnvilChunkSection(sectionY, this.level.hasSkyLight());
        }

        section.setBlockState(x, y, z, state);
    }

    @Override
    public Biome getBiome(int x, int y, int z) {
        int sectionY = y >> 4;

        ChunkSection section = this.getSection(sectionY);
        if (section == null) {
            return Biome.PLAINS;
        }

        return section.getBiome(x, y, z);
    }

    @Override
    public void setBiome(int x, int y, int z, Biome biome) {
        int sectionY = y >> 4;

        ChunkSection section = this.getSection(sectionY);
        if (section == null) {
            int arrayY = sectionY - (this.level.getMinHeight() >> 4);
            section = this.sections[arrayY] = new AnvilChunkSection(sectionY, this.level.hasSkyLight());
        }

        section.setBiome(x, y, z, biome);
    }

    @Override
    public ChunkSection getSection(int y) {
        return this.sections[y - (this.level.getMinHeight() >> 4)];
    }
}
