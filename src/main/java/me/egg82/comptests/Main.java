package me.egg82.comptests;

import com.github.luben.zstd.ZstdException;
import java.io.*;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;
import me.egg82.comptests.tests.generic.ByteTest;
import me.egg82.comptests.tests.lz4.LZ4Stream;
import me.egg82.comptests.tests.zlib.*;
import me.egg82.comptests.tests.zstd.*;

public class Main {
    public static void main(String[] args) { new Main(args); }

    private Region[] regions = new Region[0];

    private static final DecimalFormat percentFormat = new DecimalFormat("0.##");
    private static final DecimalFormat ratioFormat = new DecimalFormat("0.#####");

    private boolean doVerification = false;
    private boolean doChunkDump = false;

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

        if (args.length >= 2) {
            String[] flags = Arrays.copyOfRange(args, 1, args.length);
            if (contains(flags, "--verify")) {
                doVerification = true;
            }
            if (contains(flags, "--dump")) {
                doChunkDump = true;
            }
        }

        File jarDirectory;
        try {
            jarDirectory = getJarDirectory();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            return;
        }

        System.out.println();
        System.out.println("Getting region files and decompressing chunks..");
        List<Region> regionList = new ArrayList<>();
        for (int i = 0; i < regionFiles.length; i++) {
            Region region;
            try {
                region = new Region(regionFiles[i]);
            } catch (IOException | DataFormatException ex) {
                ex.printStackTrace();
                continue;
            }
            regionList.add(region);

            if (i % 5 == 0 || i == regionFiles.length - 1) {
                System.out.print("Got region " + i + "/" + (regionFiles.length - 1) + " (" + percentFormat.format(((double) i / (double) (regionFiles.length - 1)) * 100.0d) + "%)   \r");
            }
        }
        regions = regionList.toArray(new Region[0]);
        System.out.println();
        System.out.println();

        if (doChunkDump) {
            System.out.println("Dumping raw chunks..");
            File rawChunksDir = new File(regionDir, "rawchunks");
            rawChunksDir.mkdirs();
            dump(rawChunksDir);
            return;
        }

        System.out.println("Getting dictionaries..");
        byte[] zlibDict;
        byte[] zstdDict;
        try {
            zlibDict = toBytes(getClass().getClassLoader().getResourceAsStream("paper.zlib.dict"));
            zstdDict = toBytes(getClass().getClassLoader().getResourceAsStream("paper.zstd.dict"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        System.out.println();

        if (doVerification) {
            System.out.println("Verifying Zlib stream");
            verify(new ZlibStream());

            System.out.println("Verifying Zlib stream (with dictionary)");
            verify(new ZlibStreamDict(zlibDict));

            System.out.println("Verifying Zlib byte array");
            verify(new ZlibByteArray());

            System.out.println("Verifying Zlib byte array (with dict)");
            verify(new ZlibByteArrayDict(zlibDict));

            System.out.println("Verifying Zlib byte array (with dict)");
            verify(new ZlibByteArrayDict(zlibDict));

            System.out.println("Verifying Zlib direct ByteBuffer");
            verify(new ZlibDirectByteBuffer());

            System.out.println("Verifying Zlib direct ByteBuffer (with dict)");
            verify(new ZlibDirectByteBufferDict(zlibDict));

            System.out.println("Verifying LZ4 stream");
            verify(new LZ4Stream());

            System.out.println("Verifying Zstd stream");
            verify(new ZstdStream());

            System.out.println("Verifying Zstd stream (with dict)");
            verify(new ZstdStreamDict(zstdDict));

            System.out.println("Verifying Zstd byte array");
            verify(new ZstdByteArray());

            System.out.println("Verifying Zstd byte array (with dict)");
            verify(new ZstdByteArrayDict(zstdDict));

            System.out.println("Verifying Zstd direct ByteBuffer");
            verify(new ZstdDirectByteBuffer());

            System.out.println("Verifying Zstd direct ByteBuffer (with dict)");
            verify(new ZstdDirectByteBufferDict(zstdDict));

            return;
        }

        System.out.println("Trying Zlib stream");
        testWithOutput(new ZlibStream(), jarDirectory, "zlib-stream");

        System.out.println("Trying Zlib stream (with dictionary)");
        testWithOutput(new ZlibStreamDict(zlibDict), jarDirectory, "zlib-stream-dict");

        System.out.println("Trying Zlib byte array");
        testWithOutput(new ZlibByteArray(), jarDirectory, "zlib-bytearray");

        System.out.println("Trying Zlib byte array (with dict)");
        testWithOutput(new ZlibByteArrayDict(zlibDict), jarDirectory, "zlib-bytearray-dict");

        System.out.println("Trying Zlib direct ByteBuffer");
        testWithOutput(new ZlibDirectByteBuffer(), jarDirectory, "zlib-bytebuffer");

        System.out.println("Trying Zlib direct ByteBuffer (with dict)");
        testWithOutput(new ZlibDirectByteBufferDict(zlibDict), jarDirectory, "zlib-bytebuffer-dict");

        System.out.println("Trying LZ4 stream");
        testWithOutput(new LZ4Stream(), jarDirectory, "lz4-stream");

        System.out.println("Trying Zstd stream");
        testWithOutput(new ZstdStream(), jarDirectory, "zstd-stream");

        System.out.println("Trying Zstd stream (with dict)");
        testWithOutput(new ZstdStreamDict(zstdDict), jarDirectory, "zstd-stream-dict");

        System.out.println("Trying Zstd byte array");
        testWithOutput(new ZstdByteArray(), jarDirectory, "zstd-bytearray");

        System.out.println("Trying Zstd byte array (with dict)");
        testWithOutput(new ZstdByteArrayDict(zstdDict), jarDirectory, "zstd-bytearray-dict");

        System.out.println("Trying Zstd direct ByteBuffer");
        testWithOutput(new ZstdDirectByteBuffer(), jarDirectory, "zstd-bytebuffer");

        System.out.println("Trying Zstd direct ByteBuffer (with dict)");
        testWithOutput(new ZstdDirectByteBufferDict(zstdDict), jarDirectory, "zstd-bytebuffer-dict");
    }

    private void dump(File chunksDir) {
        for (int i = 0; i < regions.length; i++) {
            for (Chunk chunk : regions[i].getChunks()) {
                try (FileOutputStream outputStream = new FileOutputStream(new File(chunksDir, "chunk." + chunk.getX() + "." + chunk.getZ() + ".taco"))) {
                    outputStream.write(chunk.getUncompressedData());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            if (i % 5 == 0 || i == regions.length - 1) {
                System.out.print("Dumped region " + i + "/" + (regions.length - 1) + " (" + percentFormat.format(((double) i / (double) (regions.length - 1)) * 100.0d) + "%)   \r");
            }
        }
        System.out.println();
    }

    private void verify(ByteTest test) {
        for (int i = 0; i < regions.length; i++) {
            for (Chunk chunk : regions[i].getChunks()) {
                try {
                    byte[] compressed = test.getCompressedData(chunk.getUncompressedData());
                    byte[] uncompressed = test.getDecompressedData(compressed);
                    if (!Arrays.equals(chunk.getUncompressedData(), uncompressed)) {
                        System.out.println();
                        System.err.println("Verification failed!");
                        System.out.println();
                        return;
                    }
                } catch (IOException | ZstdException ex) {
                    System.out.println();
                    ex.printStackTrace();
                    System.out.println();
                    return;
                }
            }

            if (i % 5 == 0 || i == regions.length - 1) {
                System.out.print("Verified region " + i + "/" + (regions.length - 1) + " (" + percentFormat.format(((double) i / (double) (regions.length - 1)) * 100.0d) + "%)   \r");
            }
        }
        System.out.println();
        System.out.println("Verification successful");
        System.out.println();
    }

    private void testWithOutput(ByteTest test, File jarDirectory, String partialFileName) {
        try (
                BufferedWriter ratioOutput = new BufferedWriter(new FileWriter(new File(jarDirectory, partialFileName + "-ratio.txt"), false));
                BufferedWriter compressionTimeOutput = new BufferedWriter(new FileWriter(new File(jarDirectory, partialFileName + "-compression-time.txt"), false));
                BufferedWriter decompressionTimeOutput = new BufferedWriter(new FileWriter(new File(jarDirectory, partialFileName + "-decompression-time.txt"), false))
        ) {
            test(test, ratioOutput, compressionTimeOutput, decompressionTimeOutput, System.lineSeparator());
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
                    test.testCompress(chunk.getUncompressedData(), 3);
                    test.testDecompress(test.getCompressedData(chunk.getUncompressedData()), 3);
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
                System.out.print("Tested region " + i + "/" + (regions.length - 1) + " (" + percentFormat.format(((double) i / (double) (regions.length - 1)) * 100.0d) + "%)   \r");
            }
        }

        System.out.println();
        System.out.println("Avg. Compression ratio: " + ratioFormat.format(regionRatio / (double) regions.length));
        System.out.println("Avg. Compression time: " + (regionCompressionTime / regions.length) + "us");
        System.out.println("Avg. Decompression time: " + (regionDecompressionTime / regions.length) + "us");
        System.out.println();
    }

    private File getJarDirectory() throws URISyntaxException {
        return new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
    }

    private static byte[] toBytes(InputStream inStream) throws IOException {
        if (inStream == null) {
            throw new IllegalArgumentException("inStream cannot be null.");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(inStream.available());
        byte[] buffer = new byte[1024 * 64];

        int bytesRead;
        while ((bytesRead = inStream.read(buffer)) > 0) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        return out.toByteArray();
    }

    private static boolean contains(String[] flags, String arg) {
        for (String flag : flags) {
            if (flag.equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }
}
