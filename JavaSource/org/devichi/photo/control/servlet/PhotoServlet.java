package org.devichi.photo.control.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.devichi.photo.i18n.Message;
import org.devichi.photo.model.config.PhotoConfiguration;
import org.devichi.photo.model.photo.PhotoFactory;
import org.devichi.photo.model.user.PhotoUser;
import org.devichi.photo.model.user.PhotoUserFactory;

/**
 * Superclass for all servlet used in the photo web application.
 * 
 * this class provides convenience methods to access session data
 * (current config, current user & current file), and to log messages
 * 
 * It should be the preferred way session data is read, as it also ensures session
 * data integrity (creating missing data).
 * 
 */
public class PhotoServlet extends HttpServlet {

	static final long serialVersionUID = 1;
	static final Logger log = Logger.getLogger(PhotoServlet.class);

	protected Map bundles;
	
	// no need to synchronize this class, since not tied to an instance variables
	
	/**
	 * Returns the current user.
	 * It cannot be null, since the main filter will create one if needed
	 */
	protected PhotoUser getCurrentUser(HttpServletRequest request) {
		
		return (PhotoUser)request.getSession().getAttribute(PhotoConstants.SESSION_USER);
	}
	
	/**
	 * Returns the application configuration (from the servlet context)
	 */
	protected PhotoConfiguration getConfiguration() {
		
		return (PhotoConfiguration)getServletContext().getAttribute(PhotoConstants.CONTEXT_CONFIGURATION);
	}
	
	protected PhotoFactory getPhotoFactory() {
		return getConfiguration().getPhotoFactory();
	}
	
	protected PhotoUserFactory getPhotoUserFactory() {
		return getConfiguration().getPhotoUserFactory();
	}
	
	protected void setMessage(String key, String error, HttpServletRequest request) {
		
		String lang = null;
		try {
			lang = getCurrentUser(request).getLanguage();
		} catch (Exception e) {
			// this can happen when config isn't defined yet (when install servlet sets a message)
			lang = request.getLocale().getLanguage();
		}
		StringBuffer message = new StringBuffer(Message.getResource(key, lang));
		message.append(" (").append(error).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		request.getSession().setAttribute(PhotoConstants.SESSION_MESSAGE,message.toString());
	}
	
	// sets a message in the user language.
	// IF config does not exist, use request language instead
	protected void setMessage(String key, HttpServletRequest request) {
		
		String language = request.getLocale().getLanguage();
		try {
			language =  getCurrentUser(request).getLanguage();
		} catch (Exception e) {
			// this can fail is config is not initialized (ex: install servlet/do post)
		}
		String message = Message.getResource(key, language);
		request.getSession().setAttribute(PhotoConstants.SESSION_MESSAGE,message);
	}

	protected void sendUnknownCommandError(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		setMessage("unknownCommand", request); //$NON-NLS-1$
		response.sendRedirect(request.getContextPath()+NavigationServlet.URI);
	}
	
	/**
	 * Redirect to login by changing the URI (it adds login before the command) and redirecting.
	 * Switch to Https if needed
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	protected void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		String command = request.getRequestURI();
		command = command.substring(request.getContextPath().length());
		StringBuffer url = request.getRequestURL();
		url.delete(url.length() - request.getRequestURI().length(), url.length());
		
		if (getConfiguration().isSecurityMode() && !request.getScheme().equals("https") ) {
			url.replace(0, request.getScheme().length(), "https");
		}
		
		url.append(request.getContextPath());
		url.append(LoginServlet.URI).append(command);
		response.sendRedirect(url.toString());
	}
	
	/**
	 * Redirect to the URI obtained by removing the first part of from current URI.
	 * For instance if URI is 
	 * /login/view/1, it returns /view/1
	 * /user/login/view/1, it returns /login/view/1
	 * 
	 * Also it reverts back to HTTP if scheme was HTTPS
	 */
	protected void returnToFormerState(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		StringBuffer url = request.getRequestURL();
		if (request.getScheme().equals("https")) {
			url.replace(0, request.getScheme().length(), "http");
		}
		url.delete(url.length() - request.getRequestURI().length(), url.length());	
		
		String command = getCommand(request);
		int pos = command.indexOf(PhotoConstants.SEPARATOR_PATH,1); 
		if ( pos != -1)
			command = command.substring(pos);
		else
			command="";
		url.append(request.getContextPath()).append(command);
		response.sendRedirect(url.toString());
	}
	
	/**
	 * Returns the part of the URI after the context root.
	 * It starts with a /, or it's ''
	 * 
	 * Exemple: URI is /photo/view/1, it returns /view/1
	 * 
	 * Note that it is set by MainFilter, and not available during installation
	 * process (ie, do not use in install servlet / pages)
	 */ 
	public static String getCommand(HttpServletRequest request) {
		String command = (String)request.getAttribute(PhotoConstants.REQUEST_ORIGINAL_RELATIVE_URI);
		return command;
	}
	
	/**
	 * Tests that the string is not null, and not empty
	 */
	public static boolean isEmpty(String s) {
		return ( s == null || s.length() == 0);
	}
	
	public static boolean isRequestAttributeEmpty(HttpServletRequest request, String s) {
		 String p = (String)request.getAttribute(s);
		 return isEmpty(p);
	}
}
