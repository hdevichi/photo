package org.devichi.photo.model.photo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.devichi.photo.model.config.PhotoConfigurationConstants;
import org.devichi.photo.utils.PhotoException;
import org.devichi.photo.utils.PhotoUtils;

/**
 * Manages asynchronous resize operations
 * 
 * Maintains a black list of (id of )images that couldn't be resized - so
 * that the photo factory can query it before asking for a resize
 * 
 * @author Hadri
 */
public class ResizeManager extends Thread {
	
	public static final String FILENAME_BLACKLIST = "resizeBlackList.txt"; //$NON-NLS-1$
		
	private static final Logger log = Logger.getLogger(ResizeManager.class);
	
	private FilePhotoFactory factory;
	private ConcurrentLinkedQueue resizeQueue;
	private boolean interrupted = false;
	private Set<String> blackList;
	
	protected ResizeManager(FilePhotoFactory factory, ConcurrentLinkedQueue queue) {
		this.resizeQueue = queue;
		this.factory = factory;
		setDaemon(true);
		blackList = Collections.synchronizedSet(new TreeSet<String>());
	}
	
	public synchronized void run() {
		
		log.info("Starting resize manager");
		do {
			if (! resizeQueue.isEmpty()) {
				String id = (String)resizeQueue.peek();
				try {
					processResize(id);
					resizeQueue.poll(); // since resize does call getDescription which would put it again in queue, do not remove from queue until process is done
				} catch (PhotoException e) {
					log.error("Error resizing, couldn't save scale to disk!",e);
					resizeQueue.poll(); // since resize does call getDescription which would put it again in queue, do not remove from queue until process is done
				} finally {
					try {
						factory.removeFromCache(id);
					} catch (Exception e) {
						log.error("Error removing from cache",e);
					}
				}
			}
			try {
				wait(1000);
			} catch (InterruptedException e) {
				interrupted = true;
			}
		} while (!interrupted);
	}
	
	public synchronized void setInterrupted(boolean b) {
		interrupted = b;
	}
	
	// Note: resize are requested:
	// - by img admin controller, on request
	// - by FilePhotoFactory when reading images, if missing thumbnail or display image
	// An error on resize will put it on blacklist. Explicit resize request will remove it from blacklist
	private synchronized void processResize(String id) throws PhotoException { // exception thrown if scale couldnt be saved

		if (isBlacklisted(id))
			return;
		
		long start = 0;
		if (log.isInfoEnabled()) {
			log.info("Processing resize request for: "+id);
			start = System.currentTimeMillis();
		}
		
		int imageScale = -1;
		File image = null;
		File displayImage = factory.getDisplayFileById(id);
		File thumbnailImage = factory.getThumbnailFileById(id);

		try {
			image = factory.getFileById(id);
			imageScale = PhotoUtils.getImageScale(image);
		
			// Create display image at correct size
			log.info("Creating display image for: "+id);
			if ( imageScale > factory.getConfiguration().getMaximumSize() ) {
				
				// Copy & resize
				PhotoUtils.resizeImage(image, displayImage, true, factory.getConfiguration().getMaximumSize()); 
				imageScale = factory.getConfiguration().getMaximumSize();
			
			} else {
				
				// Just copy
				FileChannel sourceChannel = new FileInputStream(image).getChannel();
				FileChannel destinationChannel = new FileOutputStream(displayImage).getChannel();
				long result = sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
				long sourceSize = sourceChannel.size();
				sourceChannel.close();
				destinationChannel.close();
				if (result != sourceSize)
				    throw new IOException("Resize error: copy didn't complete. Disk full?");
			}
			
			// Create thumbnail at correct size
			log.info("Creating thumbnail for: "+id);
			PhotoUtils.resizeImage(image, thumbnailImage, true, PhotoConfigurationConstants.THUMBNAIL_SIZE);
			
		} catch (Throwable e) {
			log.warn("Error while processing resize request: "+e.getMessage(),e);
			blackList.add(id);
			
		} finally {
		
			if (log.isInfoEnabled()) 
				log.info("Resize request completed in (ms): "+(System.currentTimeMillis()-start));
		}
	}
	
	public boolean isBlacklisted(String id) {
		return blackList.contains(id);
	}
	
	/**
	 * Remove from black list, if present
	 * @param id
	 * @return
	 */
	public void forceNextResize(String id) {
		blackList.remove(id);
	}
}