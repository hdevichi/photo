package org.devichi.photo.control.servlet;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;
import org.devichi.photo.i18n.Message;

/**
 * Servlet handling the register page.
 * 
 * - a GET request displays the register page
 * - a POST request processes the register form 
 * 
 * URI may contain a command after the servlet URI, in this case this
 * is considered as the return URI (called by redirect)
 * to use after registration
 * 
 * When calling this servlet you should usually ensure the return URI = current URI
 * at the time of the call
 * 
 * @author Hadrien Devichi
 */
public class RegisterServlet extends PhotoServlet {

	public static final String URI = "/register";
	
	// Register form control names
	public static final String FIELD_LOGIN = "user_login"; //$NON-NLS-1$
	public static final String FIELD_PASSWORD = "user_password"; //$NON-NLS-1$
	public static final String FIELD_PASSWORD_CONFIRM = "user_password2"; //$NON-NLS-1$
	public static final String FIELD_LANGUAGE = "user_language"; //$NON-NLS-1$
	public static final String FIELD_THEME = "user_theme"; //$NON-NLS-1$
	public static final String FIELD_EMAIL = "user_email"; //$NON-NLS-1$
	public static final String FIELD_COLUMNS = "user_cols"; //$NON-NLS-1$
	
	static final long serialVersionUID = 1;
	static final Logger log = Logger.getLogger(RegisterServlet.class);
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher(PhotoConstants.REGISTER_PAGE).forward(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if (log.isDebugEnabled()) {
			log.debug("Entering RegisterServlet, doPost"); //$NON-NLS-1$
		}
		
		String login = (String)request.getAttribute(FIELD_LOGIN);
		String password = (String)request.getAttribute(FIELD_PASSWORD);
		String passwordConfirm = (String)request.getAttribute(FIELD_PASSWORD_CONFIRM);
		String language = (String)request.getAttribute(FIELD_LANGUAGE);
		String theme = (String)request.getAttribute(FIELD_THEME);
		String email = (String)request.getAttribute(FIELD_EMAIL);
		String columns = (String)request.getAttribute(FIELD_COLUMNS);
		
		if (login == null || login.length() == 0) {
			setMessage("register.missingLogin", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		if (password == null || password.length() == 0) {
			setMessage("register.missingPassword", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		if (password.length() < 6) {
			setMessage("register.badPassword", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		if (passwordConfirm == null || passwordConfirm.length() == 0) {
			setMessage("register.missingPassword2", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		if (! password.equals(passwordConfirm)) {
			setMessage("register.passwordMismatch", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		int cols = 0;
		try {
			cols = Integer.parseInt(columns);
		} catch (NumberFormatException e) {
			setMessage("register.badColumns", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}

		if (getConfiguration().getPhotoUserFactory().userExists(login)) {
			setMessage("register.alreadyExists", request); //$NON-NLS-1$
			response.sendRedirect(request.getRequestURI());
			return;
		}
		getConfiguration().getPhotoUserFactory().addUser(login, password, language,theme, cols, email);
		
		if (  getConfiguration().isSendMail()) {
			
			try {
				SimpleEmail mail;
				StringBuffer message;
				if (email != null && email.length() > 0 ) {
					// Mail de confirmation
					mail = new SimpleEmail();
					mail.setFrom( getConfiguration().getAdminEMailAdress(), getConfiguration().getAdminEMailName());
					mail.addTo(email);
					mail.setSubject(Message.getResource("register.mailTitle", language)); //$NON-NLS-1$
					message = new StringBuffer();
					message.append(Message.getResource("register.mailGreetings", language)).append(" ").append(login).append(PhotoConstants.SEPARATOR_LINE).append(PhotoConstants.SEPARATOR_LINE); //$NON-NLS-1$
					message.append(Message.getResource("register.mailBody", language)); //$NON-NLS-1$
					message.append(Message.getResource("register.mailLogin", language)).append(login).append(PhotoConstants.SEPARATOR_LINE); //$NON-NLS-1$
					message.append(Message.getResource("register.mailPassword", language)).append(password).append(PhotoConstants.SEPARATOR_LINE); //$NON-NLS-1$
					mail.setMsg(message.toString());
					mail.setHostName(getConfiguration().getSMTPServer());
					mail.send();

				}
				// Mail to admin
				mail = new SimpleEmail();
				mail.setFrom( getConfiguration().getAdminEMailAdress(), getConfiguration().getAdminEMailName());
				mail.addTo(getConfiguration().getAdminEMailAdress());
				mail.setSubject(Message.getResource("register.mailToAdminTitle", Locale.getDefault().getLanguage())); //$NON-NLS-1$
				message = new StringBuffer();
				message.append(Message.getResource("register.mailToAdminBody", Locale.getDefault().getLanguage())).append(login).append(PhotoConstants.SEPARATOR_LINE).append(PhotoConstants.SEPARATOR_LINE); //$NON-NLS-1$ 
				mail.setMsg(message.toString());
				mail.setHostName(getConfiguration().getSMTPServer());
				mail.send();
				log.info("Mail to admin sent"); //$NON-NLS-1$
				
			} catch (EmailException e) {
				log.error("Erreur durant l'envoi d'un mail d'enregistrement pour "+login,e); //$NON-NLS-1$
			}

		}
		
		setMessage("register.success", request); //$NON-NLS-1$
		response.sendRedirect(request.getContextPath()+LoginServlet.URI);
		return;	
	}
}
