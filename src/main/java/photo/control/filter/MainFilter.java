package photo.control.filter;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import photo.control.servlet.InstallServlet;
import photo.control.servlet.LoginServlet;
import photo.control.servlet.NavigationServlet;
import photo.control.servlet.PhotoConstants;
import photo.i18n.Message;
import photo.model.config.PhotoConfiguration;
import photo.model.config.PhotoConfigurationFactory;
import photo.model.user.PhotoUser;
import photo.utils.PhotoException;
import photo.utils.ProgressMonitorFileItemFactory;
import photo.i18n.Message;
import photo.utils.PhotoException;

/**
 * This is the main filter for the photo web application.
 * 
 * It handles general controller related matters:
 * - performs installation if needed
 * - performs auto login if needed
 * - parse request parameters
 * - puts original URL requested as attribute
 * 
 * The URL scheme for the application as is follow:
 * 
 * GET view/[resource id|about|root] view a resource (index, or photo page)
 * GET thumbnail/[resource id] thumbnail for a resource
 * GET image/[resource id] display image for an id
 * GET original/[resource id] original image for an id
 * admin/[resource id|app] admin a resource, or the application
 * (GET = display admin page, POST = process it)
 * login login page (GET = display, POST = process (login, ou remind ou ...)
 * install/pageid (GET = display, POST = process)
 * settings (GET = display, POST = process)
 * register (GET = display, POST = process)
 * ajax => ajax
 * 
 * See javadoc of each servlet for more details
 * 
 * Note: constants in each servlet define the URI & forms.
 * @author Hadrien Devichi
 */
public class MainFilter implements Filter {

	static final long serialVersionUID = 1;
	private static final Logger log = Logger.getLogger(MainFilter.class);
	private static final Logger accessLog = Logger.getLogger("org.devichi.photo.access");
		
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)resp;

		String requestedURI = request.getRequestURI();
		
		log.debug("Entering main filter"); //$NON-NLS-1$
		
		incrementRequestCounter(request);
		request.setCharacterEncoding(PhotoConstants.ENCODING);
		
		// Redirect to view root if no URI
		if (requestedURI.equals(request.getContextPath()) || 
				requestedURI.equals(request.getContextPath()+PhotoConstants.SEPARATOR_PATH)) {
			response.sendRedirect(request.getContextPath()+NavigationServlet.URI);
			return;
		}
		
		// Do nothing for CSS (otherwise, when install is needed, a request for CSS will be redirected too...)
		if (requestedURI.endsWith(".css")) {
			chain.doFilter(request, response);
			return;
		}
		
		// if multipart: put form data in request attributes & uploaded files in the incoming directory,
		// otherwise just put parameters in attributes
		if (ServletFileUpload.isMultipartContent(new ServletRequestContext(request))) {			
			parseMultipartParameters(request);
		} else {
			parseParameters(request);
		}
		
		// Note: installation process cannot rely on user, because it cannot be created
		// without a factory (config defined...)
		
		// Are we during the installation process? - if so, go directly to the install controller
		if ( requestedURI.startsWith(request.getContextPath()+InstallServlet.URI) ) {
			chain.doFilter(request, response);
			return;
		}
		
		// Install not in progress, is it needed?
		if ( request.getSession().getServletContext().getAttribute(PhotoConstants.CONTEXT_CONFIGURATION) == null ) {
			log.info("configuration not found, redirecting to install page");
			request.getRequestDispatcher(PhotoConstants.INSTALL_PAGE).forward(request, response);
			return;
		}
	
		checkUser(request, response);
			
		String command = request.getRequestURI().substring(request.getContextPath().length());
		request.setAttribute(PhotoConstants.REQUEST_ORIGINAL_RELATIVE_URI, command);
		chain.doFilter(request, response);
	}

	/**
	 * Reads the application configuration and, if valid, stores it in the application
	 * context under the attribute name: PhotoConstants.CONTEXT_CONFIGURATION
	 * 
	 * Also initializes uptime statistics
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
				
		// set system properties used by log4j properties file
		System.setProperty(PhotoConstants.SYSTEM_ROOT,filterConfig.getServletContext().getRealPath(""));
		
		// Retrieves properties directory
		File propertiesDir = new File(filterConfig.getServletContext().getRealPath(PhotoConstants.PROPERTIES_PATH));
		
		// Retrieves & read logging properties
		if (propertiesDir.exists() && propertiesDir.isDirectory()) {
			String logPropertiesFileName = PhotoConstants.PROPERTIES_LOG_FILE;
			File log4jFile = new File(propertiesDir, logPropertiesFileName);
			PropertyConfigurator.configure(log4jFile.getAbsolutePath());
			log.info("log4j initialisation complete"); //$NON-NLS-1$
		} else {
			throw new ServletException("Application initialisation failed!"); //$NON-NLS-1$
		}
		
		// Read photo.properties
		PhotoConfiguration config = null;
		try {
			config = PhotoConfigurationFactory.readConfiguration(filterConfig.getServletContext().getRealPath(""));
		} catch (PhotoException e) {
			throw new ServletException("Cannot read application configuration");
		} 
		
		// store configuration in application context if valid 
		// (If config is invalid, it will be null in doFilter)
		if (config.isValid() ) {
			filterConfig.getServletContext().setAttribute(PhotoConstants.CONTEXT_CONFIGURATION, config);
			Message.init(config.getSupportedLocales());
		} else {
			// we still need message, but use default locales
			String[] locales = {"en", "fr"};
			Message.init(locales); 
			log.info("Configuration invalid or missing");
			log.info(config.getErrors("en", true));
		}
		
		// Sets init time (for uptime computation)
		filterConfig.getServletContext().setAttribute(PhotoConstants.CONTEXT_STARTTIME, new Long(System.currentTimeMillis()));
		// Init request count
		filterConfig.getServletContext().setAttribute(PhotoConstants.CONTEXT_REQUESTS, new Long(0));
		
	}
	
	@Override
	public void destroy() {
		// Release log4j files
		LogManager.shutdown(); 
	}
	
	public static Logger getAccessLog() {
		return accessLog;
	}
		
	private void parseParameters(HttpServletRequest request) {
		
		log.debug("Form data is standard (not multipart)"); //$NON-NLS-1$
		log.debug("Request Parameters:"); //$NON-NLS-1$
		Enumeration parameters = request.getParameterNames();
		while (parameters.hasMoreElements()) {
			String paramName = (String)parameters.nextElement();
			String paramValue = request.getParameter(paramName);
			if (log.isDebugEnabled()) {
				if (paramName.equals(LoginServlet.FIELD_PASSWORD))
					log.debug(paramName+": ******");
				else
					log.debug(paramName+": "+paramValue); //$NON-NLS-1$
			}
			request.setAttribute(paramName, paramValue);
		}
	}
	
	private void parseMultipartParameters( HttpServletRequest request ) throws ServletException, IOException {
		
		log.debug("Form has multipart content"); //$NON-NLS-1$
		
		request.getSession().setAttribute("FileUpload.Progress", null); //$NON-NLS-1$
    	request.getSession().setAttribute("FileUpload", null); //$NON-NLS-1$
    	request.getSession().setAttribute("FileUpload.Status", null); //$NON-NLS-1$
    	
		// Parse request
		ServletFileUpload upload = new ServletFileUpload();
		upload.setFileItemFactory( new ProgressMonitorFileItemFactory(request));
		List /* FileItem */ items;
		try {
			items = upload.parseRequest(request);
		} catch (FileUploadException e) {
			throw new ServletException(PhotoConstants.UPLOAD_ERROR,e);
		}
		
		// Process the uploaded items & form fields
		log.debug("Request Parameters:"); //$NON-NLS-1$
		Iterator iter = items.iterator();
		while (iter.hasNext()) {
		    FileItem item = (FileItem) iter.next();

		    if (item.isFormField()) {
		    	String name = item.getFieldName();
			    String value = item.getString(PhotoConstants.ENCODING);
			    if (log.isDebugEnabled()) {
			    	log.debug(name+": "+value);
			    }
		        request.setAttribute(name, value);
		        
		    } else {
			    request.setAttribute(item.getFieldName() , item);
		    }
		}
	}
	
	private void checkUser( HttpServletRequest request, HttpServletResponse response ) throws ServletException {
		
		PhotoUser user = (PhotoUser)request.getSession().getAttribute(PhotoConstants.SESSION_USER);
		if (user != null)
			return;
		
		// This is a new session 
		if (MainFilter.getAccessLog().isInfoEnabled()) {
			MainFilter.getAccessLog().info(DateFormatUtils.ISO_DATETIME_FORMAT.format(new Date())+" - Connection from: "+request.getRemoteAddr());
		}
		
		// First, check cookies, to see if we can perform auto login
		PhotoConfiguration config = (PhotoConfiguration)request.getSession().getServletContext().getAttribute(PhotoConstants.CONTEXT_CONFIGURATION);
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies ) {
				if (cookie.getName().equals(PhotoConstants.COOKIE_USER)) {
					
					String cookieValue = cookie.getValue();
					if (cookieValue == null || cookieValue.length() == 0)
						break;
						
					// Checking validity of user cookie
					log.info("Attempting automatic login using cookie");
					int separatorPosition = cookieValue.lastIndexOf(PhotoConstants.SEPARATOR_PATH);
					if (separatorPosition == -1)
						break;
					String login = cookieValue.substring(0, separatorPosition);
					String cookieIdField = cookieValue.substring(separatorPosition+1);
					long cookieId = -1;
					try {
						cookieId = Long.parseLong(cookieIdField);
					} catch (NumberFormatException e) {
						break;
					}
					PhotoUser cookieUser = null;
					try {
						cookieUser = config.getPhotoUserFactory().readUser(login);
					} catch (PhotoException e) {
						log.error(e.getMessage(),e);
					}
					if (cookieUser == null)  {
						log.warn("Automatic login Failed, user not found");
						break;
					}
					if (cookieId != cookieUser.getCookieId()) {
						log.error("Cookie is not valid for user: "+login);
						accessLog.warn(DateFormatUtils.ISO_DATETIME_FORMAT.format(new Date())+" - Automatic user login failed: "+login);
						break;
					}
					// Log in the user
					if (log.isInfoEnabled()) {
						log.info("Automatic login of: "+login);
					}
					if (accessLog.isInfoEnabled()) {
						accessLog.info(DateFormatUtils.ISO_DATETIME_FORMAT.format(new Date())+" - Automatic login of: "+login);
					}
					user = cookieUser;
					request.getSession().setAttribute(PhotoConstants.SESSION_USER, user);
				}
			}
		}
		
		if (user != null)
			return;
		
		// No auto login: create a guest user
		log.info("User not found in session or cookie, using Guest"); //$NON-NLS-1$
		user = config.getPhotoUserFactory().getGuestUser(request.getLocale().getLanguage());
		request.getSession().setAttribute(PhotoConstants.SESSION_USER, user);
	}	
	
	private void incrementRequestCounter(HttpServletRequest request) {
		
		Long counter = (Long)request.getSession().getServletContext().getAttribute(PhotoConstants.CONTEXT_REQUESTS);
		counter++;
		request.getSession().getServletContext().setAttribute(PhotoConstants.CONTEXT_REQUESTS, counter);
	}
	
	public static Logger getLogger() {
		return log;
	}
}
