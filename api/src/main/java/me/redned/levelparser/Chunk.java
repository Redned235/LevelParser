package me.redned.levelparser;

import org.cloudburstmc.nbt.NbtMap;

import java.util.List;

/**
 * Represents a chunk as part of a {@link Level}.
 * <p>
 * A chunk is a 16x16 region that spans the height of the world.
 * It is what contains all block data, block entities, and various
 * other miscellaneous information about the world. It is broken
 * up into multiple 16x16x16 {@link ChunkSection}s.
 */
public interface Chunk {

    /**
     * Gets the {@link Level} this chunk belongs to.
     *
     * @return the level this chunk belongs to
     */
    Level getLevel();

    /**
     * Gets the x position of the chunk.
     *
     * @return the x position of the chunk
     */
    int getX();

    /**
     * Gets the z position of the chunk.
     *
     * @return the z position of the chunk
     */
    int getZ();

    /**
     * Gets the {@link BlockState} at the given coordinates.
     *
     * @param x the x position
     * @param y the y position
     * @param z the z position
     * @return the block state at the given coordinates
     */
    BlockState getBlockState(int x, int y, int z);

    /**
     * Sets the {@link BlockState} at the given coordinates.
     *
     * @param x the x position
     * @param y the y position
     * @param z the z position
     * @param state the block state to set
     */
    void setBlockState(int x, int y, int z, BlockState state);

    /**
     * Gets the {@link Biome} at the given coordinates.
     *
     * @param x the x position
     * @param y the y position
     * @param z the z position
     * @return the biome at the given coordinates
     */
    Biome getBiome(int x, int y, int z);

    /**
     * Sets the {@link Biome} at the given coordinates.
     *
     * @param x the x position
     * @param y the y position
     * @param z the z position
     * @param biome the biome to set
     */
    void setBiome(int x, int y, int z, Biome biome);

    /**
     * Gets the block entities within this chunk.
     *
     * @return the block entities within this chunk
     */
    List<NbtMap> getBlockEntities();

    /**
     * Gets all the {@link ChunkSection}s within this chunk.
     *
     * @return all the chunk sections within this chunk
     */
    ChunkSection[] getSections();

    /**
     * Gets a {@link ChunkSection} from the given section y
     * position.
     * <p>
     * This y position represents where the chunk section is
     * in relation to the *section* position, rather than the
     * actual block y position. This can typically be converted
     * from a block y to a section y by doing <b>y >> 4</b>.
     *
     * @param y the section y position
     * @return the chunk section at the given section y position
     */
    ChunkSection getSection(int y);
}
