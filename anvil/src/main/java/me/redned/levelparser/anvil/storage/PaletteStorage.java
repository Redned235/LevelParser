package me.redned.levelparser.anvil.storage;

import me.redned.levelparser.anvil.storage.palette.Palette;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;

import java.util.Collections;
import java.util.List;

public class PaletteStorage<T> {
    private final Palette<T> palette;
    private BitStorage storage;

    public PaletteStorage(Palette.Type<T, ?> type) {
        this(new Palette<>(type));
    }

    private PaletteStorage(Palette<T> palette) {
        this(palette, null);
    }

    private PaletteStorage(Palette<T> palette, long[] data) {
        this.palette = palette;
        this.storage = new BitStorage(
                Math.max(palette.getType().minBitsPerEntry(), ceillog2(this.palette.size())),
                palette.getType().maxSize(),
                i -> data == null ? new long[i] : data
        );
    }

    public T get(int x, int y, int z) {
        int id = this.storage.get(this.index(x, y, z));
        return this.palette.valueFor(id);
    }

    public void set(int x, int y, int z, T value) {
        int oldSize = this.palette.size();
        int id = this.palette.idFor(value);
        if (oldSize != this.palette.size()) {
            this.resize();
            id = this.palette.idFor(value);
        }

        int index = this.index(x, y, z);
        this.storage.set(index, id);
    }

    public NbtMap serialize() {
        List<Object> entries = this.palette.serialize();
        if (entries.isEmpty()) {
            Object defaultValue = this.palette.getType().serializer().serialize(
                    this.palette.getType().defaultValue()
            );

            return NbtMap.builder()
                    .putList("palette", NbtType.byClass(defaultValue.getClass()), (List) Collections.singletonList(defaultValue))
                    .putLongArray("data", this.storage.getRaw())
                    .build();
        }

        Class<Object> type = (Class<Object>) entries.get(0).getClass();
        NbtMapBuilder builder = NbtMap.builder()
                .putList("palette", NbtType.byClass(type), entries);

        if (entries.size() > 1) {
            builder.putLongArray("data", this.storage.getRaw());
        }

        return builder.build();
    }

    public static <T, S> PaletteStorage<T> deserialize(Palette.Type<T, S> type, NbtMap tag) {
        List<S> types = tag.getList("palette", NbtType.byClass(type.storageType()));

        long[] data = tag.getLongArray("data");
        Palette<T> palette = Palette.deserialize(type, types);
        PaletteStorage<T> storage = new PaletteStorage<>(palette, data.length == 0 ? null : data);
        storage.resize(); // Resize to ensure data is in the proper place

        return storage;
    }

    private void resize() {
        BitStorage oldStorage = this.storage;
        if (oldStorage.getBits() != Math.max(this.palette.getType().minBitsPerEntry(), ceillog2(this.palette.size()))) {
            this.storage = new BitStorage(
                    Math.max(this.palette.getType().minBitsPerEntry(), ceillog2(this.palette.size())),
                    this.palette.getType().maxSize()
            );
            for (int i = 0; i < this.palette.getType().maxSize(); i++) {
                this.storage.set(i, this.palette.idFor(this.palette.valueFor(oldStorage.get(i))));
            }
        }
    }

    public static int ceillog2(int i) {
        return i > 0 ? 32 - Integer.numberOfLeadingZeros(i - 1) : 0;
    }

    private int index(int x, int y, int z) {
        return this.palette.getType().indexer().index(x, y, z);
    }
}
