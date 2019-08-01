package me.egg82.comptests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;
import me.egg82.comptests.tests.generic.ByteTest;
import me.egg82.comptests.tests.ZlibByteArray;
import me.egg82.comptests.tests.ZlibStream;

public class Main {
    public static void main(String[] args) { new Main(args); }

    private Region[] regions = new Region[0];

    private static final DecimalFormat percentFormat = new DecimalFormat("0.###");
    private static final DecimalFormat ratioFormat = new DecimalFormat("0.#####");

    private Main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Must provide the region directory as an argument.");
        }

        File regionDir = new File(args[0]);
        if (!regionDir.exists() || !regionDir.isDirectory()) {
            throw new IllegalArgumentException("Argument provided is not a directory or does not exist.");
        }

        File[] regionFiles = regionDir.listFiles((f, n) -> n.endsWith(".mca") && n.startsWith("r."));
        if (regionFiles == null || regionFiles.length == 0) {
            throw new IllegalArgumentException("No valid regions found in the directory.");
        }

        File jarDirectory;
        try {
            jarDirectory = getJarDirectory();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            return;
        }

        System.out.println("Getting region files and decompressing chunks..");
        List<Region> regionList = new ArrayList<>();
        for (File regionFile : regionFiles) {
            Region region;
            try {
                region = new Region(regionFile);
            } catch (IOException | DataFormatException ex) {
                ex.printStackTrace();
                continue;
            }
            regionList.add(region);
        }
        regions = regionList.toArray(new Region[0]);

        System.out.println("Trying Zlib stream");
        try (
                BufferedWriter ratioOutput = new BufferedWriter(new FileWriter(new File(jarDirectory, "zlib-stream-ratio.txt"), false));
                BufferedWriter compressionTimeOutput = new BufferedWriter(new FileWriter(new File(jarDirectory, "zlib-stream-compression-time.txt"), false));
                BufferedWriter decompressionTimeOutput = new BufferedWriter(new FileWriter(new File(jarDirectory, "zlib-stream-decompression-time.txt"), false))
        ) {
            test(new ZlibStream(), ratioOutput, compressionTimeOutput, decompressionTimeOutput, System.lineSeparator());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("Trying Zlib byte array");
        try (
                BufferedWriter ratioOutput = new BufferedWriter(new FileWriter(new File(jarDirectory, "zlib-bytearray-ratio.txt"), false));
                BufferedWriter compressionTimeOutput = new BufferedWriter(new FileWriter(new File(jarDirectory, "zlib-bytearray-compression-time.txt"), false));
                BufferedWriter decompressionTimeOutput = new BufferedWriter(new FileWriter(new File(jarDirectory, "zlib-bytearray-decompression-time.txt"), false))
        ) {
            test(new ZlibByteArray(), ratioOutput, compressionTimeOutput, decompressionTimeOutput, System.lineSeparator());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void test(ByteTest test, BufferedWriter ratioOutput, BufferedWriter compressionTimeOutput, BufferedWriter decompressionTimeOutput, String lineEnding) {
        double regionRatio = 0.0d;
        long regionCompressionTime = 0L;
        long regionDecompressionTime = 0L;

        for (int i = 0; i < regions.length; i++) {
            double chunkRatio = 0.0d;
            long chunkCompressionTime = 0L;
            long chunkDecompressionTime = 0L;

            for (Chunk chunk : regions[i].getChunks()) {
                try {
                    test.testCompress(chunk.getUncompressedData(), 5);
                    test.testDecompress(test.getCompressedData(chunk.getUncompressedData()), 5);
                    if (test.getCompressionRatio() != -1.0d) {
                        chunkRatio += test.getCompressionRatio();
                        ratioOutput.write(test.getCompressionRatio() + lineEnding);
                    }
                    if (test.getCompressionTime(TimeUnit.NANOSECONDS) != -1L) {
                        chunkCompressionTime += test.getCompressionTime(TimeUnit.MICROSECONDS);
                        compressionTimeOutput.write(test.getCompressionTime(TimeUnit.MICROSECONDS) + lineEnding);
                    }
                    if (test.getDecompressionTime(TimeUnit.NANOSECONDS) != -1L) {
                        chunkDecompressionTime += test.getDecompressionTime(TimeUnit.MICROSECONDS);
                        decompressionTimeOutput.write(test.getDecompressionTime(TimeUnit.MICROSECONDS) + lineEnding);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            regionRatio += chunkRatio / (double) regions[i].getChunks().length;
            regionCompressionTime += chunkCompressionTime / regions[i].getChunks().length;
            regionDecompressionTime += chunkDecompressionTime / regions[i].getChunks().length;

            if (i % 5 == 0 || i == regions.length - 1) {
                System.out.println("Tested region " + i + "/" + (regions.length - 1) + " (" + percentFormat.format(((double) i / (double) (regions.length - 1)) * 100.0d) + "%)");
            }
        }

        System.out.println("Avg. Compression ratio: " + ratioFormat.format(regionRatio / (double) regions.length));
        System.out.println("Avg. Compression time: " + (regionCompressionTime / regions.length) + "us");
        System.out.println("Avg. Decompression time: " + (regionDecompressionTime / regions.length) + "us");
        System.out.println();
    }

    private File getJarDirectory() throws URISyntaxException {
        return new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
    }
}
