package pl.psnc.dl.ege.webapp.config;

import pl.psnc.dl.ege.EGEConstants;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Singleton class which provides EGE web application
 * with access to local labels and names.
 * 
 * @author mariuszs
 *
 */
public final class LabelProvider
{
	private static LabelProvider instance = null;
	
	private LabelProvider(){
	}
	
	public static LabelProvider getInstance(){
		if(instance == null){
			instance = new LabelProvider();
		}
		return instance;
	}
	
	/**
	 * Returns specified label
	 * of default locale.<br/> 
	 * If label does not exists method
	 * will return an empty String.  
	 * 
	 * @param key
	 * @return
	 */
	public String getLabel(String key){
	    return getLabel(key,EGEConstants.LOCALE);
	}
	
	/**
	 * Returns label 
	 * in language specified by locale.<br/> 
	 * If label does not exists method
	 * will return an empty String.  
	 * 
	 * @param key
	 * @param locale
	 * @return
	 */
	public String getLabel(String key, String locale){
        return ResourceBundle.getBundle("/labels", new Locale(locale)).getString(key);
	}
}
