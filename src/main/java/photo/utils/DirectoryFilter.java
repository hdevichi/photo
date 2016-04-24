package photo.utils;

import java.io.File;
import java.io.FileFilter;

import org.apache.log4j.Logger;

/**
 * FileFilter for the photo web application.
 * Accepts only directories for which the user has authorisation
 * (takes the group in constructor, or null to list all dirs)
 * 
 * @author Hadrien Devichi
 */
public class DirectoryFilter implements FileFilter {

	static final Logger log = Logger.getLogger(DirectoryFilter.class);
	
	public DirectoryFilter() {
	}
	
	/**
	 * Accepts pathname only if this is a directory
	 */
	public boolean accept(File pathname) {
		
		if (!pathname.isDirectory()) 
			return false;
		
		return true;
	}
}
