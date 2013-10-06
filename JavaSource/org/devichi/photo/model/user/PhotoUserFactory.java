package org.devichi.photo.model.user;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import org.apache.log4j.Logger;
import org.devichi.photo.control.servlet.PhotoConstants;
import org.devichi.photo.model.config.PhotoConfiguration;
import org.devichi.photo.utils.PhotoException;

public abstract class PhotoUserFactory {

	static final Logger log = Logger.getLogger(PhotoUserFactory.class);
		
	protected Hashtable<String, PhotoUser> userCache = new Hashtable<String, PhotoUser>();
	private PhotoConfiguration config;
	private Random cookieIdGenerator;
	
	public PhotoUserFactory() {	
		cookieIdGenerator = new Random(System.currentTimeMillis());
	}
	
	public void setConfiguration(PhotoConfiguration config) {
		this.config = config;
	}
	
	public PhotoConfiguration getConfiguration() {
		if (config == null)
			throw new RuntimeException(PhotoConstants.CONFIG_ERROR);
		return config;
	}
	
	public PhotoUserFactory( PhotoConfiguration config) {
		this.config = config;
	}
		
	public PhotoUser getGuestUser(String locale) { 
			
		return new PhotoUser(
		PhotoConstants.GUEST_GROUP_NAME, 
		PhotoConstants.GUEST_GROUP_NAME,
		PhotoConstants.GUEST_GROUP_NAME, 
		locale, 
		false, getConfiguration().getDefaultColumns(), 
		getConfiguration().getDefaultTheme(),
		"", false, -1L);  //$NON-NLS-1$
	}
		
	/**
	 * return null if not found, or if login is null
	 * throws runtime exception if exception
	 */
	public PhotoUser readUser( String login ) throws PhotoException {
		
		if (login == null)
			return null;
			
		PhotoUser user = userCache.get(login);
		if ( user != null) {
			if (user.getAge() < (1000 * getConfiguration().getUserTimeout()) )
				return user;
		}
				
		user = internalReadUser( login) ;
		if (user!= null)
			userCache.put(login,user);
		return user;

	}
		
	public synchronized void deleteUser( String login ) throws PhotoException {	
			
		if (login == null)
			return;
		
		if (!userExists(login))
			throw new PhotoException(PhotoConstants.USER_MISSING_ERROR);
		
		internalDeleteUser( login );	
		userCache.remove( login );		
	}
	
	/**
	 * Creates an user in the default group
	 * @param login
	 * @param password
	 * @param language
	 * @param theme
	 * @param columns
	 * @param email
	 * @throws PhotoException
	 */
	public synchronized void addUser( String login, String password, String language, String theme, int columns, String email ) throws PhotoException {
		
		if (userExists(login))
			throw new PhotoException(PhotoConstants.USER_ALREADY_EXISTS_ERROR);
		
		long cookieId = cookieIdGenerator.nextLong();
		PhotoUser user = new PhotoUser(login, password, getConfiguration().getDefaultGroup(), language, false, columns, theme, email, false, cookieId);
		internalAddUser(user);
		userCache.put(user.getLogin(), user);	
	}
	
	public ArrayList<PhotoUser> readAllUsers() throws PhotoException {
				
		// Read the list of logins
		String[] logins = readAllLogins();
			
		ArrayList<PhotoUser> users = new ArrayList<PhotoUser>();
		for (String s : logins) {
			users.add(readUser(s));
		}	
		return users;
	}

	public boolean userExists(String login) throws PhotoException {

		if (login == null)
			return false;
		
		return (readUser(login) != null);	
	}

	protected abstract void internalDeleteUser( String login ) throws PhotoException;
	protected abstract void internalAddUser( PhotoUser user ) throws PhotoException;
	public abstract void updateUser( PhotoUser user ) throws PhotoException;
	protected abstract PhotoUser internalReadUser( String login ) throws PhotoException;	
	
	public abstract void deleteGroup( String group ) throws PhotoException;
	public abstract void addGroup( String group ) throws PhotoException;
	public abstract boolean groupExists(String group) throws PhotoException;
	
	public abstract String[] readAllGroups() throws PhotoException;
	public abstract String[] readAllLogins() throws PhotoException;	
	
	public abstract void initBase() throws PhotoException;
	
	/**
	 * Performs a Basic test ensuring:
	 * - that the user database is reachable 
	 * - that it contains at least one admin user
	 **/
	public abstract boolean test()  throws PhotoException;
}
