package org.devichi.photo.control.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.devichi.photo.i18n.Message;
import org.devichi.photo.model.config.PhotoConfiguration;
import org.devichi.photo.model.config.PhotoConfigurationFactory;
import org.devichi.photo.model.user.PhotoUser;
import org.devichi.photo.utils.PhotoException;

/**
 * This servlet is in charge of the installation pages.
 * 
 * It accepts the following URI scheme
 * - install/pageid (GET = display, POST = process)
 * 
 * @author Hadri
 */
public class InstallServlet extends PhotoServlet {

	public static final String URI = "/install";
	
	public static final String COMMAND_STEP1 = "1";
	public static final String COMMAND_STEP2 = "2";
	
	// Install form control names;
	public static final String ACTION = "Iinstall"; //$NON-NLS-1$	
	public static final String FIELD_IMAGES = "imgRoot"; //$NON-NLS-1$
	public static final String FIELD_PHOTO_FACTORY = "photoFactory"; //$NON-NLS-1$
	public static final String FIELD_USER_FACTORY = "userFactory"; //$NON-NLS-1$
	public static final String FIELD_DBLOGIN = "dbLogin"; //$NON-NLS-1$
	public static final String FIELD_DBPASSWORD = "dbPass"; //$NON-NLS-1$
	public static final String FIELD_DBURL = "dbUrl"; //$NON-NLS-1$
	public static final String FIELD_DBDRIVER = "dbDriver"; //$NON-NLS-1$
	
	public static final String ACTION2 = "Iinstall2"; //$NON-NLS-1$	
	public static final String FIELD_DEFAULT_GROUP = "dGroup"; //$NON-NLS-1$
	public static final String FIELD_LOGIN = "aLogin"; //$NON-NLS-1$
	public static final String FIELD_PASS = "aPass"; //$NON-NLS-1$
	public static final String FIELD_PASS2 = "aPass2"; //$NON-NLS-1$
	
	static final long serialVersionUID = 1;
	static final Logger log = Logger.getLogger(InstallServlet.class);
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String uri = ((HttpServletRequest)request).getRequestURI();
		if (uri.endsWith("2"))
			request.getRequestDispatcher(PhotoConstants.INSTALL2_PAGE).forward(request, response);
		else
			request.getRequestDispatcher(PhotoConstants.INSTALL_PAGE).forward(request, response);
	}
		
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		log.debug("Entering install servlet, do post"); //$NON-NLS-1$
		
		String command = request.getRequestURI().substring(request.getContextPath().length());
		
		if (command.endsWith(COMMAND_STEP2)) {
			doStep2(request, response);
			return;
		}
		
		if (!command.endsWith(COMMAND_STEP1)) {
			sendUnknownCommandError(request, response);
			return;
		}
		
		String login = (String)request.getAttribute(FIELD_DBLOGIN);
		String password = (String)request.getAttribute(FIELD_DBPASSWORD);
		String url = (String)request.getAttribute(FIELD_DBURL);
		String driver = (String)request.getAttribute(FIELD_DBDRIVER);
		String images = (String)request.getAttribute(FIELD_IMAGES);
		String photoUserFactory = (String)request.getAttribute(FIELD_USER_FACTORY);
		String photoFileFactory = (String)request.getAttribute(FIELD_PHOTO_FACTORY);
		
		if (photoUserFactory == null || photoUserFactory.length() == 0 ) {
			setMessage("install.nullUserFactory", request); //$NON-NLS-1$
			request.getRequestDispatcher(PhotoConstants.INSTALL_PAGE).forward(request, response);
			return;
		}
		
		if (photoUserFactory.endsWith("JDBCPhotoUserFactory")) { //$NON-NLS-1$
		
			if (login == null || login.length() == 0 ) {
				setMessage("install.nullLogin", request); //$NON-NLS-1$
				response.sendRedirect(request.getRequestURI());
				return;
			}
			
			if (password == null || password.length() == 0 ) {
				setMessage("install.nullPassword", request); //$NON-NLS-1$
				response.sendRedirect(request.getRequestURI());
				return;
			}
			
			if (url == null || url.length() == 0 ) {
				setMessage("install.nullURL", request); //$NON-NLS-1$
				response.sendRedirect(request.getRequestURI());
				return;
			}
			
			if (driver == null || driver.length() == 0 ) {
				setMessage("install.nullDriver", request); //$NON-NLS-1$
				response.sendRedirect(request.getRequestURI());
				return;
			}
		}
		
		if (images == null || images.length() == 0 ) {
			setMessage("install.nullRoot", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		
		PhotoConfiguration config = null;
		try {
			config = PhotoConfigurationFactory.readConfiguration(getServletContext().getRealPath(""));
		} catch (PhotoException e) { 
			log.error("Internal Properties file not found or error reading it"); //$NON-NLS-1$
			setMessage("install.internalError", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		} 
		
		if (photoUserFactory.endsWith("JDBCPhotoUserFactory")) { //$NON-NLS-1$
			config.setJDBCDriver(driver);
			config.setJDBCLogin(login);
			config.setJDBCPassword(password);
			config.setJDBCUrl(url);
		}
		config.setPhotoFactoryClass(photoFileFactory);
		config.setPhotoUserFactoryClass(photoUserFactory);
		config.setPathToImages(images);
		
		// Test validity of config 
		if (!config.isImageRootValid()) {
			setMessage("install.invalidConfig",config.getErrors(request.getLocale().getLanguage(), false), request);
			request.getRequestDispatcher(PhotoConstants.INSTALL_PAGE).forward(request, response);
			return;
		}
		
		// Save (attempt to read an external config present in the newly specified root before)
		PhotoConfigurationFactory.readInternalConfiguration(config);
		PhotoConfigurationFactory.writeConfiguration(config);
		getServletContext().setAttribute(PhotoConstants.CONTEXT_CONFIGURATION, config);
		Message.init(config.getSupportedLocales());
		
		// Test if step 2 is needed
		if (!config.isUserDatabaseAvailable()) {
			try {
				getConfiguration().getPhotoUserFactory().initBase();
			} catch (PhotoException e) {
				setMessage("install.dbError",e.getMessage(), request); //$NON-NLS-1$
				response.sendRedirect(request.getRequestURI());
				return;
			}
			
			response.sendRedirect(request.getContextPath()+InstallServlet.URI+PhotoConstants.SEPARATOR_PATH+InstallServlet.COMMAND_STEP2);  
			return;
		}
		
		// return to main servlet
		setMessage("install.success", request); //$NON-NLS-1$
		response.sendRedirect(request.getContextPath()+NavigationServlet.URI); 
	}
	
	protected void doStep2(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String login = (String)request.getAttribute(FIELD_LOGIN);
		String pass =(String)request.getAttribute(FIELD_PASS);
		String passRepeat =(String)request.getAttribute(FIELD_PASS2);
		String dGroup  = (String)request.getAttribute(FIELD_DEFAULT_GROUP);
		
		if (login == null || login.length() == 0 ) {
			setMessage("install2.nullLogin", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		
		if (dGroup == null || dGroup.length() == 0 ) {
			setMessage("install2.nullGroup", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		
		if (pass == null || pass.length() == 0 ) {
			setMessage("install2.nullPassword", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		
		if (!pass.equals(passRepeat)) {
			setMessage("install2.nullPassword", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		
		try {
			if (!getConfiguration().getPhotoUserFactory().groupExists(dGroup))
				getConfiguration().getPhotoUserFactory().addGroup(dGroup);
			PhotoUser user = getConfiguration().getPhotoUserFactory().readUser(login);
			if (user == null) {
				getConfiguration().getPhotoUserFactory().addUser(login, pass, request.getLocale().getLanguage(), getConfiguration().getDefaultTheme(), getConfiguration().getDefaultColumns(), "");
				user = getConfiguration().getPhotoUserFactory().readUser(login);
			} 
			user.setAdmin(true);
			user.updatePassword(user.getPassword(), pass);
			getConfiguration().getPhotoUserFactory().updateUser(user);
			setMessage("install.success", request); //$NON-NLS-1$
		} catch (PhotoException e) {
			log.error(e.getMessage(),e);
			setMessage("install2.error", e.getMessage(), request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		response.sendRedirect(NavigationServlet.URI); 
	}
		
}
