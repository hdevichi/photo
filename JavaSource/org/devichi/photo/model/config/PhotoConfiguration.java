package org.devichi.photo.model.config;

import java.io.File;

import org.apache.log4j.Logger;
import org.devichi.photo.control.servlet.PhotoConstants;
import org.devichi.photo.i18n.Message;
import org.devichi.photo.model.photo.PhotoFactory;
import org.devichi.photo.model.user.PhotoUserFactory;
import org.devichi.photo.utils.DirectoryFilter;

/**
 * This class holds the configuration of an instance of the photo web application.
 * It is instanciable only via static methods (returning config read from a file or 
 * a default, to ensure instances integrity).
 * 
 * @author Hadrien Devichi
 */
public class PhotoConfiguration {

	static final Logger log = Logger.getLogger(PhotoConfiguration.class);

	// Attributes taken from internal & external configuration file
	protected int defaultColumns = 4;
	protected String defaultGroup = "default"; //$NON-NLS-1$
	protected String defaultTheme = PhotoConfigurationConstants.DEFAULT_THEME;
	protected String[] supportedLocales = {"en", "fr"}; // separated by spaces

	protected long userTimeout = 900; // in seconds!
	
	protected String pathToImages;
	protected String pathToApplication;
	protected String photoFactoryClass = "org.devichi.photo.model.photo.FilePhotoFactory"; //$NON-NLS-1$
	protected long imageTimeout = 3600; // in seconds
	protected int maximumSize = 800;
	
	protected String photoUserFactoryClass = "org.devichi.photo.model.user.FilePhotoUserFactory"; //$NON-NLS-1$
	protected String jdbcDriverName = PhotoConfigurationConstants.DEFAULT_JDBC_DRIVER;
	protected String jdbcConnectionURL = PhotoConfigurationConstants.DEFAULT_JDBC_CONNECTION_URL;
	protected String jdbcLogin = PhotoConfigurationConstants.DEFAULT_DATABASE_USER;
	protected String jdbcPassword = PhotoConfigurationConstants.DEFAULT_DATABASE_PASSWORD;
	
	protected String smtpServer = null; // by default, no server & no mail is sent
	protected String adminEMail = null;
	protected String adminEMailName = "Administrateur Photo"; //$NON-NLS-1$;
	
	protected boolean security = false;
	
	// Computed attributes
	protected File imageRoot;
	protected File themeRoot;
	private PhotoFactory fileFactory;
	private PhotoUserFactory userFactory;
	
	// Prevents direct instantiation (only way to obtain one is to read a file or
	// get default config. Ensures object integrity.
	protected PhotoConfiguration() {	
	}
	
	/**
	 * Return a list of the available themes, that is all the
	 * subdirectories of the theme root dir.
	 * (does not check for the theme's validity)
	 * @return
	 */
	public String[] getAvailableThemes() {

		File[] themes = getThemeRoot().listFiles(new DirectoryFilter());
		if (themes == null)
			return new String[0];
		
		String[] availableThemes = new String[themes.length];
		for (int i = 0 ; i < themes.length ; i++ ) {
			availableThemes[i] = themes[i].getName();
		}
		
		return availableThemes;
	}
	
	protected File getThemeRoot() {
		if (themeRoot == null) {
			themeRoot = new File(pathToApplication,PhotoConstants.PATH_TO_THEMES);
		}
		return themeRoot;
	}
	
	/**
	 * Redefined in the user settings, so this is only a default value
	 * @return
	 */
	public int getDefaultColumns() {
		return defaultColumns;
	}

	public void setDefaultColumns(int c) {
		defaultColumns = c;
	}
	
	public void setDefaultTheme(String s) {
		defaultTheme = s;
	}
	
	public void setJDBCUrl( String s) {
		jdbcConnectionURL = s;
	}
	
	public void setJDBCDriver( String s) {
		jdbcDriverName = s;
	}
	
	public void setPathToImages( String s ) {
		pathToImages = s;
		imageRoot = new File(pathToImages);
	}
	
	public String getPathToImages() {
		return pathToImages;
	}

	/**
	 * Default value, redefined at user leel
	 * @return
	 */
	public String getDefaultTheme() {
		return defaultTheme;
	}
		
	public File getImageRoot() {
		if (imageRoot == null) {
			imageRoot = new File(getPathToImages());
		}
		return imageRoot;
	}
	
	public String getDefaultGroup() {
		return defaultGroup;
	}
	
	public void setDefaultGroup(String s) {
		defaultGroup = s;
	}
	
	public String[] getSupportedLocales() {
		return supportedLocales;
	}
	
	public void setSupportedLocales(String[] locs) {
		supportedLocales = locs;
	}
	
	public long getUserTimeout() {
		return userTimeout;
	}
	
	public void setUserTimeout( int t) {
		userTimeout = t;
	}
	
	public long getImageTimeout() {
		return imageTimeout;	
	}
	
	public void setImageTimeout(int t) {
		imageTimeout = t;
	}
	
	public String getJDBCConnectionUrl() {
		return jdbcConnectionURL;
	}
	
	public String getJDBCDriverName() {
		return jdbcDriverName;
	}
	
	public String getJDBCLogin() {
		return jdbcLogin;
	}
	
	public void setJDBCLogin(String s) {
		jdbcLogin = s;
	}
	
	public String getJDBCPassword() {
		return jdbcPassword;
	}
	
	public void setJDBCPassword(String s) {
		jdbcPassword = s;
	}
	
	public boolean isSendMail() {
		if (smtpServer == null || smtpServer.length() == 0)
			return false;
		if (adminEMail == null || adminEMail.length() == 0)
			return false;
		return true;
	}
	
	public String getSMTPServer() {
		return smtpServer;
	}
	
	public void setSMTPServer(String s) {
		smtpServer =s;
	}
	
	public String getAdminEMailAdress() {
		return adminEMail;
	}
	
	public void setAdminEmailAdress(String s) {
		adminEMail = s;
	}
	
	public String getAdminEMailName() {
		return adminEMailName; 
	}
	
	public void setAdminEmailName(String s) {
		adminEMailName =s;
	}
	
	public String getPhotoFactoryClass() {
		return photoFactoryClass;
	}
	
	public void setPhotoFactoryClass(String s) {
		photoFactoryClass = s;
		fileFactory = null;
	}
	
	public String getPhotoUserFactoryClass() {
		return photoUserFactoryClass;
	}
	
	public void setPhotoUserFactoryClass(String s) {
		photoUserFactoryClass = s;
		userFactory = null;
	}
	
	public PhotoFactory getPhotoFactory() {

		if (fileFactory != null)
			return fileFactory;
		
		try {
			Class implementationClass = Class.forName(photoFactoryClass);
			fileFactory = (PhotoFactory)implementationClass.newInstance();
			fileFactory.setConfiguration(this);
			return fileFactory;
			
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(PhotoConstants.IMAGE_FACTORY_MISSING_ERROR);
		} catch (InstantiationException e) {
			throw new RuntimeException(PhotoConstants.IMAGE_FACTORY_MISSING_ERROR);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(PhotoConstants.IMAGE_FACTORY_MISSING_ERROR);
		}
	}
	
	public PhotoUserFactory getPhotoUserFactory() {
		
		if (userFactory != null)
			return userFactory;
		
		try {
			Class implementationClass = Class.forName(photoUserFactoryClass);
			userFactory = (PhotoUserFactory)implementationClass.newInstance();
			userFactory.setConfiguration(this);
			return userFactory;
			
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(PhotoConstants.USER_FACTORY_MISSING_ERROR);
		} catch (InstantiationException e) {
			throw new RuntimeException(PhotoConstants.USER_FACTORY_MISSING_ERROR);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(PhotoConstants.USER_FACTORY_MISSING_ERROR);
		}
	}
	
	public boolean isImageRootValid() {
		
		return getImageRoot().exists();
	}
	
	public boolean isValid() {
		
		if (!isImageRootValid())
			return false;
		
		if (defaultColumns < 1 || defaultColumns > 10)
			return false;
		
		if ( userTimeout < 0 || imageTimeout < 0 || maximumSize < 0)
			return false;
		
		if ( defaultGroup.equals(PhotoConstants.GUEST_GROUP_NAME))
			return false;
		
		try {
			Class.forName(photoFactoryClass);
		} catch (Exception e) {
			return false;
		}
		
		if (!isUserDatabaseAvailable())
			return false;
		
		return true;
	}
	
	/**
	 * Checks that the user db is as expected by attempting a read in it
	 * @return
	 */
	public boolean isUserDatabaseAvailable() {
		try {
			Class.forName(photoUserFactoryClass);
			if (!getPhotoUserFactory().test())
				return false;
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public String getErrors( String locale, boolean includeUserErrors ) {
		
		if (isValid() && isUserDatabaseAvailable())
			return Message.getResource("config.noErrors", locale); //$NON-NLS-1$
		
		String errors = ""; //$NON-NLS-1$
		if (!getImageRoot().exists())
			errors += Message.getResource("config.imageAccessError", locale)+PhotoConstants.SEPARATOR_LINE; //$NON-NLS-1$
		try {
			Class.forName(photoFactoryClass);
		} catch (Exception e) {
			errors += Message.getResource("config.imageFactoryError", locale)+PhotoConstants.SEPARATOR_LINE; //$NON-NLS-1$
		}
		try {
			Class.forName(photoUserFactoryClass);
		} catch (Exception e) {
			errors += Message.getResource("config.nouserFactoryError", locale)+PhotoConstants.SEPARATOR_LINE; //$NON-NLS-1$
		}
	
		if (defaultColumns < 1 || defaultColumns > 10)
			errors += Message.getResource("config.columnsOutOfRange", locale)+PhotoConstants.SEPARATOR_LINE; //$NON-NLS-1$
		
		if ( userTimeout < 0 )
			errors += Message.getResource("config.userTimeoutInvalid", locale)+PhotoConstants.SEPARATOR_LINE; //$NON-NLS-1$
		
		if	( imageTimeout < 0 )
			errors += Message.getResource("config.imageTimeoutInvalid", locale)+PhotoConstants.SEPARATOR_LINE; //$NON-NLS-1$
	
		if ( defaultGroup.equals(PhotoConstants.GUEST_GROUP_NAME))
			errors += Message.getResource("config.defaultGroupInvalid", locale)+PhotoConstants.SEPARATOR_LINE; //$NON-NLS-1$
		
		if (includeUserErrors) {
			try {
				getPhotoUserFactory().userExists("test"); //$NON-NLS-1$
			} catch (Exception e) {
				errors += Message.getResource("config.userAccessError", locale)+"("+e.getMessage()+")"+PhotoConstants.SEPARATOR_LINE; //$NON-NLS-1$
			}
		}
		
		return errors;
	}
	
	public int getMaximumSize() {
		return maximumSize;
	}
	
	public boolean isSecurityMode() {
		return security;
	}

	public void setSecurityMode(boolean b) {
		security = b;
	}
}