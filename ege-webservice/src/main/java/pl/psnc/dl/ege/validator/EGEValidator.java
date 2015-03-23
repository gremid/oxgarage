package pl.psnc.dl.ege.validator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.JDOMException;
import org.xml.sax.SAXParseException;

import pl.psnc.dl.ege.Validator;
import pl.psnc.dl.ege.EGEException;
import pl.psnc.dl.ege.ValidatorException;
import pl.psnc.dl.ege.types.DataType;
import pl.psnc.dl.ege.types.ValidationResult;
import pl.psnc.dl.ege.validator.xml.XmlValidator;

/**
 * <p>Default EGE implementation of Validator interface.</p>
 * Provides validation of XML data.
 * 
 * @author mariuszs
 *
 */
public class EGEValidator
	implements Validator
{
	
	private static final Logger LOGGER = Logger.getLogger(EGEValidator.class.getName());
	
	private static final XmlValidatorsProvider provider = XmlValidatorsProvider
			.getInstance();

	
	public List<DataType> getSupportedValidationTypes()
	{
		return provider.getSupportedDataTypes();
	}


	public ValidationResult validate(InputStream inputData,
			DataType inputDataType)
		throws IOException, ValidatorException, EGEException
	{	
		checkIfSupported(inputDataType);
		XmlValidator validator = (XmlValidator)provider.getValidator(inputDataType);
		try {
			StandardErrorHandler seh = new StandardErrorHandler();
			validator.validateXml(inputData,seh);
			return seh.getValidationResult();
		}
		catch (SAXParseException ex) {
			return new ValidationResult(ValidationResult.Status.FATAL,
					"Error in line (" + ex.getLineNumber() + "), column  ("
							+ ex.getColumnNumber() + ") : " + ex.getMessage());
		}
		catch (JDOMException ex){
			return new ValidationResult(ValidationResult.Status.FATAL,
				"Unexpected JDOM parse error occured : " + ex.getMessage());
		}
		catch (FileNotFoundException ex){
			return new ValidationResult(ValidationResult.Status.FATAL,
				"Probably because relative (not absolute) reference to a resource :" + ex.getMessage());
		}
		catch(IOException ex){
			throw ex;
		}
		catch (Exception ex) {
			LOGGER.log(Level.SEVERE, ex, ex::getMessage);
			ValidatorException ve = new ValidatorException(ex.getMessage());
			ve.setStackTrace(ex.getStackTrace());
			throw ve;
		}

	}

	
	private void checkIfSupported(DataType dataType)
		throws ValidatorException
	{
		for (DataType dt : getSupportedValidationTypes()) {
			if (dt.equals(dataType)) {
				return;
			}
		}
		throw new ValidatorException(dataType);
	}

	
	
	
}
