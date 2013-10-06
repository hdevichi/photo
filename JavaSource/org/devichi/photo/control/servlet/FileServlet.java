package org.devichi.photo.control.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.devichi.photo.model.photo.PhotoDescription;
import org.devichi.photo.utils.PhotoException;

/**
 * This servlet serves the image files.
 * They cannot be served by the application server, since the resource
 * are located outside the application home directory.
 * 
 * This servlet accepts the following URI scheme:
 * 
 * GET thumbnail/[resource id] thumbnail for a resource
 * GET display/[resource id] display image for an id
 * GET original/[resource id] original image for an id
 * 
 * Note: those 3 URI can be configured using constants
 * 
 * @author Hadri
 */
public class FileServlet extends PhotoServlet {
	
	public static final String URI_THUMBNAIL = "/thumbnail"; 
	public static final String URI_DISPLAY = "/image";
	public static final String URI_ORIGINAL = "/original";
					
	static final long serialVersionUID = 1;
	static final Logger log = Logger.getLogger(FileServlet.class);

	private static final int BUFFER_SIZE = 8 * 1024;

	private static final SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		// Retrieves photo request
		// do not use getPathInfo since its result is encoded and that would mess a filename with special chars in it
		String requestCommand = request.getRequestURI();
		
		int separator = requestCommand.lastIndexOf(PhotoConstants.SEPARATOR_PATH);
		if (separator == -1) {
			sendUnknownCommandError(request, response);
			return;
		}
		
		String command = requestCommand.substring(request.getContextPath().length(), separator);
		String requestedId = requestCommand.substring(separator+1);
	
		PhotoDescription photo;	
		try {
			photo = getPhotoFactory().getDescription(requestedId);
		} catch (PhotoException e) {
			log.error(e.getMessage());
			response.sendError(HttpServletResponse.SC_NOT_FOUND, requestedId);
			return;
		}
		
		// Check user rights
		if (!photo.isUserAuthorized(getCurrentUser(request))) {
			log.info("File servlet: acces to file forbidden"); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		// Original is for admin only
		if (URI_ORIGINAL.equals(command)) {
			if(!photo.isUserAuthorizedToAdmin(getCurrentUser(request))) {
				log.info("File servlet: acces to file forbidden"); //$NON-NLS-1$
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
		}
		
		// Retrieve photo etag & last modification date
		long lastModified = -1;
		
		// Last modification computation starts with last modif date of the file requested
		if (URI_DISPLAY.equals(command))
			lastModified = getPhotoFactory().getLastDisplayModificationDate(requestedId);
    	if (URI_THUMBNAIL.equals(command))
    		lastModified = getPhotoFactory().getLastThumbnailModificationDate(requestedId);
    	if (URI_ORIGINAL.equals(command))
    		lastModified = getPhotoFactory().getLastOriginalModificationDate(requestedId);
		
    	// Now we check if registration timestamp is more recent
    	if (photo.getRegistrationTimestamp() > lastModified)
    		lastModified = photo.getRegistrationTimestamp();
    	
		String eTag = "\""+photo.getSize()+"-"+lastModified+"\"";
		
		// Treat If-Match header if present
        String headerValue = request.getHeader("If-Match");
        if (headerValue != null) {
            if (headerValue.indexOf('*') == -1) {

                StringTokenizer commaTokenizer = new StringTokenizer (headerValue, ",");
                boolean conditionSatisfied = false;

                while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
                    String currentToken = commaTokenizer.nextToken();
                    if (currentToken.trim().equals(eTag))
                        conditionSatisfied = true;
                }

                // If none of the given ETags match, 412 Precodition failed is
                // sent back
                if (!conditionSatisfied) {
                    response.sendError (HttpServletResponse.SC_PRECONDITION_FAILED);
                    return;
                }
            }
        }
		
		// Treat If-Modified-Since header if present
        long headerModified = request.getDateHeader("If-Modified-Since");
        if (headerModified != -1 && (request.getHeader("If-None-Match") == null) && (lastModified <= headerModified + 1000) ) {
            // The entity has not been modified since the date
            // specified by the client. This is not an error case.
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
		
        // treat If-None-Match header if present
        headerValue = request.getHeader("If-None-Match");
        if (headerValue != null) {

            boolean conditionSatisfied = false;

            if (!headerValue.equals("*")) {

                StringTokenizer commaTokenizer =
                    new StringTokenizer(headerValue, ",");

                while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
                    String currentToken = commaTokenizer.nextToken();
                    if (currentToken.trim().equals(eTag))
                        conditionSatisfied = true;
                }

            } else {
                conditionSatisfied = true;
            }

            if (conditionSatisfied) {

                // For GET and HEAD, we should respond with
                // 304 Not Modified.
                // For every other method, 412 Precondition Failed is sent
                // back.
                if ( ("GET".equals(request.getMethod()))
                     || ("HEAD".equals(request.getMethod())) ) {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                } else {
                    response.sendError
                        (HttpServletResponse.SC_PRECONDITION_FAILED);
                    return;
                }
            }
        }
 
        // Treat If-Unmodified-since header if present
        headerModified = request.getDateHeader("If-Unmodified-Since");
        if (headerModified != -1) {
            if ( lastModified > (headerModified + 1000)) {
                response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                return;
            }
        }
        
        // End of header checks: A response will be returned
                
        // Set content type of response        
		try {
			String contentType = getPhotoFactory().getMimeType(photo);
			response.setContentType(contentType);
		} catch (PhotoException e) {
			log.error(e.getMessage(),e);
			response.sendRedirect(request.getContextPath()+PhotoConstants.PATH_TO_THEMES+getCurrentUser(request).getTheme()+"/images/missing.gif");
			return;
		}
	
		// Set last modification date of response
	    Date lastModifiedDate = new Date(lastModified);
	    String lastModifiedHttp;
	    synchronized (httpDateFormat) {
            lastModifiedHttp = httpDateFormat.format(lastModifiedDate);
        }
	    response.setHeader("Last-Modified",lastModifiedHttp);
	    response.setHeader("Cache-Control", "max-age=600000"); // 1 week
	    
	    // Set Etag header
	    response.setHeader("ETag", eTag);
	    
	    // set response content
		InputStream is = null;
	    try {
	    	if (URI_DISPLAY.equals(command))
	    		is = getPhotoFactory().getDisplayStream(requestedId);
	    	if (URI_THUMBNAIL.equals(command))
	    		is = getPhotoFactory().getThumbnailStream(requestedId);
	    	if (URI_ORIGINAL.equals(command))
	    		is = getPhotoFactory().getOriginalStream(requestedId);
	    } catch (IOException e) {
	    	log.error("error while reading image id:"+requestedId, e);
	    	is = request.getSession().getServletContext().getResourceAsStream(PhotoConstants.PATH_TO_THEMES+getCurrentUser(request).getTheme()+"/images/missing.gif");
	    } catch (PhotoException e) {
	    	log.error(e.getMessage());
	    	is = request.getSession().getServletContext().getResourceAsStream(PhotoConstants.PATH_TO_THEMES+getCurrentUser(request).getTheme()+"/images/missing.gif");
	   }		
	    
	    try {
	    	OutputStream os = response.getOutputStream();
			byte[] buf = new byte[BUFFER_SIZE];  // 4K buffer
			int bytesRead;
			while ((bytesRead = is.read(buf)) != -1) {
				os.write(buf, 0, bytesRead);
			}
	    } finally {
	      if (is != null) is.close();
	    }
	    
	}

}