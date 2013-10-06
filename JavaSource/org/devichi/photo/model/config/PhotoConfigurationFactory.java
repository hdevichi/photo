package org.devichi.photo.model.config;

import java.io.File;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.devichi.photo.control.servlet.PhotoConstants;
import org.devichi.photo.utils.PhotoException;

public class PhotoConfigurationFactory {

	static final Logger log = Logger.getLogger(PhotoConfigurationFactory.class);
	
	/**
	 * Attempts to read configuration from a config file
	 * If the file is missing or if IOException happens, default configuration is returned (method cannot fail)
	 * If some keys in the file are missing, default values are used.
	 * 
	 * The name of the keys expected in the file are found in PhotoConstants class.
	 * @param file
	 * @param context
	 * @return
	 */
	public static PhotoConfiguration readConfiguration( String pathToApplication ) throws PhotoException {

		if (log.isDebugEnabled())
			log.debug("Attempting to read internal configuration"); //$NON-NLS-1$
		
		File file = getInternalConfigurationFile(pathToApplication);
		if (!file.exists()) {
			log.error(PhotoConstants.FILE_NOT_FOUND_ERROR+", "+file.getName());
			throw new PhotoException(PhotoConstants.FILE_NOT_FOUND_ERROR);
		}
		
		// Internal configuration
		PhotoConfiguration config = new PhotoConfiguration();
		PropertiesConfiguration configFile = null;
		try {
			configFile = new PropertiesConfiguration(file);
		} catch (ConfigurationException e) {
			log.error("Error reading internal properties");
			throw new PhotoException(PhotoConstants.CONFIG_IO_ERROR);
		}
		
		config.pathToApplication = pathToApplication;
		
		String pathToImages = configFile.getString(PhotoConfigurationConstants.PATH_TO_IMAGES_KEY);
		if (pathToImages != null)
			config.pathToImages = pathToImages;
		
		String photoFactoryClass = configFile.getString(PhotoConfigurationConstants.PHOTO_FILE_FACTORY_CLASS_NAME_KEY);
		if (photoFactoryClass != null)
			config.photoFactoryClass = photoFactoryClass;
		
		String photoUserFactoryClass = configFile.getString(PhotoConfigurationConstants.PHOTO_USER_FACTORY_CLASS_NAME_KEY);
		if (photoUserFactoryClass != null)
			config.photoUserFactoryClass = photoUserFactoryClass;

		if (config.photoUserFactoryClass.endsWith("JDBCPhotoUserFactory")) {
			String jdbcConnectionURL = configFile.getString(PhotoConfigurationConstants.JDBC_CONNECTION_URL_KEY);
			if (jdbcConnectionURL != null)
				config.jdbcConnectionURL = jdbcConnectionURL;
						
			String jdbcDriverName = configFile.getString(PhotoConfigurationConstants.JDBC_DRIVER_KEY);
			if (jdbcDriverName != null)
				config.jdbcDriverName = jdbcDriverName;
				
			String jdbcLogin = configFile.getString(PhotoConfigurationConstants.DATABASE_LOGIN_KEY);
			if (jdbcLogin != null)
				config.jdbcLogin = jdbcLogin;
						
			String jdbcPassword = configFile.getString(PhotoConfigurationConstants.DATABASE_PASSWORD_KEY);
			if (jdbcPassword != null)
				config.jdbcPassword = jdbcPassword;
		} 
		
		readInternalConfiguration(config);
		return config;
	}
	
	public static void readInternalConfiguration(PhotoConfiguration config) {
		
		// External configuration
		File configuration = getConfigurationFile(config);
		if (configuration == null || !configuration.exists())
			return;
		
		PropertiesConfiguration configFile = null;
		try {
			configFile = new PropertiesConfiguration(configuration);
		} catch (ConfigurationException e) {
			log.warn("Error reading external properties");
			return;
		}
		
		int defaultColumns = configFile.getInt(PhotoConfigurationConstants.DEFAULT_COLUMNS_KEY, -1);
		if ( defaultColumns != -1 ) 
			config.defaultColumns = defaultColumns;
		
		String defaultGroup = configFile.getString(PhotoConfigurationConstants.DEFAULT_GROUP_KEY);
		if (defaultGroup != null)
			config.defaultGroup = defaultGroup;
		
		String defaultTheme = configFile.getString(PhotoConfigurationConstants.DEFAULT_THEME_KEY);
		if (defaultTheme != null)
			config.defaultTheme = defaultTheme;
		
		String[] supportedLocales = configFile.getStringArray(PhotoConfigurationConstants.SUPPORTED_LOCALES_KEY);
		if (supportedLocales != null && supportedLocales.length >0)
			config.supportedLocales = supportedLocales;
			
		int imageTimeout = configFile.getInt(PhotoConfigurationConstants.IMAGE_TIMEOUT_KEY, -1);
		if ( imageTimeout != -1 ) 
			config.imageTimeout = imageTimeout;
		
		int  maximumSize = configFile.getInt(PhotoConfigurationConstants.IMAGE_MAX_SIZE_KEY, -1);
		if ( maximumSize != -1)
			config.maximumSize = maximumSize;
		
		int userTimeout =  configFile.getInt(PhotoConfigurationConstants.USER_TIMEOUT_KEY, -1);
		if ( userTimeout != -1 ) {
			config.userTimeout = userTimeout;
		}
						
		String smtpServer = configFile.getString(PhotoConfigurationConstants.SMTP_SERVER_KEY);
		if ( smtpServer != null)
			config.smtpServer = smtpServer;
		
		String adminEMail = configFile.getString(PhotoConfigurationConstants.ADMINISTRATOR_EMAIL_KEY);
		if ( adminEMail != null)
			config.adminEMail = adminEMail;
		
		String adminEMailName = configFile.getString(PhotoConfigurationConstants.ADMINISTRATOR_EMAIL_NAME_KEY);
		if ( adminEMailName != null)
			config.adminEMailName = adminEMailName;
	
		try {
			boolean security = configFile.getBoolean(PhotoConfigurationConstants.SECURITY_MODE_KEY);
			config.security = security;
		} catch (Exception e) {// ignore	
		}
	}
	
	/**
	 * Update the configuration files with the result of admin form
	 * Adds a modification comment at the beginning of file
	 * Updated config objet is returned, so no need to reparse the file
	 * 
	 * @param config
	 */
	public synchronized static void writeConfiguration(PhotoConfiguration config) throws IOException {
		
		log.info("Updating config"); //$NON-NLS-1$
		
		File internalConfigFile = getInternalConfigurationFile(config.pathToApplication);
		
		try {
			
			PropertiesConfiguration configurationFile = new PropertiesConfiguration(internalConfigFile);
			configurationFile.setProperty( PhotoConfigurationConstants.PATH_TO_IMAGES_KEY , config.getPathToImages() );
			configurationFile.setProperty( PhotoConfigurationConstants.PHOTO_FILE_FACTORY_CLASS_NAME_KEY , config.getPhotoFactoryClass() );
			configurationFile.setProperty( PhotoConfigurationConstants.PHOTO_USER_FACTORY_CLASS_NAME_KEY , config.getPhotoUserFactoryClass() );
			configurationFile.setProperty( PhotoConfigurationConstants.JDBC_DRIVER_KEY , config.getJDBCDriverName() );
			configurationFile.setProperty( PhotoConfigurationConstants.JDBC_CONNECTION_URL_KEY , config.getJDBCConnectionUrl() );
			configurationFile.setProperty( PhotoConfigurationConstants.DATABASE_LOGIN_KEY , config.getJDBCLogin() );
			configurationFile.setProperty( PhotoConfigurationConstants.DATABASE_PASSWORD_KEY , config.getJDBCPassword() );
			configurationFile.save();
			
			File configFile = getConfigurationFile(config);
			configurationFile = new PropertiesConfiguration(configFile);
			if ( !configFile.exists()) {
				log.info("Properties file not found; creating"); //$NON-NLS-1$
			} 
	       
	        configurationFile.setProperty( PhotoConfigurationConstants.DEFAULT_THEME_KEY, config.getDefaultTheme());
	        configurationFile.setProperty( PhotoConfigurationConstants.DEFAULT_COLUMNS_KEY, config.getDefaultColumns());
	        configurationFile.setProperty( PhotoConfigurationConstants.DEFAULT_GROUP_KEY, config.getDefaultGroup()); 
	        configurationFile.setProperty( PhotoConfigurationConstants.USER_TIMEOUT_KEY, config.getUserTimeout());
	        configurationFile.setProperty( PhotoConfigurationConstants.SUPPORTED_LOCALES_KEY, config.getSupportedLocales()); 
	        configurationFile.setProperty( PhotoConfigurationConstants.IMAGE_TIMEOUT_KEY, config.getImageTimeout());																
	        configurationFile.setProperty( PhotoConfigurationConstants.SMTP_SERVER_KEY, config.getSMTPServer());
	        configurationFile.setProperty( PhotoConfigurationConstants.ADMINISTRATOR_EMAIL_NAME_KEY, config.getAdminEMailName());
	        configurationFile.setProperty( PhotoConfigurationConstants.ADMINISTRATOR_EMAIL_KEY, config.getAdminEMailAdress());
	        configurationFile.setProperty( PhotoConfigurationConstants.SECURITY_MODE_KEY, config.isSecurityMode());
	        configurationFile.save();
	        
		} catch (ConfigurationException e) {
			throw new IOException(PhotoConstants.CONFIG_IO_ERROR);
		}
	}

	/** 
	 * Returns the internal configuration file
	 * 
	 */
	protected static File getInternalConfigurationFile(String pathToApplication) {
		
		String configPath = PhotoConstants.PROPERTIES_PATH;
		String configFileName = PhotoConstants.SETUP_FILE;
		
		// read  configuration
		File configDir = new File(pathToApplication, configPath);
		File configFile = new File (configDir, configFileName);
		return configFile;
	}
	
	protected static File getConfigurationFile(PhotoConfiguration config) {
		return new File(config.getImageRoot(), PhotoConstants.CONFIGURATION_FILE);
	}
}
