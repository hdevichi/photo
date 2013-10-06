package org.devichi.photo.control.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.devichi.photo.model.user.PhotoUser;
import org.devichi.photo.utils.PhotoException;

public class AdminUserControl {

	public static final String FIELD_USER_SELECT = "userSelect"; //$NON-NLS-1$
	public static final String FIELD_USER_ADMIN = "user_admin"; //$NON-NLS-1$
	public static final String FIELD_USER_GROUP = "user_group"; //$NON-NLS-1$
	
	public static void processAction(AdminServlet caller, PhotoUser user, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if (!caller.getCurrentUser(request).isAdmin()) {
			caller.setMessage("forbidden", request);
			caller.redirectToLogin(request, response);
			return;
		}
		
		try {
			doAppUpdateUserAction(caller, user, request, response);
		} catch (PhotoException e) {
			AdminServlet.log.error(e.getMessage(),e);
			caller.setMessage(e.getMessage(), request);
		}
	}
	
	private static void doAppUpdateUserAction(AdminServlet caller, PhotoUser toAdmin, HttpServletRequest request, HttpServletResponse response) throws PhotoException {
		
		String login = (String)request.getAttribute(FIELD_USER_SELECT);
		String admin = (String)request.getAttribute(FIELD_USER_ADMIN);
		String group = (String)request.getAttribute(FIELD_USER_GROUP);
		
		// no param check needed (only combos & checkbox; cannot be missing)
		
		boolean isAdmin = false;
		if (admin != null && admin.equals(PhotoConstants.CHECKBOX_ON))
			isAdmin = true;
		
		PhotoUser user = caller.getConfiguration().getPhotoUserFactory().readUser(login);
		user.setAdmin(isAdmin);
		user.setGroup(group);
		caller.getConfiguration().getPhotoUserFactory().updateUser(user);
		caller.setMessage("updateUser.success", request); //$NON-NLS-1$
	}
}
