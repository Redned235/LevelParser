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
    public static final Type<BlockState> BLOCKS = new Type<>(
            4,
            8,
            4096,
            (x, y, z) -> y << 8 | z << 4 | x,
            Palette::serializeBlockState,
            BlockState.AIR
    );

    public static final Type<Biome> BIOMES = new Type<>(
            1,
            2,
            64,
            (x, y, z) -> (y << 2 | z) << 2 | x,
            Palette::serializeBiome,
            Biome.PLAINS
    );

    private final Palette.Type<T> type;
    private final List<T> values;
    private final Map<T, Integer> valueToId = new HashMap<>();

    private int nextId;

    public Palette(Palette.Type<T> type) {
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

    public List<Object> serialize() {
        List<Object> values = new ArrayList<>();
        for (T value : this.values) {
            values.add(this.type.serializer().serialize(value));
        }

        return values;
    }

    public int size() {
        return this.nextId;
    }

    private static NbtMap serializeBlockState(BlockState state) {
        NbtMapBuilder builder = NbtMap.builder()
                .putString("Name", state.getIdentifier());

        NbtMapBuilder propertiesBuilder = NbtMap.builder();
        propertiesBuilder.putAll(state.getProperties());

        builder.putCompound("Properties", propertiesBuilder.build());
        return builder.build();
    }

    private static String serializeBiome(Biome biome) {
        return biome.getIdentifier();
    }

    public record Type<T>(
            int minBitsPerEntry,
            int maxBitsPerEntry,
            int maxSize,
            Indexer indexer,
            Serializer<T> serializer,
            T defaultValue) {

        @FunctionalInterface
        public interface Indexer {
            int index(int x, int y, int z);
        }

        @FunctionalInterface
        public interface Serializer<T> {
            Object serialize(T value);
        }
    }
}
