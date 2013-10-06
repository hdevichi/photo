package org.devichi.photo.control.servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;
import org.devichi.photo.control.filter.MainFilter;
import org.devichi.photo.i18n.Message;
import org.devichi.photo.model.user.PhotoUser;
import org.devichi.photo.utils.PhotoException;

/**
 * Servlet handling the login form.
 * 
 * - a GET request to URI displays the login form, with a redirect if a switch to
 * HTTPS is required 
 * - a GET request to URI/URI_OFF logs off
 * - a POST request to URI processes the login form (either a login or remind action)
 * 
 * URI may contain a command after the servlet URI, in this case this
 * is considered as the return URI (called by redirect)
 * to use after login success or cancel (or logoff in this case). If missing, returns to the root of the app
 * 
 * In case of a cancel, if the return path is not authorized for current user,
 * its parent will be used
 * 
 * Note: to display the login, you should call redirectToLogin() in PhotoServlet
 * instead of forwarding to the servlet.
 * 
 * @author Hadrien Devichi
 */
public class LoginServlet extends PhotoServlet {

	public static final String URI = "/login";
	public static final String URI_OFF = "/off";
	
	// Login form control names
	public static final String ACTION_LOGIN = "login";
	public static final String ACTION_REMIND = "remind";
	public static final String FIELD_LOGIN = "_login"; // login form login control //$NON-NLS-1$
	public static final String FIELD_PASSWORD = "_password"; // login form password control //$NON-NLS-1$
	public static final String FIELD_REMENBER = "_remenber"; 
	
	// Used to send info to the login page
	public static final String REQUEST_LOGIN_COMMAND_ATTRIBUTE = "loginC";
		
	static final long serialVersionUID = 1;
	static final Logger log = Logger.getLogger(LoginServlet.class);
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String command = getCommand(request);
		command = command.substring(URI.length());
		
		if (command.startsWith(URI_OFF)) {
			request.getSession().invalidate();
			Cookie cookie = new Cookie(PhotoConstants.COOKIE_USER, "");
			cookie.setPath(request.getContextPath());
			cookie.setMaxAge(0);
			response.addCookie(cookie);
			command = command.substring(URI_OFF.length());
			response.sendRedirect(request.getContextPath()+command);
			return;
		}
		
		if (getConfiguration().isSecurityMode() && !request.getScheme().equals("https") ) {
			StringBuffer url = request.getRequestURL();
			//url.delete(url.length() - request.getRequestURI().length(), url.length());
			url.replace(0, request.getScheme().length(), "https");
			//url.append(request.getContextPath()).append(PhotoConstants.SEPARATOR_PATH);
			response.sendRedirect(url.toString());
			return;
		}
		
		request.setAttribute(REQUEST_LOGIN_COMMAND_ATTRIBUTE, command);
		request.getRequestDispatcher(PhotoConstants.LOGIN_PAGE).forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if (getConfiguration().isSecurityMode()) {
			if (!request.getScheme().equals("https"))
				throw new ServletException("Unsecure login not allowed with current security level!");
		}
		
		String action = (String) request.getAttribute(ACTION_LOGIN);
		String actionRemind = (String) request.getAttribute(ACTION_REMIND);
		
		if (log.isDebugEnabled()) {
			log.debug("Entering LoginServlet, doPost, with action: "+action+","+actionRemind); //$NON-NLS-1$
		}
		
		if (action == null && actionRemind == null) {
			sendUnknownCommandError(request, response);
		}
		
		if (actionRemind != null ) {
			processRemind( request, response);
			return;
		}
				
		processLogin( request, response);
	}
	
	private void processLogin (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String login = (String)request.getAttribute(FIELD_LOGIN);
		String password = (String)request.getAttribute(FIELD_PASSWORD);
		String remenber = (String)request.getAttribute(FIELD_REMENBER);

		
		if (login == null || password == null) {
			setMessage("login.missingField", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURL().toString());
			return;
		}
			
		log.info("User/ Password check for "+login); //$NON-NLS-1$
		try {
			PhotoUser user = getConfiguration().getPhotoUserFactory().readUser(login);
			if ( user == null || !user.checkPassword(password)) {
				// redirect to loginfailed page
				MainFilter.getAccessLog().warn(DateFormatUtils.ISO_DATETIME_FORMAT.format(new Date())+" - User login failed: "+user.getLogin());
				log.info("Login failed!"); //$NON-NLS-1$
				setMessage("login.notFound", request); //$NON-NLS-1$
				response.sendRedirect(request.getRequestURL().toString());
				return;
			}

			log.info("Success"); //$NON-NLS-1$
			if (MainFilter.getAccessLog().isInfoEnabled()) {
				MainFilter.getAccessLog().info(DateFormatUtils.ISO_DATETIME_FORMAT.format(new Date())+" - User login: "+user.getLogin());
			}
			
			request.getSession().setAttribute(PhotoConstants.SESSION_USER, user);
			
			if ( remenber != null && remenber.equals(PhotoConstants.CHECKBOX_ON)) {
				String cookieValue =  user.getLogin() + PhotoConstants.SEPARATOR_PATH;
				cookieValue += user.getCookieId();
				Cookie cookie = new Cookie(PhotoConstants.COOKIE_USER, cookieValue);
				cookie.setPath(request.getContextPath());
				cookie.setMaxAge(86400 * 365); // cookie will last 1 year
				response.addCookie(cookie);
			}
			
			returnToFormerState(request, response);
			return;
			
		} catch (PhotoException e) {
			log.error(e);
			setMessage("login.dbError", request); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private void processRemind( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
		
		if (!getConfiguration().isSendMail()) {
			setMessage("login.mailError", request); //$NON-NLS-1$
			returnToFormerState(request, response);
			return;
		}
		
		String login = (String)request.getAttribute(FIELD_LOGIN);
		PhotoUser user;
		try {
			user = getConfiguration().getPhotoUserFactory().readUser(login);
		} catch (PhotoException e) {
			throw new ServletException(PhotoConstants.USER_ACCESS_ERROR,e);
		}
		
		if (user == null) {
			setMessage("login.notFound", request); //$NON-NLS-1$
			returnToFormerState(request, response);
			return;
		}
		
		if (user.getEmail() == null || user.getEmail().length() == 0) {
			setMessage("login.noMail", request); //$NON-NLS-1$
			returnToFormerState(request, response);
			return;
		} 
		
		try {
			SimpleEmail email = new SimpleEmail();
			email.setFrom( getConfiguration().getAdminEMailAdress(), getConfiguration().getAdminEMailName());
			email.addTo(user.getEmail());
			email.setSubject(Message.getResource("reminderMail.title", user.getLanguage())); //$NON-NLS-1$
			StringBuffer message = new StringBuffer();
			message.append(Message.getResource("reminderMail.greetings", user.getLanguage())).append(" ").append(user.getLogin()).append(",").append(PhotoConstants.SEPARATOR_LINE).append(PhotoConstants.SEPARATOR_LINE); //$NON-NLS-1$ //$NON-NLS-2$ 
			message.append(Message.getResource("reminderMail.password", user.getLanguage())).append(user.getPassword()).append(PhotoConstants.SEPARATOR_LINE); //$NON-NLS-1$
			email.setMsg(message.toString());
			email.setHostName(getConfiguration().getSMTPServer());
			email.send();
			setMessage("login.reminderSuccess", request); //$NON-NLS-1$
		} catch (EmailException e) {
			log.error(e);
			e.printStackTrace();
			setMessage("login.mailError", request); //$NON-NLS-1$
		}
	
		response.sendRedirect(request.getRequestURI());
	}
}
