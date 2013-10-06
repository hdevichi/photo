package org.devichi.photo.control.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.devichi.photo.model.photo.PhotoDescription;
import org.devichi.photo.utils.PhotoException;

/**
 * This is the controller in charge of administration mode in the photo
 * web application.
 * 
 * The base URI (defined by the constant URI) can be followed by:
 * - a / followed by:
 * 		- the id of the object to administrate
 * 		- COMMAND_APPLICATION
 * 		- COMMAND_USERS+login 
 * - nothing: in this case, it will be treated as beeing followed by
 *   a / followed by COMMAND_APPLICATION
 * 
 * For any URI,
 * GET displays the admin page for the requested object
 * POST processes the admin form
 *  
 *  URI may be followed by an optional suffix, in this case it will return to this suffix after processing
 * @author Hadrien Devichi
 */
public class AdminServlet extends PhotoServlet {
	
	// URI & Commands
	public static final String URI = "/admin";
	public static final String COMMAND_APPLICATION = "app";
	public static final String COMMAND_USERS = "users";
	
	public static final String REQUEST_ATTRIBUTE_USER_TO_ADMIN = "userToAdmin";
	public static final String REQUEST_ATTRIBUTE_RETURN_URI = "returnURI";
	
	static final long serialVersionUID = 1;
	static final protected Logger log = Logger.getLogger(AdminServlet.class);
	
	/**
	 * Returns the admin command in the URI
	 * 
	 * For instance if the URI is 
	 *  /contextRoot/URI/command/returnCommand,
	 *  it returns command (without any slashes)
	 * @param request
	 * @return
	 */
	private String getAdminCommand(HttpServletRequest request) {
		
		String uri = request.getRequestURI();
		int position = uri.indexOf(URI);
		if (position == -1 ) {
			throw new RuntimeException("Not an admin URI");
			
		}
		
		String objectId = uri.substring(position+URI.length());
		if (objectId.startsWith(PhotoConstants.SEPARATOR_PATH))
			objectId = objectId.substring(1);
		position = objectId.indexOf(PhotoConstants.SEPARATOR_PATH);
		if (position != -1) {
			objectId = objectId.substring(0, position);
		}
		return objectId; 
	}
	
	/**
	 * Returns the return command in the URI
	 * 
	 * For instance if the URI is 
	 *  /contextRoot/URI/command/returnCommand,
	 *  it returns returnCommand (without any slashes)
	 *  
	 *  Return URI of nav servlet if no return command
	 * @param request
	 * @return
	 */
	private String getReturnCommand(HttpServletRequest request) {
		
		String uri = request.getRequestURI();
		int position = uri.lastIndexOf(URI);
		if (position == -1 ) {
			throw new RuntimeException("Not an admin URI");
		}
		
		String objectId = uri.substring(position+URI.length());
		if (objectId.startsWith(PhotoConstants.SEPARATOR_PATH))
			objectId =objectId.substring(1);
		position = objectId.indexOf(PhotoConstants.SEPARATOR_PATH);
		if (position != -1)
			objectId = objectId.substring(position);
		else
			objectId = NavigationServlet.URI;
		
		return objectId; 
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String objectId = getAdminCommand(request);
		
		request.setAttribute(REQUEST_ATTRIBUTE_RETURN_URI, getReturnCommand(request));
		
		if (objectId.startsWith(COMMAND_APPLICATION)) {
			if (getCurrentUser(request).isAdmin()) {
				request.getRequestDispatcher(PhotoConstants.ADMIN_PAGES_URI+PhotoConstants.ADMIN_APPLICATION_PAGE).forward(request, response);
				return;
			} else {
				log.warn("Attempt to admin application without authorization, forwarding to login"); //$NON-NLS-1$
				setMessage("forbidden", request);
				redirectToLogin(request, response);
				return;
			}
		}
		
		if (objectId.startsWith(COMMAND_USERS)) {
			if (getCurrentUser(request).isAdmin()) {
				String userLogin = objectId.substring(COMMAND_USERS.length());
				request.setAttribute(REQUEST_ATTRIBUTE_USER_TO_ADMIN, getPhotoUserFactory().readUser(userLogin));
				request.getRequestDispatcher(PhotoConstants.ADMIN_PAGES_URI+PhotoConstants.ADMIN_USER_PAGE).forward(request, response);
				return;
			} else {
				log.warn("Attempt to admin users without authorization, forwarding to login"); //$NON-NLS-1$
				setMessage("forbidden", request);
				redirectToLogin(request, response);
				return;
			}
		}
		
		PhotoDescription toAdmin = getPhotoFactory().getDescription(objectId);
	
		// Check access
		if (  !toAdmin.isUserAuthorizedToAdmin(getCurrentUser(request)) ) {
			log.warn("Attempt to admin unauthorized object, forwarding to login"); //$NON-NLS-1$
			setMessage("forbidden", request);
			redirectToLogin(request, response);
			return;
		} 
		
		request.setAttribute(PhotoConstants.REQUEST_OBJECT_TO_DISPLAY, toAdmin);
		// forward to the appropriate JSP (index, photo, custom)
		if (toAdmin.isDirectory()) {
			request.getRequestDispatcher(PhotoConstants.ADMIN_PAGES_URI+PhotoConstants.ADMIN_DIRECTORY_PAGE).forward(request, response);
		} else {
			request.getRequestDispatcher(PhotoConstants.ADMIN_PAGES_URI+PhotoConstants.ADMIN_IMAGE_PAGE).forward(request, response);
		}
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			// Retrieve action & object, check validity, and checks user rights
		
			String objectId = getAdminCommand(request);
			
			if (objectId.startsWith(COMMAND_APPLICATION)) {
				AdminAppControl.processAction(this, request, response);
				response.sendRedirect(request.getRequestURI());
				return;
			}
			
			if (objectId.startsWith(COMMAND_USERS)) {
				
				String userLogin = objectId.substring(COMMAND_USERS.length());
				if (userLogin.length() == 0) {
					sendUnknownCommandError(request, response);
					return;
				}
				AdminUserControl.processAction(this, getPhotoUserFactory().readUser(userLogin), request, response);
				response.sendRedirect(request.getContextPath()+getReturnCommand(request));
				return;
			}
			
			PhotoDescription toAdmin = getPhotoFactory().getDescription(objectId);
			if (toAdmin == null) {
				sendUnknownCommandError(request, response);
				return;
			}
			
			if (log.isDebugEnabled()) {
				log.debug("Entering admin object servlet, on: "+toAdmin.getPath()); //$NON-NLS-1$
			}
			
			if (!toAdmin.isUserAuthorizedToAdmin(getCurrentUser(request))) {
				log.info("Access rights not sufficient for admin!");
				setMessage("forbidden", request);
				redirectToLogin(request, response);
				return;
			}
			
			String returnTo = null;
			if (toAdmin.isDirectory()) {
				returnTo = AdminDirControl.processAction(this, toAdmin, request, response);
			} else {	
				returnTo = AdminImgControl.processAction(this, toAdmin, request, response);
			}
			
			if (returnTo == null)
				response.sendRedirect(request.getContextPath()+getReturnCommand(request));
			else
				response.sendRedirect(returnTo);
			
		} catch (PhotoException e) {
			log(e.getMessage(),e);
			setMessage(e.getMessage(), request);
			response.sendRedirect(request.getRequestURI());
		}
		
	}	
}
