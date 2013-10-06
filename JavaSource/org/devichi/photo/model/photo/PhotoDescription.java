/*
 * Created on 16 mars 2005
 */
package org.devichi.photo.model.photo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.devichi.photo.control.servlet.PhotoConstants;
import org.devichi.photo.model.user.PhotoUser;
import org.devichi.photo.utils.PhotoException;

/**
 * This class represents the artefacts managed by the photo web application:
 * directories (sets of images) and images.
 * 
 * A photo description corresponds to a directory or an image, and is defined by an id (mandatory, path relative to the image
 * root in the web app), along
 * with a title (if missing, the object id), a description (not mandatory), and a view count. 
 * It also adds user rights management and some navigation information
 * (and index in page for photos), 
 * 
 * title and description are localized
 * 
 * for directories, this object also hold access & admin rights, and a theme
 * 
 * if available, for pictures, it also retrieves image EXIF metadat
 *  
 * @author Hadrien Devichi
 */
public class PhotoDescription implements Comparable {
	
	// the field scale indicate to which scale the image has been resized, or:
	public static final int SCALE_RESIZE_REQUEST = -2; // regeneration request (for thumbs)
	public static final int SCALE_RESIZE_ERROR = -1; // image has been resized, resize error
	public static final int SCALE_NEVER_RESIZED = 0; // image has never been resized
	
	static final Logger log = Logger.getLogger(PhotoDescription.class);
	
	public class PhotoMetadata {
		
		protected String camera;
		protected String timestamp;	
		protected String focal;
		protected String iso;
		protected String aperture;
		protected String exposure;
		
		public PhotoMetadata() {
		}
		
		public String getCamera() {
			return camera;
		}	
		public String getTimestamp() {
			return timestamp;
		}	
		public String getFocal() {
			
			if (focal.indexOf("/") != -1) {
				String[] fraction = focal.split("/");
				BigDecimal num = new BigDecimal(fraction[0]);
				BigDecimal den = new BigDecimal(fraction[1]);
				num = num.divide(den, MathContext.DECIMAL32);
				return num.setScale(0, BigDecimal.ROUND_HALF_EVEN).toString()+" mm";
			} else {
				BigDecimal num = new BigDecimal(focal);
				return num.setScale(0, BigDecimal.ROUND_HALF_EVEN).toString()+" mm";
			}
		}
		
		public String getIso() {
			if (iso != null)
				return iso;
			else
				return "?";
		}
		
		public String getAperture() {
			
			if (aperture == null)
				return "?";
			
			String[] fraction = aperture.split("/");
			BigDecimal num = new BigDecimal(fraction[0]);
			BigDecimal den = new BigDecimal(fraction[1]);
			den = den.divide(num, MathContext.DECIMAL32);
			return "f/"+den.setScale(1, BigDecimal.ROUND_HALF_DOWN).toString();
			//return aperture;
		}
		
		public String getExposure() {
			return exposure;
		}
	}
	
	// Id of this object
	private String id;
	
	// Path of this object (relative to the root)
	private String path; // cannot be null, complete path of the file in the photo app, "" for root
	// Time at which this object has been registered in the application
	private long registrationTimestamp;
	
	// Description of this object (initialized on creation)
	private boolean isDirectory;
	private Map<String, String> titles; // cannot be null, if a title is null getter returns id
	private Map<String, String> texts; // cannot be null , if a text is null getter returns ""
	private String theme; // (for directories only)
	private long size = -1; // image size in bytes for images, number of *images* for dirs
	
	private int viewCount; // the number of times this object has been requested using the photo web app. Must be >=0
	
	private List<String> authorizedGroups; // List of groups authorized to view this object. Cannot be null
	private List<String> adminGroups; // List of groups authorized to admin this object. Cannot be null

	// Lazy init for this one (to not slow down display of a directory)
	private PhotoMetadata metadata;
	private boolean metadataRetrieved = false; // used for lazy init, since object can be null
	
	// children (for directories)
	private PhotoDescription[] childDirectories;
	private PhotoDescription[] childImages;
	
	// Internal Technical attributes
	private PhotoFactory parentFactory;
	// Time at which this object has been instanciated (for cache expiry)
	private long creationTime;
	private String name;

	
	/**
	 * Default constructor.
	 * Arguments that would generate an incorrect object throw a PhotoException.
	 */
	public  PhotoDescription( PhotoFactory parentFactory, String id, String path, String displayPath, String thumbnailPath, boolean isDirectory, Map<String, String> titles, Map<String, String> texts, List<String> groups, List<String> adminGroups, String theme, long size, int viewCount, long timestamp ) throws PhotoException {
		
		if ( parentFactory == null || path == null || titles == null || texts == null || groups == null || adminGroups == null) {
			log.error("Error: PhotoDescription constructor, null parameter");
			throw new PhotoException(PhotoConstants.NULL_PARAMETER_ERROR);
		}
		
		// index can be =1 for root, or thumbnails
		
		if (viewCount < 0 || size < 0) {
			log.error("Invalid viewcount or size at creation of description for id: "+path);
			throw new PhotoException(PhotoConstants.INVALID_PARAMETER_ERROR);
		}
		
		if (log.isDebugEnabled()) {
			log.debug("  PhotoDescription creation, Arguments ok for: "+path); //$NON-NLS-1$
		}
		// create object
		this.id = id;
		this.path = path;
		this.titles = titles;
		this.texts = texts;
		this.viewCount = viewCount;
		this.authorizedGroups = groups;
		this.adminGroups = adminGroups;
		this.authorizedGroups = groups;
		this.size = size;
		this.isDirectory = isDirectory;
		this.parentFactory = parentFactory;
		this.theme = theme;
		
		this.registrationTimestamp = timestamp;
		this.creationTime = System.currentTimeMillis();
	}
	
	/**
	 * Returns whether the user can access the object
	 * @param user
	 * @return
	 */
    public boolean isUserAuthorized( PhotoUser user ) {
    	
    	if ( user.isAdmin())
    		return true;
    	
		return authorizedGroups.contains(user.getGroup());
    }
    
	/**
	 * Returns whether the user passed as parameter can admin the object
	 * @param user
	 * @return
	 */
    public boolean isUserAuthorizedToAdmin( PhotoUser user) {
    	
    	if (user.isAdmin())
    		return true;
    	
    	return adminGroups.contains(user.getGroup());
    }
    
    public boolean isGroupAuthorized( String group ) {
    	return authorizedGroups.contains(group);
    }
    
    public boolean isGroupAuthorizedToAdmin( String group ) {
    	return adminGroups.contains(group);
    }
    
    public void addAuthorizedGroup( String s ) throws PhotoException {

    	authorizedGroups.add(s);  	
    }
    
    public void addAdminGroup( String s ) throws PhotoException {

    	adminGroups.add(s);
    }
    
    /**
     * ignore if group wasn't in list
     */ 
    public void removeAuthorizedGroup( String s) throws PhotoException {
    	authorizedGroups.remove(s);		
    }

    /**
     * ignore if group wasn't in list
     */ 
    public void removeAdminGroup( String s ) throws PhotoException {
    	adminGroups.remove(s); 	
    }

    /**
     * Return title for a locale. 
     */
    public String getTitle(String locale) {
    	
    	String t = titles.get(locale);
    	if (t == null || t.length() == 0) {
    		
    		String name = path;
    		int separatorPos = name.lastIndexOf(PhotoConstants.SEPARATOR_PATH);
    		if (separatorPos != -1)
    			name = name.substring(separatorPos+1);
    		int extPos = name.lastIndexOf(PhotoConstants.SEPARATOR_EXTENSION);
    		if (extPos != -1)
    			name = name.substring(0, extPos);
    		return name;
    	}
    	return t;
    }
    
    public boolean hasTitle(String locale) {
    	String t = titles.get(locale);
    	return (t != null && t.length() > 0);
    }
    
    /**
     * return text for a locale or ""
     * @param locale
     * @return
     */
    public String getText(String locale) {
    	
    	String t = texts.get(locale);
    	if (t == null)
    		return ""; //$NON-NLS-1$
    	return t;
    }
    
    protected List<String> getAuthorizedGroups() {
    	return authorizedGroups;
    }
    
    protected List<String> getAdminGroups() {
    	return adminGroups;
    }
    
    protected Map<String, String> getTitles() {
    	return titles;
    }
    
    protected Map<String, String> getTexts() {
    	return texts;
    }
    
    public boolean isDirectory() {
    	return isDirectory;
    }
   
    public String getId() {
    	return id;
    }
    
    /**
     * Return qualified name, ie path relative to the root of the photos
     * @return
     */
    public String getPath() {
    	return path;
    }
    
    protected void setPath(String s) {
    	path = s;
    }
    
    /**
     * index of the image (resp. directory) in the parent set of image (resp. set of directories). -1 for root
	 * slow because not cached ! (too complex to keep in cache, since can be changed by operation on other objects)
	 */
    public int getIndex() throws PhotoException {
    	
    	return parentFactory.getIndex(getId());
    }
    
    public PhotoDescription getPrevious() {
    	
    	int previousIndex = getIndex();
    	if (previousIndex == 0) {
    		previousIndex = getParent().getNumberOfChildImages()-1;
    	} else {
    		previousIndex--;
    	}
    	return getParent().getChildImage(previousIndex);
    }
    
    public PhotoDescription getNext() {
    	
    	int nextIndex = getIndex();
    	if (nextIndex == getParent().getNumberOfChildImages()-1) {
    		nextIndex = 0;
    	} else {
    		nextIndex++;
    	}
    	return getParent().getChildImage(nextIndex);
    }
	
	public void setText(String text, Locale locale) {
		this.texts.put(locale.getLanguage(), text);
	}

	public void setTitle(String title, Locale locale) {
		this.titles.put(locale.getLanguage(), title);
	}
	
	public void setTheme(String th) {
		theme = th;
	}
	
	public String toString() {
		return id+" "+path;
	}
	
	protected void incrementViewCount() {
		viewCount++;
	}
	
	public int getViewCount() {
		return viewCount;
	}
	
	// for directories, hold number of images, but not updated when a pic is added or moved,
	// so better to use PhotoFactory methods to compute number of children (otherwise, need to wait for cache to expire)
	public long getSize() {
		return size;
	}
	
	/**
	 * Return directory theme, for the user
	 * 
	 * if the directory defines a theme, this one is used,
	 * if no, the user's default theme is used,
	 */
	public String getTheme(PhotoUser user) {
		if (theme != null && theme.length() > 0)
			return theme;
		
		return user.getTheme();
	}
	
	public String getTheme() {
		return theme;
	}
	
	public PhotoDescription getParent() throws PhotoException {
		return parentFactory.getDescription(parentFactory.getParent(getId()));
	}
	
	protected PhotoDescription[] getChildImages() throws PhotoException {
		
		if (childImages == null) {
			childImages = parentFactory.getChildImages(id);
		}
		return childImages;
	}

	protected PhotoDescription[] getChildDirectories() throws PhotoException {
		
		if (childDirectories == null ) {
			childDirectories = parentFactory.getChildDirectories(id);
		}
		return childDirectories;
	}
	
	public PhotoDescription getChildImage( int index )  throws PhotoException {
		if (index < 0 || index > (getNumberOfChildImages() - 1) ) {
			log.error("Index ("+index+") out of range in PhotoDescription.getChildImage() for: "+path);
			throw new  PhotoException(PhotoConstants.INVALID_PARAMETER_ERROR);
		}
		return getChildImages()[index];
	}
	
	public PhotoDescription getChildDirectory( int index )  throws PhotoException {
		if (index < 0 || index > (getNumberOfChildDirectories() - 1) ) {
			log.error("Index ("+index+") out of range in PhotoDescription.getChildDirectory() for: "+path);
			throw new  PhotoException(PhotoConstants.INVALID_PARAMETER_ERROR);
		}
		return getChildDirectories()[index];
	}
		
	public int getNumberOfChildImages() throws PhotoException {

		return getChildImages().length;
	}
	
	public int getNumberOfChildDirectories() throws PhotoException {
		return getChildDirectories().length;
	}
		
	protected long getAge() {
		return System.currentTimeMillis() - creationTime;
	}
	
	public PhotoMetadata getMetadata() {
		
		if (isDirectory)
			return null;
		
		if (metadataRetrieved)
			return metadata;
		
		try {
			metadata = parentFactory.getExifMetaData(id);
		} catch (PhotoException e) {
			log.error(e);
			metadata = null;
		}
		metadataRetrieved = true;
		return metadata;
	}

	public int compareTo(Object o) {
		if (! (o instanceof  PhotoDescription)) {
			throw new RuntimeException("Attempting to compare PhotoDescription to something else!");
		}
		return id.compareTo(((PhotoDescription)o).getId());
	}
	
	public String getName() {
		if (name == null) {
			int lastSeparator = path.lastIndexOf(PhotoConstants.SEPARATOR_PATH);
			if ( lastSeparator == -1)
				name = path;
			else 
				name = path.substring(path.lastIndexOf(PhotoConstants.SEPARATOR_PATH)+1);
		}
		
		return name;
	}
	
	public long getRegistrationTimestamp() {
		return registrationTimestamp;
	}
}	
