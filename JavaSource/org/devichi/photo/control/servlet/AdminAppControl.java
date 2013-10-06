package org.devichi.photo.control.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.devichi.photo.i18n.Message;
import org.devichi.photo.model.config.PhotoConfiguration;
import org.devichi.photo.model.config.PhotoConfigurationConstants;
import org.devichi.photo.model.config.PhotoConfigurationFactory;
import org.devichi.photo.model.user.PhotoUser;
import org.devichi.photo.utils.PhotoException;

public class AdminAppControl {

	static final Logger log = Logger.getLogger(AdminServlet.class);
	
	// Application administration form
	public static final String ACTION_USER_DELETE = "Adeleteuser"; //$NON-NLS-1$
	public static final String ACTION_GROUP_ADD = "Aaddgroup"; //$NON-NLS-1$
	public static final String ACTION_GROUP_DELETE = "Adeletegroup"; //$NON-NLS-1$
	public static final String ACTION_CONFIG = "Aconfigure"; //$NON-NLS-1$
	public static final String FIELD_USER_SELECT = "userSelect"; //$NON-NLS-1$
	public static final String FIELD_USER_DELETE_USER = "user_todelete"; //$NON-NLS-1$
	public static final String FIELD_GROUP_ADD_NAME = "groupToAdd"; //$NON-NLS-1$
	public static final String FIELD_GROUP_DELETE_GROUP = "groupToDelete"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_SECURITY = "aAppSecu"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_DEFAULT_THEME = "defaultTheme"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_DEFAULT_COLUMNS = "defaultCols"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_PATH = "pathToImg"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_DB_URL = "dbUrl"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_DB_DRIVER = "dbDriver"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_DB_LOGIN = "dbLogin"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_DB_PASSWORD = "dbPass"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_USER_TIMEOUT = "usrTimeout"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_IMAGE_TIMEOUT = "imgTimeout"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_LOCALES = "locales"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_FACTORY = "ffactory"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_USER_FACTORY = "ufactory"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_DEFAULT_GROUP = "defGroup"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_MAIL_SERVER = "mailSrv"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_ADMIN_MAIL = "admEmail"; //$NON-NLS-1$
	public static final String FIELD_CONFIG_ADMIN_NAME = "admName"; //$NON-NLS-1$
	
	public static void processAction(AdminServlet caller, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if (!caller.getCurrentUser(request).isAdmin()) {
			caller.setMessage("forbidden", request);
			caller.redirectToLogin(request, response);
			return;
		}
		
		try {
			if ( !PhotoServlet.isRequestAttributeEmpty(request,ACTION_GROUP_ADD) ) {
				doAppAddGroupAction(caller, request, response);
			}
			
			if ( !PhotoServlet.isRequestAttributeEmpty(request,ACTION_GROUP_DELETE) ) {
				doAppDeleteGroupAction(caller, request, response);
			}
			
			if ( !PhotoServlet.isRequestAttributeEmpty(request,ACTION_USER_DELETE) ) {
				doAppDeleteUserAction(caller, request, response);
			}
				
			if ( !PhotoServlet.isRequestAttributeEmpty(request,ACTION_CONFIG) ) {
				doAppConfigAction(caller, request, response);
			}
			
		} catch (PhotoException e) {
			AdminServlet.log.error(e.getMessage(),e);
			caller.setMessage(e.getMessage(), request);
		}
		
	}

	private static void doAppAddGroupAction(AdminServlet caller, HttpServletRequest request, HttpServletResponse response) throws PhotoException{
		
		String group = (String)request.getAttribute(FIELD_GROUP_ADD_NAME);
		if (group == null || group.length() == 0) {
			throw new PhotoException("addGroup.noName"); //$NON-NLS-1$
		}
			
		if ( caller. getConfiguration().getPhotoUserFactory().groupExists(group) ) {
			throw new PhotoException("addGroup.alreadyExists"); //$NON-NLS-1$
		}
		
		caller.getConfiguration().getPhotoUserFactory().addGroup(group);
		caller.setMessage("addGroup.success", request); //$NON-NLS-1$
	}
		
	private static void doAppDeleteGroupAction(AdminServlet caller, HttpServletRequest request, HttpServletResponse response) throws PhotoException {

		String group = (String)request.getAttribute(FIELD_GROUP_DELETE_GROUP);
		if (group == null || group.length() == 0) {
			throw new PhotoException("deleteGroup.noName"); //$NON-NLS-1$
		}
		
		try {
			caller.getConfiguration().getPhotoUserFactory().deleteGroup(group);
			caller.setMessage("deleteGroup.success", request); //$NON-NLS-1$
		} catch (PhotoException e) {
			caller.setMessage("deleteGroup.error", e.getMessage(), request);
		}
		// TODO si on enleve un groupe, les entrées de droits relatives à ce groupe ne sont
		// pas supprimées dans description.txt (donne l'impression que les droits bougent quand on edite la liste des groupes...)

	}

	private static void doAppDeleteUserAction(AdminServlet caller, HttpServletRequest request, HttpServletResponse response) throws PhotoException {
		
		String user = (String)request.getAttribute(FIELD_USER_SELECT);
		
		if (user == null || user.length() == 0) {
			throw new PhotoException("deleteUser.noUser"); //$NON-NLS-1$
		}
		
		PhotoUser photoUser = caller.getConfiguration().getPhotoUserFactory().readUser(user);
		if (photoUser.isAdmin()) {
			throw new PhotoException("deleteUser.userIsAdmin"); //$NON-NLS-1$
		}
		
		caller.getConfiguration().getPhotoUserFactory().deleteUser(user);
		caller.setMessage("deleteUser.success", request); //$NON-NLS-1$
	}
		
	private static void doAppConfigAction(AdminServlet caller, HttpServletRequest request, HttpServletResponse response) throws PhotoException {
			
		String security = (String)request.getAttribute(FIELD_CONFIG_SECURITY);

		String theme = (String)request.getAttribute(FIELD_CONFIG_DEFAULT_THEME);
	    String columns = (String)request.getAttribute(FIELD_CONFIG_DEFAULT_COLUMNS);
	    String group = (String)request.getAttribute(FIELD_CONFIG_DEFAULT_GROUP);
	    
	    String url = (String)request.getAttribute(FIELD_CONFIG_DB_URL);
	    String driver = (String)request.getAttribute(FIELD_CONFIG_DB_DRIVER);
	    String dbLogin = (String)request.getAttribute(FIELD_CONFIG_DB_LOGIN);
		String dbPassword = (String)request.getAttribute(FIELD_CONFIG_DB_PASSWORD);
		String userTimeout = (String)request.getAttribute(FIELD_CONFIG_USER_TIMEOUT);
		String userFactory = (String)request.getAttribute(FIELD_CONFIG_USER_FACTORY);
			   
	    String pathToImages = (String)request.getAttribute(FIELD_CONFIG_PATH);
	    String statsTimeout = (String)request.getAttribute(FIELD_CONFIG_IMAGE_TIMEOUT);
	    String locales = (String)request.getAttribute(FIELD_CONFIG_LOCALES);
	    String modelFactory = (String)request.getAttribute(FIELD_CONFIG_FACTORY);
	    	
	    String smtpServer = (String)request.getAttribute(FIELD_CONFIG_MAIL_SERVER);
	    String adminName = (String)request.getAttribute(FIELD_CONFIG_ADMIN_NAME);
	    String adminEmail = (String)request.getAttribute(FIELD_CONFIG_ADMIN_MAIL);
	    	
	    if ( columns == null || columns.length() == 0) {
	    	throw new PhotoException("config.missingColumns"); //$NON-NLS-1$
	    }
	    // do not test group, theme: selection from a combo => assumed valid
	    
	    if ( url == null || url.length() == 0) {
	    	throw new PhotoException("config.missingURL"); //$NON-NLS-1$
	    }
	    if ( driver == null || driver.length() == 0) {
	    	throw new PhotoException("config.missingDriver"); //$NON-NLS-1$
	    }
	    if ( dbLogin == null || dbLogin.length() == 0) {
	    	throw new PhotoException("config.missingLogin"); //$NON-NLS-1$
	    }
	    if ( dbPassword == null || dbPassword.length() == 0) {
	    	throw new PhotoException("config.missingPassword"); //$NON-NLS-1
	    }
	    if (userTimeout == null || userTimeout.length() == 0) {
	    	throw new PhotoException("config.missingUserTimeout"); //$NON-NLS-1
	    }
	    if (userFactory == null || userFactory.length() == 0) {
	    	throw new PhotoException("config.missingUserFactory"); //$NON-NLS-1
	    }
	    try {
	    	Class.forName(userFactory);
	    } catch (ClassNotFoundException e) {
	    	throw new PhotoException("config.userFactoryNotFound"); //$NON-NLS-1$
	    }
 
	    if ( pathToImages == null || pathToImages.length() == 0) {
			throw new PhotoException("config.missingPath"); //$NON-NLS-1$		
	    }
	    if (statsTimeout == null || statsTimeout.length() == 0) {
	    	throw new PhotoException("config.missingImageTimeout"); //$NON-NLS-1$		
	    }
	    if (locales == null || locales.length() == 0) {
	    	throw new PhotoException("config.missingLocales"); //$NON-NLS-1$
	    }
	    if (modelFactory == null || modelFactory.length() == 0) {
	    	throw new PhotoException("config.missingFactory"); //$NON-NLS-1$
	    }
	    try {
	    	Class.forName(modelFactory);
	    } catch (ClassNotFoundException e) {
	    	throw new PhotoException("config.FactoryNotFound"); //$NON-NLS-1$
	    }

	    // 3 mails params are not mandatory	  
	    
	    int cols;
	    try {
	    	cols = Integer.parseInt(columns);
	    } catch (NumberFormatException e) {
	    	throw new PhotoException("config.columnsError"); //$NON-NLS-1$
	    }
	    int imgTimeout, usrTimeout;
	    try {
	    	imgTimeout = Integer.parseInt(statsTimeout);
	    } catch (NumberFormatException e) {
	    	throw new PhotoException("config.imageTimeoutError"); //$NON-NLS-1$
	    }
	    try {
	    	usrTimeout = Integer.parseInt(userTimeout);
	    } catch (NumberFormatException e) {
	    	throw new PhotoException("config.userTimeoutError"); //$NON-NLS-1$
	    }

	    String[] supportedLocales = locales.split(PhotoConfigurationConstants.SUPPORTED_LOCALES_SEPARATOR);

	    PhotoConfiguration config = caller.getConfiguration();
	    if (security != null && security.length()> 0) {
	    	config.setSecurityMode(true);
	    } else {
	    	config.setSecurityMode(false);
	    }
	    
	    config.setDefaultColumns(cols);
	    config.setDefaultTheme(theme);
	    config.setDefaultGroup(group);
	    
	    config.setPathToImages(pathToImages);
	    config.setImageTimeout(imgTimeout);
	    config.setPhotoFactoryClass(modelFactory);
	    config.setSupportedLocales(supportedLocales);
	    
	    config.setJDBCDriver(driver);
	    config.setJDBCUrl(url);
	    config.setJDBCLogin(dbLogin);
	    config.setJDBCPassword(dbPassword);
	    config.setUserTimeout(usrTimeout);
	    config.setPhotoUserFactoryClass(userFactory);
	    
	    config.setSMTPServer(smtpServer);
	    config.setAdminEmailAdress(adminEmail);
	    config.setAdminEmailName(adminName);
	    
	    if (!config.isValid() && config.isUserDatabaseAvailable()) {
	    	caller.setMessage("config.invalid",config.getErrors(caller.getCurrentUser(request).getLanguage(), true), request); //$NON-NLS-1$
	    	return;
	    }
	    	
	    try {
	    	Message.init(config.getSupportedLocales());
	    	PhotoConfigurationFactory.writeConfiguration( config);
	    } catch (IOException e) {
	    	log.error(e);
	    	throw new PhotoException("config.error"); //$NON-NLS-1$
	    }
	    caller.setMessage("config.success", request); //$NON-NLS-1$		
	}
}