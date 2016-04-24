package photo.control.servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import photo.i18n.Message;
import photo.model.photo.PhotoDescription;
import photo.utils.PhotoException;
import photo.i18n.Message;
import photo.utils.PhotoException;

public class AdminDirControl {
	
	static final Logger log = Logger.getLogger(AdminServlet.class);
	
	// Admin directory controls
	public static final String ACTION_DESCRIPTION = "DdirDescrAction"; //$NON-NLS-1$
	public static final String ACTION_RIGHTS = "DModifier"; //$NON-NLS-1$
	public static final String ACTION_ADD_CHILD_DIR = "DCreer"; //$NON-NLS-1$
	public static final String ACTION_UPLOAD_IMAGES = "DAjouter"; //$NON-NLS-1$
	public static final String ACTION_DELETE = "DEffacer"; //$NON-NLS-1$
	public static final String ACTION_MOVE = "DDepl";  //$NON-NLS-1$
	public static final String ACTION_THEME = "Dtheme"; //$NON-NLS-1$
	public static final String ACTION_BATCH_MOVE = "DDeplGroupe"; //$NON-NLS-1$
	public static final String ACTION_BATCH_DELETE = "DSupGroupe"; //$NON-NLS-1$
	public static final String ACTION_BATCH_UPDATE = "DModGroupe"; //$NON-NLS-1$
	public static final String ACTION_RENAME = "DRenome"; //$NON-NLS-1$
	public static final String FIELD_RIGHTS_GROUP = "group";  // suffixed by a group name //$NON-NLS-1$
	public static final String FIELD_RIGHTS_ADMIN = "adminGroup"; // suffixed by a group name //$NON-NLS-1$
	public static final String FIELD_RIGHTS_RECURSIVE = "recurs";
	public static final String FIELD_DESCRIPTION_TITLE_PREFIX = "dirTitle"; // suffixed by a language code //$NON-NLS-1$
	public static final String FIELD_DESCRIPTION_TEXT_PREFIX = "dirText"; // suffixed by a language code //$NON-NLS-1$
	public static final String FIELD_CHILD_DIR = "dirChildName"; //$NON-NLS-1$
	public static final String FIELD_CHILD_DIR_RIGHTS = "dirChildRights"; //$NON-NLS-1$
	public static final String FIELD_IMAGE = "addPicture"; //$NON-NLS-1$
	public static final String FIELD_MOVE_DESTINATION = "dirMoveDest"; //$NON-NLS-1$
	public static final String FIELD_THEME = "dirSelectTheme"; //$NON-NLS-1$
	public static final String FIELD_THEME_DEFAULT = "dirThDefault"; //$NON-NLS-1$
	public static final String FIELD_BATCH_MOVE_DESTINATION = "DDeplGroupeDest"; //$NON-NLS-1$
	public static final String FIELD_RENAME_NEWNAME = "DRenomeNouv"; //$NON-NLS-1$
	public static final String FIELD_BATCH_UPDATE_NAME = "DModGroupeName"; //$NON-NLS-1$
	public static final String FIELD_BATCH_UPDATE_TITLE = "DModGroupeTitle"; //$NON-NLS-1$
	public static final String FIELD_BATCH_UPDATE_TEXT = "DModGroupeText"; //$NON-NLS-1$
	
	public static String processAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, PhotoException {
		
		try {
			if ( !PhotoServlet.isRequestAttributeEmpty(request, ACTION_DESCRIPTION) ) {
				doDirDescriptionAction(caller, toAdmin, request);
			}
			
			if ( !PhotoServlet.isRequestAttributeEmpty(request, ACTION_THEME) ) {
				doDirDescriptionAction(caller, toAdmin, request);
			}
			
			// Note: add child image processed by ajax servlet
			
			if ( !PhotoServlet.isRequestAttributeEmpty(request, ACTION_RIGHTS)  ) {
				doDirRightsAction(caller, toAdmin, request);	
			}
			
			if (!PhotoServlet.isRequestAttributeEmpty(request, ACTION_ADD_CHILD_DIR)) {
				doDirAddChildDirAction(caller, toAdmin, request);	
			}
	
			if (!PhotoServlet.isRequestAttributeEmpty(request, ACTION_DELETE)) {
				doDirDeleteAction(caller, toAdmin,request);	
			}

			if (!PhotoServlet.isRequestAttributeEmpty(request, ACTION_MOVE)) {
				doDirMoveAction(caller, toAdmin, request);
			}
			
			if (!PhotoServlet.isRequestAttributeEmpty(request, ACTION_RENAME)) {
				doDirRenameAction(caller, toAdmin, request);
			}
			
			if (!PhotoServlet.isRequestAttributeEmpty(request, ACTION_BATCH_MOVE)) {
				doBatchMoveAction(caller, toAdmin, request);
			}
			
			if (!PhotoServlet.isRequestAttributeEmpty(request, ACTION_BATCH_DELETE)) {
				doBatchDeleteAction(caller, toAdmin, request);
			}
			
			if (!PhotoServlet.isRequestAttributeEmpty(request, ACTION_BATCH_UPDATE)) {
				doBatchUpdateAction(caller, toAdmin, request);
			}
		
			if (!PhotoServlet.isRequestAttributeEmpty(request, ACTION_UPLOAD_IMAGES)) {
				doImageUploadAction(caller, toAdmin, request);
			}
			
		} catch (PhotoException e) {
			AdminServlet.log.error(e.getMessage(),e);
			caller.setMessage(e.getMessage(), request);
		}
		return null;
	}	
	
	/**
	 * Changes the description (titles, text, theme) of a directory
	 * @param toAdmin
	 * @param request
	 * @throws PhotoException
	 */
	private static void doDirDescriptionAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request) throws PhotoException {
		
		// Get parameters from request
		String[] languages = caller.getConfiguration().getSupportedLocales();
		String titles[] = new String[languages.length];
		String texts[] = new String[languages.length];
		for ( int i = 0 ; i < languages.length ; i++ ) {
			titles[i] = (String)request.getAttribute(FIELD_DESCRIPTION_TITLE_PREFIX+languages[i]) ; 
			texts[i] = (String)request.getAttribute(FIELD_DESCRIPTION_TEXT_PREFIX+languages[i]) ; 
		}
		String theme = (String)request.getAttribute(FIELD_THEME);
		
		// Perform modification
		for ( int i = 0 ; i < languages.length ; i++ ) {
			toAdmin.setTitle( titles[i], new Locale(languages[i]));
			toAdmin.setText( texts[i], new Locale(languages[i]));
		}
		if (theme == null || theme.length() == 0 || 
				theme.equals(FIELD_THEME_DEFAULT)) {
			toAdmin.setTheme("");
		} else {
			toAdmin.setTheme(theme);
		}
		
		// Save results
		caller.getPhotoFactory().writeDescription(toAdmin);
		caller.setMessage("dirDescription.success", request); //$NON-NLS-1$
	}
	
	private static void doDirAddChildDirAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request) throws PhotoException, IOException {
		
		String childName = (String)request.getAttribute(FIELD_CHILD_DIR);
		String rights =  (String)request.getAttribute(FIELD_CHILD_DIR_RIGHTS);
		
		if (childName == null || childName.length() == 0) {
			throw new PhotoException("dirAddChildDir.missingName"); //$NON-NLS-1$
		}
		
		// Create new dir
		PhotoDescription newDir = caller.getPhotoFactory().createChildDirectory(toAdmin.getId(), childName);
		
		// give child creator access rights
		newDir.addAuthorizedGroup(caller.getCurrentUser(request).getGroup());
		newDir.addAdminGroup(caller.getCurrentUser(request).getGroup());
		
		// Use parent rights if option was checked
		if ( rights != null && rights.length() > 0 ) {
			String[] groups = caller.getPhotoUserFactory().readAllGroups();
			for (int i = 0 ; i < groups.length ; i++ ) {
				if (toAdmin.isGroupAuthorized(groups[i]))
					newDir.addAuthorizedGroup(groups[i]);
				if (toAdmin.isGroupAuthorizedToAdmin(groups[i]))
					newDir.addAdminGroup(groups[i]);
			}
		}
		
		// Save rights change to disk
		caller.getPhotoFactory().writeDescription(newDir);
		caller.setMessage("dirAddChildDir.success", request); //$NON-NLS-1$
	}		
	
	private static void doDirRightsAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request) throws PhotoException, IOException {
		
		log.info("Entering dodirRightsAction");
				
		String[] groups;
		groups = caller.getConfiguration().getPhotoUserFactory().readAllGroups(); // all groups + guest
		
		String[] viewingRights = new String[groups.length]; 
		String[] adminRights = new String[groups.length];
		
		boolean recursive = false;
		String recursiveCheckbox = (String)request.getAttribute(FIELD_RIGHTS_RECURSIVE);
		if (recursiveCheckbox != null && recursiveCheckbox.length() > 0 ) {
			log.debug("recursive!");
			recursive = true;
		}
				
		List descriptionsToUpdate = new Vector();
		descriptionsToUpdate.add(toAdmin);
		if (recursive) {
			descriptionsToUpdate.addAll(caller.getPhotoFactory().getAllChildDirectories(toAdmin.getId()));
		}
		for ( int i = 0 ; i < groups.length ; i++ ) {
			viewingRights[i] = (String)request.getAttribute(FIELD_RIGHTS_GROUP+groups[i]);
			adminRights[i] = (String)request.getAttribute(FIELD_RIGHTS_ADMIN+groups[i]);
			if (viewingRights[i] != null && viewingRights[i].length() > 0 ) {
				for (int j = 0 ; j < descriptionsToUpdate.size() ; j++ ) {
					if (!((PhotoDescription)descriptionsToUpdate.get(j)).isGroupAuthorized(groups[i])) {
						((PhotoDescription)descriptionsToUpdate.get(j)).addAuthorizedGroup(groups[i]);
					}
				}
			} else {
				for (int j = 0 ; j < descriptionsToUpdate.size() ; j++ ) {
					((PhotoDescription)descriptionsToUpdate.get(j)).removeAuthorizedGroup(groups[i]);
				}
			}
			if (adminRights[i] != null && adminRights[i].length() > 0) {
				for (int j = 0 ; j < descriptionsToUpdate.size() ; j++ ) {
					if (!((PhotoDescription)descriptionsToUpdate.get(j)).isGroupAuthorizedToAdmin(groups[i])) {
						((PhotoDescription)descriptionsToUpdate.get(j)).addAdminGroup(groups[i]);
					}
				}
			} else {
				for (int j = 0 ; j < descriptionsToUpdate.size() ; j++ ) {
					((PhotoDescription)descriptionsToUpdate.get(j)).removeAdminGroup(groups[i]);
				}
			}
		}
		
		caller.getPhotoFactory().writeDescriptions(descriptionsToUpdate);
		caller.setMessage("dirRights.success", request); //$NON-NLS-1$
	}
	
	private static void doDirDeleteAction(AdminServlet caller, PhotoDescription toAdmin,HttpServletRequest request) throws PhotoException {
		
		// check if toAdmin is root
		if (toAdmin.getPath().equals("")) { //$NON-NLS-1$
			throw new PhotoException("dirDelete.rootError"); //$NON-NLS-1$
		}
		
		caller.getPhotoFactory().deleteAllChildren(toAdmin.getId(), caller.getCurrentUser(request));
		caller.getPhotoFactory().delete( toAdmin.getId());

		caller.setMessage("dirDelete.success", request); //$NON-NLS-1$
	}
	
	private static void doDirMoveAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request) throws PhotoException {
		
		String destinationDirectoryId = (String)request.getAttribute(FIELD_MOVE_DESTINATION);
		
		if (destinationDirectoryId.equals(toAdmin.getId()))
			throw new PhotoException("dirMove.cannotMoveIntoSelf");
		
		if (destinationDirectoryId.equals(toAdmin.getParent().getId()))
			throw new PhotoException("dirMove.destinationIsParent");
		
		PhotoDescription root = caller.getPhotoFactory().getRoot();
		
		if (destinationDirectoryId == root.getId()) {
			// destination is root
			
			// check that user has rights on destination
			if (!root.isUserAuthorizedToAdmin(caller.getCurrentUser(request))) {
				throw new PhotoException("dirMove.destinationNotAuthorized"); //$NON-NLS-1$
			}
			
			caller.getPhotoFactory().move( toAdmin.getId(), root.getId()); //$NON-NLS-1$
			
		} else {
		
			PhotoDescription destination = caller.getPhotoFactory().getDescription(destinationDirectoryId);
			
			// check that destination exists
			if (destination == null) {
				throw new PhotoException("dirMove.destinationNotExist"); //$NON-NLS-1$
			}
			
			// check that user has rights on destination
			if (!destination.isUserAuthorizedToAdmin(caller.getCurrentUser(request))) {
				throw new PhotoException("dirMove.destinationNotAuthorized"); //$NON-NLS-1$
			}
			
			caller.getPhotoFactory().move( toAdmin.getId(), destination.getId());
		}
		
		log.info("Move successful"); //$NON-NLS-1$
		caller.setMessage("dirMove.success", request); //$NON-NLS-1$
	}
	
	private static void doDirRenameAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request) throws PhotoException {
		
		String newName = (String)request.getAttribute(FIELD_RENAME_NEWNAME);
		
		if (newName == null || newName.length() == 0 || (newName.lastIndexOf(PhotoConstants.SEPARATOR_PATH) != -1)) {
			throw new PhotoException("dirRename.invalidName"); //$NON-NLS-1$
		}
		
		caller.getPhotoFactory().rename(toAdmin.getId(), newName);
		caller.setMessage("dirRename.success", request); //$NON-NLS-1$
	}
	
	private static void doBatchMoveAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request) throws PhotoException {
		
		String destination = (String)request.getAttribute(FIELD_BATCH_MOVE_DESTINATION);
		if (destination == null ) {
			throw new PhotoException("dirBatchMove.noDestination"); //$NON-NLS-1$
		}
			
		if (destination.equals(toAdmin.getPath()) ) {
			throw new PhotoException("dirBatchMove.invalidDestination"); //$NON-NLS-1$
		}
		
		caller.getPhotoFactory().getDescription(destination);
		
		// Retrieve list of child dir to move
		int dirs = toAdmin.getNumberOfChildDirectories();
		List dirsToMove = new ArrayList();
		for (int i = 0 ; i < dirs; i++) {
			PhotoDescription dir = toAdmin.getChildDirectory(i);
			String dirCheckbox = (String)request.getAttribute(dir.getName());
			if ( dirCheckbox != null && dirCheckbox.length() > 0)
				dirsToMove.add(dir);
		}
		
		// Retrieve list of child images to move
		int pics = toAdmin.getNumberOfChildImages();
		List picsToMove = new ArrayList();
		for (int i = 0 ; i < pics; i++) {
			PhotoDescription pic = toAdmin.getChildImage(i);
			String picCheckbox = (String)request.getAttribute(pic.getName());
			if ( picCheckbox != null && picCheckbox.length() > 0)
				picsToMove.add(pic);
		}
	
		if (picsToMove.isEmpty() && dirsToMove.isEmpty()) {
			throw new PhotoException("dirBatchMove.noSelection"); //$NON-NLS-1$
		}

		for (int i = 0 ; i < dirsToMove.size() ; i++ ) {
			PhotoDescription dir = (PhotoDescription)dirsToMove.get(i);
			caller.getPhotoFactory().move(dir.getPath(), destination);
		}
		for (int i = 0 ; i < picsToMove.size() ; i++ ) {
			PhotoDescription pic = (PhotoDescription)picsToMove.get(i);
			caller.getPhotoFactory().move(pic.getId(), destination);
		}
		
		caller.setMessage("dirBatchMove.success", request); //$NON-NLS-1$
	}

	private static void doBatchDeleteAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request) throws PhotoException {
							
		// Retrieve list of child dir to delete
		int dirs = toAdmin.getNumberOfChildDirectories();
		List dirsToDelete = new ArrayList();
		for (int i = 0 ; i < dirs; i++) {
			PhotoDescription dir = toAdmin.getChildDirectory(i);
			String dirCheckbox = (String)request.getAttribute(dir.getName());
			if ( dirCheckbox != null && dirCheckbox.length() > 0)
				dirsToDelete.add(dir);
		}
		
		// Retrieve list of child images to move
		int pics = toAdmin.getNumberOfChildImages();
		List picsToDelete = new ArrayList();
		for (int i = 0 ; i < pics; i++) {
			PhotoDescription pic = toAdmin.getChildImage(i);
			String picCheckbox = (String)request.getAttribute(pic.getName());
			if ( picCheckbox != null && picCheckbox.length() > 0)
				picsToDelete.add(pic);
		}
	
		if (picsToDelete.isEmpty() && dirsToDelete.isEmpty()) {
			throw new PhotoException("dirBatchDelete.noSelection"); //$NON-NLS-1$
		}

		for (int i = 0 ; i < dirsToDelete.size() ; i++ ) {
			PhotoDescription dir = (PhotoDescription)dirsToDelete.get(i);
			caller.getPhotoFactory().delete(dir.getId());
		}
		for (int i = 0 ; i < picsToDelete.size() ; i++ ) {
			PhotoDescription pic = (PhotoDescription)picsToDelete.get(i);
			caller.getPhotoFactory().delete(pic.getId());
		}
		
		caller.setMessage("dirBatchDelete.success", request); //$NON-NLS-1$
	}
	
	private static void doBatchUpdateAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request) throws PhotoException {
		
		// Retrieve list of child dir to update
		int dirs = toAdmin.getNumberOfChildDirectories();
		List dirsToUpdate = new ArrayList();
		List newNamesForDirs = new ArrayList();
		List newTitlesForDirs = new ArrayList();
		List newTextsForDirs = new ArrayList();
		for (int i = 0 ; i < dirs; i++) {
			PhotoDescription dir = toAdmin.getChildDirectory(i);
			String dirCheckbox = (String)request.getAttribute(dir.getName());
			if ( dirCheckbox != null && dirCheckbox.length() > 0) {
				String newName = (String)request.getAttribute(FIELD_BATCH_UPDATE_NAME+i);
				if (newName == null || newName.length() == 0 || (newName.lastIndexOf(PhotoConstants.SEPARATOR_PATH) != -1)) {
					throw new PhotoException("dirRename.invalidName"); //$NON-NLS-1$
				}
				String newTitle = (String)request.getAttribute(FIELD_BATCH_UPDATE_TITLE+i);
				String newText = (String)request.getAttribute(FIELD_BATCH_UPDATE_TEXT+i);
				dirsToUpdate.add(dir);
				newNamesForDirs.add(newName);
				newTitlesForDirs.add(newTitle);
				newTextsForDirs.add(newText);
			}
		}
		
		// Retrieve list of child images to update
		int pics = toAdmin.getNumberOfChildImages();
		List picsToUpdate = new ArrayList();
		List newNamesForPics = new ArrayList();
		List newTitlesForPics = new ArrayList();
		List newTextsForPics = new ArrayList();
		for (int i = 0 ; i < pics; i++) {
			PhotoDescription pic = toAdmin.getChildImage(i);
			String picCheckbox = (String)request.getAttribute(pic.getName());
			if ( picCheckbox != null && picCheckbox.length() > 0) {
				String newName = (String)request.getAttribute(FIELD_BATCH_UPDATE_NAME+(dirs+i));
				if (newName == null || newName.length() == 0 || (newName.lastIndexOf(PhotoConstants.SEPARATOR_PATH) != -1)) {
					throw new PhotoException("imgRename.invalidName"); //$NON-NLS-1$
				}
				String newTitle = (String)request.getAttribute(FIELD_BATCH_UPDATE_TITLE+(dirs+i));
				String newText = (String)request.getAttribute(FIELD_BATCH_UPDATE_TEXT+(dirs+i));
				picsToUpdate.add(pic);
				newNamesForPics.add(newName);
				newTitlesForPics.add(newTitle);
				newTextsForPics.add(newText);
			}
		}
	
		if (picsToUpdate.isEmpty() && dirsToUpdate.isEmpty()) {
			throw new PhotoException("dirBatchUpdate.noSelection"); //$NON-NLS-1$
		}

		for (int i = 0 ; i < dirsToUpdate.size() ; i++ ) {
			PhotoDescription dir = (PhotoDescription)dirsToUpdate.get(i);
			dir.setTitle((String)newTitlesForDirs.get(i), new Locale(caller.getCurrentUser(request).getLanguage()));
			dir.setText((String)newTextsForDirs.get(i), new Locale(caller.getCurrentUser(request).getLanguage()));
			caller.getPhotoFactory().writeDescription(dir);
			String newName = (String)newNamesForDirs.get(i);
			if (!dir.getName().equals(newName)) 
				caller.getPhotoFactory().rename(dir.getId(), newName );
		}
		for (int i = 0 ; i < picsToUpdate.size() ; i++ ) {
			PhotoDescription pic = (PhotoDescription)picsToUpdate.get(i);
			pic.setTitle((String)newTitlesForPics.get(i), new Locale(caller.getCurrentUser(request).getLanguage()));
			pic.setText((String)newTextsForPics.get(i), new Locale(caller.getCurrentUser(request).getLanguage()));
			caller.getPhotoFactory().writeDescription(pic);
			String newName = (String)newNamesForPics.get(i);
			String extension = pic.getPath().substring(pic.getPath().lastIndexOf(PhotoConstants.SEPARATOR_EXTENSION));
			newName += extension;
			if (!pic.getName().equals(newName))
				caller.getPhotoFactory().rename(pic.getId(), newName);
		}
		
		caller.setMessage("dirBatchUpdate.success", request); //$NON-NLS-1$
	}
	
	private static void doImageUploadAction(AdminServlet caller, PhotoDescription toAdmin, HttpServletRequest request) throws PhotoException {
		
		if ( !toAdmin.isUserAuthorizedToAdmin(caller.getCurrentUser(request))) {
			log.warn("Access rights not sufficient for admin!");
			// delete any temp file
			int numberOfUpload = 1;
			FileItem image;
			while ( (image = (FileItem)request.getAttribute(FIELD_IMAGE+numberOfUpload)) != null) {
				numberOfUpload++;
				image.delete();
			}
			request.getSession().setAttribute(AjaxServlet.SESSION_ATTRIBUTE_UPLOAD_STATUS, "Operation not authorized");
			request.getSession().setAttribute(AjaxServlet.SESSION_ATTRIBUTE_UPLOAD_PROGRESS, 100d);
			request.getSession().setAttribute(AjaxServlet.SESSION_ATTRIBUTE_UPLOAD_COMPLETE, AjaxServlet.UPLOAD_COMPLETE);
			throw new PhotoException("User not allowed to upload!");
		}
		
		int numberOfUpload = 1;
		String successMessage = "";
		String errorMessage = "";
		FileItem image;
		while ( (image = (FileItem)request.getAttribute(FIELD_IMAGE+numberOfUpload)) != null) {
			numberOfUpload++;
			if (image.getName().length() == 0)
				break;
			try {
				String name = image.getName();
				name = FilenameUtils.getName(name);
				File newImage = new File(name);
				image.write(newImage);		
	
				caller.getPhotoFactory().createChildImage( toAdmin.getId(), newImage);
				if (successMessage.length() > 0)
					successMessage += ", ";
				successMessage += name;
				
			} catch (Exception e) {
				if (errorMessage.length() > 0)
					errorMessage += ", ";
				errorMessage += FilenameUtils.getName(image.getName());
				errorMessage += ": "+e.getMessage();
				log.error(e.getMessage(),e);	
			} finally {
				image.delete();
			}
		}
		if (successMessage.length() > 0)
			successMessage = Message.getResource("dirImage.success",caller.getCurrentUser(request).getLanguage())+" ("+successMessage+"). ";
		if (errorMessage.length() > 0)
			errorMessage = Message.getResource("dirImage.error",caller.getCurrentUser(request).getLanguage())+" ("+errorMessage+"). ";
		
		// Set 'manually' since no need to internationalize (already done)
		request.getSession().setAttribute(AjaxServlet.SESSION_ATTRIBUTE_UPLOAD_STATUS,successMessage+errorMessage);
		// put upload status to 100% in session
		request.getSession().setAttribute(AjaxServlet.SESSION_ATTRIBUTE_UPLOAD_PROGRESS, 100d);
		request.getSession().setAttribute(AjaxServlet.SESSION_ATTRIBUTE_UPLOAD_COMPLETE, AjaxServlet.UPLOAD_COMPLETE);
	}
}