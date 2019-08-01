package me.egg82.comptests;

import java.nio.ByteBuffer;
import java.util.Objects;

public class Chunk {
    private final int x;
    private final int z;

    private byte compressionType;
    private final ByteBuffer compressedData;

    private final int hash;

    public Chunk(int x, int z, byte compressionType, ByteBuffer compressedData) {
        this.x = x;
        this.z = z;

        this.compressionType = compressionType;
        this.compressedData = compressedData;

        this.hash = Objects.hash(x, z);
    }

    public int getX() { return x; }

    public int getZ() { return z; }

    public byte getCompressionType() { return compressionType; }

    public ByteBuffer getCompressedData() { return compressedData; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chunk)) return false;
        Chunk chunk = (Chunk) o;
        return x == chunk.x &&
                z == chunk.z;
    }

    public int hashCode() { return hash; }
}
