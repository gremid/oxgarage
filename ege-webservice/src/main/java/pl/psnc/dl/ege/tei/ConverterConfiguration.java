package pl.psnc.dl.ege.tei;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Logger;

import pl.psnc.dl.ege.types.Conversion;
import pl.psnc.dl.ege.types.DataType;
import pl.psnc.dl.ege.configuration.EGEConstants;

final class ConverterConfiguration
{

	private static final Logger LOGGER = Logger.getLogger(TEIConverter.class.getName());

	public static final String PROFILE_NOT_FOUND_MSG = "Profile not found, setting default profile...";

	public static final String TEI = "TEI";

	public static final String XML_MIME = "text/xml";
	
	public static final String ZIP_MIME = "application/zip";
	
	public static final List<Conversion> CONVERSIONS = new ArrayList<Conversion>();

	public static final String PROFILE_KEY = "pl.psnc.dl.ege.tei.profileNames";

	public static final String IMAGES_KEY = "oxgarage.getImages";

	public static final String FETCHIMAGES_KEY = "oxgarage.getOnlineImages";

	public static final String TEXTONLY_KEY = "oxgarage.textOnly";

        public static final String LANGUAGE_KEY = "oxgarage.lang";


	static {
		File basePath = new File(EGEConstants.TEI_STYLESHEETS, "profiles");
		if (basePath.exists()) {
			for (Format format : Format.values()) {
				StringBuffer sbParams = new StringBuffer();
				sbParams.append("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">");
				sbParams.append("<properties>");
				sbParams.append("<entry key=\"");
				sbParams.append(IMAGES_KEY);
				sbParams.append("\">");
				sbParams.append("true,false");
				sbParams.append("</entry><entry key=\"" + IMAGES_KEY + ".type\">pathBoolean</entry>");
				sbParams.append("<entry key=\"");
				sbParams.append(FETCHIMAGES_KEY);
				sbParams.append("\">");
				sbParams.append("true,false");
				sbParams.append("</entry>");
				sbParams.append("<entry key=\"" + FETCHIMAGES_KEY + ".type\">pathBoolean</entry>");
				sbParams.append("<entry key=\"");
				sbParams.append(LANGUAGE_KEY);
				sbParams.append("\">");
				sbParams.append("en,de,es,fr,it,kr,ja,zh-tw");
				sbParams.append("</entry>");
				sbParams.append("<entry key=\"" + LANGUAGE_KEY + ".type\">array</entry>");
				sbParams.append("<entry key=\"");
				sbParams.append(TEXTONLY_KEY);
				sbParams.append("\">");
				sbParams.append("true,false");
				sbParams.append("</entry>");
				sbParams.append("<entry key=\"" + TEXTONLY_KEY + ".type\">pathBoolean</entry>");
				sbParams.append("<entry key=\"");
				sbParams.append(PROFILE_KEY);
				sbParams.append("\">");

				
				String[] profileDirNames = basePath.list();
				Arrays.sort(profileDirNames);
				List<String> profileNames = new ArrayList<String> (Arrays.asList(profileDirNames));
				if(profileNames.contains(EGEConstants.TEI_PROFILE)) {
					profileNames.remove(EGEConstants.TEI_PROFILE);
					profileNames.add(0, EGEConstants.TEI_PROFILE);
				}
				for (String profileName : profileNames) {
					File profileDir = new File(basePath + File.separator
							+ profileName + File.separator);
					for (String profConv : profileDir.list()) {
						if (profConv.equals(format.getProfile())) {
							sbParams.append(profileName);
							sbParams.append(",");
						}
					}
				}
				if(sbParams.charAt(sbParams.length() - 1)==',') sbParams.deleteCharAt(sbParams.length() - 1);
				sbParams.append("</entry><entry key=\"" + PROFILE_KEY + ".type\">array</entry>");
				sbParams.append("</properties>");
				Conversion caa = new Conversion(
					      new DataType(format.getFormatName(), XML_MIME, format.getInputDescription(), 
								EGEConstants.getType(format.getInputType())), 
					      new DataType(format.getId(), format.getMimeType(), 
							format.getOutputDescription(), EGEConstants.getType(format.getOutputType())),
						sbParams.toString(), format.getVisible(), format.getCost());
				if (format.equals(Format.XLSX)) { }
				else
				    {
					CONVERSIONS.add(caa);					
					LOGGER.fine(() -> "registered type " + format.getMimeType());
				    }
				if (format.equals(Format.XHTML) || format.equals(Format.DOCX) || format.equals(Format.ODT)) {
					Conversion caa2 = new Conversion(
							new DataType(format.getId(), format.getMimeType(), 
								format.getOutputDescription(), EGEConstants.TEXTFAMILY),
							new DataType(format.getFormatName(), XML_MIME, 
								format.getInputDescription(), EGEConstants.TEXTFAMILY), 
							sbParams.toString(), format.getVisible(), format.getCost());
                    LOGGER.fine(() -> "registered alt type " + format.getMimeType());
					CONVERSIONS.add(caa2);
				}
				else if (format.equals(Format.XLSX) ) {
					Conversion caa2 = new Conversion(
							new DataType(format.getId(), format.getMimeType(), 
								format.getOutputDescription(), EGEConstants.SPREADSHEETFAMILY),
							new DataType(format.getFormatName(), XML_MIME, 
								format.getInputDescription(), EGEConstants.SPREADSHEETFAMILY), 
							sbParams.toString(), format.getVisible(), format.getCost());
                    LOGGER.fine(() -> "registered alt type " + format.getMimeType());
					CONVERSIONS.add(caa2);
				}
			}
		}
	}


	/**
	 * Check if profile of conversion for chosen formatId exists.
	 * Returns boolean value of 'true' if profile was found.
	 * 
	 * @param profileName
	 * @param formatId
	 * @return
	 */
	public static boolean checkProfile(String profileName, String formatId)
	{
		if (profileName != null && profileName.equals("")
				|| profileName == null) {
			return false;
		}
		File profile = new File(EGEConstants.TEI_STYLESHEETS, "profiles" + File.separator + profileName + File.separator + formatId + File.separator);
		return profile.exists();
	}


	private ConverterConfiguration()
	{

	}

}
