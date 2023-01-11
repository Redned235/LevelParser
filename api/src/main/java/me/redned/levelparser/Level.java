package me.redned.levelparser;

/**
 * Represents a Minecraft level.
 */
public interface Level {

    /**
     * Gets whether a {@link Chunk} exists at the given chunk coordinates.
     *
     * @param x the chunk x
     * @param z the chunk z
     * @return whether a chunk exists at the current chunk coordinates
     */
    boolean chunkExists(int x, int z);

    /**
     * Gets the {@link Chunk} at the given chunk coordinates.
     * <p>
     * Note that if no chunk exists, this will create an empty one.
     * If you would like to ensure that no chunk exists at the given
     * coordinates, please check {@link #chunkExists(int, int)} beforehand.
     *
     * @param x the chunk x
     * @param z the chunk z
     * @return the chunk at the given chunk coordinates
     */
    Chunk getChunk(int x, int z);

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
     * Gets the minimum height of the level.
     *
     * @return the minimum height of the level
     */
    int getMinHeight();

    /**
     * Gets the maximum height of the level.
     *
     * @return the maximum height of the level
     */
    int getMaxHeight();
}
