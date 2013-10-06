package org.devichi.photo.model.user;

import org.apache.log4j.Logger;
import org.devichi.photo.control.servlet.PhotoConstants;

/**
 * Describes an user.
 * 
 * Users belong to a group (groups are used to determine access right
 * 
 * There is 3 levels of users: normal and admin + guest
 * 
 * @author Hadrien Devichi
 */
public class PhotoUser {

	static final Logger log = Logger.getLogger(PhotoUser.class);
	
	private String login;
	private String group;
	private boolean isAdmin;
	private boolean displayAdmin;
	private String password;
	private String email;
	private long cookieId; // 64 bit integer specific to the user, avoiding to store pwd in automatic login cookie
	
	// user preferences
	private String language;
	private int columns;
	private String theme;
	
	private long creationTime;

	/**
	 * Create an user
	 * @param login
	 * @param lang
	 */
	protected PhotoUser(String login, String password, String group, String lang, boolean admin, int columns, String theme, String email, boolean displayAdmin, long cookieId ) {
		
		if (login == null || password == null || group == null || lang == null || theme == null) {
			log.error("Error: PhotoUser constructor, null parameter");
			throw new RuntimeException(PhotoConstants.NULL_PARAMETER_ERROR);
		}
		
		if (columns < 1) {
			log.error("Invalid columns parameter at user creation");
			throw new RuntimeException(PhotoConstants.INVALID_PARAMETER_ERROR);
		}
		this.login = login;
		this.password = password;
		this.isAdmin = admin;
		this.language = lang;
		this.group = group;
		this.columns = columns;
		this.theme = theme;
		this.email = email;
		this.cookieId = cookieId;
		this.displayAdmin = displayAdmin;
		
		this.creationTime = System.currentTimeMillis();
	}

	public void setAdmin(boolean b) {
		isAdmin = b;
	}
	
	public void setGroup(String g) {
		group = g;
	}
	
	public String getLogin() {
		return login;
	}
	
	public String getGroup() {
		return group;
	}
	
	public boolean updatePassword( String oldPassword, String newPassword ) {
		
		if (oldPassword != null && oldPassword.equals(newPassword)) {
			password = newPassword;
			return true;
		}
		return false;
	}
	
	public boolean isInGroup(String g ) {
		
		if (g == null) {
			log.error("Error: is not group, null parameter");
			throw new RuntimeException(PhotoConstants.NULL_PARAMETER_ERROR);
		}
		return group.equals(g);
	}
	
	public boolean isAdmin() {
		return isAdmin;
	}
	
	public boolean isGuest() {
		if (group == null || group.length() == 0)
			return true;
		return group.equals(PhotoConstants.GUEST_GROUP_NAME);
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String s) {
		language = s;
	}
	
	
	
	protected long getAge() {
		return System.currentTimeMillis() - creationTime;
	}
	
	public String getPassword() {
		return password;
	}
	
	public boolean checkPassword(String s) {
		return (s.equals(password));
	}
	
	public String getTheme() {
		return theme;
	}
	
	public void setTheme( String th) {
		theme = th;
	}
	
	public int getColumns() {
		return columns;
	}
	
	public void setColumns(int c) {
		if (c < 1) {
			log.error("invalid parameter for setColumns "+c);
			throw new RuntimeException(PhotoConstants.INVALID_PARAMETER_ERROR);
		}
		columns = c;
	}
	
	public void setEmail(String s) {
		email = s;
	}
	
	public String getEmail() {
		return email;
	}
	
	public long getCookieId() {
		return cookieId;
	}
	
	public void setDisplayAdmin(boolean b) {
		displayAdmin = b;
	}
	
	public boolean getDisplayAdmin() {
		return displayAdmin;
	}
	
	public String toString() {
		
		StringBuffer string = new StringBuffer(login);
		if (isAdmin)
			string.append("[admin]"); //$NON-NLS-1$
		string.append(" ("); //$NON-NLS-1$
		string.append(group);
		string.append(")"); //$NON-NLS-1$
		string.append(",lang: "); //$NON-NLS-1$
		string.append(language);
		string.append(", theme: "); //$NON-NLS-1$
		string.append(theme);
		string.append(", col: "); //$NON-NLS-1$
		string.append(columns);
		string.append(", email: "); //$NON-NLS-1$
		string.append(email);
		return string.toString();
	}
}
