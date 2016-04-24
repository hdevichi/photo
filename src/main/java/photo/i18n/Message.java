package photo.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class Message {

	private static Message theMessage;
	
	private Map bundles;
	
	private Message( String[] locales ) {
		
		if ( bundles == null) {
			bundles = new HashMap();
			for (int i = 0 ; i < locales.length ; i++ ) {
				ResourceBundle bundle = PropertyResourceBundle.getBundle("org.devichi.photo.i18n.i18n", new Locale(locales[i])); //$NON-NLS-1$
				bundles.put(locales[i], bundle);
			}
		}
	}
		
	public static void init(String[] locales) {
		theMessage = new Message(locales);
	}
	
	public static String getResource( String key, String locale) {
		
		if ( theMessage == null)
			throw new RuntimeException("Message component not initialized!"); //$NON-NLS-1$
		
		ResourceBundle bundle = (ResourceBundle)theMessage.bundles.get(locale);
		
		if ( bundle == null || key == null || key.length() == 0)
			return "???"+key+"???"; //$NON-NLS-1$ //$NON-NLS-2$
		
		try {
			String message = bundle.getString(key);
			if (message == null || message.length() == 0)
				return "???"+key+"???"; //$NON-NLS-1$ //$NON-NLS-2$
			return message;
		} catch (MissingResourceException e) {
			return "???"+key+"???"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
	}
}
