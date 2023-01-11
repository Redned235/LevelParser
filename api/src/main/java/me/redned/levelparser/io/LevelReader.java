package me.redned.levelparser.io;

import me.redned.levelparser.Level;

import java.io.IOException;
import java.nio.file.Path;

public interface LevelReader<T extends Level> {

    T read(Path path, int minHeight, int maxHeight) throws IOException;
}
