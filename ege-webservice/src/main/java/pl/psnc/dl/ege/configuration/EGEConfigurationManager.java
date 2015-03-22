package pl.psnc.dl.ege.configuration;

import pl.psnc.dl.ege.MultiXslConverter;
import pl.psnc.dl.ege.component.Converter;
import pl.psnc.dl.ege.component.NamedConverter;
import pl.psnc.dl.ege.component.Recognizer;
import pl.psnc.dl.ege.component.Validator;
import pl.psnc.dl.ege.tei.TEIConverter;
import pl.psnc.dl.ege.types.ConversionActionArguments;
import pl.psnc.dl.ege.types.DataType;
import pl.psnc.dl.ege.utils.IOResolver;
import pl.psnc.dl.ege.utils.ZipIOResolver;
import pl.psnc.dl.ege.validator.EGEValidator;
import uk.ac.ox.oucs.oxgarage.oo.OOConverter;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configuration manager performs initialization of standard components: loads
 * JPF based extensions : Validators, Converters, Recognizers.
 * 
 * Implemented as Singleton.
 * 
 * @author mariuszs
 */
public class EGEConfigurationManager {

	private final IOResolver ioResolver = new ZipIOResolver(1);

    private static final EGEConfigurationManager INSTANCE = new EGEConfigurationManager();

	private EGEConfigurationManager() {
	}

	/**
	 * Returns available instance of <code>EGEConfigurationManager</code>.
	 * 
	 * @return instance of manager
	 */
	public static EGEConfigurationManager getInstance()
	{
		return INSTANCE;
	}

	/**
	 * Returns list of all available converters.
	 * 
	 * @return list of converters.
	 */
	public List<Converter> getAvailableConverters()
	{
        final DataType teiP5DataType = new DataType("TEI", "text/xml", "TEI P5 XML Document", EGEConstants.TEXTFAMILY);
        final DataType teiP4DataType = new DataType("P4", "text/xml", "TEI P4 XML Document", EGEConstants.TEXTFAMILY);
        final DataType teiSimpleDataType = new DataType("Simple", "text/xml", "TEI Simple XML Document", EGEConstants.TEXTFAMILY);
        final DataType teiTiteDataType = new DataType("Tite", "text/xml", "TEI Tite XML Document", EGEConstants.TEXTFAMILY);

        final DataType oddDataType = new DataType("ODD", "text/xml", "ODD Document", EGEConstants.TEXTFAMILY);
        final DataType oddcDataType = new DataType("ODDC", "text/xml", "Compiled TEI ODD", EGEConstants.TEXTFAMILY);
        final DataType schematronDataType = new DataType("sch", "text/xml", "Schematron constraints", EGEConstants.TEXTFAMILY);
        final DataType isoSchematronDataType = new DataType("isosch", "text/xml", "ISO Schematron constraints", EGEConstants.TEXTFAMILY);

        final DataType markdownDataType = new DataType("markdown", "text/plain", "Markdown tagging", EGEConstants.TEXTFAMILY);
        final DataType docbookDataType = new DataType("DBK", "text/xml", "DocBook Document", EGEConstants.TEXTFAMILY);
        final DataType verbatimXmlDataType = new DataType("verbatimxml", "text/xml", "VerbatimXML tagging", EGEConstants.TEXTFAMILY);
        final DataType nlmDataType = new DataType("NLM", "text/xml", "National Library of Medicine (NLM) DTD 3.0", EGEConstants.TEXTFAMILY);
        final DataType tcpDataType = new DataType("TCP", "text/xml", "TCP XML Document", EGEConstants.TEXTFAMILY);
        final DataType cocoaDataType = new DataType("cocoa", "text/plain", "Cocoa tagging", EGEConstants.TEXTFAMILY);

        final DataType csvDataType = new DataType("csv", "text/csv", "Comma-Separated Values (.csv)", EGEConstants.SPREADSHEETFAMILY);
        final DataType wordpressFeedDataType = new DataType("wordpress", "text/xml", "Wordpress RSS feed  of blog", EGEConstants.TEXTFAMILY);

        return Arrays.<Converter>asList(
                new NamedConverter("TEI Converter", new TEIConverter()),
                new NamedConverter("OpenOffice Converter", new OOConverter()),
                new NamedConverter("TEI to Simple", new MultiXslConverter(URI.create("teixsl:simple/teitosimple.xsl"), new ConversionActionArguments(teiP5DataType, teiSimpleDataType, true, 11))),
                new NamedConverter("Tite to TEI", new MultiXslConverter(URI.create("teixsl:tite/tite-to-tei.xsl"), new ConversionActionArguments(teiTiteDataType, teiP5DataType, true, 11))),
                new NamedConverter("TEI to NLM", new MultiXslConverter(URI.create("teixsl:nlm/tei_to_nlm.xsl"), new ConversionActionArguments(teiP5DataType, nlmDataType, true, 11))),
                new NamedConverter("ODDC to Schematron", new MultiXslConverter(URI.create("teixsl:odds/extract-sch.xsl"), new ConversionActionArguments(oddcDataType, schematronDataType, true, 11))),
                new NamedConverter("ODDC to ISO Schematron", new MultiXslConverter(URI.create("teixsl:odds/extract-isosch.xsl"), new ConversionActionArguments(oddcDataType, isoSchematronDataType, true, 11))),
                new NamedConverter("TEI to Docbook", new MultiXslConverter(URI.create("teixsl:docbook/teitodocbook.xsl"), new ConversionActionArguments(teiP5DataType, docbookDataType, true, 10))),
                new NamedConverter("Docbook to TEI", new MultiXslConverter(URI.create("teixsl:docbook/docbooktotei.xsl"), new ConversionActionArguments(docbookDataType, teiP5DataType, true, 10))),
                new NamedConverter("P4 to P5", new MultiXslConverter(URI.create("teixsl:profiles/default/p4/from.xsl"), new ConversionActionArguments(teiP4DataType, teiP5DataType, true, 10))),
                new NamedConverter("TCP to P5", new MultiXslConverter(URI.create("teixsl:tcp/tcp2tei.xsl"), new ConversionActionArguments(tcpDataType, teiP5DataType, true, 10))),
                new NamedConverter("ODD to Compiled ODD", new MultiXslConverter(URI.create("teixsl:odds/odd2odd.xsl"), new ConversionActionArguments(oddDataType, oddcDataType, true, 10))),
                new NamedConverter("TEI to VerbatimXML", new MultiXslConverter(URI.create("teixsl:verbatimxml/teitoverbatim.xsl"), new ConversionActionArguments(teiP5DataType, verbatimXmlDataType, true, 11))),
                new NamedConverter("CSV to TEI", new MultiXslConverter(URI.create("teixsl:profiles/default/csv/from.xsl"), new ConversionActionArguments(csvDataType, teiP5DataType, true, 9))),
                new NamedConverter("TEI to CSV", new MultiXslConverter(URI.create("teixsl:profiles/default/csv/to.xsl"), new ConversionActionArguments(teiP5DataType, csvDataType, true, 11))),
                new NamedConverter("MarkDown to TEI", new MultiXslConverter(URI.create("teixsl:profiles/default/markdown/from.xsl"), new ConversionActionArguments(markdownDataType, teiP5DataType, true, 9))),
                new NamedConverter("TEI to MarkDown", new MultiXslConverter(URI.create("teixsl:profiles/default/markdown/to.xsl"), new ConversionActionArguments(teiP5DataType, markdownDataType, true, 11))),
                new NamedConverter("Cocoa to TEI", new MultiXslConverter(URI.create("teixsl:profiles/default/cocoa/from.xsl"), new ConversionActionArguments(cocoaDataType, teiP5DataType, true, 9))),
                new NamedConverter("Wordpress to TEI", new MultiXslConverter(URI.create("teixsl:profiles/default/wordpress/from.xsl"), new ConversionActionArguments(wordpressFeedDataType, teiP5DataType, true, 9)))
        );
	}


	/**
	 * Returns list of all available recognizers.
	 * 
	 * @return list of recognizers.
	 */
	public List<Recognizer> getAvailableRecognizers()
	{
		return Collections.emptyList();
	}


	/**
	 * Returns list of all available validators.
	 * 
	 * @return list of validators.
	 */
	public List<Validator> getAvailableValidators()
	{
        return Arrays.<Validator>asList(new EGEValidator());
	}
	
	/**
	 * Returns standard EGE input/output resolver.
	 * 
	 * @return standard EGE input/output resolver
	 */
	public IOResolver getStandardIOResolver(){
		return ioResolver;
	}

}
