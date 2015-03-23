package pl.psnc.dl.ege;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

/**
 * Additional useful static data.
 * 
 * @author mariuszs
 */
public final class EGEConstants {

    public static final String LOCALE = System.getProperty("oxgarage.locale", "en");

    public static final File TEI_STYLESHEETS = new File(System.getProperty("oxgarage.tei.stylesheets.dir", "/usr/share/xml/tei/stylesheet"));

    public static final Optional<File> TEI_CONFIG_DIRECTORY = Optional.ofNullable(System.getProperty("oxgarage.tei.config.dir")).map(File::new);

    public static final String TEI_PROFILE = System.getProperty("oxgarage.tei.profile", "default");

    public static final File OPEN_OFFICE_HOME = new File(System.getProperty("oxgarage.oo.home", "/usr/ib/libreoffice"));

	// name for document family consisting of text documents
	public static final String TEXTFAMILY = "Documents";
	public static final String TEXTFAMILYCODE = "text";

	// name for document family consisting of spreadsheet documents
	public static final String SPREADSHEETFAMILY = "Spreadsheets";
	public static final String SPREADSHEETFAMILYCODE = "spreadsheet";

	// name for document family consisting of presentation documents
	public static final String PRESENTATIONFAMILY = "Presentations";
	public static final String PRESENTATIONFAMILYCODE = "presentation";

	// default name for documents from unrecognized family
	public static final String DEFAULTFAMILY = "Other documents";

    public static File tempDir() {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"), "oxgarage");
        if (!tempDir.isDirectory() && !tempDir.mkdirs()) {
            throw new IllegalStateException(tempDir.toString());
        }
        return tempDir;
    }

	/**
	 * Returns appropriate name of text family based on its code name
	 */
	public static String getType(String typeCode) {
		if(typeCode.equals(TEXTFAMILYCODE))         return TEXTFAMILY;
		if(typeCode.equals(SPREADSHEETFAMILYCODE))  return SPREADSHEETFAMILY;
		if(typeCode.equals(PRESENTATIONFAMILYCODE)) return PRESENTATIONFAMILY;
		return DEFAULTFAMILY;
	}

    /**
     * Deletes a directory.
     *
     * @param dir
     *            The directory to delete
     * @return Returns true on success.
     */
    public static void deleteDirectory(File dir) throws IOException {
if (!dir.isDirectory()) {
throw new IllegalArgumentException(dir.toString());
}
Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc == null) {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        } else {
            throw exc;
        }
    }
});
}
}