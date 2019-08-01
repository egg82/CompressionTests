package me.egg82.comptests;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) { new Main(args); }

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

        for (File regionFile : regionFiles) {
            Region region;
            try {
                region = new Region(regionFile);
            } catch (IOException ex) {
                ex.printStackTrace();
                continue;
            }

            System.out.println("Got region at " + region.getX() + ", " + region.getZ());

            for (Chunk chunk : region.getChunks()) {
                System.out.println("Got chunk at " + chunk.getX() + ", " + chunk.getZ());
            }
        }
    }
}
