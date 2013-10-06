<%-- Common header for Photo JSP --%>
<%@ page contentType="text/html" pageEncoding="UTF-8" language="java" errorPage="/error.jsp" import="org.devichi.photo.model.photo.PhotoDescription" import="org.devichi.photo.model.user.PhotoUser" import="org.devichi.photo.control.servlet.PhotoConstants" import="java.io.File" import="org.devichi.photo.model.config.PhotoConfiguration" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 
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

<%PhotoDescription photo = (PhotoDescription)request.getAttribute(PhotoConstants.REQUEST_OBJECT_TO_DISPLAY);
if (photo == null) 
	throw new RuntimeException("Internal error - current is null");
if ( !photo.isUserAuthorized(user)) 
	throw new RuntimeException("Photo Page error: unauthorized access!");
PhotoDescription parent = photo.getParent();%>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+parent.getTheme(user)%>/photo.css" >
<HEAD>
	<TITLE>
		<fmt:message key="photo.title">
			<fmt:param value="<%=photo.getTitle(user.getLanguage())%>"/>
		</fmt:message>
	</TITLE>
</HEAD>

<BODY>

	<div id="title">
		<c:out value="<%=photo.getTitle(user.getLanguage())%>"/>
    	<% if (photo.isUserAuthorizedToAdmin(user) && user.getDisplayAdmin()) {%>
			<a href="<%=request.getContextPath()+AdminServlet.URI+PhotoConstants.SEPARATOR_PATH+photo.getId()+PhotoServlet.getCommand(request)%>"><img src="/photo/themes/<%=user.getTheme()%>/images/admin.gif" alt="<fmt:message key="photo.adminPic"/>"></a>
		<%} %>
	</div>
	
	<%@include file="/fragments/header.jspf"%>
	<DIV id="content">
		<div id="content2">
			<div id="photoHeader">
				
				<%int index = photo.getIndex();%>	
				<span id="navPrev">
					<a href="<%=request.getContextPath()+NavigationServlet.URI+PhotoConstants.SEPARATOR_PATH+photo.getPrevious().getId()%>" >
						<c:out value="<%="<"%>"/>
					</a>
				</span>
				
				<span>
					<c:out value="<%=parent.getTitle(user.getLanguage())+" ("+(index+1)+"/"+parent.getNumberOfChildImages()+")"%>"/>
				</span>
				
				<span id="navNext">
					<a href="<%=request.getContextPath()+NavigationServlet.URI+PhotoConstants.SEPARATOR_PATH+photo.getNext().getId()%>" >
						<c:out value="<%=">"%>"/>
					</a>
				</span>
			</div>
			
			<%@include file="/fragments/message.jspf"%>
				
			<div id="photo">
				<img src="<%=request.getContextPath()+FileServlet.URI_DISPLAY+PhotoConstants.SEPARATOR_PATH+photo.getId()%>" alt="<c:out value="<%=photo.getTitle(user.getLanguage())%>"/>">
			</div>
			
			<div>	
				<%if ( photo.getText(user.getLanguage()).length() > 0) {%>
					<c:out value="<%=photo.getText(user.getLanguage())%>"/>
				<%} else{%>
					<fmt:message key="photo.missingText"/>
				<%}%>
			</div>	
			
			
			<%if (photo.getMetadata() != null) { 
			org.devichi.photo.model.photo.PhotoDescription.PhotoMetadata meta = photo.getMetadata();%>
				<div id="photoMetadata">
				<fmt:message key="photo.metadata"/>
				<table>
					<tr>
						<td><fmt:message key="photo.metadataTimestamp"/></td>
						<td><fmt:message key="photo.metadataCamera"/></td>
						<td><fmt:message key="photo.metadataIso"/></td>
						<td><fmt:message key="photo.metadataFocal"/></td>
						<td><fmt:message key="photo.metadataAperture"/></td>
						<td><fmt:message key="photo.metadataExposure"/></td>
					</tr>
					<tr>
						<td><%=meta.getTimestamp() %></td>
						<td><%=meta.getCamera() %></td>
						<td><%=meta.getIso() %></td>
						<td><%=meta.getFocal() %></td>
						<td><%=meta.getAperture() %></td>
						<td><%=meta.getExposure() %></td>
					</tr>
				</table>
				</div>
			<%}%>
			
			<p>
				<a href="<%=request.getContextPath()+NavigationServlet.URI+PhotoConstants.SEPARATOR_PATH+parent.getId()%>">
					<fmt:message key="photo.back"/>
				</a>
			</p>
		</DIV>
	</DIV>
	<%@include file="/fragments/footer.jspf" %>
</BODY>
</fmt:bundle>
</HTML>
