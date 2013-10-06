package org.devichi.photo.control.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.devichi.photo.model.user.PhotoUser;
import org.devichi.photo.utils.PhotoException;

/**
 * Controller in charge of processing user actions that affect the current state of
 * the application:
 * - change of locale
 * - toggle display admin on or off
 * - logoff
 * 
 * It only accepts POST requests (GET will return an error).
 * 
 * The request URI is the base URI, followed by the return URI (called when
 * the action has been processed). 
 * 
 * When calling this servlet you should usually ensure the return URI = current URI
 * at the time of the call
 * 
 * Every action processed is followed to a redirect, to the URI obtained by 
 * replacing the base URI of this servlet by the base URI of navigation servlet.
 * 
 * @author Hadri
 */
public class UserServlet extends PhotoServlet {

	// URI
	public static final String URI = "/user";
	
	// User form control names
	public static final String ACTION = "userAction";
	public static final String ACTION_LOGOFF = "logoff";
	public static final String ACTION_LOCALE = "chLocale";
	public static final String ACTION_TOGGLE_ADMIN = "toggleAdmin";
	public static final String FIELD_LOCALE = "newLocale"; //$NON-NLS-1$
	public static final String FORM = "user";
	
	static final long serialVersionUID = 0;
	static final Logger log = Logger.getLogger(UserServlet.class);
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String action = (String)request.getAttribute(ACTION);
		
		if (log.isDebugEnabled())
			log.debug("User servlet, action = "+action); //$NON-NLS-1$
		
		if (action == null) {
			sendUnknownCommandError(request, response);
			return;
		}

		if (action.equals(ACTION_LOGOFF) ) {
			request.getSession().invalidate();
			Cookie cookie = new Cookie(PhotoConstants.COOKIE_USER, "");
			cookie.setMaxAge(-1); // cookie will be deleted
			response.addCookie(cookie);
			returnToFormerState(request, response);
			return;
		}
		
		if (action.equals(ACTION_TOGGLE_ADMIN)) {
			PhotoUser user = getCurrentUser(request);
			
			if (user.getDisplayAdmin())
				user.setDisplayAdmin(false);
			else
				user.setDisplayAdmin(true);
			// write change to user DB
			try {
				getConfiguration().getPhotoUserFactory().updateUser(user);
			} catch (PhotoException e) {
				setMessage("user.toggleAdminSaveError", request); //$NON-NLS-1$
			}
			returnToFormerState(request, response);
			return;
		}
			
		if (!action.equals(ACTION_LOCALE)) {
			sendUnknownCommandError(request, response);
			return;
		}
		
		// language change: test if new locale is supported
		String newLocale = (String)request.getAttribute(FIELD_LOCALE);
		boolean found = false;
		for (String s : getConfiguration().getSupportedLocales() ) {
			if (s.equalsIgnoreCase(newLocale)) {
				found = true;
				break;
			}
		}
		if (!found) {
			setMessage("user.unsupportedLocale", request); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
		getCurrentUser(request).setLanguage(newLocale);
		if (getCurrentUser(request).isGuest()) {
			// write change to user DB
			try {
				getConfiguration().getPhotoUserFactory().updateUser(getCurrentUser(request));
			} catch (PhotoException e) {
				setMessage("user.changeLocaleSaveError", request); //$NON-NLS-1$
			}
		}
		// a new language has been selected, return to source
		returnToFormerState(request, response);
	}
}
