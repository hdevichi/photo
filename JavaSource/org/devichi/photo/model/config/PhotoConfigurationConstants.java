package org.devichi.photo.model.config;

public class PhotoConfigurationConstants {

	// Application hardcoded configuration
	public static final int STATISTICS_TIMEOUT_SECONDS = 36000; 
	public static final int THUMBNAIL_SIZE = 160; // The size in pixels of the biggest dimension of the thumbnails created
	public static final String[] SUPPORTED_FILE_EXTENSION = { ".jpg", ".jpeg", ".JPG", ".JPEG", ".gif", ".GIF", ".png", ".PNG" }; // extension accepted by the image filter //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
	public static final long MINIMUM_DISK_SPACE = 100; // number of mb that must be left in order to allow upload
	public static final String DEFAULT_THEME = "white";
	public static final String DEFAULT_JDBC_CONNECTION_URL = "jdbc:mysql:///login"; //$NON-NLS-1$;
	public static final String DEFAULT_DATABASE_USER = "tomcat"; //$NON-NLS-1$;
	public static final String DEFAULT_DATABASE_PASSWORD = "tomcat"; //$NON-NLS-1$;
	public static final String DEFAULT_JDBC_DRIVER = "org.gjt.mm.mysql.Driver"; //$NON-NLS-1$;
	
	// (some is also in PhotoConfiguration (init params) and PhotoConstants)
	
	// Internal Property file keys
	public static final String PHOTO_FILE_FACTORY_CLASS_NAME_KEY = "photoFileFactory"; //$NON-NLS-1$
	public static final String PATH_TO_IMAGES_KEY = "pathToImages"; //$NON-NLS-1$
	public static final String PHOTO_USER_FACTORY_CLASS_NAME_KEY = "photoUserFactory"; //$NON-NLS-1$
	public static final String JDBC_DRIVER_KEY = "jdbcDriver"; //$NON-NLS-1$
	public static final String JDBC_CONNECTION_URL_KEY = "jdbcConnectionUrl"; //$NON-NLS-1$
	public static final String DATABASE_LOGIN_KEY = "jdbcLogin"; //$NON-NLS-1$
	public static final String DATABASE_PASSWORD_KEY = "jdbcPassword"; //$NON-NLS-1$

	// External property file keys
	public static final String DEFAULT_COLUMNS_KEY = "defaultColumns"; //$NON-NLS-1$
	public static final String DEFAULT_GROUP_KEY = "defaultGroup"; //$NON-NLS-1$
	public static final String DEFAULT_THEME_KEY = "defaultTheme"; //$NON-NLS-1$
	public static final String SUPPORTED_LOCALES_KEY = "supportedLocales"; //$NON-NLS-1$
	public static final String SUPPORTED_LOCALES_SEPARATOR = ","; //$NON-NLS-1$
	public static final String IMAGE_TIMEOUT_KEY = "imageTimeout"; //$NON-NLS-1$
	public static final String IMAGE_MAX_SIZE_KEY = "maximumImageSize"; //$NON-NLS-1$
	public static final String USER_TIMEOUT_KEY = "userTimeout"; //$NON-NLS-1$
	public static final String SMTP_SERVER_KEY = "smtpServer"; //$NON-NLS-1$
	public static final String ADMINISTRATOR_EMAIL_KEY = "adminEMail"; //$NON-NLS-1$
	public static final String ADMINISTRATOR_EMAIL_NAME_KEY = "adminEMailName"; //$NON-NLS-1$
	public static final String SECURITY_MODE_KEY = "secureLogin"; //$NON-NLS-1$
}
