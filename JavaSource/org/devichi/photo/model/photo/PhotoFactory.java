package org.devichi.photo.model.photo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.devichi.photo.control.servlet.PhotoConstants;
import org.devichi.photo.model.config.PhotoConfiguration;
import org.devichi.photo.model.config.PhotoConfigurationConstants;
import org.devichi.photo.model.photo.PhotoDescription.PhotoMetadata;
import org.devichi.photo.model.user.PhotoUser;
import org.devichi.photo.utils.PhotoException;

/**
 * An interface who allows to read PhotoDescription objects from the storage.
 * The data read is a tree of directories and pictures, as well as meta information.
 * 
 * Subclasses are expected to create thumbnail when required, and also provide
 * image rotate capabilities.
 * 
 * Note that resized or rotated images are always JPEG even if original isn't.
 * So that file extension may be different between original/resized.
 *  
 * Note: root ignores access rights
 * 
 * @author Hadrien Devichi
 */
public abstract class PhotoFactory {
		
	static final Logger log = Logger.getLogger(PhotoFactory.class);
	
	/**
	 * Helper class that allows picture statistics computations to run asynchronously
	 * in its own thread, on a regular delay
	 * 
	 * @author Hadrien Devichi
	 *
	 */
	protected class StatisticsManager extends Thread {
		
		private boolean interrupted = false;
		
		protected StatisticsManager() {
			setDaemon(true);
		}
		
		public synchronized void run() {
			
			log.info("Starting statistics manager");
			do {
				try {
					wait(1000 * PhotoConfigurationConstants.STATISTICS_TIMEOUT_SECONDS);
					log.info("Updating stats");
					updatePhotoStatistics();
				} catch (InterruptedException e) {
					interrupted = true;
				}
			} while (!interrupted);
		}
		
		public synchronized void setInterrupted(boolean b) {
			interrupted = b;
		}
	}
	
	/**
	 * Cache for PhotoDescription objects read from disk by the factory
	 */
	protected Hashtable<String, PhotoDescription> descriptionCache = new Hashtable<String, PhotoDescription>();
	
	private PhotoConfiguration config;
		
	// image statistics & manager
	protected int numberOfPictures = -1;
	protected long lastModificationTime = -1;
	protected long lastComputationTime = -1;
	protected StatisticsManager statisticsManager;
	
	public PhotoFactory() {	
	}
	
	public void setConfiguration(PhotoConfiguration config) {
		this.config = config;
	}
	
	public PhotoConfiguration getConfiguration() {
		if (config == null) {
			log.error("PhotoFactory has no configuration!");
			throw new RuntimeException(PhotoConstants.CONFIG_ERROR);
		}
		return config;
	}
	
	public PhotoFactory( PhotoConfiguration config) {
		this.config = config;
	}
	
	// Read methods
	protected abstract PhotoDescription internalGetDescription( String id ) throws PhotoException;
	
	/**
	 * These 3 method return a output stream on the image file
	 * or ioexception if error reading
	 * or photo exception if the file doesn't exist or is a directory
	 * @param id
	 * @return
	 * @throws PhotoException
	 * @throws IOException
	 */
	public abstract InputStream getDisplayStream( String id ) throws PhotoException, IOException;
	public abstract InputStream getThumbnailStream( String id ) throws PhotoException, IOException;
	public abstract InputStream getOriginalStream( String id ) throws PhotoException, IOException;
	
	public abstract String getMimeType(PhotoDescription description) throws PhotoException;
	public abstract String getResizedImagesMimeType() throws PhotoException;
	
	public abstract PhotoMetadata getExifMetaData( String id ) throws PhotoException;
	
	// (slow - for admin image)
	protected abstract int getOriginalScale(String id);
	
	// Internal information (needed for deletion confirmation)
	// list all children of the object, whether they are related to the app or not (which can happen with a filesystem implementation)
	public abstract boolean hasChildren(String id) throws PhotoException;
	public abstract String[] listAllChildren(String id) throws PhotoException;
	// this one takes a user as safety
	public abstract void deleteAllChildren(String id, PhotoUser user) throws PhotoException;
	
	// Write methods
	protected abstract void internalWriteDescription( PhotoDescription photo ) throws PhotoException;
    // batch modification mode
    public abstract void internalWriteDescriptions( List photos) throws PhotoException;
    public abstract void incrementViewCount( PhotoDescription photo) throws PhotoException;
    // return the newly created item
    protected abstract PhotoDescription internalCreateChildImage( String id, File image) throws PhotoException; 
    protected abstract PhotoDescription internalCreateChildDirectory( String id, String name) throws PhotoException; 
    
    // Manipulation
    protected abstract void internalDelete( String id ) throws PhotoException;
    protected abstract void internalMove( PhotoDescription source, String destinationId ) throws PhotoException;
    protected abstract void internalRename( PhotoDescription source, String newName ) throws PhotoException;
    
    protected abstract void internalRotate( String id) throws PhotoException;    
    // Navigation
    protected abstract String getParent(String id) throws PhotoException;
    protected abstract PhotoDescription[] getChildDirectories( String id ) throws PhotoException;
    protected abstract PhotoDescription[] getChildImages( String id ) throws PhotoException;
    // For performance reasons (often requested, so need efficient implementation)
    protected abstract int getNumberOfChildImages( String id) throws PhotoException;
    protected abstract int getNumberOfChildDirectories( String id ) throws PhotoException;
    
    // needed for admin (recursive)
    public abstract List getAllChildDirectories(String id) throws PhotoException;
    // free space for pic storage in megabyte
    public abstract long getFreeSpace();
    // slow method
    protected abstract int getIndex(String id) throws PhotoException;
    
    // displayed in about.jsp
    public abstract String getStatusString( String locale);
    
	protected abstract void updatePhotoStatistics();
	
	public synchronized int getNumberOfPictures() {
			
		if (statisticsManager == null) {
			updatePhotoStatistics();
			statisticsManager = new StatisticsManager();
			statisticsManager.start();
		}
		
		return numberOfPictures;
	}
	
	public synchronized long getLastModificationTime() {
		
		if (statisticsManager == null) {
			updatePhotoStatistics();
			statisticsManager = new StatisticsManager();
			statisticsManager.start();
		}
		return lastModificationTime;
	}
	
	public String getLastModificationTimeAsString(Locale loc) {
		return DateFormat.getDateInstance(DateFormat.LONG, loc).format(new Date(getLastModificationTime()));
	}
	
	
	/**
	 * throws PhotoException if id does not exist 
	 * 
	 */
	public PhotoDescription getDescription( String id ) throws PhotoException {
		
		if (id == null) {
			log.error("Null parameter for PhotoFactory.getDescription()");
			throw new PhotoException("Null parameter for PhotoFactory.getDescription()");
		}
			
		PhotoDescription description = descriptionCache.get(id);
		if ( description != null) {
			if (description.getAge() < (1000 * getConfiguration().getImageTimeout()) )
				return description;
		}
				
		description = internalGetDescription( id) ;
		descriptionCache.put(id,description);
		return description;

	}
	
	public abstract PhotoDescription getRoot() throws PhotoException;
   
    public void delete( String id ) throws PhotoException {
    	removeFromCache(id);
    	String parentId = getParent(id);
    	internalDelete(id);
    	removeFromCache(parentId);
    }
    
    /**
     * Move an object (whose id is source id) to the directory 
     * whose id is destinationID
     * 
     * Remove from cache and call implementor factory internalMove
     * for a directory, it is under the responsability of the implementor
     * of internalmove to also remove children from cache (or to recursively call this method)
     * @param sourceId
     * @param destinationId
     * @throws PhotoException
     */
    public void move( String sourceId, String destinationDirectoryId ) throws PhotoException {
    	
    	PhotoDescription source = getDescription(sourceId);
    	if (source == null)
			throw new RuntimeException("Photo not found for Id: "+sourceId);
    	removeFromCache(sourceId);
    	internalMove(source, destinationDirectoryId);
    	removeFromCache(destinationDirectoryId);
    }
    
    public void rename(String sourceId, String newName) throws PhotoException {
		
		PhotoDescription source = getDescription(sourceId);
		if (source == null)
			throw new RuntimeException("Photo not found for Id: "+sourceId);
		removeFromCache(sourceId);
		internalRename(source, newName);
    }
    
    public PhotoDescription createChildImage( String directoryId, File image) throws PhotoException {
    	PhotoDescription child = internalCreateChildImage(directoryId, image);
    	removeFromCache(directoryId);
    	return child;
    }
    
    public PhotoDescription createChildDirectory( String parentDirectoryId, String name) throws PhotoException {
    	PhotoDescription child = internalCreateChildDirectory(parentDirectoryId, name);
    	removeFromCache(parentDirectoryId);
    	return child;
    }
    
    protected void removeFromCache(String id) {
    	if (! descriptionCache.containsKey(id))
    		return;
    	
    	PhotoDescription toRemove = getDescription(id);
    	descriptionCache.remove(id);
    	// Also remove ancestors, because they can cache a copy of this description
    	if (!toRemove.getPath().equals("")) {
    		try {
    			removeFromCache(getParent(id));
    		} catch (PhotoException e) {
    			log.error(e);
    		}
    	}
    	
    	// voir references à la methode pour corriger les enlevement de parent en trop
    }
    
    /*
    public void writeScale( PhotoDescription photo ) throws PhotoException {
    	internalWriteDescription(photo);
    	// remove from cache since we need to reread from disk to trigger generation
    	removeFromCache(photo.getPath());
    }
    */
    
    public void writeDescription( PhotoDescription photo ) throws PhotoException {
    	internalWriteDescription(photo);
    	removeFromCache(photo.getParent().getId());
    }
    
    public void writeDescriptions( List<PhotoDescription> photos ) throws PhotoException {
    	for (PhotoDescription photo : photos)
    		removeFromCache(photo.getParent().getId());
    	internalWriteDescriptions(photos);
    }
    
    public void rotate( PhotoDescription photo ) throws PhotoException {
    	
    	if (photo.isDirectory())
    		throw new PhotoException("Cannot rotate a directory!");
    	
    	internalRotate(photo.getId());
    }
    
    public abstract void requestResize(String id, boolean forceResize);
    
    // Returns the scale of the image, or -1
    public abstract int getScale(String id); 
    
    
    public abstract long getLastDisplayModificationDate(String requestedId);
    public abstract long getLastThumbnailModificationDate(String requestedId);
    public abstract long getLastOriginalModificationDate(String requestedId);
 
    // for use in admin img.jsp
    public abstract String getStorageReference(String id);
}
