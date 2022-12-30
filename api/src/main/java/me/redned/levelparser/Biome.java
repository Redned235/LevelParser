package me.redned.levelparser;

/**
 * Represents a biome.
 */
public class Biome {
    public static final Biome PLAINS = Biome.of("minecraft:plains");

    private final String identifier;

    private Biome(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the identifier of the biome.
     *
     * @return the identifier of the biome
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Creates a new biome with the given identifier.
     *
     * @param identifier the identifier of the biome
     * @return a new biome with the given identifier
     */
    public static Biome of(String identifier) {
        return new Biome(identifier);
    }
}
