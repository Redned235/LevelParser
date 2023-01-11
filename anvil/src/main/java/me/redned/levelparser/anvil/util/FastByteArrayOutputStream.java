package me.redned.levelparser.anvil.util;

import java.io.ByteArrayOutputStream;

public class FastByteArrayOutputStream extends ByteArrayOutputStream {

    public FastByteArrayOutputStream(int size) {
        super(size);
    }

    public synchronized byte[] getByteArray() {
        return this.buf;
    }
}
