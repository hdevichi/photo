package photo.control.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import photo.model.user.PhotoUser;
import photo.utils.PhotoException;
import photo.utils.PhotoException;

/**
 * Servlet handling the settings page.
 * 
 * - a GET request displays the settings page
 * - a POST request processes the settings form 
 * 
 * URI may contain a command after the servlet URI, in this case this
 * is considered as the return URI (called by redirect)
 * to use after settings edition
 * 
 * When calling this servlet you should usually ensure the return URI = current URI
 * at the time of the call
 * 
 * @author Hadrien Devichi
 */
public class SettingsServlet extends PhotoServlet {
	
	public static final String URI = "/settings";

	// Settings form control names
	public static final String ACTION_PASSWORD = "SChanger"; //$NON-NLS-1$
	public static final String ACTION_SETTINGS = "SMettreajour"; //$NON-NLS-1$
	public static final String FIELD_PASSWORD_OLD = "old_pwd"; //$NON-NLS-1$
	public static final String FIELD_PASSWORD_NEW = "new_pwd"; //$NON-NLS-1$
	public static final String FIELD_PASSWORD_CONFIRM = "confirm_pwd"; //$NON-NLS-1$
	public static final String FIELD_COLUMNS = "columns"; //$NON-NLS-1$
	public static final String FIELD_THEME = "theme"; //$NON-NLS-1$
	public static final String FIELD_LANGUAGE = "language"; //$NON-NLS-1$
	
	// Used to send info to the settings page (state to go back to)
	public static final String REQUEST_SETTINGS_COMMAND_ATTRIBUTE = "settingsC";
		
	static final long serialVersionUID = 1;
	static final Logger log = Logger.getLogger(SettingsServlet.class);
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if (getCurrentUser(request).isGuest()) {
			setMessage("forbidden", request);
			redirectToLogin(request, response);
			return;
		}
			
		String command = getCommand(request);
		command = command.substring(URI.length());
		request.setAttribute(REQUEST_SETTINGS_COMMAND_ATTRIBUTE, command);
		
		request.getRequestDispatcher(PhotoConstants.SETTINGS_PAGE).forward(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if (getCurrentUser(request).isGuest()) {
			setMessage("forbidden", request);
			redirectToLogin(request, response);
			return;
		}
		
		String settingsCommandS = (String)request.getAttribute(ACTION_SETTINGS);
		String settingsCommandP = (String)request.getAttribute(ACTION_PASSWORD);
		
		if (log.isDebugEnabled()) {
			log.debug("Entering user servlet, do post"); //$NON-NLS-1$
		}
		
		if (settingsCommandS != null ) {
			processSettings(request, response);
			return;
		}
		
		if (settingsCommandP != null ) {
			processPasswordChange(request, response);
			return;
		}
			
		sendUnknownCommandError(request, response);
	}
	
	private void processSettings(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		int col = -1; 
		try { 
			col = Integer.parseInt((String)request.getAttribute(FIELD_COLUMNS));
		} catch (NumberFormatException e) {
			setMessage("settings.badColumns", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		
		if (col < 1 || col > 8) {
			setMessage("config.columnsOutOfRange", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		
		// Those are selected from a combo, so no need to check validity
		String theme = (String)request.getAttribute(FIELD_THEME);
		String language = (String)request.getAttribute(FIELD_LANGUAGE);
		
		getCurrentUser(request).setColumns(col);
		getCurrentUser(request).setTheme(theme);
		getCurrentUser(request).setLanguage(language);
		
		try {
			log.debug("User updated, attempting to save settings"); //$NON-NLS-1$
			getConfiguration().getPhotoUserFactory().updateUser(getCurrentUser(request));
			log.debug("Settings saved"); //$NON-NLS-1$
		} catch (PhotoException e) {
			log.error(e);
			setMessage("settings.saveError"+e.getMessage(), request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		
		setMessage("settings.success", request); //$NON-NLS-1$
		returnToFormerState(request, response);
	}
	
	private void processPasswordChange(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		log.debug("Processing password changed"); //$NON-NLS-1$
		String oldPassword = (String)request.getAttribute(FIELD_PASSWORD_OLD);
		if (!getCurrentUser(request).checkPassword(oldPassword)) {
			setMessage("settings.invalidPassword", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		
		String newPassword = (String)request.getAttribute(FIELD_PASSWORD_NEW);
		String newPassword2 = (String)request.getAttribute(FIELD_PASSWORD_CONFIRM);
		
		if (newPassword == null || newPassword2 == null) {
			setMessage("settings.missingPassword", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		
		if (newPassword.length() < 6 ) {
			setMessage( "settings.badPassword", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		
		if (! newPassword.equals(newPassword2)) {
			setMessage("settings.passwordMismatch", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		
		PhotoUser user = getCurrentUser(request);
		try {
			log.debug("New password valid, trying to save it"); //$NON-NLS-1$
			user.updatePassword(oldPassword, newPassword);
			getConfiguration().getPhotoUserFactory().updateUser(user);
			log.debug("Password saved"); //$NON-NLS-1$
		} catch (PhotoException e) {
			log.error(e);
			user.updatePassword(newPassword, oldPassword);
			setMessage("password.passwordError",e.getMessage(), request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		
		setMessage("settings.passwordSuccess", request); //$NON-NLS-1$
		returnToFormerState(request, response);
	}
}
