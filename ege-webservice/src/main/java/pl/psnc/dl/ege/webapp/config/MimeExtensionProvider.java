package pl.psnc.dl.ege.webapp.config;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides mime to file extension map.
 * Map parameters are read from .xml configuration file.
 * 
 * @author mariuszs
 *
 */
public final class MimeExtensionProvider {

	private static MimeExtensionProvider instance = null;
	
	private final Map<String,String> fileExtensionsMap;
	
	private MimeExtensionProvider(Map<String,String> fileExtensionsMap){
        this.fileExtensionsMap = fileExtensionsMap;
	}
	
	public static MimeExtensionProvider getInstance(){
		if (instance == null){
            try (InputStream mappingStream = MimeExtensionProvider.class.getResourceAsStream("/file-extensions-map.xml")) {
                final Map<String,String> fileExtensionsMap = new HashMap<>();
                SAXParserFactory.newInstance().newSAXParser().parse(mappingStream, new DefaultHandler() {
                    @Override
                    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
                        fileExtensionsMap.put(attributes.getValue("mime"), attributes.getValue("ext"));
                    }
                });
                instance = new MimeExtensionProvider(fileExtensionsMap);
            } catch (SAXException | IOException | ParserConfigurationException e) {
                throw new RuntimeException(e);
            }

		}
		return instance;
	}

    public String getFileExtension(String mimeType){
        return fileExtensionsMap.getOrDefault(mimeType, "");
    }
}
