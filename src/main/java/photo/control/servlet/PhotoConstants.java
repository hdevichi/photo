package photo.control.servlet;

/**
 * This class holds the constants used by the photo application:
 * (all, excepted the form description constants that are in PhotoFormConstants)
 * 
 * - description of the request, session, application & init attributes used by the application
 * - description of the file format used to store images properties
 * - description of the file format used to save application properties
 * - description of the database used to save user information
 * - default value for all application properties (in case file is missing)
 * - internal application configuration
 */
public class PhotoConstants {

	public static final String AUTHOR_EMAIL = "h.devichi@wanadoo.fr";
	
	// Configuration file related constants
	public static final String PROPERTIES_PATH = "properties"; //$NON-NLS-1$
	public static final String SETUP_FILE = "setup.properties"; //$NON-NLS-1$
	public static final String PROPERTIES_LOG_FILE = "log4j.properties"; //$NON-NLS-1$
	public static final String CONFIGURATION_FILE = "photo.properties"; //$NON-NLS-1$
	
	// Exception messages thrown by application (those are not localized)
	public static final String USER_ACCESS_ERROR = "An error occurred while reading users. "; //$NON-NLS-1$
	public static final String IMAGE_ACCESS_ERROR = "An error occured while reading images. "; //$NON-NLS-1$
	public static final String UPLOAD_ERROR = "An error occured while uploading image. "; //$NON-NLS-1$
	public static final String CONFIG_ERROR = "Configuration not found. "; //$NON-NLS-1$
	public static final String CONFIG_IO_ERROR = "I/O error while accessing configuration. "; //$NON-NLS-1$
	public static final String NULL_PARAMETER_ERROR = "A mandatory parameter is null. "; //$NON-NLS-1$
	public static final String INVALID_PARAMETER_ERROR = "A mandatory parameter is invalid. "; //$NON-NLS-1$
	public static final String FILE_NOT_FOUND_ERROR = "File not found";  //$NON-NLS-1$
	public static final String IMAGE_FACTORY_MISSING_ERROR = "Image factory implementation not found."; //$NON-NLS-1$
	public static final String USER_FACTORY_MISSING_ERROR = "User factory implementation not found."; //$NON-NLS-1$
	public static final String IMAGE_ALREADY_EXISTS_ERROR = "Image already exists. ";  //$NON-NLS-1$
	public static final String DESTINATION_ALREADY_EXISTS_ERROR = "Destination (image or directory) already exists. ";  //$NON-NLS-1$
	public static final String DIRECTORY_ALREADY_EXISTS_ERROR = "Directory already exists. ";  //$NON-NLS-1$
	public static final String USER_ALREADY_EXISTS_ERROR = "User already exists. ";  //$NON-NLS-1$
	public static final String USER_MISSING_ERROR = "User does not exist. ";  //$NON-NLS-1$
	public static final String NOT_DIRECTORY_ERROR = "File is not a directory. ";  //$NON-NLS-1$
	public static final String GROUP_ALREADY_EXISTS_ERROR = "Group already exists. ";  //$NON-NLS-1$
	public static final String GROUP_MISSING_ERROR = "Group does not exist. ";  //$NON-NLS-1$
	public static final String JDBC_DRIVER_ERROR = "JDBC Driver not found. ";   //$NON-NLS-1$
	public static final String USER_BASE_INIT_ERROR = "An error occured while initializing user database. ";  //$NON-NLS-1$
	public static final String CANNOT_DELETE_GUEST_ERROR ="Impossible to delete guest group.";  //$NON-NLS-1$
	
	// Name of the guest group - a special group not kept in database, always present, with no rights
	public static final String GUEST_GROUP_NAME = "guest"; //$NON-NLS-1$
	
	// Name of the cookie used to remenber login
	public static final String COOKIE_USER = "ph_user";
	
	// Attributes put in the application context by the application
	public static final String CONTEXT_CONFIGURATION = "photo.configuration"; // a PhotoConfiguration //$NON-NLS-1$
	public static final String CONTEXT_STARTTIME = "photo.startTime";
	public static final String CONTEXT_REQUESTS = "photo.requests";
	
	// System properties set by the app
	public static final String SYSTEM_ROOT = "photo.webapp.root";
	
	// Attributes put in the session by the application
	public static final String SESSION_USER = "photo.user"; //  the user associated to the session //$NON-NLS-1$

	// Attributes put in the request by the application
	public static final String SESSION_MESSAGE = "intMsg"; // messages between pages //$NON-NLS-1$
	public static final String REQUEST_OBJECT_TO_DISPLAY = "photoDescriptionToDisplay"; // msg between servlet &jsp //$NON-NLS-1$
	public static final String REQUEST_ORIGINAL_RELATIVE_URI = "photoOriURL";
	
	// low level properties
	public static final String SEPARATOR_PATH = "/"; //$NON-NLS-1$
	public static final String SEPARATOR_EXTENSION = "."; //$NON-NLS-1$
	public static final String SEPARATOR_LINE = System.getProperty("line.separator"); //$NON-NLS-1$
	public static final String ENCODING = "UTF-8";
	public static final String CHECKBOX_ON = "on"; //$NON-NLS-1$
	
	// Subdirectories of WebContent that are not in PhotoConfiguration
	public static final String SCRIPTS_URI = "/script/"; //$NON-NLS-1$
	// no final / for adminpages since always followed by a page URI that begin with /
	public static final String ADMIN_PAGES_URI = "/adminpages"; //$NON-NLS-1$
	public static final String FRAGMENTS_URI = "/fragments/"; //$NON-NLS-1$
	public static final String LOG_URI = "/log/"; //$NON-NLS-1$
	
	public static final String PATH_TO_THEMES = "/themes/"; // Path to the themes (relative to the Web-Content dir of the web app //$NON-NLS-1$
	
	// URI of the application pages
	public static final String LOGIN_PAGE = "/login.jsp"; //$NON-NLS-1$
	public static final String INDEX_PAGE = "/index.jsp"; //$NON-NLS-1$
	public static final String PHOTO_PAGE = "/photo.jsp"; //$NON-NLS-1$
	public static final String ABOUT_PAGE = "/about.jsp"; //$NON-NLS-1$
	public static final String REGISTER_PAGE = "/register.jsp"; //$NON-NLS-1$
	public static final String SETTINGS_PAGE = "/settings.jsp"; //$NON-NLS-1$
	public static final String ADMIN_APPLICATION_PAGE = "/admin_application.jsp"; //$NON-NLS-1$
	public static final String ADMIN_DIRECTORY_PAGE = "/admin_directory.jsp"; //$NON-NLS-1$
	public static final String ADMIN_IMAGE_PAGE = "/admin_image.jsp"; //$NON-NLS-1$
	public static final String ADMIN_USER_PAGE = "/admin_user.jsp"; //$NON-NLS-1$
	public static final String INSTALL_PAGE = "/install.jsp"; //$NON-NLS-1$
	public static final String INSTALL2_PAGE = "/install2.jsp"; //$NON-NLS-1$
	
	// URI of the application page fragments
	public static final String BACK_FRAGMENT = "back.jspf"; //$NON-NLS-1$
	public static final String FOOTER_FRAGMENT = "footer.jspf"; //$NON-NLS-1$
	public static final String HEADER_FRAGMENT = "header.jspf"; //$NON-NLS-1$
	public static final String JAVASCRIPT_WARNING_FRAGMENT = "jsWarning.jspf"; //$NON-NLS-1$
	public static final String MESSAGE_FRAGMENT = "message.jspf"; //$NON-NLS-1$
	public static final String META_FRAGMENT = "meta.jspf"; //$NON-NLS-1$
	public static final String TOKEN_FRAGMENT = "token.jspf"; //$NON-NLS-1$
}
