package photo.utils;

import java.awt.Graphics2D;
import java.awt.Image;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import photo.control.servlet.PhotoConstants;
import photo.i18n.Message;
import photo.model.photo.PhotoDescription;
import photo.i18n.Message;


/**
 * This class hold various utilities used by the photo web application.
 * It is able to create the thumbnails used by the photo web application, 
 * for an image, or in batch processing for a directory.
 * It is also able to generate the properties file for a directory.
 */
public class PhotoUtils {
	
	static final Logger log = Logger.getLogger(PhotoUtils.class);
	
	/**
	 * Resize an image to a specified size (ie the max dimension of the image = size)
	 * 
	 * Takes as parameter the image file to resize, the destination file, and the size.
	 * 
	 * The boolean force indicates if the destination should be overwritten if it exists,
	 * 
	 * if original image is smaller than the sizel, it is merely copied
	 * 
	 * Synchronized to ensure only 1 redimentionnement at a time (otherwise easy to have out of memory problems)
	 */	
	
	public static synchronized void resizeImage(File imageFile, File resizedImageFile, boolean force, int size) throws PhotoException {
	
		if (imageFile == null || imageFile.isDirectory() || !imageFile.exists())  {
			log.error("resizeImage: image does not exist or is a dir: "+imageFile.getName());
			throw new IllegalArgumentException(PhotoConstants.FILE_NOT_FOUND_ERROR);
		}

		if (size <=0)
			throw new IllegalArgumentException(PhotoConstants.INVALID_PARAMETER_ERROR);
		
		if (!force && resizedImageFile.exists())
			return;
		
		if (log.isDebugEnabled()) {
			log.debug("Resizing image: "+imageFile.getName()+" to size: "+size);
		}
		
		BufferedImage image = null;
		try {
			image = ImageIO.read(imageFile);
		} catch (IOException e) {
			log.error(e);
			throw new PhotoException("Image resize: error while reading source image",e);
		}
		
		if (image == null) {
			log.warn("Image file format not supported!");
			throw new PhotoException("Image resize: unsupported file format");
		}
		
		// Verify size & compute resize factor
		int height = image.getHeight();
		int width = image.getWidth();
		
		float ratio = -1f;
		
		if ( height > width) {
			//If image is Portrait 
			if (height > size) {
				ratio = ((float)size) / ((float)height); 
			}
		} else {
			// If image is Landscape
			if (width > size) {
				ratio = ((float)size) / ((float)width); 
			}
		}
		
		// Image is smaller than request size: merely copy
		if (ratio == -1f) {
			log.info("Image does not need to be resized");
			
			if (resizedImageFile.equals(imageFile)) {
				log.info("Source and destination identiqual, existing");
				return;
			}
			try {
				resizedImageFile.createNewFile();
				FileChannel sourceChannel = new FileInputStream(imageFile).getChannel();
				FileChannel destinationChannel = new FileOutputStream(resizedImageFile).getChannel();
				long result = sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
			    long sourceSize = sourceChannel.size();
				sourceChannel.close();
			    destinationChannel.close();
			    if (result != sourceSize)
			    	throw new IOException("File copy didn't complete. Disk full?");
			} catch (IOException e) {
				log.error(e);
				throw new PhotoException("Image resize: Error when writing destination file,", e);
			}
			return;
		} 
		
		// Image needs to be resized
		log.info("Resize factor: "+ratio);
		
		// Create an image buffer in which to paint on.
		Image resizedImage = getScaledInstanceAWT(image, ratio);
		
		try {
			ImageIO.write(toBufferedImage(resizedImage), "jpeg", resizedImageFile);
		} catch (IOException e) {
			throw new PhotoException("Image resize: error while writing resized image",e);
		}
			
	}	
	
    private static Image getScaledInstanceAWT(BufferedImage source, double factor) {
        int w = (int) (source.getWidth() * factor);
        int h = (int) (source.getHeight() * factor);
        return source.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING);
    }
    
    private static BufferedImage toBufferedImage(Image image) {
       
    	if (image instanceof BufferedImage)
    		return (BufferedImage)image;
    	
    	int w = image.getWidth(null);
        int h = image.getHeight(null);
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB );
        Graphics2D g = result.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }
        
	public static int getImageScale(File imageFile) throws PhotoException {
		
		if (imageFile == null || imageFile.isDirectory() || !imageFile.exists())  {
			log.error("getImageScale: image does not exist or is a dir: "+imageFile.getName());
			throw new IllegalArgumentException(PhotoConstants.FILE_NOT_FOUND_ERROR);
		}

		BufferedImage image = null;
		try {
			image = ImageIO.read(imageFile);
		} catch (IOException e) {
			throw new PhotoException("Image resize: error while reading source image",e);
		}
		
		if (image == null) {
			log.info("Image file format not supported!");
			throw new PhotoException("Image resize: unsupported file format");
		}
		
		// Verify size & compute resize factor
		int height = image.getHeight();
		int width = image.getWidth();

		if (height > width) 
			return height;
		
		return width;
	}
	
	public static void generateFolderTreeMarkup(String treeId, String functionName, PhotoDescription directory, StringBuffer output, String language) throws PhotoException {
		if (directory ==null)
			throw new RuntimeException("generateFolderTreeMarkup: illegal argument");
		
		if (!directory.isDirectory())
			return;
		
		if (directory.getPath().equals("")) {
			output.append("<ul id=\"").append(treeId).append("\" class=\"admin_folder_tree\">");
		}
		
		String name = directory.getName();
		if (directory.getPath().equals(""))
			name = Message.getResource("rootDir", language);
		output.append("<li><a href=\"#\" onclick=\"javascript:");
		output.append(functionName);
		output.append("('").append(directory.getId()).append("')\" >").append(name).append("</a>");
		
		int numberOfChildDirectories = directory.getNumberOfChildDirectories();
		if (numberOfChildDirectories != 0) {
			output.append("<ul>");
			for (int i = 0 ; i < numberOfChildDirectories ; i++) {
				PhotoDescription child = directory.getChildDirectory(i);
				generateFolderTreeMarkup(treeId, functionName, child, output, language);
			}
			output.append("</ul>");
		}
		
		output.append("</li>");
		if (directory.getPath().equals("")) {
			output.append("</ul>");
		} 
	}
	
	public static synchronized void rotateImage(File imageFile) throws PhotoException {
		
		if (imageFile == null || imageFile.isDirectory() || !imageFile.exists())  {
			log.error("rotateImage: image does not exist or is a dir: "+imageFile.getName());
			throw new IllegalArgumentException(PhotoConstants.FILE_NOT_FOUND_ERROR);
		}

		if (log.isDebugEnabled()) {
			log.debug("Rotating image: "+imageFile.getName());
		}
		
		BufferedImage image = null;
		try {
			image = ImageIO.read(imageFile);
		} catch (IOException e) {
			log.error(e);
			throw new PhotoException("Image rotate: error while reading source image",e);
		}
		
		if (image == null) {
			log.warn("Image file format not supported!");
			throw new PhotoException("Image rotate: unsupported file format");
		}
				
    	int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage result = new BufferedImage(h, w, BufferedImage.TYPE_INT_RGB );
        Graphics2D g = result.createGraphics();
        AffineTransform transform = new AffineTransform();
        transform.translate(h,0);
        transform.rotate(Math.toRadians(90),0,0);
        
        g.drawImage(image, transform, null);
        
        g.dispose();
		
		try {
			ImageIO.write(result, "jpeg", imageFile);
			int separatorPosition = imageFile.getName().lastIndexOf(PhotoConstants.SEPARATOR_EXTENSION);
			String fileExtension = imageFile.getName().substring(separatorPosition+1);
			if (!fileExtension.equalsIgnoreCase("jpeg") && !fileExtension.equalsIgnoreCase("jpg")) {
				File newFile = new File(imageFile.getParent(),imageFile.getName().substring(0, separatorPosition+1)+"jpg");
				imageFile.renameTo(newFile);
			}
		} catch (IOException e) {
			throw new PhotoException("Image resize: error while writing resized image",e);
		}
			
	}	
	
	
}