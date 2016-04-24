package photo.model.photo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.drew.metadata.exif.ExifDirectoryBase;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import photo.control.servlet.PhotoConstants;
import photo.i18n.Message;
import photo.model.config.PhotoConfigurationConstants;
import photo.model.photo.PhotoDescription.PhotoMetadata;
import photo.model.user.PhotoUser;
import photo.utils.DirectoryFilter;
import photo.utils.PhotoException;
import photo.utils.PhotoUtils;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;

import static com.drew.metadata.exif.ExifDirectoryBase.*;

/**
 * This photo factory is implemented for ease of administration & manual
 * overrides. It is not aimed at performance.
 * 
 * Photo hierarchy, paths, index & size deduced from disk.
 * 
 * Photo viewcount from a properties file at the root (viewcount.txt)
 * Photo ids, rights, texts & titles from a properties file at the root (description.txt)
 * Path ==> ID index (paths.txt)
 * 
 * NOTE: description.txt also stores a timestamp. this is the time at which the
 * picture has been registered in the application. It is used in FileServlet
 * to check if image requested by id has changed due to an id reset.
 * 
 * NOTE: it asserts that prefix of files give an accurate description, ie that a thumbnail
 * prefixed by __th160 is indeed 160 pixels wide, for instance.
 * 
 * @author Hadrien Devichi
 */
public class FilePhotoFactory extends PhotoFactory {

	public static final String RESIZE_MIME_TYPE = "image/jpeg";
	public static final String RESIZE_EXTENSION = "jpg";
	
	public static final String PREFIX_THUMBNAIL = "__th"; //$NON-NLS-1$
	public static final String PREFIX_DISPLAY = "__photo"; //$NON-NLS-1$
	
	// Constants describing file formats used
	public static final String FILENAME_DESCRIPTIONS = "description.txt"; //$NON-NLS-1$
	public static final String FILENAME_VIEWCOUNTS = "viewcount.txt"; //$NON-NLS-1$
	public static final String FILENAME_IDS = "id.txt"; //$NON-NLS-1$
	public static final String FILENAME_PATHS = "path.txt"; //$NON-NLS-1$
	
	public static final String KEY_ID_SEQUENCE = "ID"; //$NON-NLS-1
	public static final String KEY_TIMESTAMP = "timestamp"; //$NON-NLS-1
	public static final String KEY_PATH = "path"; //$NON-NLS-1
	public static final String KEY_TITLE = "title"; //$NON-NLS-1$
	public static final String KEY_TEXT = "text"; //$NON-NLS-1$
	public static final String KEY_AUTHORIZED = "authorized"; //$NON-NLS-1$
	public static final String KEY_ADMIN = "admin"; //$NON-NLS-1$
	public static final String KEY_THEME = "theme"; //$NON-NLS-1$
	public static final String KEY_SEPARATOR = "."; //$NON-NLS-1$		
	
	// root path is "", it cannot be used as is as a key in the index
	public static final String KEY_ROOT_PATH = "__ROOT";
	
	// Resize queue
	protected ConcurrentLinkedQueue resizeQueue = null;
	private ResizeManager resizeManager = null;
	
	private PropertiesConfiguration descriptions = null;
	// View count are kept in a separate file for performance reasons
	// (edited most often, of all properties)
	private PropertiesConfiguration viewCounts = null;
	private PropertiesConfiguration pathsToIds = null;
	
	private ImageFilter imageFilter;
	private DirectoryFilter directoryFilter;
	
	protected class PhotoComparator implements Comparator<PhotoDescription> {

		@Override
		public int compare(PhotoDescription o1, PhotoDescription o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
		
	}
	
	
	public synchronized void internalDelete(String id) throws PhotoException {

		internalDeleteWithoutSave(id);
		
		try {
			getDescriptions().save();
			getPathToIdIndexes().save();
			getViewCounts().save();
		} catch (ConfigurationException e) {
			log.error(e.getMessage(),e);
			throw new PhotoException("Error while saving description to disk!");
		}
	}
	
	public synchronized void internalDeleteWithoutSave(String id) throws PhotoException {

		checkId(id);
		
		File file = getFileById(id);
		File thumbnail = getThumbnailFileById(id);
		File displayFile = getDisplayFileById(id);
		
		// File operations are most likely to fail (esp. delete directory), so do first
		if (file.isDirectory()) {
			if (! file.delete()) {
				log.error("Delete directory did not succeed for "+file.getAbsolutePath());
				throw new PhotoException("Couldn't delete directory");
			}
		} else {
			if (!file.delete())
				throw new PhotoException("Couldn't delete image");
			// delete thumb & backup if they are present
			// do not use shortcut getFile() since it throws an exception if missing
			
			if (thumbnail.exists())
				thumbnail.delete();
			
			if (displayFile.exists())
				displayFile.delete();
		}
		
		deleteDescription(id);
	}

	private synchronized void deleteDescription(String id) throws PhotoException {
		
		getViewCounts().clearProperty(id);
		getDescriptions().clearProperty(getAdminGroupsKey(id));
		getDescriptions().clearProperty(getAuthorizedGroupsKey(id));
		getDescriptions().clearProperty(getThemeKey(id));
		
		String[] locales = getConfiguration().getSupportedLocales();
		for (int i = 0; i < locales.length; i++) {
			getDescriptions().clearProperty(getTitleKey(id, locales[i]));
			getDescriptions().clearProperty(getTextKey(id, locales[i]));
		}
		
		String path = getPathForId(id);
		getDescriptions().clearProperty(getPathKey(id));
		getPathToIdIndexes().clearProperty(path);
	}

	private void checkValidImage(File file) throws PhotoException {
		if (!file.exists()) {
			throw new PhotoException(PhotoConstants.FILE_NOT_FOUND_ERROR+", "+file.getName());
		}
		
		if (file.isDirectory()) {
			throw new PhotoException(PhotoConstants.FILE_NOT_FOUND_ERROR+", expecting an image, not a directory");
		}
	}
	
	public InputStream getDisplayStream(String id) throws PhotoException, IOException {
		
		checkId(id);
		File image = getDisplayFileById(id);
		checkValidImage(image);

		return new FileInputStream(image);
	}
	
	public InputStream getOriginalStream(String id) throws PhotoException, IOException {
		
		checkId(id);
		File image = getFileById(id);
		checkValidImage(image);
		
		return new FileInputStream(image);
	}
	
	public InputStream getThumbnailStream(String id) throws PhotoException, IOException {
		
		checkId(id);
		File image = getThumbnailFileById(id);
		checkValidImage(image);
		
		return new FileInputStream(image);
	}
	
	public PhotoDescription getRoot() throws PhotoException {
	
		String id = getIdForPath(KEY_ROOT_PATH);
		if (id == null) {
			register("");
			id = getIdForPath(KEY_ROOT_PATH);
		}
		return getDescription(id);
	}

	protected PhotoDescription internalGetDescription(String id) throws PhotoException {
		
		checkId(id);
		
		long start = 0;
		if (log.isDebugEnabled()) {
			log.debug("Creating description for: "+id);
			start = System.currentTimeMillis();
		}
		
		File file = getFileById(id);
		boolean isDirectory = file.isDirectory();
		
		HashMap<String, String> titles = new HashMap<String, String>();
		HashMap<String, String> texts = new HashMap<String, String>();	
		String[] locales = getConfiguration().getSupportedLocales();
		for (int i = 0 ; i < locales.length; i++ ) {
			titles.put(locales[i], getDescriptions().getString(getTitleKey(id, locales[i])));
			texts.put(locales[i], getDescriptions().getString(getTextKey(id, locales[i])));
		}
		
		List<String> groups = new ArrayList<String>();
		List<String> adminGroups = new ArrayList<String>();
		if (isDirectory) {
			for ( Object item : getDescriptions().getList(getAuthorizedGroupsKey(id))) {
				groups.add(item.toString());
			}
			for ( Object item : getDescriptions().getList(getAdminGroupsKey(id))) {
				adminGroups.add(item.toString());
			}
		} else {
			for ( Object item : getDescriptions().getList(getAuthorizedGroupsKey(getParent(id)))) {
				groups.add(item.toString());
			}
			for ( Object item : getDescriptions().getList(getAdminGroupsKey(getParent(id)))) {
				adminGroups.add(item.toString());
			}
		}
		
		
		String theme = "";
		if (isDirectory) {
			theme = getDescriptions().getString(getThemeKey(id));
		}
	
		long size = -1;
		if (isDirectory) {
			size = file.listFiles(getImageFilter()).length; 
		} else {
			size = file.length();	
		}
		
		int viewcount = getViewCounts().getInt(id, 0);
		
        String path = getPathForId(id);
        long timestamp = 0;
        try {
        	timestamp = getDescriptions().getLong(getTimestampKey(id));
        } catch (NoSuchElementException e) {
        	// Shouldn't happen, but will happen if migrating from 1.3beta or before
        	timestamp = System.currentTimeMillis();
        	getDescriptions().setProperty(getTimestampKey(id), timestamp);
        	try {
    			getDescriptions().save();
    		} catch (ConfigurationException ee) {
    			throw new PhotoException("Error while creating description for id:"+id);
    		}
        }
		PhotoDescription description = new PhotoDescription(this, id, path, getDisplayPathForPath(path), getThumbnailPathForPath(path), isDirectory, titles, texts, groups, adminGroups, theme, size, viewcount, timestamp);
		
		if (!isDirectory) {
			
			File thumbnail = new File(getConfiguration().getImageRoot(), getThumbnailPathForPath(path));
			File display = new File(getConfiguration().getImageRoot(), getDisplayPathForPath(path));
			
			// resize
			if (!isOnResizeBlackList(id)) {
				if (!thumbnail.exists() || !display.exists() ) {
					requestResize(id, false);
				}
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Creation completed in (ms): "+ (System.currentTimeMillis()-start));
		}
		return description;
	}

	// Do not check blacklist here, because forced resize using admin GUI
	// should not check it.
	// this is called:
	// - by admin controllers
	// - by getDescription , if not on black list, when resized image found missing
	public void requestResize(String id, boolean forceResize) {
		
		if (forceResize) {
			if (resizeManager.isBlacklisted(id)) {
				resizeManager.forceNextResize(id);
			}
		}
		
		if (!getResizeQueue().contains(id)) {
			getResizeQueue().offer(id);
			if (log.isInfoEnabled()) {
				log.info("Adding to resize queue: "+id);
				log.info("Queue size: "+getResizeQueue().size());
			}
		}
	}
	
	/**
	 * Return the id of all child directories
	 */
	protected PhotoDescription[] getChildDirectories(String id) throws PhotoException {
		
		checkId(id);
			
		String path = getPathForId(id);
		File file = getFileById(id);
		if (!file.isDirectory()) {
			log.error("Call to getChildDirectories on an image! "+path);
			throw new PhotoException(PhotoConstants.IMAGE_ACCESS_ERROR);
		}
		
		File[] childDirectories = file.listFiles(getDirectoryFilter());
		
		StringBuffer buffer = new StringBuffer(path);
		if (!path.equals(""))
			buffer.append(PhotoConstants.SEPARATOR_PATH);
		PhotoDescription[] children = new PhotoDescription[childDirectories.length];
		for (int i = 0 ; i < children.length ; i++) {
			StringBuffer childPath = new StringBuffer(buffer);
			String childId = getIdForPath(childPath.append(childDirectories[i].getName()).toString());
			if (childId == null) {
				childId = register(childPath.toString());
			}
			children[i] = getDescription(childId);
		}
		Arrays.sort(children, new PhotoComparator());
		return children;
	}

	protected PhotoDescription[] getChildImages(String id) throws PhotoException {

		checkId(id);
		
		String path = getPathForId(id);
		File file = getFileById(id);
		if (!file.isDirectory()) {
			log.error("Call to getChildImages on an image! "+path);
			throw new PhotoException(PhotoConstants.IMAGE_ACCESS_ERROR);
		}
		
		File[] childImages = file.listFiles(getImageFilter());
		
		StringBuffer buffer = new StringBuffer(path);
		if (!path.equals(""))
			buffer.append(PhotoConstants.SEPARATOR_PATH);
		PhotoDescription[] children = new PhotoDescription[childImages.length];
		for (int i = 0 ; i < children.length ; i++) {
			StringBuffer childPath = new StringBuffer(buffer);
			String childId = getIdForPath(childPath.append(childImages[i].getName()).toString());
			if (childId == null) {
				childId = register(childPath.toString());
			}
			children[i] = getDescription(childId);
		}
		Arrays.sort(children, new PhotoComparator());
		return children;
	}

	protected synchronized void internalRename(PhotoDescription source, String newName) throws PhotoException {
		
		String sourcePath = source.getPath();
		String destinationPath = source.getParent().getPath();
		if (destinationPath.length() != 0)
			destinationPath += PhotoConstants.SEPARATOR_PATH;
		destinationPath += newName;
		
		// check if destination exists
		File destination = new File(getFileById(source.getParent().getId()), newName);
		if (destination.exists())
			throw new PhotoException(PhotoConstants.DESTINATION_ALREADY_EXISTS_ERROR);
		
		if (!source.isDirectory()) {
			// Case of an image
			
			// Attempt to move the files first (most likely to fail)
			File image = getFileById(source.getId());
			File thumbnail = getThumbnailFileById(source.getId());
			File display = getDisplayFileById(source.getId());
			boolean success =true;
			success = image.renameTo(destination);
			if (!success) {
				throw new PhotoException(PhotoConstants.IMAGE_ACCESS_ERROR);
			} else {
				if (display.exists())
					display.renameTo(new File(getConfiguration().getImageRoot(), getDisplayPathForPath(destinationPath)));
				if (thumbnail.exists())
					thumbnail.renameTo(new File(getConfiguration().getImageRoot(), getThumbnailPathForPath(destinationPath)));
			}
			getDescriptions().clearProperty(getPathKey(source.getId()));
			getDescriptions().setProperty( getPathKey(source.getId()), destinationPath);
			getPathToIdIndexes().clearProperty(source.getPath());
			getPathToIdIndexes().setProperty(destinationPath, source.getId());
			try {
				getPathToIdIndexes().save();
				getDescriptions().save();
			} catch (ConfigurationException e) {
				throw new PhotoException("error saving",e);
			}
		} else {
			// Move dir
			File dir = getFileById(source.getId());
			boolean success = dir.renameTo(new File(getConfiguration().getImageRoot(), destinationPath));
			if (!success) {
				throw new PhotoException(PhotoConstants.IMAGE_ACCESS_ERROR);
			}
			getDescriptions().clearProperty(getPathKey(source.getId()));
			getDescriptions().setProperty(getPathKey(source.getId()), destinationPath);
			getPathToIdIndexes().clearProperty(source.getPath());
			getPathToIdIndexes().setProperty(destinationPath, source.getId());
			updatePathForAllChildren(source, sourcePath);
			// Save done in the last call
		}
	}

	protected synchronized void internalMove(PhotoDescription source, String destinationDirectoryId) throws PhotoException {

		// Check arguments
		checkId(destinationDirectoryId);
		
		// check if destination dir exists
		File destinationDir = getFileById(destinationDirectoryId);
		if (!destinationDir.exists() || !destinationDir.isDirectory()) {
			throw new PhotoException("Destination directory does not exist!");
		}

		// check if destination exists
		String name = source.getName();
		File destination = new File(destinationDir, name);
		if (destination.exists())
			throw new PhotoException(PhotoConstants.DESTINATION_ALREADY_EXISTS_ERROR);
		
		// compute destination path
		String sourcePath = source.getPath();
		String destinationPath = getPathForId(destinationDirectoryId);
		if (!destinationPath.equals(""))
			destinationPath += PhotoConstants.SEPARATOR_PATH;
		destinationPath += name;
		
		if (!source.isDirectory()) {
			// Case of an image
			
			// Attempt to move the files first (most likely to fail)
			File image = getFileById(source.getId());
			File thumbnail = getThumbnailFileById(source.getId());
			File display = getDisplayFileById(source.getId());
			boolean success =true;
			success = image.renameTo(new File(getConfiguration().getImageRoot(), destinationPath));
			if (!success) {
				throw new PhotoException(PhotoConstants.IMAGE_ACCESS_ERROR);
			} else {
				if (display.exists())
					display.renameTo(new File(getConfiguration().getImageRoot(), getDisplayPathForPath(destinationPath)));
				if (thumbnail.exists())
					thumbnail.renameTo(new File(getConfiguration().getImageRoot(), getThumbnailPathForPath(destinationPath)));
			}
			getDescriptions().clearProperty(getPathKey(source.getId()));
			getDescriptions().setProperty( getPathKey(source.getId()), destinationPath);
			getPathToIdIndexes().clearProperty(source.getPath());
			getPathToIdIndexes().setProperty(destinationPath, source.getId());
			try {
				getPathToIdIndexes().save();
				getDescriptions().save();
			} catch (ConfigurationException e) {
				throw new PhotoException("error saving",e);
			}
		} else {
			// Move dir
			File dir = getFileById(source.getId());
			boolean success = dir.renameTo(new File(getConfiguration().getImageRoot(), destinationPath));
			if (!success) {
				throw new PhotoException(PhotoConstants.IMAGE_ACCESS_ERROR);
			}
			getDescriptions().clearProperty(getPathKey(source.getId()));
			getDescriptions().setProperty(getPathKey(source.getId()), destinationPath);
			getPathToIdIndexes().clearProperty(source.getPath());
			getPathToIdIndexes().setProperty(destinationPath, source.getId());
			updatePathForAllChildren(source, sourcePath);
			// Save done in the last call
		}
	}

	/**
	 * Update path and index for all children (whatever depth) of an object (but does not update object itself)
	 * Also removes everything in this tree from the cache
	 *
	 * @throws PhotoException
	 */
	private void updatePathForAllChildren(PhotoDescription source, String oldPath) throws PhotoException {
		
		String path = getPathForId(source.getId()); // Read this way, because source hasn't been updated yet (it will be after removal from cache, when read again)
		
		// first update all path to id indexes entries (to avoid creation of extra entries when calling getChildDirectories & getChildImages)
		
		Iterator<String> k = getPathToIdIndexes().getKeys();
		List<String> keys =IteratorUtils.toList(k);
		for (String key: keys ) {
			// Do not update if it was not a child
			if (!key.startsWith(oldPath))
				continue;
			// Do not update self
			if (key.equals(oldPath)) // this one has already been treated
				continue;
			// Do not update if it is merely a different file with a similar path
			// Note: here, key is longer than oldPath (since it's different and has it as suffix), so charAt is safe
			String test = key.substring(oldPath.length(), oldPath.length()+1);
			if (!test.equals("/"))
				continue;
			
			// We found a index entry to update
			String id = getPathToIdIndexes().getString(key);
			getPathToIdIndexes().clearProperty(key);
			key = path + key.substring(oldPath.length());
			getPathToIdIndexes().setProperty(key, id);
			
			getDescriptions().setProperty(getPathKey(id), key);
			removeFromCache(id);
		}
		
		try {
			getDescriptions().save();
			getPathToIdIndexes().save();
		} catch (ConfigurationException e) {
			log.error(e.getMessage(),e);
			throw new PhotoException("Error while saving description to disk!");
		}
	}

	protected synchronized void internalWriteDescription(PhotoDescription photo) throws PhotoException {
		
		if (photo == null) {
			log.error("Error: writeDescription , null parameter");
			throw new RuntimeException(PhotoConstants.NULL_PARAMETER_ERROR);
		}
		
		internalWriteDescriptionWithoutSave(photo);
		
		try {
			getDescriptions().save();
			getPathToIdIndexes().save();
		} catch (ConfigurationException e) {
			log.error(e.getMessage(),e);
			throw new PhotoException("Error while saving description to disk!");
		}
	}
	
	private synchronized void internalWriteDescriptionWithoutSave(PhotoDescription photo ) throws PhotoException {
		
		// Clear former path (in case it changed)
		String formerPath = getDescriptions().getString(getPathKey(photo.getId()));
		getPathToIdIndexes().clearProperty(formerPath);
		
		getDescriptions().setProperty(getPathKey(photo.getId()), photo.getPath());
		if (photo.getPath().length() > 0)
			getPathToIdIndexes().setProperty(photo.getPath(), photo.getId());
		else
			getPathToIdIndexes().setProperty(KEY_ROOT_PATH, photo.getId());
		if (photo.isDirectory()) {
			getDescriptions().setProperty(getAdminGroupsKey(photo.getId()), photo.getAdminGroups());
			getDescriptions().setProperty(getAuthorizedGroupsKey(photo.getId()), photo.getAuthorizedGroups());
			getDescriptions().setProperty(getThemeKey(photo.getId()), photo.getTheme());
		} 
		String[] locales = getConfiguration().getSupportedLocales();
		for (int i = 0 ; i < locales.length ; i++ ) {
			getDescriptions().setProperty(getTitleKey(photo.getId(), locales[i]), photo.getTitle(locales[i]));
			getDescriptions().setProperty(getTextKey(photo.getId(), locales[i]), photo.getText(locales[i]));
		}
	}

	public synchronized void incrementViewCount(PhotoDescription photo) throws PhotoException {
		
		if (photo == null) {
			log.error("FilePHotoFactory.writeViewCount, null parameter");
			throw new RuntimeException(PhotoConstants.NULL_PARAMETER_ERROR);
		}
		
		photo.incrementViewCount();
		getViewCounts().setProperty(photo.getId(), photo.getViewCount());	
		
		try {
			getViewCounts().save();
		} catch (ConfigurationException e) {
			log.error(e.getMessage(),e);
			throw new PhotoException("Error while saving viewcount to disk!");
		}
	}
	
	/**
	 * Return the id of the parent
	 * parent of root dir is itself
	 */
	protected String getParent(String id) throws PhotoException {
		
		if ( id == null ) {
			log.error("Error: FilePhotoFactory.getParent, null parameter");
			throw new RuntimeException(PhotoConstants.NULL_PARAMETER_ERROR);
		}
		
		String path = getPathForId(id);
		int lastSeparatorPosition = path.lastIndexOf(PhotoConstants.SEPARATOR_PATH);
		String parentPath= "";
		if ( lastSeparatorPosition != -1 ) {
			parentPath = path.substring(0, lastSeparatorPosition);
		} 
		return getIdForPath(parentPath);
	}
	
	private ImageFilter getImageFilter() {
		if (imageFilter == null)
			imageFilter = new ImageFilter();
		return imageFilter;
	}
	
	private DirectoryFilter getDirectoryFilter() {
		if (directoryFilter == null)
			directoryFilter = new DirectoryFilter();
		return directoryFilter;
	}
	
	protected int getIndex(String id) throws PhotoException {
		
		int index = -1;
		File f = getFileById(id);
		if (!f.isDirectory()) {
			index = findFileIndex(f.getParentFile().listFiles(getImageFilter()), f);
		} else {
			// root index = -1;
			if ( !id.equals(""))
				index = findFileIndex(f.getParentFile().listFiles(getDirectoryFilter()), f);
		}
    	return index;
	}
	
	private int findFileIndex( File[] files, File file) {
		int index = -1;
		for (int i = 0 ; i < files.length ; i++ ) {
			if (files[i].compareTo(file) == 0) {
				index = i;
				break;
			}
		}
		return index;
	}
		
	protected int getNumberOfChildDirectories( String id ) throws PhotoException {
		
		checkId(id);
		File f = getFileById(id);
		return f.listFiles(getDirectoryFilter()).length;
	}
	
	protected int getNumberOfChildImages( String id ) throws PhotoException {
		
		checkId(id);
		File f = getFileById(id);
		return f.listFiles(getImageFilter()).length;
	}
	
	private String register(String path) throws PhotoException {
		String id = getNextId();
		if (path.length() > 0)
			getPathToIdIndexes().setProperty(path, id);
		else
			getPathToIdIndexes().setProperty(KEY_ROOT_PATH, id);
		getDescriptions().setProperty(getPathKey(id),path);
		getDescriptions().setProperty(getTimestampKey(id), System.currentTimeMillis());
		try {
			getPathToIdIndexes().save();
			getDescriptions().save();
			return id;
		} catch (ConfigurationException e) {
			throw new PhotoException("Error while creating description for id:"+id);
		}
	}
	
	protected PhotoDescription internalCreateChildDirectory( String parentDirectoryId, String name ) throws PhotoException {
		
		checkId(parentDirectoryId);
		File directory = getFileById(parentDirectoryId);
		if (!directory.isDirectory())
			throw new PhotoException(PhotoConstants.NOT_DIRECTORY_ERROR);
		
		File newDir = new File(directory, name);
		if (newDir.exists())
			throw new PhotoException(PhotoConstants.DIRECTORY_ALREADY_EXISTS_ERROR);
		
		if (!newDir.mkdir())
			throw new PhotoException(PhotoConstants.IMAGE_ACCESS_ERROR);
		
		StringBuffer directoryPath = new StringBuffer(getPathForId(parentDirectoryId));
		if (directoryPath.length() != 0)
			directoryPath.append(PhotoConstants.SEPARATOR_PATH);
		directoryPath.append(name);
		String id = register(directoryPath.toString());
		return getDescription(id);
	}
	
	protected PhotoDescription internalCreateChildImage( String directoryId, File image ) throws PhotoException {
		
		checkId(directoryId);
		File directory = getFileById(directoryId);
		if (!directory.isDirectory())
			throw new PhotoException(PhotoConstants.NOT_DIRECTORY_ERROR);
		
		File newImage = new File( directory, image.getName());
		if (newImage.exists())
			throw new PhotoException(PhotoConstants.IMAGE_ALREADY_EXISTS_ERROR);
		
		image.renameTo(newImage);
		StringBuffer imagePath = new StringBuffer(getPathForId(directoryId));
		if (!imagePath.equals(""))
			imagePath.append(PhotoConstants.SEPARATOR_PATH);
		imagePath.append(image.getName());
		String id = register(imagePath.toString());
		// will asynchronously create thumb & resize if needed
		return getDescription(id);
	}
	
	/**
	 * returns true if the dir has children (photo related or not)
	 */
	public boolean hasChildren(String id) throws PhotoException {
		
		checkId(id);
		
		File dir = getFileById(id);
		if (!dir.exists() || !dir.isDirectory())
			throw new PhotoException("Not a directory!");
		String[] children =dir.list();
		return (children.length != 0);
	}
	
	/**
	 * list everything contained in the directory, even non photo related files
	 */
	public String[] listAllChildren(String id) throws PhotoException {
		
		checkId(id);
		
		File f = getFileById(id);
		
		File[] files = f.listFiles();
		if (files == null)
			return null;
		String[] names = new String[files.length];
		for (int i = 0 ; i < files.length ; i++)
			names[i] = files[i].getName();
		return names;
	}
	
	
	/**
	 * Delete everything contained in the directory, even non photo related files
	 * 
	 * Cannot be called on a directory containing sub directories.
	 * Will also delete the description of the images in the directory
	 */
	public void deleteAllChildren(String id, PhotoUser user) throws PhotoException {
		
		checkId(id);
		PhotoDescription description = getDescription(id);
		if (id.equals("") )
			throw new PhotoException("internalDeleteChildren forbidden on root directory!");
		
		if (!description.isUserAuthorizedToAdmin(user))
			throw new PhotoException("internalDeleteChildren: user does not have sufficient rights!");
		
		if (getChildDirectories(id).length > 0)
			throw new PhotoException("internalDeleteChildren: directory contains sub directories");
		
		PhotoDescription[] images = getChildImages(id);
		for (int i = 0 ; i < images.length ; i++) {	
			internalDeleteWithoutSave(images[i].getId());
		}
		
		try {
			getDescriptions().save();
			getPathToIdIndexes().save();
			getViewCounts().save();
		} catch (ConfigurationException e) {
			log.error(e.getMessage(),e);
			throw new PhotoException("Error while saving description to disk!");
		}
		
		// Load it before deleting the description 
		File f = getFileById(id);
		File[] files = f.listFiles();
		if (files == null)
			return ;
		
		for (int i = 0 ; i < files.length ; i++) {	
			files[i].delete();
		}
	}
	
	/**
	 * Get all child directories. Recursive, so returns child whatever the level
	 * Slow (uncached, recursive ..)
	 */
	public List<PhotoDescription> getAllChildDirectories( String id ) throws PhotoException {
		
		checkId(id);
		
		List<PhotoDescription> allChildren = new Vector();
		PhotoDescription[] children = getChildDirectories(id);
		for (int i = 0 ; i < children.length ; i++ ) {
			List<PhotoDescription> grandChildren = getAllChildDirectories(children[i].getId());
			allChildren.addAll(grandChildren);
			allChildren.add(children[i]);
		}
		return allChildren;
	}
	
	/**
	 * Write a list of descriptions in batch mode
	 */
	public void internalWriteDescriptions(List photos) throws PhotoException {
		
		getDescriptions().setAutoSave(false);
		
		for (int i = 0 ; i < photos.size() ; i++) {
			internalWriteDescriptionWithoutSave( ((PhotoDescription)photos.get(i)));
		}
		try {
			getDescriptions().save();
		} catch (ConfigurationException e) {
			log.error(e);
			throw new PhotoException("Error while saving description to disk!");
		}
		
		getDescriptions().setAutoSave(true);
	}
	
	private ConcurrentLinkedQueue getResizeQueue() {
		if (resizeQueue == null ) {
			resizeQueue = new ConcurrentLinkedQueue();
			ResizeManager resizeManager = new ResizeManager(this, resizeQueue);
			resizeManager.start();
			this.resizeManager = resizeManager;
		}
		return resizeQueue;
	}
	
	private boolean isOnResizeBlackList(String id) {
		getResizeQueue();
		return resizeManager.isBlacklisted(id);
	}
	
	public String getStatusString( String locale) {
		StringBuffer buffer = new StringBuffer(Message.getResource("filePhotoFactory.status", locale));
		buffer.append(" ").append(getResizeQueue().size());
		return buffer.toString();
	}
	
	/**
	 * Returns the free space in MB
	 */
	public long getFreeSpace() {
		
		long bytes = getConfiguration().getImageRoot().getFreeSpace();
		bytes = bytes / 1024;
		bytes = bytes / 1024;
		return bytes;
	}
	
	protected void updatePhotoStatistics() {
		
		log.info("Updating application statistics"); //$NON-NLS-1$
		lastComputationTime = System.currentTimeMillis();
		
		lastModificationTime = 0;
		numberOfPictures = getNumberOfPicsInDir( getConfiguration().getImageRoot() );
		
		if (log.isInfoEnabled()) {
			log.info("Statistics updated in (ms): "+(System.currentTimeMillis()-lastComputationTime));
		}
	}
	
	// helper method, calculate number of pics in a dir while updating
	// last modification time
	protected int getNumberOfPicsInDir( File f ) {
		
		File[] dirs = f.listFiles( new DirectoryFilter() );
		File[] pics = f.listFiles( new ImageFilter() );
		
		for ( File pic : pics) {
			if (pic.lastModified() > lastModificationTime)
				lastModificationTime = pic.lastModified();
		}
			
		int result = pics.length;
		for ( File dir : dirs ) {
			if (dir.compareTo(f) != 0)
				result += getNumberOfPicsInDir(dir);
		}
		
		return result;
	}
	
	protected int getOriginalScale(String id) {
		
		try {
			File f = getFileById(id);
			return PhotoUtils.getImageScale(f);
		} catch (Exception e) {
			return -1;
		}
	}
	
	public PhotoMetadata getExifMetaData( String id ) throws PhotoException {
		
		checkId(id);
		
		// First look in the bakcup if it exists
		File image = getFileById(id);
		if (!image.exists()) 
			throw new PhotoException("Cannot read meta data, file not found");
			
		Metadata metadata = null;
		try {
			metadata = JpegMetadataReader.readMetadata(image);
			
			Directory exifDirectory = metadata.getFirstDirectoryOfType(ExifDirectoryBase.class);
			String cameraModel = exifDirectory.getString(TAG_MODEL);
			if (cameraModel == null || cameraModel.length() == 0)
				return null;
			
			String focal = exifDirectory.getString(TAG_FOCAL_LENGTH);
			String dateTime = exifDirectory.getString(TAG_DATETIME);
			String exposure = exifDirectory.getString(TAG_EXPOSURE_TIME);
			String iso = exifDirectory.getString(TAG_ISO_EQUIVALENT);
					
			PhotoDescription photo = getDescription(id);
			PhotoMetadata meta = photo.new PhotoMetadata();
			meta.camera = cameraModel;
			meta.timestamp = dateTime;
			meta.focal = focal;
			meta.exposure = exposure+" s";
			meta.iso = iso;

			try {
				Float aperture = exifDirectory.getFloat(TAG_APERTURE);
				float roundedAperture = Math.round(aperture*100);
				meta.aperture = "1/" + (roundedAperture/100);
			} catch (Exception e) {
				meta.aperture = exifDirectory.getString(TAG_APERTURE);
			}
			return meta;
			
		} catch (Exception e) {
			log.error("Error retrieving metadata for: "+id+","+e);
			return null;
		}
	}
	   
	protected void internalRotate( String id ) throws PhotoException {
		
		checkId(id);
	
		File image = getFileById(id);
		if (image.exists()) {
			PhotoUtils.rotateImage(image);
		}
	
		if (!getResizeQueue().contains(id)) {
			getResizeQueue().offer(id);
			if (log.isInfoEnabled()) {
				log.info("Adding to resize queue (for rotation): "+id);
				log.info("Queue size: "+getResizeQueue().size());
			}
		}
	}

	protected PropertiesConfiguration getPathToIdIndexes() throws PhotoException {
		if (pathsToIds == null) {
			try {
				File f = new File(getConfiguration().getImageRoot(), FILENAME_PATHS);
				try {
					if (!f.exists())
						f.createNewFile();
				} catch (IOException e) {
					throw new PhotoException(PhotoConstants.IMAGE_ACCESS_ERROR,e);
				}
				pathsToIds = new PropertiesConfiguration(f);
			} catch (ConfigurationException e) {
				throw new PhotoException(PhotoConstants.IMAGE_ACCESS_ERROR,e);
			}
		}
		return pathsToIds;
	}
	
	protected PropertiesConfiguration getDescriptions() throws PhotoException {
		if (descriptions == null) {
			try {
				File f = new File(getConfiguration().getImageRoot(), FILENAME_DESCRIPTIONS);
				try {
					if (!f.exists())
						f.createNewFile();
				} catch (IOException e) {
					throw new PhotoException(PhotoConstants.IMAGE_ACCESS_ERROR,e);
				}
				descriptions = new PropertiesConfiguration(f);
				
			} catch (ConfigurationException e) {
				throw new PhotoException(PhotoConstants.IMAGE_ACCESS_ERROR,e);
			}
		}
		return descriptions;
	}
	
	protected PropertiesConfiguration getViewCounts() throws PhotoException {
		if (viewCounts == null) {
			try {
				viewCounts = new PropertiesConfiguration(new File(getConfiguration().getImageRoot(), FILENAME_VIEWCOUNTS));
			} catch (ConfigurationException e) {
				log.error("Error while reading viewcount file");
				throw new PhotoException(PhotoConstants.IMAGE_ACCESS_ERROR);
			}
		}
		return viewCounts;
	}
	
	private String getPathKey(String id) {
		StringBuffer key = new StringBuffer(id);
		key.append(KEY_SEPARATOR).append(KEY_PATH);
		return key.toString();
	}
	
	private String getTimestampKey(String id) {
		StringBuffer key = new StringBuffer(id);
		key.append(KEY_SEPARATOR).append(KEY_TIMESTAMP);
		return key.toString();
	}
	
	private String getTitleKey(String id, String language) {
		StringBuffer key = new StringBuffer(id);
		key.append(KEY_SEPARATOR).append(KEY_TITLE).append(KEY_SEPARATOR).append(language);
		return key.toString();
	}
	
	private String getTextKey(String id, String language) {
		StringBuffer key = new StringBuffer(id);
		key.append(KEY_SEPARATOR).append(KEY_TEXT).append(KEY_SEPARATOR).append(language);
		return key.toString();
	}
	
	private String getAuthorizedGroupsKey(String id) {
		StringBuffer key = new StringBuffer(id);
		key.append(KEY_SEPARATOR).append(KEY_AUTHORIZED);
		return key.toString();
	}
	
	private String getAdminGroupsKey(String id) {
		StringBuffer key = new StringBuffer(id);
		key.append(KEY_SEPARATOR).append(KEY_ADMIN);
		return key.toString();
	}
	
	private String getThemeKey(String id) {
		StringBuffer key = new StringBuffer(id);
		key.append(KEY_SEPARATOR).append(KEY_THEME);
		return key.toString();
	}
	
	/**
	 * Given a path like a/c.ZZZ, and a prefix YYY, transforms it in:
	 * a/YYYc.RESIZE_EXTENSION
	 * 
	 * Note that extension is changed. a (in this exemple) can be missing
	 * @param path
	 * @param prefix
	 * @return
	 */
	private String insertPrefixInPath(String path, String prefix, int size) {
		
    	int lastSeparatorPosition = path.lastIndexOf(PhotoConstants.SEPARATOR_PATH);
    	int extensionPosition = path.lastIndexOf(PhotoConstants.SEPARATOR_EXTENSION);
    	
    	StringBuffer buffer = new StringBuffer();
    	if (lastSeparatorPosition == -1) {
    		buffer.append(prefix).append(size);
        	if (extensionPosition != -1)
        		buffer.append(path.substring(0, extensionPosition));
        	else
        		buffer.append(path);
    	} else {
    		buffer = new StringBuffer(path.substring(0, lastSeparatorPosition+1));
	    	buffer.append(prefix).append(size);
	    	if (extensionPosition != -1)
	    		buffer.append(path.substring(lastSeparatorPosition+1, extensionPosition));
	    	else 
	    		buffer.append(path.substring(lastSeparatorPosition+1));
    	}
	    buffer.append(PhotoConstants.SEPARATOR_EXTENSION).append(RESIZE_EXTENSION);
    	return buffer.toString();
	}
	
	private String getDisplayPathForPath(String path) {
    	return insertPrefixInPath(path, PREFIX_DISPLAY, getConfiguration().getMaximumSize());
	}

    private String getThumbnailPathForPath(String path) {
    	return insertPrefixInPath(path, PREFIX_THUMBNAIL, PhotoConfigurationConstants.THUMBNAIL_SIZE);
    }
    
	/**
	 * Checks that the id is not null
	 * @param id
	 * @throws PhotoException
	 */
	private void checkId(String id) throws PhotoException {
		if (id == null) {
			log.error("FilePhotoFactory, id is null");
			throw new PhotoException(PhotoConstants.NULL_PARAMETER_ERROR);
		}
	}
	
	private String getIdForPath(String path) throws PhotoException {
		if (path.equals(""))
			path = KEY_ROOT_PATH;
		return getPathToIdIndexes().getString(path);
	}
	
	private String getPathForId(String id) throws PhotoException {
		return getDescriptions().getString(getPathKey(id));
	}
	
	protected File getFileById( String id ) throws PhotoException {
		
		String path = getPathForId(id);
		if (path == null) {
			throw new PhotoException(PhotoConstants.FILE_NOT_FOUND_ERROR+" id:"+id);
		}
		File file = new File(getConfiguration().getImageRoot(), path);
		if (!file.exists()) {
			throw new PhotoException(PhotoConstants.FILE_NOT_FOUND_ERROR+" id:"+id);
		}
		return file;
	}
	
	protected File getDisplayFileById( String id ) throws PhotoException {
		
		File file = new File(getConfiguration().getImageRoot(), getDisplayPathForPath(getPathForId(id)));
		return file;
	}
	
	/**
	 * does not check for existence
	 * @param id
	 * @return
	 * @throws PhotoException
	 */
	protected File getThumbnailFileById( String id ) throws PhotoException {
		
		File file = new File(getConfiguration().getImageRoot(), getThumbnailPathForPath(getPathForId(id))); 
		return file;
	}
	
	private String getNextId() throws PhotoException {
		long next = getDescriptions().getLong(KEY_ID_SEQUENCE, -1);
		next++;
		getDescriptions().setProperty(KEY_ID_SEQUENCE, next);
		return ""+next;
	}

	@Override
	public String getMimeType(PhotoDescription description) throws PhotoException {
		if (description == null || description.isDirectory())
			throw new PhotoException("Null argument");
		
		int separatorPosition = description.getPath().lastIndexOf(PhotoConstants.SEPARATOR_EXTENSION);
		if (separatorPosition == -1) {
			throw new PhotoException("Unknown file type");
		}
		
		String extension = description.getPath().substring(separatorPosition);
		
		if (extension.equalsIgnoreCase(".jpeg") || extension.equalsIgnoreCase(".jpg")) { //$NON-NLS-1$ //$NON-NLS-2$
			return "image/jpeg"; //$NON-NLS-1$
		} 
		if (extension.equalsIgnoreCase(".gif")) { //$NON-NLS-1$
			return "image/gif"; //$NON-NLS-1$
		} 
		if (extension.equalsIgnoreCase(".png")) { //$NON-NLS-1$
			return "image/png"; //$NON-NLS-1$
		}
		
		throw new PhotoException("Unknown file type");
	}

	@Override
	public String getResizedImagesMimeType() throws PhotoException {
		return RESIZE_MIME_TYPE;
	}
	
	public int getScale(String id) {
		try {
			return PhotoUtils.getImageScale(getFileById(id));
		} catch (PhotoException e) {
			return -1;
		}
	}
	
    public long getLastDisplayModificationDate(String requestedId) {
    	File file = getDisplayFileById(requestedId);
    	if (!file.exists())
    		return -1;
    	return file.lastModified();
    }
    public long getLastThumbnailModificationDate(String requestedId) {
    	File file = getThumbnailFileById(requestedId);
    	if (!file.exists())
    		return -1;
    	return file.lastModified();
    }
    public long getLastOriginalModificationDate(String requestedId) {
    	File file = getFileById(requestedId);
    	if (!file.exists())
    		return -1;
    	return file.lastModified();
    }
 
    // used in JSP
    public String getStorageReference(String id) {
    	return getFileById(id).getAbsolutePath();
    }
}
