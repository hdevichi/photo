package org.devichi.photo.control.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.devichi.photo.i18n.Message;

public class AjaxServlet extends PhotoServlet {
	
	public static final String URI = "/ajax";
	
	public static final String SESSION_ATTRIBUTE_UPLOAD_PROGRESS = "FileUpload.Progress";
	public static final String SESSION_ATTRIBUTE_UPLOAD_COMPLETE = "FileUpload.Complete";
	public static final String UPLOAD_COMPLETE = "TRUE";
	public static final String SESSION_ATTRIBUTE_UPLOAD_STATUS = "FileUpload.Status";
	public static final String SESSION_ATTRIBUTE_UPLOAD_FILENAME = "FileUpload";
	
	// Ajax form constants
	public static final String ACTION = "ajaxAction"; //$NON-NLS-1$
	public static final String AJAX_ACTION_PROGRESS = "ajaxProgress"; //$NON-NLS-1$
	
	static final long serialVersionUID = 1;
	static final Logger log = Logger.getLogger(AjaxServlet.class);

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if (log.isInfoEnabled())
			log.info("Processing AJAX request for progress update");
		
		// This is a request to get the progress on the upload request.
		//Get the progress and render it as XML
	   
	    // set the header information for the response
	    response.setContentType("text/xml");
	    response.setHeader("Cache-Control", "no-cache");
	    
	    PrintWriter writer = response.getWriter();
	    writer.write("<?xml version=\"1.0\" encoding=\""+PhotoConstants.ENCODING+"\"?>");
	    writer.write("<status-response>");
	   
	    // Retrieves upload info
	    String fileName = (String)request.getSession().getAttribute(SESSION_ATTRIBUTE_UPLOAD_FILENAME);
	    Double progressCount = (Double)request.getSession().getAttribute(SESSION_ATTRIBUTE_UPLOAD_PROGRESS);
	    String status = (String)request.getSession().getAttribute(SESSION_ATTRIBUTE_UPLOAD_STATUS);
	    String complete = (String)request.getSession().getAttribute(SESSION_ATTRIBUTE_UPLOAD_COMPLETE);
    	
	    if (fileName == null || progressCount == null) {
	    	// no upload in progress
	    	writer.write("<complete>");
    		writer.write("0");
    		writer.write("</complete>");
	    	writer.write("<status>");
	    	writer.write(Message.getResource("ajax.waiting",getCurrentUser(request).getLanguage()));
	    	writer.write("</status>");
	    	writer.write("<progress>");
	    	writer.write("0");
	    	writer.write("</progress>");
	    	log.info("Waiting");
	    } else {
	    	if (log.isInfoEnabled())
	    		log.info("Progress count= "+progressCount+" for "+fileName);
	    	
	    	if (UPLOAD_COMPLETE.equals(complete) ) {
	    		writer.write("<complete>");
	    		writer.write("1");
	    		writer.write("</complete>");
	    		writer.write("<status>");
	    		if (status != null)
	    			writer.write(status);
	    		else
	    			writer.write("Unexpected error");
		    	writer.write("</status>");
		    	
		    	// cleanup
		    	request.getSession().setAttribute(SESSION_ATTRIBUTE_UPLOAD_PROGRESS, null);
		    	request.getSession().setAttribute(SESSION_ATTRIBUTE_UPLOAD_FILENAME, null);
		    	request.getSession().setAttribute(SESSION_ATTRIBUTE_UPLOAD_COMPLETE, null);
		    	
	    	} else {
	    		writer.write("<complete>");
	    		writer.write("0");
	    		writer.write("</complete>");
		    	writer.write("<status>");
		    	writer.write("envoi de "+fileName+" ("+progressCount.intValue()+"%)");
		    	writer.write("</status>");
	    	}
	    	writer.write("<progress>");
	    	writer.write(""+progressCount.intValue());
	    	writer.write("</progress>");
	    }
	    writer.write("</status-response>");
	}
}
