package photo.control.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import photo.model.photo.PhotoDescription;
import photo.utils.PhotoException;
import photo.utils.PhotoException;

/**
 * This is the controler in charge of the navigation in the 
 * photo web application.
 * 
 * GET or POST have the same effect.
 * 
 * The base URI (defined by the constant URI) can be followed by:
 * - nothing: in this case, it will be treated as beeing followed by
 *   a / followed by COMMAND_ROOT
 * - a / followed by the id of the navigated target 
 * - a / followed by COMMAND_ABOUT 
 * - a / followed by COMMAND_ROOT
 * 
 * @author Hadrien Devichi
 */
public class NavigationServlet extends PhotoServlet {
	
	public static final String URI = "/view";
	
	// Commands accepted
	public static final String COMMAND_ABOUT = "about";
	public static final String COMMAND_ROOT = "home";
	
	public static final long serialVersionUID = 1;
	private static final Logger log = Logger.getLogger(NavigationServlet.class);
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String command = request.getRequestURI().substring(request.getContextPath().length());
		int pos = command.lastIndexOf(URI);
		if ( command.length() <= URI.length() +1) {
			command = COMMAND_ROOT;
		} else {
			command = command.substring(pos+URI.length()+1);
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Entering NavigationControler, do get. Command is: "+command); //$NON-NLS-1$
		}
		
		if (command.equals(COMMAND_ABOUT)) {
			request.getRequestDispatcher(PhotoConstants.ABOUT_PAGE).forward(request, response);
			return;
		}
		
		PhotoDescription current = null;
		
		if (command.equals(COMMAND_ROOT)) {
			current = getPhotoFactory().getRoot();
		}
				
		if (current == null) {
			try {
				current = getPhotoFactory().getDescription(command);
			} catch (PhotoException e) {
				log.info("Invalid navigation command: "+command+". Trying to display root");
				current = getPhotoFactory().getRoot();
			}
		}
		
		displayObjectOrLogin(current, request, response);	
	}
		
	private void displayObjectOrLogin( PhotoDescription file, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// Check access 
		if ( !file.isUserAuthorized(getCurrentUser(request))) {
			log.warn("Attempt to access unauthorized object, redirect to login"); //$NON-NLS-1$
			setMessage("forbidden", request);
			redirectToLogin(request, response);
			return;
		} 
		
		try {
			getPhotoFactory().incrementViewCount(file);
		} catch (PhotoException e) {
			// Not blocking, and error already logged in factory
		}
		
		request.setAttribute(PhotoConstants.REQUEST_OBJECT_TO_DISPLAY, file);
		// forward to the appropriate JSP (index, photo, custom)
		if (file.isDirectory()) {
			request.getRequestDispatcher(PhotoConstants.INDEX_PAGE).forward(request, response);
		} else {
			request.getRequestDispatcher(PhotoConstants.PHOTO_PAGE).forward(request, response);
		}
	}
}
