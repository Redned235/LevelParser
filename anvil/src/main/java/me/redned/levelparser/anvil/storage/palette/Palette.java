package me.redned.levelparser.anvil.storage.palette;

import lombok.Getter;
import me.redned.levelparser.Biome;
import me.redned.levelparser.BlockState;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Palette<T> {
    public static final Type<BlockState, NbtMap> BLOCKS = new Type<>(
            4,
            8,
            4096,
            (x, y, z) -> y << 8 | z << 4 | x,
            Palette::serializeBlockState,
            Palette::deserializeBlockState,
            BlockState.AIR,
            NbtMap.class
    );

    public static final Type<Biome, String> BIOMES = new Type<>(
            1,
            2,
            64,
            (x, y, z) -> (y << 2 | z) << 2 | x,
            Palette::serializeBiome,
            Palette::deserializeBiome,
            Biome.PLAINS,
            String.class
    );

    private final Palette.Type<T, ?> type;
    private final List<T> values;
    private final Map<T, Integer> valueToId = new HashMap<>();

    private int nextId;

    public Palette(Palette.Type<T, ?> type) {
        this.type = type;
        this.values = new ArrayList<>();

        this.idFor(type.defaultValue());
    }

    public int idFor(T value) {
        Integer id = this.valueToId.get(value);
        if (id == null) {
            id = this.nextId++;
            this.values.add(value);
            this.valueToId.put(value, id);
        }

        return id;
    }

    public T valueFor(int id) {
        return this.values.get(id);
    }

    public int size() {
        return this.nextId;
    }

    public <S> List<S> serialize() {
        List<S> values = new ArrayList<>();
        for (T value : this.values) {
            values.add((S) this.type.serializer().serialize(value));
        }

        return values;
    }

    public static <T, S> Palette<T> deserialize(Type<T, S> type, List<S> serializedTag) {
        Palette<T> palette = new Palette<>(type);
        for (S tag : serializedTag) {
            T value = type.deserializer().deserialize(tag);
            palette.idFor(value);
        }

        return palette;
    }

    private static NbtMap serializeBlockState(BlockState state) {
        NbtMapBuilder builder = NbtMap.builder()
                .putString("Name", state.getIdentifier());

        NbtMapBuilder propertiesBuilder = NbtMap.builder();
        propertiesBuilder.putAll(state.getProperties());

        builder.putCompound("Properties", propertiesBuilder.build());
        return builder.build();
    }

    private static BlockState deserializeBlockState(NbtMap tag) {
        String name = tag.getString("Name");
        NbtMap propertiesTag = tag.getCompound("Properties");
        return BlockState.of(name, propertiesTag);
    }

    private static String serializeBiome(Biome biome) {
        return biome.getIdentifier();
    }

    private static Biome deserializeBiome(String identifier) {
        return Biome.of(identifier);
    }

    public record Type<T, S>(
            int minBitsPerEntry,
            int maxBitsPerEntry,
            int maxSize,
            Indexer indexer,
            Serializer<T, S> serializer,
            Deserializer<T, S> deserializer,
            T defaultValue,
            Class<S> storageType) {

        @FunctionalInterface
        public interface Indexer {
            int index(int x, int y, int z);
        }

        @FunctionalInterface
        public interface Serializer<T, S> {
            S serialize(T value);
        }

        @FunctionalInterface
        public interface Deserializer<T, S> {
            T deserialize(S value);
        }
    }
}
