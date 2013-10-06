package org.devichi.photo.model.photo;

import java.io.File;
import java.io.FileFilter;

import org.apache.log4j.Logger;
import org.devichi.photo.model.config.PhotoConfigurationConstants;

/**
 * FileFilter for the photo web application.
 * Accepts only images files (extension described in PhotoConstants.SUPPORTED_FILE_EXTENSION, not case sensitive), 
 * who are not thumbnails (last characters before extension are NOT Constants.THUMBNAIL_SUFFIX)
 * 
 * case sensitive
 * 
 * @author Hadrien Devichi
 */
public class ImageFilter implements FileFilter {

	static final Logger log = Logger.getLogger(ImageFilter.class);
	
	/**
	 * Accepts only image files whose extension is described by PhotoConstants.SUPPORTED_FILE_EXTENSION,
	 * who are not thumbnails (last characters before extension are NOT PhotoConstants.THUMBNAIL_SUFF
	 * 
	 * case sensitive
	 */
	public boolean accept(File pathname) {

		String fileName = pathname.getName();
		
		// Returns false for thumbnail & resized for display files
		
		if (fileName.startsWith(FilePhotoFactory.PREFIX_DISPLAY) || 
			fileName.startsWith(FilePhotoFactory.PREFIX_THUMBNAIL)) 
			return false;
		
		// Returns true only if the extension is supported
		int separatorPosition = fileName.lastIndexOf('.');
		if (separatorPosition == -1)
			return false;
	
		String extension = fileName.substring(separatorPosition);
		boolean accept = false;
		for ( String s : PhotoConfigurationConstants.SUPPORTED_FILE_EXTENSION ) { 
			if (extension.equals(s)) {
				accept = true;
				break;
			}
		}
		return accept;
	}
}
