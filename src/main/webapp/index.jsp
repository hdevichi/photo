<%-- Common header for Photo JSP --%>
<%@ page contentType="text/html" pageEncoding="UTF-8" language="java" errorPage="/error.jsp" import="org.devichi.photo.model.photo.PhotoDescription" import="org.devichi.photo.model.user.PhotoUser" import="org.devichi.photo.control.servlet.PhotoConstants" import="java.io.File" import="org.devichi.photo.model.config.PhotoConfiguration" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 
<%@page import="org.devichi.photo.model.config.PhotoConfigurationConstants"%>
<%@page import="org.devichi.photo.control.servlet.NavigationServlet"%>
<%@page import="org.devichi.photo.control.servlet.AdminServlet"%>
<%@page import="org.devichi.photo.control.servlet.FileServlet"%>
<HTML>
<%@include file="/fragments/meta.jspf"%>
<% // get session attributes
PhotoConfiguration photoConfig = (PhotoConfiguration)getServletContext().getAttribute(PhotoConstants.CONTEXT_CONFIGURATION);
if (photoConfig == null)
	throw new RuntimeException("Internal error - Config is null");
PhotoUser user = (PhotoUser)session.getAttribute(PhotoConstants.SESSION_USER);
if (user == null)
	user = photoConfig.getPhotoUserFactory().getGuestUser(request.getLocale().getLanguage());
%>
<fmt:setLocale value="<%=user.getLanguage()%>"/>
<fmt:bundle basename="org.devichi.photo.i18n.i18n">
<%-- End of Common header for Photo JSP --%>

<%-- Define variable and scripts used in page --%>
<%PhotoDescription directory = (PhotoDescription)request.getAttribute(PhotoConstants.REQUEST_OBJECT_TO_DISPLAY); 
if (!directory.isDirectory()) {
	throw new RuntimeException("Index Page error: object is not a directory!");
}
int images = directory.getNumberOfChildImages();
int directories = directory.getNumberOfChildDirectories();
%>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+directory.getTheme(user)%>/photo.css" >

<%-- Page header --%>
<HEAD>		
	<TITLE>
		<fmt:message key="index.pageTitle">
			<fmt:param value="<%=directory.getTitle(user.getLanguage())%>"/>
		</fmt:message>
	</TITLE>
</HEAD>

<%-- Page body --%>
<BODY>

	<%-- logo & user menu divs --%>
	<%@include file="/fragments/header.jspf" %>
	<%@include file="/fragments/jsWarning.jspf"%>
	
	<%-- title div --%>
	<DIV id="title">
		<%-- Insert directory title --%>	
		<%=directory.getTitle(user.getLanguage())%>
		
		<%if (directory.isUserAuthorizedToAdmin(user) && user.getDisplayAdmin()) {%> 
			<a href="<%=request.getContextPath()+AdminServlet.URI+PhotoConstants.SEPARATOR_PATH+directory.getId()+PhotoServlet.getCommand(request)%>">
				<img src="<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+user.getTheme()%>/images/admin.gif" alt="<fmt:message key="index.adminDir"/>">
			</a>
		<%}%>
	</DIV>
	
	<%-- body div --%>
	<DIV id="content">
		<div id="content2">

			<%@include file="/fragments/message.jspf"%>
			
			<%-- Directory description --%>		
			<p>
				<%// Display directory text
				if (directory.getText(user.getLanguage()).length() == 0) {%>
					<fmt:message key="index.missingText"/>
				<%} else  {%>
					<c:out value="<%=directory.getText(user.getLanguage())%>"/>
				<%}%>
			</p>
			
			<%-- Directory Content --%>	
			<table id="index">
				<%int i= 0;
				// number of objects to display = directories (parent won't increment counter) + images; if no child, still display parent.
				while ( (i < directories + images) || i == 0 ) {%>
					<tr>
					<%for (int j = 0 ; j < user.getColumns() ; j++ ) {%>
						<%-- Display parent without incrementing counter if directory != root --%>			
						<%if ( i + j == 0 && !directory.getPath().equals("") ) { %>
							<td class="indexCell">
								<div class="thumbDir">
									<a class="thumb2" href="<%=request.getContextPath()+NavigationServlet.URI+PhotoConstants.SEPARATOR_PATH+directory.getParent().getId()%>">
										<IMG class="thumb3" src="<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+user.getTheme()%>/images/parentFolder.gif" alt="<fmt:message key="index.up"/>">
									</a>
								</div>
								<div class="description">
									<fmt:message key="index.up"/>
								</div>
							</td>
							<%j++; i--;
						}%>
						<%-- Display a child (directory or image) --%>	
						<td class="indexCell">
						<%if ( i + j <  directories + images ) {
							// Child is a directory ...
							if ( i + j <  directories ) {
								PhotoDescription dir = directory.getChildDirectory(i+j); 
								%>
								<div class="thumbDir">
									<a class="thumb2" href="<%=request.getContextPath()+NavigationServlet.URI+PhotoConstants.SEPARATOR_PATH+dir.getId()%>">
										<IMG class="thumb3" src="<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+user.getTheme()%>/images/<%if (!dir.isUserAuthorized(user)) {%>locked<%} %>folder.gif" alt="<c:out value="<%=dir.getTitle(user.getLanguage())%>"/>">
									</a>
								</div>
								<div class="description">
									<c:out value="<%=dir.getTitle(user.getLanguage())%>"/>
									<%if (dir.isUserAuthorizedToAdmin(user) && user.getDisplayAdmin()) {%>
										<a href="<%=request.getContextPath()+AdminServlet.URI+PhotoConstants.SEPARATOR_PATH+dir.getId()+PhotoServlet.getCommand(request)%>">
											<img src="<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+user.getTheme()%>/images/admin.gif" alt="<fmt:message key="index.adminChildDir"/>">
										</a>
									<%}%>
								</div>
							<%} else {
							// Child is an image. need 3 divs to center vertically in all browers!%>
								<div class="thumbPic">
									<%PhotoDescription childImage = directory.getChildImage(  i+j-directories);%>
									<a class="thumb2" href="<%=request.getContextPath()+NavigationServlet.URI+PhotoConstants.SEPARATOR_PATH+childImage.getId()%>">
										<img class="thumb3" src="
											<%if (PhotoConfigurationConstants.THUMBNAIL_SIZE != PhotoDescription.SCALE_RESIZE_ERROR) {
												if ( PhotoConfigurationConstants.THUMBNAIL_SIZE != PhotoDescription.SCALE_NEVER_RESIZED) {%>
													<%=request.getContextPath()+FileServlet.URI_THUMBNAIL+PhotoConstants.SEPARATOR_PATH+childImage.getId()%>" 
												<% } else {%>
													<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+user.getTheme()+"/images/resize.gif" %>"
												<% }
											} else { %>
											<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+user.getTheme()+"/images/missing.gif" %>"
											<%} %>
											alt="<c:out value="<%=childImage.getTitle(user.getLanguage())%>"/>">
									</a>	
								</div>
								<div class="description">	
									<c:out value="<%=childImage.getTitle(user.getLanguage())%>"/>
									<%if (directory.isUserAuthorizedToAdmin(user) && user.getDisplayAdmin()) {%>
										<a href="<%=request.getContextPath()+AdminServlet.URI+PhotoConstants.SEPARATOR_PATH+childImage.getId()+PhotoServlet.getCommand(request)%>"><img src="<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+user.getTheme()%>/images/admin.gif" alt="<fmt:message key="index.adminChildImg"/>"></a>
									<%}%>									
								</div>
								<div class="viewed">
									<fmt:message key="index.viewed">
										<fmt:param value="<%=childImage.getViewCount() %>"/>
									</fmt:message>
								</div>
							<%}					
						}%>
						</td>
					<%}%>
					</tr>
					<%//if (i == -1) if not commented, the last image is missing from child dirs (those which display 'up'
					  //	i++;
					i += user.getColumns();
				}%>
			</table>
		</div>
	</DIV>
	
	<%@include file="/fragments/footer.jspf" %>
</BODY>
</fmt:bundle>
</HTML>