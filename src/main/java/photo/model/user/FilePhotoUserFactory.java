package photo.model.user;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration.Node;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import photo.control.servlet.PhotoConstants;
import photo.utils.PhotoException;
import photo.utils.PhotoException;

/**
 * An implementation of PhotoUserFactory that uses the file system
 * The users file is kept at the root of the photo directory
 * 
 * The users file is a XML file file, structured as:
 * <configuration>
 * 		<groups>
 * 			<group name=""/>
 * 		</groups>
 * 		<users>
 * 			<user
 * 				password=""
 * 				group=""
 * 				language=""
 * 				columns=""
 * 				email=""
 * 				cookie=""
 * 				admin=""
 * 				theme=""
 * 				displayAdmin="">login</user>
 * 		</users>
 * </configuration>
 * 
 * @author Hadri
 *
 */
public class FilePhotoUserFactory extends PhotoUserFactory {

	// File used to save the configuration (in the image root directory)
	public static final String USERS_FILE = "users.properties";
	
	private XMLConfiguration users;
	
	/**
	 * return null if not found
	 * throws runtime exception if exception
	 */
	protected PhotoUser internalReadUser( String login ) throws PhotoException {
		
		if (login == null)
			return null;
		
		if ( users.getString("users/user[@login='"+login+"']/@login") == null)
			return null;
		
		XMLConfiguration users = getUsers();
				
		String password = users.getString("users/user[@login='"+login+"']/@password");
		String group = users.getString("users/user[@login='"+login+"']/@group");
		String language = users.getString("users/user[@login='"+login+"']/@language");
		boolean admin = users.getBoolean("users/user[@login='"+login+"']/@admin");
		int columns = users.getInt("users/user[@login='"+login+"']/@columns");
		String theme = users.getString("users/user[@login='"+login+"']/@theme");
		String email = users.getString("users/user[@login='"+login+"']/@email");
		boolean displayAdmin = users.getBoolean("users/user[@login='"+login+"']/@displayAdmin");
		long cookieId = users.getLong("users/user[@login='"+login+"']/@cookie");
			
		if (columns <= 0) {
			columns = getConfiguration().getDefaultColumns();
		}
	
		if (theme == null || theme.length() == 0) {
			theme = getConfiguration().getDefaultTheme();
		}
		
		if (group == null || group.length() == 0) {
			group = PhotoConstants.GUEST_GROUP_NAME;
		}
			
		return new PhotoUser( login, password, group, language, admin, columns, theme, email, displayAdmin, cookieId);
	}
	
	protected void internalDeleteUser( String login ) throws PhotoException {
		
		XMLConfiguration users = getUsers();
		
		users.clearTree("users/user[@login='"+login+"']");
		
		try {
			users.save();
		} catch (ConfigurationException e) {
			log.error(e.getMessage(), e);
			throw new PhotoException("Error while deleting group",e);
		}
	}
	
	public synchronized void deleteGroup( String group ) throws PhotoException {
		
		if (group == null)
			return;
			
		if (group.equals(PhotoConstants.GUEST_GROUP_NAME))
			throw new PhotoException(PhotoConstants.CANNOT_DELETE_GUEST_ERROR);
		
		if (!groupExists(group))
			throw new PhotoException(PhotoConstants.GROUP_MISSING_ERROR);
		
		if (isGroupUsed(group))
			throw new PhotoException("group is in use, remove group users first!");
		
		XMLConfiguration users = getUsers();
		
		users.clearTree("groups/group[@name='"+group+"']");
		
		
		try {
			users.save();
		} catch (ConfigurationException e) {
			log.error(e.getMessage(), e);
			throw new PhotoException("Error while deleting group",e);
		}
	}
	
	public synchronized void updateUser( PhotoUser user ) throws PhotoException {
		
		if (user == null)
			throw new PhotoException("updateUser error: null argument");
		
		if ( !groupExists(user.getGroup()))
			throw new PhotoException("updateUser error: the user's group does not exist!");
		
		XMLConfiguration users = getUsers();
		
		String test = users.getString("users/user[@login='"+user.getLogin()+"']/@login");
		if (test == null || test.length() == 0)
			throw new PhotoException("Impossible to update user: user does not exist");
		
		users.clearProperty("users/user[@login='"+user.getLogin()+"']/@password");
		users.addProperty("users/user[@login='"+user.getLogin()+"'] @password", user.getPassword());
		users.clearProperty("users/user[@login='"+user.getLogin()+"']/@theme");
		users.addProperty("users/user[@login='"+user.getLogin()+"'] @theme", user.getTheme());
		users.clearProperty("users/user[@login='"+user.getLogin()+"']/@admin");
		users.addProperty("users/user[@login='"+user.getLogin()+"'] @admin", user.isAdmin());
		users.clearProperty("users/user[@login='"+user.getLogin()+"']/@columns");
		users.addProperty("users/user[@login='"+user.getLogin()+"'] @columns", user.getColumns());
		users.clearProperty("users/user[@login='"+user.getLogin()+"']/@displayAdmin");
		users.addProperty("users/user[@login='"+user.getLogin()+"'] @displayAdmin", user.getDisplayAdmin());
		users.clearProperty("users/user[@login='"+user.getLogin()+"']/@cookie");
		users.addProperty("users/user[@login='"+user.getLogin()+"'] @cookie", user.getCookieId());
		users.clearProperty("users/user[@login='"+user.getLogin()+"']/@email");
		users.addProperty("users/user[@login='"+user.getLogin()+"'] @email", user.getEmail());
		users.clearProperty("users/user[@login='"+user.getLogin()+"']/@group");
		users.addProperty("users/user[@login='"+user.getLogin()+"'] @group", user.getGroup());
		users.clearProperty("users/user[@login='"+user.getLogin()+"']/@language");
		users.addProperty("users/user[@login='"+user.getLogin()+"'] @language", user.getLanguage());

		try {
			users.save();
		} catch (ConfigurationException e) {
			log.error(e.getMessage(), e);
			throw new PhotoException("Error while saving user",e);
		}
	}
	
	protected synchronized void internalAddUser( PhotoUser user ) throws PhotoException {
		
		if (user == null)
			throw new PhotoException("addUser error: null argument");
		
		if (!user.getGroup().equals(PhotoConstants.GUEST_GROUP_NAME) && !groupExists(user.getGroup()))
			throw new PhotoException("addUser error: the user's group does not exist!");
		
		// No need to test prior existence: superclass does it
		
		XMLConfiguration users = getUsers();
		
		Node newUser = new HierarchicalConfiguration.Node("user");
		newUser.addAttribute(new HierarchicalConfiguration.Node("login",user.getLogin()));
		newUser.addAttribute(new HierarchicalConfiguration.Node("password",user.getPassword()));
		newUser.addAttribute(new HierarchicalConfiguration.Node("group",user.getGroup()));
		newUser.addAttribute(new HierarchicalConfiguration.Node("language",user.getLanguage()));
		newUser.addAttribute(new HierarchicalConfiguration.Node("theme",user.getTheme()));
		newUser.addAttribute(new HierarchicalConfiguration.Node("admin",user.isAdmin()));
		newUser.addAttribute(new HierarchicalConfiguration.Node("cookie",user.getCookieId()));
		newUser.addAttribute(new HierarchicalConfiguration.Node("columns",user.getColumns()));
		newUser.addAttribute(new HierarchicalConfiguration.Node("displayAdmin",user.getDisplayAdmin()));
		newUser.addAttribute(new HierarchicalConfiguration.Node("email",user.getEmail()));
		List nodes = new Vector();
		nodes.add(newUser);
		users.addNodes("users", nodes);
		
		try {
			users.save();
		} catch (ConfigurationException e) {
			log.error(e.getMessage(), e);
			throw new PhotoException("Error while saving user",e);
		}
	}
	
	public synchronized void addGroup( String group ) throws PhotoException {
		
		// Default group should not be put into database
		if (group.equals(PhotoConstants.GUEST_GROUP_NAME))
			return;
		
		if (groupExists(group))
			throw new PhotoException("This group already exists");

		XMLConfiguration users = getUsers();
		
		Node newGroup = new HierarchicalConfiguration.Node("group");
		Node newGroupAttribute = new HierarchicalConfiguration.Node("name", group);
		newGroupAttribute.setAttribute(true);
		newGroup.addAttribute(newGroupAttribute);
		List nodes = new Vector();
		nodes.add(newGroup);
		users.addNodes("groups", nodes); 
		try {
			users.save();
		} catch (ConfigurationException e) {
			log.error(e.getMessage(), e);
			throw new PhotoException("Error while saving group",e);
		}
	}
	
	private XMLConfiguration getUsers() throws PhotoException {
		
		if (users == null) {
			try {
			users = new XMLConfiguration(getUserFile());
			users.setEncoding(PhotoConstants.ENCODING);
			users.setExpressionEngine(new XPathExpressionEngine());
			} catch (ConfigurationException e) {
				log.error(e.getMessage(),e);
				throw new PhotoException("Error reading users",e);
			}
		}
		return users;
	}
	
	private File getUserFile() throws PhotoException {
		
		File usersFile = new File(getConfiguration().getImageRoot(), USERS_FILE); 
		return usersFile;
	}

	/**
	 * Return a list of all the groups
	 * @return
	 */
	public String[] readAllGroups() throws PhotoException {
		
		// Read all used groups
		XMLConfiguration users = getUsers();
		String[] groups = users.getStringArray("groups/group/@name");
		String[] groupsWithGuest = Arrays.copyOf(groups, groups.length+1);
		groupsWithGuest[groups.length] = PhotoConstants.GUEST_GROUP_NAME;
		return groupsWithGuest;
	}
	
	public String[] readAllLogins() throws PhotoException {
		
		XMLConfiguration users = getUsers();
		return users.getStringArray("users/user/@login");
	}
	
	public boolean groupExists(String group ) throws PhotoException {
		
		if (group == null || group.length() == 0)
			return false;
		
		if (group.equals(PhotoConstants.GUEST_GROUP_NAME))
			return true;
		
		XMLConfiguration users = getUsers();
		return (users.getString("groups/group[@name='"+group+"']/@name") != null);
	}
	
	/**
	 * Will create the users & groups node if they don't exist - otherwise, do nothing 
	 */
	public void initBase() throws PhotoException {

		try {
			
			XMLConfiguration config = getUsers();
			try {
				users.configurationAt("groups");
			} catch (Exception e) {
				users.clearProperty("groups");
				Node node = new HierarchicalConfiguration.Node("groups");
				List nodes = new Vector();
				nodes.add(node);
				config.addNodes("", nodes);
				config.save();
			}
			try {
				users.configurationAt("users");
			} catch (Exception e) {
				Node node = new HierarchicalConfiguration.Node("users");
				List nodes = new Vector();
				nodes.add(node);
				config.addNodes("", nodes);
				config.save();
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new PhotoException("Error creating user file",e);
		}
	}
	
	public boolean test() throws PhotoException {

		XMLConfiguration users = getUsers();
		
		if (users.getStringArray("groups/group/@name").length == 0)
			return false;
		
		if (users.getStringArray("users/user[@admin='true']/@login").length == 0)
			return false;
	
		return true;
	}
	
	protected boolean isGroupUsed( String group ) throws PhotoException {
		
		if (group == null || group.length() == 0)
			throw new PhotoException("isGroupUsed: Invalid argument");
		
		if (group.equals(PhotoConstants.GUEST_GROUP_NAME))
			return true;
		
		// Read all logins using this group
		XMLConfiguration users = getUsers();
		String[] logins = users.getStringArray("users/user[@group='"+group+"']/@login");
		if (logins != null && logins.length > 0)
			return true;
		return false;
	}
}
