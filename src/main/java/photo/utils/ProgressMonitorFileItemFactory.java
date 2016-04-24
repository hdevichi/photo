package photo.utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;


import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import photo.control.servlet.AjaxServlet;
import photo.control.servlet.AjaxServlet;


public class ProgressMonitorFileItemFactory extends DiskFileItemFactory {

	private File temporaryDirectory;
	private HttpServletRequest requestRef;
	private long requestLength;
  
	public ProgressMonitorFileItemFactory(HttpServletRequest request) {
	    super();
	    temporaryDirectory = (File)request.getSession().getServletContext().
	                         getAttribute("javax.servlet.context.tempdir");
	    requestRef = request;
	    String contentLength = request.getHeader("content-length");
	    if(contentLength != null){
	       requestLength = Long.parseLong(contentLength.trim());
	    }
	}
  
	public FileItem createItem(String fieldName, String contentType,
                             boolean isFormField, String fileName) {
 
    SessionUpdatingProgressObserver observer = null;
    if(isFormField == false) //This must be a file upload.
       observer = new SessionUpdatingProgressObserver( fileName);
       ProgressMonitorFileItem item = new ProgressMonitorFileItem(
                                       fieldName,
                                       contentType,
                                       isFormField,
                                       fileName,
                                       2048,
                                       temporaryDirectory,
                                       observer,
                                       requestLength);
       return item;
	}

	public class SessionUpdatingProgressObserver {
		
		private String fileName;
		
		public SessionUpdatingProgressObserver( String fileName){
			this.fileName = fileName;
		}
		
		
		public void setProgress(double progress) {
			
			if(requestRef != null){
				requestRef.getSession().setAttribute(AjaxServlet.SESSION_ATTRIBUTE_UPLOAD_PROGRESS,progress);
				requestRef.getSession().setAttribute(AjaxServlet.SESSION_ATTRIBUTE_UPLOAD_FILENAME,fileName);
				
				//requestRef.getSession().setAttribute("FileUpload.Size."+fieldName,fileSize);
				
				//List uploads = (List)requestRef.getSession().getAttribute("FileUpload");
				//if (uploads == null)
				//	uploads = new Vector();
				//if (!uploads.contains(fileName))
				//	uploads.add(fileName);
			}
		}	
	}
  
	public class ProgressMonitorFileItem extends DiskFileItem {

		static final long serialVersionUID = 1;
		
		private SessionUpdatingProgressObserver observer;
		private long passedInFileSize;
		private long bytesRead;
		
		private boolean isFormField;
		
		public ProgressMonitorFileItem(String fieldName, String contentType, 
									   boolean isFormField, String fileName, 
									   int sizeThreshold, File repository,
									   SessionUpdatingProgressObserver observer,
									   long passedInFileSize) {
			super(fieldName, contentType, isFormField, fileName, sizeThreshold, repository);
			this.observer = observer;
			this.passedInFileSize = passedInFileSize;
			this.isFormField = isFormField;
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			OutputStream baseOutputStream = super.getOutputStream();
			if(isFormField == false){
				return new BytesCountingOutputStream(baseOutputStream);
			}else{
				return baseOutputStream;
			}
		}
		
		private class BytesCountingOutputStream extends OutputStream{
			
			private long previousProgressUpdate;
			private OutputStream base;
			
			public BytesCountingOutputStream(OutputStream ous){
				base = ous;
			}

			public void close() throws IOException {
				base.close();
			}

			public boolean equals(Object arg0) {
				return base.equals(arg0);
			}

			public void flush() throws IOException {
				base.flush();
			}

			public int hashCode() {
				return base.hashCode();
			}

			public String toString() {
				return base.toString();
			}

			public void write(byte[] bytes, int offset, int len) throws IOException {
				base.write(bytes, offset, len);
				fireProgressEvent(len);
			}

			public void write(byte[] bytes) throws IOException {
				base.write(bytes);
				fireProgressEvent(bytes.length);
			}

			public void write(int b) throws IOException {
				base.write(b);
				fireProgressEvent(1);
			}
			
			private void fireProgressEvent(int b){
				bytesRead += b;
				if(bytesRead - previousProgressUpdate > (passedInFileSize / 500.0) ){
					observer.setProgress(( ((double)(bytesRead)) / passedInFileSize) * 100.0);
					previousProgressUpdate = bytesRead;
				}
			}	
		}
	}

}
