package me.egg82.comptests;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class Chunk {
    private final int x;
    private final int z;

    private byte compressionType;
    private final byte[] uncompressedData;

    private final int hash;

    private final Inflater zlibInflater = new Inflater();
    private final byte[] decompressionBuffer = new byte[1024 * 64];

    public Chunk(int x, int z, byte compressionType, byte[] compressedData) throws DataFormatException, IOException {
        this.x = x;
        this.z = z;

        this.compressionType = compressionType;
        this.uncompressedData = inflate(compressedData);

        this.hash = Objects.hash(x, z);
    }

    public int getX() { return x; }

    public int getZ() { return z; }

    public byte getCompressionType() { return compressionType; }

    public byte[] getUncompressedData() { return uncompressedData; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chunk)) return false;
        Chunk chunk = (Chunk) o;
        return x == chunk.x &&
                z == chunk.z;
    }

    public int hashCode() { return hash; }

    private byte[] inflate(byte[] input) throws DataFormatException, IOException {
        byte[] retVal;
        ByteBuffer outBuf;
        int power = 1;
        int totalBytes = 0;
        do {
            boolean resize = false;
            outBuf = ByteBuffer.allocateDirect(1024 * 64 * power);

            zlibInflater.setInput(input, 0, input.length);
            int decompressedBytes;
            while (!zlibInflater.finished()) {
                decompressedBytes = zlibInflater.inflate(decompressionBuffer);
                if (decompressedBytes == 0) {
                    if (zlibInflater.needsDictionary()) {
                        throw new IOException("Inflater needs dictionary (not vanilla chunks)");
                    } else if (zlibInflater.needsInput()) {
                        throw new IOException("Invalid compressed chunk data");
                    }
                }
                try {
                    outBuf.put(decompressionBuffer, 0, decompressedBytes);
                    totalBytes += decompressedBytes;
                } catch (BufferOverflowException ignored) {
                    resize = true;
                    totalBytes = 0;
                    break;
                }
            }
            zlibInflater.reset();

            if (!resize) {
                break;
            }
            power++;
        } while (true);
        retVal = new byte[totalBytes];
        outBuf.rewind();
        outBuf.get(retVal);
        return retVal;
    }
}
