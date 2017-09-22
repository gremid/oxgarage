package pl.psnc.dl.ege.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * <p>
 * Recovers data from ZIP input/output streams.
 * </p>
 * 
 * @author mariuszs
 */
public class ZipStreams {

	private static final int ZIP_LEVEL = Math.min(Math.max(0, Integer.parseInt(System.getProperty("oxgarage.zip.level", "1"))), 9);

	private ZipStreams() {
	}

	public static void zip(File source, OutputStream out) throws IOException {
        try (ZipOutputStream zipStream = new ZipOutputStream(new BufferedOutputStream(out))) {
            zipTree(source.toPath(), zipStream);
        }
	}

    public static void zipTree(Path start, ZipOutputStream zipStream) throws IOException {
        zipStream.setLevel(ZIP_LEVEL);
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                zipStream.putNextEntry(new ZipEntry(start.relativize(path).toString()));
                Files.copy(path, zipStream);
                return FileVisitResult.CONTINUE;
            }

        });
    }

    public static void unzip(InputStream in, File target) throws IOException {
        if (!target.isDirectory() && !target.mkdirs()) {
            throw new IllegalArgumentException(target.toString());
        }

        try (ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(in))) {
            while (true) {
                final ZipEntry entry = zipIn.getNextEntry();
                if (entry == null) {
                    break;
                }
                if (entry.isDirectory()) {
                    // if it is a directory, create it
                    final File dir = new File(target, entry.getName());
                    if (!dir.exists() && !dir.mkdirs()) {
                        throw new IllegalStateException(dir.toString());
                    }
                } else {
                    final File file = new File(target, entry.getName());

                    // create directories if necessary
                    final File parentFile = file.getParentFile();
                    if (!parentFile.isDirectory() && !parentFile.mkdirs()) {
                        throw new IllegalStateException(parentFile.toString());
                    }

                    Files.copy(zipIn, file.toPath());
                }
            }
        }
    }

	public static boolean hasSingleFileEntry(File zipFile) throws IOException {
		try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(zipFile))) {
			int count = 0;
            while (true) {
                final ZipEntry zipEntry = zipStream.getNextEntry();
                if (zipEntry == null) {
                    break;
                }
                if (zipEntry.isDirectory()) {
                    continue;
                }
                if (++count > 1) {
                    return false;
                }
            }
            return (count == 1);
		}
	}
}
