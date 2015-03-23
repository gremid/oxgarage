package pl.psnc.dl.ege.configuration;

import java.io.File;
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
}