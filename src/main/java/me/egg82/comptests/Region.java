package me.egg82.comptests;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.zip.DataFormatException;

public class Region {
    private final Chunk[] chunks;

    private final boolean[] sectors;
    private final int[] offsets = new int[1024];
    private final int[] timestamps = new int[2014];

    private final int regionX;
    private final int regionZ;

    private final int hash;

    public Region(File regionFile) throws IOException {
        int chunkXOffset;
        int chunkZOffset;

        String[] name = regionFile.getName().split("\\.");
        try {
            regionX = Integer.parseInt(name[1]);
            regionZ = Integer.parseInt(name[2]);
            chunkXOffset = regionX * 32;
            chunkZOffset = regionZ * 32;
        } catch (NumberFormatException ex) {
            throw new IOException("Region file at \"" + regionFile.getAbsolutePath() + "\" has an invalid name.");
        }

        long regionLength = regionFile.length();

        // Total header size is 8192
        if (regionLength < 8192L) {
            throw new IOException("Region file at \"" + regionFile.getAbsolutePath() + "\" has invalid header length.");
        }

        // Make sure we actually have the "locations" header
        if ((regionLength & 4095L) != 0L) {
            throw new IOException("Region file at \"" + regionFile.getAbsolutePath() + "\" has invalid header.");
        }

        sectors = new boolean[(int) (regionLength / 4096L)];
        if (sectors.length > 2) {
            for (int i = 2; i < sectors.length; i++) {
                sectors[i] = true;
            }
        }

        try (
                RandomAccessFile r = new RandomAccessFile(regionFile, "r");
                FileChannel fileIn = r.getChannel()
        ) {
            MappedByteBuffer fileBuffer = fileIn.map(FileChannel.MapMode.READ_ONLY, 0, fileIn.size());
            fileBuffer.load();
            fileBuffer.rewind();

            IntBuffer header = getRegionHeader(fileBuffer);

            // Locations, Chunks
            Set<Chunk> chunkSet = new LinkedHashSet<>();
            for (int i = 0; i < 1024; i++) {
                // Locations
                int offset = header.get();
                offsets[i] = offset;
                int length = offset & 255;
                if (length == 255 && (offset >> 8) <= sectors.length) {
                    // Get length from chunk header
                    fileBuffer.position((offset >> 8) * 4096);
                    length = (fileBuffer.getInt() + 4) / 4096 + 1;
                }
                if (offset > 0 && (offset >> 8) > 1 && (offset >> 8) + length <= sectors.length) {
                    for (int j = 0; j < length; j++) {
                        sectors[(offset >> 8) + j] = false;
                    }
                } else if (length > 0) {
                    System.err.println("Invalid chunk: (" + (i % 32) + ", " + (i / 32) + ") Offset: " + (offset >> 8) + " Length: " + length + " runs off end file. " + regionFile);
                    continue;
                }

                // Chunks
                if (offset > 0 && (offset >> 8) > 1 && !sectors[offset >> 8]) {
                    fileBuffer.position((offset >> 8) * 4096);
                    length = fileBuffer.getInt();

                    byte compressionType = fileBuffer.get();
                    byte[] compressedData = new byte[length - 1];
                    fileBuffer.get(compressedData);
                    chunkSet.add(new Chunk(chunkXOffset + (i & 31), chunkZOffset + (i >>> 5), compressionType, compressedData));
                }
            }
            chunks = chunkSet.toArray(new Chunk[0]);

            // Timestamps
            for (int i = 0; i < 1024; i++) {
                int timestamp = header.get();
                if (offsets[i] != 0) {
                    timestamps[i] = timestamp;
                }
            }
        }

        hash = Objects.hash(regionX, regionZ);
    }

    public Chunk[] getChunks() { return chunks; }

    public int getX() { return regionX; }

    public int getZ() { return regionZ; }

    public int getTimestamp(Chunk chunk) { return timestamps[getChunkInt(chunk.getX(), chunk.getZ())]; }

    public int getTimestamp(int chunkX, int chunkZ) { return timestamps[getChunkInt(chunkX, chunkZ)]; }

    public boolean hasChunk(int chunkX, int chunkZ) { return !sectors[getChunkInt(chunkX, chunkZ)]; }

    private IntBuffer getRegionHeader(MappedByteBuffer region) {
        ByteBuffer byteHeader = ByteBuffer.allocate(8192);
        while (byteHeader.hasRemaining()) {
            byteHeader.put(region.get());
        }
        byteHeader.clear();
        return byteHeader.asIntBuffer();
    }

    private int getChunkInt(int chunkX, int chunkZ) { return (chunkX & 31) + (chunkZ & 31) * 32; }

    private int getOffset(Chunk chunk) { return offsets[getChunkInt(chunk.getX(), chunk.getZ())]; }

    private int getOffset(int chunkX, int chunkZ) { return offsets[getChunkInt(chunkX, chunkZ)]; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Region)) return false;
        Region region = (Region) o;
        return regionX == region.regionX &&
                regionZ == region.regionZ;
    }

    public int hashCode() { return hash; }
}
