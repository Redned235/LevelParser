package me.redned.levelparser;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a specific state of a block.
 */
public class BlockState {
    public static final BlockState AIR = BlockState.of("minecraft:air");

    private final String identifier;
    private final Map<String, Object> properties;

    private BlockState(String identifier) {
        this(identifier, new HashMap<>());
    }

    private BlockState(String identifier, Map<String, Object> properties) {
        this.identifier = identifier;
        this.properties = properties;
    }

    /**
     * Gets the identifier of the state.
     *
     * @return the identifier of the state
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Gets the properties of this block state.
     *
     * @return the properties of this block state
     */
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockState that = (BlockState) o;
        return Objects.equals(this.identifier, that.identifier) && Objects.equals(this.properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.identifier, this.properties);
    }

    /**
     * Creates a block state for the given identifier.
     *
     * @param identifier the identifier
     * @return a new block state
     */
    public static BlockState of(String identifier) {
        return new BlockState(identifier);
    }

    /**
     * Creates a block state for the given identifier
     * and properties.
     *
     * @param identifier the identifier
     * @param properties the properties
     * @return a new block state
     */
    public static BlockState of(String identifier, Map<String, Object> properties) {
        return new BlockState(identifier, properties);
    }
}
