<%-- Not the Common header for Photo JSP (does not get config, because this can fail --%>
<%@ page contentType="text/html" pageEncoding="UTF-8" language="java" errorPage="/error.jsp" import="org.devichi.photo.model.user.PhotoUser" import="org.devichi.photo.control.servlet.PhotoConstants" import="java.io.File" import="org.devichi.photo.model.config.PhotoConfiguration" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 
<%@page import="org.devichi.photo.control.filter.MainFilter"%>
<HTML>
<%@include file="/fragments/meta.jspf"%>

<%String theme = org.devichi.photo.model.config.PhotoConfigurationConstants.DEFAULT_THEME;
try {
	PhotoConfiguration photoConfig = (PhotoConfiguration)getServletContext().getAttribute(PhotoConstants.CONTEXT_CONFIGURATION);
	if (photoConfig != null) {
		PhotoUser user = (PhotoUser)session.getAttribute(PhotoConstants.SESSION_USER);
		if (user == null)
			user = photoConfig.getPhotoUserFactory().getGuestUser(request.getLocale().getLanguage());
		theme = user.getTheme();
	}
} catch (Exception e) {}
%>

<fmt:setLocale value="<%=request.getLocale().getLanguage()%>"/>
<fmt:bundle basename="org.devichi.photo.i18n.i18n">
<%-- End of Common header for Photo JSP --%>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+theme%>/photo.css" >

<%@page isErrorPage="true"%>

<HEAD>
	<TITLE><fmt:message key="error.title"/></TITLE>
</HEAD>

<BODY>
	<div id="title"></div>
	<DIV id="content">
	<DIV id="content2">
		<H1>
		<fmt:message key="error.title" />
		</H1>
		
		<%@include file="/fragments/message.jspf"%>
		<fmt:message key="error.body" />
		
		<%
		if (exception != null) { 
		MainFilter.getLogger().error("Error caught in error page: "+exception.getMessage(), exception);%>
		<table id="error">
			<tr>
				<td id="errorLabel"><fmt:message key="error.details" /></td>
				<td><c:out value="<%=exception.getMessage()%>"/></td>
			</tr>
		</table>
		<%} else {%>
		<fmt:message key="error.noDetails" />
		<%}%>
	</DIV>
	</DIV>
</BODY>
</fmt:bundle>
</HTML>