package pl.psnc.dl.ege.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
            zipStream.setLevel(ZIP_LEVEL);
            EGEIOUtils.constructZip(source, zipStream, "");
        }
	}

	public static void unzip(InputStream in, File target) throws IOException {
		EGEIOUtils.unzipStream(in, target);
	}
}
