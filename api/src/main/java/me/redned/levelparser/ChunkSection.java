package me.redned.levelparser;

/**
 * Represents a 16x16x16 cube of a {@link Chunk}.
 */
public interface ChunkSection {

    /**
     * Gets the section y position.
     *
     * @return the section y position
     */
    int getY();

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
}
