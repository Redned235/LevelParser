package me.redned.levelparser;

import me.redned.levelparser.io.LevelReader;
import me.redned.levelparser.io.LevelWriter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Handles parsing levels.
 *
 * @param <T> the level type
 */
public final class LevelParser<T extends Level> {
    private final LevelReader<T> reader;
    private final LevelWriter<T> writer;

    private final Path input;
    private final Path output;

    private LevelParser(LevelReader<T> reader, LevelWriter<T> writer, Path input, Path output) {
        this.reader = reader;
        this.writer = writer;
        this.input = input;
        this.output = output;
    }

    /**
     * Reads the level from the given input location using
     * the specified reader.
     *
     * @param minHeight the minimum height of the level
     * @param maxHeight the maximum height of the level
     * @return the level from the given input location
     * @throws RuntimeException if the level could not be loaded
     */
    public T readLevel(int minHeight, int maxHeight) {
        try {
            return this.reader.read(this.input, minHeight, maxHeight);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read level from location " + this.input, ex);
        }
    }

    /**
     * Writes the given level to the output location using the
     * specific writer.
     *
     * @param level the level to write
     * @throws RuntimeException if the level could not be written
     */
    public void writeLevel(T level) {
        try {
            this.writer.write(level, this.output);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write level to location " + this.output, ex);
        }
    }

    /**
     * Creates a new {@link Builder} for parsing levels.
     *
     * @return the builder
     * @param <T> the level type
     */
    public static <T extends Level> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T extends Level> {
        private LevelReader<T> reader;
        private LevelWriter<T> writer;

        private Path input;
        private Path output;

        private Builder() {
        }

        public Builder<T> reader(LevelReader<T> reader) {
            this.reader = reader;
            return this;
        }

        public Builder<T> writer(LevelWriter<T> writer) {
            this.writer = writer;
            return this;
        }

        public Builder<T> input(Path input) {
            this.input = input;
            return this;
        }

        public Builder<T> output(Path output) {
            this.output = output;
            return this;
        }

        public LevelParser<T> build() {
            return new LevelParser<>(this.reader, this.writer, this.input, this.output);
        }
    }
}
