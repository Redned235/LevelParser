package me.redned.levelparser.io;

import me.redned.levelparser.Level;

import java.io.IOException;
import java.nio.file.Path;

public interface LevelWriter<T extends Level> {

    void write(T level, Path path) throws IOException;
}
