package uk.ac.ox.oucs.oxgarage.oo;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;

import pl.psnc.dl.ege.types.DataType;
import pl.psnc.dl.ege.types.Conversion;

/**
 * <p>
 * Configuration for OOConverter
 * </p>
 * 
 * Provides configuration for the OpenOffice Converter, containing path to OpenOffice home directory and possible conversions families
 * 
 * @author Lukas Platinsky
 * 
 */

public class OOConfiguration {

	// list of conversion families
	private static final List<OOConversionsFamily> ConversionFamilies;

	// hashmap of extensions for fast look-up
	private static HashMap<String, String> extensions;

	// list of all possible conversions
	public static List<Conversion> CONVERSIONS;

	static {
		ConversionFamilies = OOConversionsFamily.getFamilies();		
		getConversions();
		constructExtensionsMap();
	}

	public static void getConversions () {
		CONVERSIONS = new ArrayList<Conversion>();
		for (OOConversionsFamily family : ConversionFamilies) {
			family.addConversions(CONVERSIONS);
		}
	}

	public static String getExtension(DataType dataType) {
		return extensions.get(dataType.getFormat() + File.separator + dataType.getMimeType());
	}

	private static void constructExtensionsMap() {
		extensions = new HashMap<String, String>();
		for (OOConversionsFamily family : ConversionFamilies) {
			family.addExtensions(extensions);
		}
	}
}
