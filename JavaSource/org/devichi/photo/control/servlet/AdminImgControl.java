package org.devichi.photo.control.servlet;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.devichi.photo.model.photo.PhotoDescription;
import org.devichi.photo.utils.PhotoException;

/**
 * This controller handles all image related administration actions.
 * 
 * It acts as delegate for AdminServlet
 * 
 * @author Hadri
 *
 */
public class AdminImgControl {

	static final Logger log = Logger.getLogger(AdminServlet.class);
	
	// Admin image controls
	public static final String FIELD_MANAGEMENT_RADIO = "picRadio"; //$NON-NLS-1$
	public static final String FIELD_DESTINATION = "destination"; //$NON-NLS-1$
	public static final String FIELD_DESCRIPTION_TEXT_PREFIX = "picText"; // suffixed by a language code //$NON-NLS-1$
	public static final String FIELD_DESCRIPTION_TITLE_PREFIX = "picTitle"; // suffixed by a language code //$NON-NLS-1$
	public static final String FIELD_NEWNAME = "picNewName"; //$NON-NLS-1$
	// Possible actions
	public static final String ACTION_DESCRIPTION = "IMettreajour"; //$NON-NLS-1$
	public static final String ACTION_MANAGEMENT = "IExecuter"; //$NON-NLS-1$
	public static final String ACTION_GENERATE = "IGen";  //$NON-NLS-1$
	public static final String ACTION_ROTATE = "IRot";  //$NON-NLS-1$
	public static final String ACTION_RENAME = "IRenome";  //$NON-NLS-1$
	public static final String ACTION_DELETE = "IDelete";  //$NON-NLS-1$
	public static final String OPTION_RENAME = "picRen";
	public static final String OPTION_MOVE = "picMove";
	public static final String OPTION_DELETE = "picDelete";
	
	protected static String processAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, PhotoException {
		
		try {
			if ( !PhotoServlet.isRequestAttributeEmpty(request, ACTION_MANAGEMENT)  ) {
				String action = (String)request.getAttribute(FIELD_MANAGEMENT_RADIO);
			
				if ( action.equals(OPTION_RENAME)) {
					doImageRenameAction(caller, toAdmin, request);
				}
				
				if ( action.equals(OPTION_MOVE)) {
					doImgMoveAction(caller, toAdmin, request);
				}
				
				if ( action.equals(OPTION_DELETE)) {
					doImgDeleteAction(caller, toAdmin, request);
				}
			}
			
			if ( !PhotoServlet.isRequestAttributeEmpty(request, ACTION_GENERATE)  ) {
				doImgGenerateAction(caller, toAdmin, request);
			}
			
			if ( !PhotoServlet.isRequestAttributeEmpty(request, ACTION_ROTATE)  ) {
				doImgRotateAction(caller, toAdmin, request);
			}
			
			if ( !PhotoServlet.isRequestAttributeEmpty(request, ACTION_DESCRIPTION)  ) {
				doImgDescriptionAction(caller, toAdmin, request);
			}
			
		} catch (PhotoException e){
			AdminServlet.log.error(e.getMessage(),e);
			caller.setMessage(e.getMessage(), request);
		}
		
		// redirect to image admin or nav - because if we delete an image, go to nav
		if ( toAdmin.isDirectory()) 
			return NavigationServlet.URI+PhotoConstants.SEPARATOR_PATH+toAdmin.getId();
		else
			return null;
	}
	
	private static void doImgMoveAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request) throws PhotoException, IOException {
		
		String destination = (String)request.getAttribute(FIELD_DESTINATION);
		String newId = destination;
		
		if (destination.equals(toAdmin.getParent().getId()))
			throw new PhotoException("imgMove.destinationIsParent");
		
		if (destination == null || destination.length() == 0) {
			// check that user has rights on destination
			PhotoDescription destinationDir = caller.getPhotoFactory().getDescription("");
			if (!destinationDir.isUserAuthorizedToAdmin(caller.getCurrentUser(request))) {
				throw new PhotoException("imgMove.destinationNotAuthorized"); //$NON-NLS-1$
			}
			// move to image root
			newId = toAdmin.getName();
			caller.getPhotoFactory().move( toAdmin.getPath(), ""); //$NON-NLS-1$
		} else {
		
			// check that destination exists
			if (caller.getPhotoFactory().getDescription(destination) == null) {
				throw new PhotoException("imgMove.destinationNotExist"); //$NON-NLS-1$
			}
			
			// check that user has rights on destination
			PhotoDescription destinationDir = caller.getPhotoFactory().getDescription(destination);
			if (!destinationDir.isUserAuthorizedToAdmin(caller.getCurrentUser(request))) {
				throw new PhotoException("imgMove.destinationNotAuthorized"); //$NON-NLS-1$
			}
			
			newId = newId + PhotoConstants.SEPARATOR_PATH + toAdmin.getName();
			caller.getPhotoFactory().move( toAdmin.getId(), destination);
		}
		
		log.info("Move successful"); //$NON-NLS-1$
		caller.setMessage("imgMove.success", request); //$NON-NLS-1$	
	}
	
	private static void doImgDeleteAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request) throws PhotoException, IOException {
		
		// no need to check if toAdmin is root since it's an image
		caller.getPhotoFactory().delete( toAdmin.getId());
		caller.setMessage("imgDelete.success", request); //$NON-NLS-1$
	}
	
	// Resize, ignoring the blacklist
	private static void doImgGenerateAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request) throws PhotoException, IOException {
		
		log.info("Entering doImgGenerateAction");
		
		caller.getConfiguration().getPhotoFactory().requestResize(toAdmin.getId(), true);
		caller.setMessage("imgGenerate.success",request); //$NON-NLS-1$
	}
	
	private static void doImgRotateAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request) throws PhotoException, IOException {
		
		log.info("Entering doImgRotateAction");
		caller.getPhotoFactory().rotate(toAdmin);
		caller.setMessage("imgRotate.success",request); //$NON-NLS-1$
	}
	
	private static void doImgDescriptionAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request) throws PhotoException, IOException {
		
		String[] languages = caller.getConfiguration().getSupportedLocales();
		String titles[] = new String[languages.length];
		String texts[] = new String[languages.length];
		for ( int i = 0 ; i < languages.length ; i++ ) {
			titles[i] = (String)request.getAttribute(FIELD_DESCRIPTION_TITLE_PREFIX+languages[i]) ; 
			texts[i] = (String)request.getAttribute(FIELD_DESCRIPTION_TEXT_PREFIX+languages[i]) ; 
			
		}

		for ( int i = 0 ; i < languages.length ; i++ ) {
			toAdmin.setTitle( titles[i], new Locale(languages[i]));
			toAdmin.setText( texts[i], new Locale(languages[i]));
		}
		
		caller.getPhotoFactory().writeDescription(toAdmin);
		caller.setMessage("imgDescription.success", request); //$NON-NLS-1$
	}
				
	private static void doImageRenameAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request) throws PhotoException {
		
		String newName = (String)request.getAttribute(FIELD_NEWNAME);
		
		if (newName == null || newName.length() == 0 || (newName.lastIndexOf(PhotoConstants.SEPARATOR_PATH) != -1)) {
			throw new PhotoException("imgRename.invalidName"); //$NON-NLS-1$
		}
		
		String extension = toAdmin.getPath().substring(toAdmin.getPath().lastIndexOf(PhotoConstants.SEPARATOR_EXTENSION));
		newName += extension;

		String newId = toAdmin.getParent().getPath();
		if (newId.length() >0)
			newId += PhotoConstants.SEPARATOR_PATH;
		newId += newName;
		caller.getPhotoFactory().rename(toAdmin.getId(), newName);

		caller.setMessage("imgRename.success", request); //$NON-NLS-1$
	}
}
