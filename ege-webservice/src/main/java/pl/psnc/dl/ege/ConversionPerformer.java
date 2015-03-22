package pl.psnc.dl.ege;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.psnc.dl.ege.exception.ConverterException;
import pl.psnc.dl.ege.types.ConversionAction;

/*
 * Thread class that performs and controls piped conversion.
 */
class ConversionPerformer
	implements Runnable
{

	private final InputStream is;

	private final OutputStream os;

	private final ConversionAction ca;

	private final ExceptionListener el;

	private static final Logger LOGGER = Logger.getLogger(ConversionPerformer.class.getName());


	public ConversionPerformer(ConversionAction ca, InputStream is,
			OutputStream os, ExceptionListener el)
	{
		this.el = el;
		this.is = is;
		this.os = os;
		this.ca = ca;
	}


	public void run()
	{
		try {
			ca.getConverter()
					.convert(is, os, ca.getConversionActionArguments());
			is.close();
			os.close();
		}
		catch (ConverterException ex) {
            LOGGER.log(Level.SEVERE, ex, ex::getMessage);
			el.catchException(ex);
		}
		catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex, ex::getMessage);
			el.catchException(ex);
		}
		catch (Exception ex) {
			LOGGER.log(Level.SEVERE, ex, ex::getMessage);
			el.catchException(ex);
		}
		finally {
			if (os != null) {
				try {
					os.close();
				}
				catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex, ex::getMessage);
					el.catchException(ex);
				}
			}
			if (is != null) {
				try {
					is.close();
				}
				catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex, ex::getMessage);
					el.catchException(ex);
				}
			}
		}

	}
}
