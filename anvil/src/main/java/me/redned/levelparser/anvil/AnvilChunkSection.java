package me.redned.levelparser.anvil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.redned.levelparser.Biome;
import me.redned.levelparser.BlockState;
import me.redned.levelparser.ChunkSection;
import me.redned.levelparser.anvil.storage.NibbleArray;
import me.redned.levelparser.anvil.storage.PaletteStorage;
import me.redned.levelparser.anvil.storage.palette.Palette;

@Getter
@Setter
@AllArgsConstructor
public class AnvilChunkSection implements ChunkSection {
    public static final AnvilChunkSection EMPTY = new AnvilChunkSection(-1);

    private final int y;

    private final PaletteStorage<BlockState> blockPalette;
    private final PaletteStorage<Biome> biomePalette;

    private NibbleArray skyLight;
    private NibbleArray blockLight;

    public AnvilChunkSection(int y) {
        this.y = y;
        this.blockPalette = new PaletteStorage<>(Palette.BLOCKS);
        this.biomePalette = new PaletteStorage<>(Palette.BIOMES);
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        return this.blockPalette.get(x & 0xF, y & 0xF, z & 0xF);
    }

    @Override
    public void setBlockState(int x, int y, int z, BlockState state) {
        this.blockPalette.set(x & 0xF, y & 0xF, z & 0xF, state);
    }

    @Override
    public Biome getBiome(int x, int y, int z) {
        return this.biomePalette.get(x & 0xF, y & 0xF, z & 0xF);
    }

    @Override
    public void setBiome(int x, int y, int z, Biome biome) {
        this.biomePalette.set(x & 0xF, y & 0xF, z & 0xF, biome);
    }
}
