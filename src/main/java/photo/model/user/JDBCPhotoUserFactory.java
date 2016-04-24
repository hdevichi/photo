package photo.model.user;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import photo.control.servlet.PhotoConstants;
import photo.utils.PhotoException;
import photo.utils.PhotoException;

/**
 * An implementation of PhotoUserFactory that uses a MySQL database
 * @author Hadri
 *
 */
public class JDBCPhotoUserFactory extends PhotoUserFactory {

	// database description constants
	public static final String DATABASE_GROUPS_TABLE = "userGroups"; //$NON-NLS-1$
	public static final String DATABASE_GROUPS_TABLE_NAME = "name"; //$NON-NLS-1$
	public static final String DATABASE_USERS_TABLE = "users"; //$NON-NLS-1$
	public static final String DATABASE_USERS_TABLE_LOGIN = "login"; //$NON-NLS-1$
	public static final String DATABASE_USERS_TABLE_PASSWORD = "password"; //$NON-NLS-1$
	public static final String DATABASE_USERS_TABLE_GROUP = "groupName"; //$NON-NLS-1$
	public static final String DATABASE_USERS_TABLE_LANGUAGE = "language"; //$NON-NLS-1$
	public static final String DATABASE_USERS_TABLE_ADMIN = "admin"; //$NON-NLS-1$
	public static final String DATABASE_USERS_TABLE_COLUMNS = "nbColumns"; //$NON-NLS-1$
	public static final String DATABASE_USERS_TABLE_THEME = "theme"; //$NON-NLS-1$
	public static final String DATABASE_USERS_TABLE_EMAIL = "email"; //$NON-NLS-1$
	public static final String DATABASE_USERS_TABLE_COOKIE_ID = "cookieId"; //$NON-NLS-1$
	public static final String DATABASE_USERS_TABLE_DISPLAY_ADMIN = "displayAdmin"; //$NON-NLS-1$
	
	/**
	 * return null if not found
	 * throws runtime exception if exception
	 */
	protected PhotoUser internalReadUser( String login ) throws PhotoException {
		
		if (login == null)
			return null;
			
		Connection connection = null;
		Statement statement = null;
		ResultSet rc = null;
		try {
			connection = getUserDatabaseConnection();
			statement = connection.createStatement();
		
			StringBuffer query = new StringBuffer("Select "); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE).append("."); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE_PASSWORD).append(", "); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE).append("."); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE_GROUP).append(", "); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE).append("."); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE_LANGUAGE).append(", "); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE).append("."); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE_ADMIN).append(", "); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE).append("."); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE_COLUMNS).append(", "); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE).append("."); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE_THEME).append(", "); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE).append("."); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE_EMAIL).append(", ");
			query.append(DATABASE_USERS_TABLE).append("."); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE_DISPLAY_ADMIN).append(", ");
			query.append(DATABASE_USERS_TABLE).append("."); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE_COOKIE_ID).append(" from "); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE).append(" where "); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE).append("."); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE_LOGIN);
			query.append(" = '").append(StringEscapeUtils.escapeSql(login)).append("'"); //$NON-NLS-1$  //$NON-NLS-2$
			rc = statement.executeQuery(query.toString());
			
			// Only 0 or 1 result (logins are unique)
			if (rc.next() == false ) {
				return null;
			}
	
			String password = rc.getString(1);
			String group = rc.getString(2);
			String language = rc.getString(3);
			boolean admin = rc.getBoolean(4);
			int columns = rc.getInt(5);
			String theme = rc.getString(6);
			String email = rc.getString(7);
			boolean displayAdmin = rc.getBoolean(8);
			long cookieId = rc.getLong(9);
			
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
		
		} catch (SQLException e) {
			throw new PhotoException(e.getMessage());
		} finally {
			try {
				if (rc != null)
					rc.close();
				if (statement != null)
					statement.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new PhotoException(e.getMessage());
			}
		}
	}
	
	protected void internalDeleteUser( String login ) throws PhotoException {
		
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getUserDatabaseConnection();
			statement = connection.createStatement();
			
			StringBuffer update = new StringBuffer("Delete from "); //$NON-NLS-1$
			update.append(DATABASE_USERS_TABLE).append(" where "); //$NON-NLS-1$
			update.append(DATABASE_USERS_TABLE).append("."); //$NON-NLS-1$
			update.append(DATABASE_USERS_TABLE_LOGIN).append(" = '"); //$NON-NLS-1$
			update.append(StringEscapeUtils.escapeSql(login)).append("'"); //$NON-NLS-1$
			statement.executeUpdate(update.toString());

		} catch (SQLException e) {
			throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);
		} finally {
			try {	
				if (statement != null)
					statement.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);
			}
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
			throw new PhotoException("deleteGroup error: group is in use, remove group users first!");
		
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getUserDatabaseConnection();
			statement = connection.createStatement();
	
			StringBuffer update = new StringBuffer("Delete from "); //$NON-NLS-1$
			update.append(DATABASE_GROUPS_TABLE).append(" where "); //$NON-NLS-1$
			update.append(DATABASE_GROUPS_TABLE).append("."); //$NON-NLS-1$
			update.append(DATABASE_GROUPS_TABLE_NAME).append(" = '"); //$NON-NLS-1$
			update.append(StringEscapeUtils.escapeSql(group)).append("'"); //$NON-NLS-1$
			statement.executeUpdate(update.toString());

		} catch (SQLException e) {
			throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);
		} finally {
			try {	
				if (statement != null)
					statement.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);
			}
		}
	}
	
	public synchronized void updateUser( PhotoUser user ) throws PhotoException {
		
		if (user == null)
			throw new PhotoException("updateUser error: null argument");
		
		if ( !user.getGroup().equals(PhotoConstants.GUEST_GROUP_NAME) && !groupExists(user.getGroup()))
			throw new PhotoException("updateUser error: the user's group does not exist!");
		
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getUserDatabaseConnection();
			statement = connection.createStatement();
			
			StringBuffer update = new StringBuffer("Update "); //$NON-NLS-1$
			update.append(DATABASE_USERS_TABLE).append(" set "); //$NON-NLS-1$
			update.append(DATABASE_USERS_TABLE).append(".").append(DATABASE_USERS_TABLE_THEME); //$NON-NLS-1$
			update.append(" = '").append(StringEscapeUtils.escapeSql(user.getTheme())).append("', "); //$NON-NLS-1$ //$NON-NLS-2$
			update.append(DATABASE_USERS_TABLE).append(".").append(DATABASE_USERS_TABLE_LANGUAGE); //$NON-NLS-1$
			update.append(" = '").append(StringEscapeUtils.escapeSql(user.getLanguage())).append("', "); //$NON-NLS-1$ //$NON-NLS-2$
			update.append(DATABASE_USERS_TABLE).append(".").append(DATABASE_USERS_TABLE_COLUMNS); //$NON-NLS-1$
			update.append(" = '").append(user.getColumns()).append("', ");	 //$NON-NLS-1$			 //$NON-NLS-2$
			update.append(DATABASE_USERS_TABLE).append(".").append(DATABASE_USERS_TABLE_EMAIL); //$NON-NLS-1$
			update.append(" = '").append(StringEscapeUtils.escapeSql(user.getEmail())).append("', "); //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-2$
			int isAdmin = 0;
			if (user.isAdmin())
				isAdmin = 1;
			update.append(DATABASE_USERS_TABLE).append(".").append(DATABASE_USERS_TABLE_ADMIN); //$NON-NLS-1$
			update.append(" = '").append(isAdmin).append("', "); //$NON-NLS-1$ //$NON-NLS-2$
			update.append(DATABASE_USERS_TABLE).append(".").append(DATABASE_USERS_TABLE_GROUP); //$NON-NLS-1$
			update.append(" = '").append(StringEscapeUtils.escapeSql(user.getGroup())).append("', "); //$NON-NLS-1$ //$NON-NLS-2$
			int displayAdmin = 0;
			if (user.getDisplayAdmin())
				displayAdmin = 1;
			update.append(DATABASE_USERS_TABLE).append(".").append(DATABASE_USERS_TABLE_DISPLAY_ADMIN); //$NON-NLS-1$
			update.append(" = '").append(displayAdmin).append("' "); //$NON-NLS-1$ //$NON-NLS-2$
			update.append(" where ").append(DATABASE_USERS_TABLE); //$NON-NLS-1$
			update.append(".").append(DATABASE_USERS_TABLE_LOGIN); //$NON-NLS-1$
			update.append(" = '").append(StringEscapeUtils.escapeSql(user.getLogin())).append("'"); //$NON-NLS-1$ //$NON-NLS-2$
			statement.executeUpdate(update.toString());

		} catch (SQLException e) {
			throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);
		} finally {
			try {	
				if (statement != null)
					statement.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);
			}
		}
	}
	
	protected void internalAddUser( PhotoUser user ) throws PhotoException {
		
		if (user == null)
			throw new PhotoException("updateUser error: null argument");
		
		if (!user.getGroup().equals(PhotoConstants.GUEST_GROUP_NAME) && !groupExists(user.getGroup()))
			throw new PhotoException("updateUser error: the user's group does not exist!");
		
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getUserDatabaseConnection();
			statement = connection.createStatement();
			
			int admin = 0;
			if (user.isAdmin())
				admin = 1;
			int displayAdmin = 0;
			if (user.getDisplayAdmin())
				admin = 1;
			
			// default group should not be in database
			String group = user.getGroup();
			if (user.getGroup().equals(PhotoConstants.GUEST_GROUP_NAME)) 
				group = ""; //$NON-NLS-1$
			StringBuffer query = new StringBuffer("Insert into "); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE);
			query.append(" values ('"); //$NON-NLS-1$
			query.append(StringEscapeUtils.escapeSql(user.getLogin()));
			query.append("','"); //$NON-NLS-1$
			query.append(StringEscapeUtils.escapeSql(user.getPassword()));
			query.append("','"); //$NON-NLS-1$
			query.append(StringEscapeUtils.escapeSql(group));
			query.append("','"); //$NON-NLS-1$
			query.append(StringEscapeUtils.escapeSql(user.getEmail()));
			query.append("','"); //$NON-NLS-1$
			query.append(StringEscapeUtils.escapeSql(user.getLanguage()));
			query.append("','"); //$NON-NLS-1$
			query.append(admin);
			query.append("','"); //$NON-NLS-1$
			query.append(user.getColumns());
			query.append("','"); //$NON-NLS-1$
			query.append(StringEscapeUtils.escapeSql(user.getTheme()));
			query.append("','");; //$NON-NLS-1$
			query.append(displayAdmin);
			query.append("','");; //$NON-NLS-1$
			query.append(user.getCookieId());
			query.append("')"); //$NON-NLS-1$
			statement.executeUpdate(query.toString());
			
		} catch (SQLException e) {
			throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);	
		} finally {
			try {	
				if (statement != null)
					statement.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);
			}
		}
	}
	
	public synchronized void addGroup( String group ) throws PhotoException {
		
		// Default group should not be put into database
		if (group.equals(PhotoConstants.GUEST_GROUP_NAME))
			return;
		
		group = StringEscapeUtils.escapeSql(group);
		
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getUserDatabaseConnection();
			statement = connection.createStatement();

			StringBuffer update = new StringBuffer("Insert into "); //$NON-NLS-1$
			update.append(DATABASE_GROUPS_TABLE).append(" values ('"); //$NON-NLS-1$
			update.append(group).append("')"); //$NON-NLS-1$
			statement.executeUpdate(update.toString());

		} catch (SQLException e) {
			throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);	
		} finally {
			try {	
				if (statement != null)
					statement.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);
			}
		}
	}
	
	/**
	 * internal convenience method
	 */
	private void loadDriver() throws PhotoException {
		
		try {
			Class.forName(getConfiguration().getJDBCDriverName());
		} catch (ClassNotFoundException e) {
			throw new PhotoException(PhotoConstants.JDBC_DRIVER_ERROR);
		}
	}
	
	private Connection getUserDatabaseConnection() throws PhotoException, SQLException {
		
		loadDriver();
		return DriverManager.getConnection(getConfiguration().getJDBCConnectionUrl(), 
											getConfiguration().getJDBCLogin(), 
											getConfiguration().getJDBCPassword());
	}

	/**
	 * Return a list of all the groups
	 * @return
	 */
	public String[] readAllGroups() throws PhotoException {
	
		Connection connection = null;
		Statement statement = null;
		ResultSet rc = null;
		try {
			connection = getUserDatabaseConnection();
			statement = connection.createStatement();
			rc = null;
		
			StringBuffer query = new StringBuffer("Select "); //$NON-NLS-1$
			query.append(DATABASE_GROUPS_TABLE).append("."); //$NON-NLS-1$
			query.append(DATABASE_GROUPS_TABLE_NAME).append(" from "); //$NON-NLS-1$
			query.append(DATABASE_GROUPS_TABLE);
			rc = statement.executeQuery(query.toString());
			
			rc.last(); 
			int rowcount = rc.getRow(); 
			// Get the row position which is also the number of rows in the ResultSet.
			String[] groups = new String[rowcount+1];
			
			rc.beforeFirst();
			int index = 0;
			while ( rc.next() ) {
				groups[index]=rc.getString(1);
				index++;
			}
			
			groups[rowcount] = PhotoConstants.GUEST_GROUP_NAME;
			return groups;
		} catch (SQLException e) {
			throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);	
		} finally {
			try {	
				if (rc != null)
					rc.close();
				if (statement != null)
					statement.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);
			}
		}
	}
	
	public String[] readAllLogins() throws PhotoException {
		
		Connection connection = null;
		Statement statement = null;
		ResultSet rc = null;
		try {
			connection = getUserDatabaseConnection();
			statement = connection.createStatement();
			rc = null;
		
			StringBuffer query = new StringBuffer("Select "); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE).append("."); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE_LOGIN).append(" from "); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE);
			rc = statement.executeQuery(query.toString());
			
			rc.last(); 
			int rowcount = rc.getRow(); 
			// Get the row position which is also the number of rows in the ResultSet.
			String[] logins = new String[rowcount];
			
			rc.beforeFirst();
			int index = 0;
			while ( rc.next() ) {
				logins[index]=rc.getString(1);
				index++;
			}
		
			return logins;
		} catch (SQLException e) {
			throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);	
		} finally {
			try {	
				if (rc != null)
					rc.close();
				if (statement != null)
					statement.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);
			}
		}
	}
	
	public boolean groupExists(String group ) throws PhotoException {
		
		if (group == null || group.length() == 0)
			return false;
		
		if (group.equals(PhotoConstants.GUEST_GROUP_NAME))
			return true;
		
		Connection connection = null;
		Statement statement = null;
		ResultSet rc = null;
		try {
			
			connection = getUserDatabaseConnection();
			statement = connection.createStatement();
			rc = null;
		
			StringBuffer buffer = new StringBuffer("Select "); //$NON-NLS-1$
			buffer.append(DATABASE_GROUPS_TABLE).append(".").append(DATABASE_GROUPS_TABLE_NAME); //$NON-NLS-1$
			buffer.append(" from ").append(DATABASE_GROUPS_TABLE); //$NON-NLS-1$
			buffer.append(" where "); //$NON-NLS-1$
			buffer.append(DATABASE_GROUPS_TABLE).append(".").append(DATABASE_GROUPS_TABLE_NAME); //$NON-NLS-1$
			buffer.append(" = '").append(StringEscapeUtils.escapeSql(group)).append("'"); //$NON-NLS-1$ //$NON-NLS-2$
			
			rc = statement.executeQuery(buffer.toString());
			
			boolean found = rc.next();
			
			return found;
				
		} catch (SQLException e) {
			throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);	
		} finally {
			try {	
				if (rc != null)
					rc.close();
				if (statement != null)
					statement.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);
			}
		}
	}
	
	public void initBase() throws PhotoException {

		// base is supposed created, user supposed to have all rights
		// use login;
		
		// delete table if they exist
		String dropGroupTable = "drop table userGroups;"; //$NON-NLS-1$
		String dropUserTable = "drop table users;"; //$NON-NLS-1$
		
		// create tables
		String createGroupTable = "CREATE TABLE userGroups ( name VARCHAR(15) NOT NULL PRIMARY KEY);"; //$NON-NLS-1$
		String createUserTable = "CREATE TABLE users( login VARCHAR(15) NOT NULL PRIMARY KEY," + //$NON-NLS-1$
		"  					 password VARCHAR(15) BINARY NOT NULL," + //$NON-NLS-1$
		" 					 groupName VARCHAR(15) , email VARCHAR(255)," + //$NON-NLS-1$
		"   				 language VARCHAR(5), admin boolean," + //$NON-NLS-1$
		"					 nbColumns INT, theme VARCHAR(255), displayAdmin boolean," + //$NON-NLS-1$
		"					 cookieId BIGINT," + //$NON-NLS-1$
        " 			         FOREIGN KEY (groupName) REFERENCES userGroups (name) );"; //$NON-NLS-1$
		
		// Launch the scripts
		Connection connection = null;
		Statement statement = null;
		try {
			log.debug("Attempting to init user db"); //$NON-NLS-1$
			connection = getUserDatabaseConnection();
			statement = connection.createStatement();
			
			try  {
				statement.executeUpdate(dropUserTable);
				statement.executeUpdate(dropGroupTable);
			} catch (SQLException e) { // normal exception happens if those table do not exist
			}
			
			statement.executeUpdate(createGroupTable);
			statement.executeUpdate(createUserTable);
			log.debug("user db initialized"); //$NON-NLS-1$
		} catch (SQLException e)  {
			throw new PhotoException(PhotoConstants.USER_BASE_INIT_ERROR+"("+e.getMessage()+")");	
		} finally {
			try {	
				if (statement != null)
					statement.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new PhotoException(PhotoConstants.USER_BASE_INIT_ERROR+"("+e.getMessage()+")");
			}
		}
	}
	
	public boolean test() throws PhotoException {

		Connection connection = null;
		try {
			log.debug("Attempting to reach user db"); //$NON-NLS-1$
			connection = getUserDatabaseConnection();
			log.debug("user db reached"); //$NON-NLS-1$
			return true;
		} catch (SQLException e)  {
			return false;
		} finally {
			try {	
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				return false;
			}
		}
		
	}
	
	protected boolean isGroupUsed( String group ) throws PhotoException {
		
		// Read all used groups
		Connection connection = null;
		Statement statement = null;
		ResultSet rc = null;
		try {
			connection = getUserDatabaseConnection();
			statement = connection.createStatement();
			rc = null;
		
			StringBuffer query = new StringBuffer("Select distinct "); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE).append("."); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE_GROUP).append(" from "); //$NON-NLS-1$
			query.append(DATABASE_USERS_TABLE);
			rc = statement.executeQuery(query.toString());
			
			rc.last(); 
			int rowcount = rc.getRow(); 
			// Get the row position which is also the number of rows in the ResultSet.
			Vector logins = new Vector(rowcount);
			
			rc.beforeFirst();
			int index = 0;
			while ( rc.next() ) {
				logins.addElement(rc.getString(1));
				index++;
			}
		
			// Is the group present in the returned list ?
			if (logins.contains(group))
				return true;
			
			return false;
			
		} catch (SQLException e) {
			throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);	
		} finally {
			try {	
				if (rc != null)
					rc.close();
				if (statement != null)
					statement.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new PhotoException(PhotoConstants.USER_ACCESS_ERROR,e);
			}
		}
		
	}
}
