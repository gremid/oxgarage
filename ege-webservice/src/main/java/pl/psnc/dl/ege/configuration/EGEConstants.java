package pl.psnc.dl.ege.configuration;

import java.io.File;
import java.util.Properties;

/**
 * Additional useful static data.
 * 
 * @author mariuszs
 */
public final class EGEConstants {
    private static final Properties oxgProps = new Properties();

        /**
	 * EGE temporary files directory
	 */
    public static final String TEIROOT = oxgProps.getProperty("TEI","/usr/share/xml/tei/");
    public static final String OpenOfficeConfig = oxgProps.getProperty("OpenOfficeConfig","/usr/lib/libreoffice/");
    public static final String DEFAULT_LOCALE = oxgProps.getProperty("defaultLocale","en"); 
    public static final String DEFAULT_PROFILE = oxgProps.getProperty("defaultProfile","default"); 	
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