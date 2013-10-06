<%-- Common header for Photo JSP --%>
<%@ page contentType="text/html" pageEncoding="UTF-8" language="java" errorPage="/error.jsp" import="org.devichi.photo.model.user.PhotoUser" import="org.devichi.photo.control.servlet.PhotoConstants" import="java.io.File" import="org.devichi.photo.model.config.PhotoConfiguration" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 
<%@page import="org.devichi.photo.control.servlet.NavigationServlet"%>
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
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+user.getTheme()%>/photo.css" >

<script type="text/javascript" src="<%=request.getContextPath()+PhotoConstants.SCRIPTS_URI%>submitNav.js"></script>
<HEAD>
	<TITLE><fmt:message key="about.title"/></TITLE>
</HEAD>

<BODY>
	<%@include file="/fragments/header.jspf"%>
	<div id="title"><fmt:message key="about.body" /></div>
	<DIV id="content">
	<div id="content2">
		<div class="big"><fmt:message key="footer.version" /></div>
		<a href="mailto:<%=PhotoConstants.AUTHOR_EMAIL %>"><fmt:message key="about.mail" /></a>
		
		<BR><BR>
		<p><fmt:message key="about.details" /></p>
		<img src="<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES%>valid-html401.png">
		
		<BR>
		<%if (user.isAdmin() ) {%>
		<a href="<%=request.getContextPath()+PhotoConstants.LOG_URI%>photo_log.html"><fmt:message key="about.accessLog" /></a> <a href="<%=request.getContextPath()+PhotoConstants.LOG_URI%>photo.log"><fmt:message key="about.errorLog" /></a>
		<BR>
		<%} %>
		<table id="about">
		<tr><td valign="top">
			<table class="form">
				<tr class="formHeaderRow">
				<td><fmt:message key="about.statsApp"/></td></tr>
				<tr><td>
				<fmt:message key="about.uptime"/>
				<%Long start = (Long)getServletContext().getAttribute(PhotoConstants.CONTEXT_STARTTIME);
				long uptime = System.currentTimeMillis() - start;
				String format = org.devichi.photo.i18n.Message.getResource("about.timeFormat", user.getLanguage());%>
				<%= org.apache.commons.lang.time.DurationFormatUtils.formatDuration(uptime, format, false) %>
				</td></tr>
				<tr><td>
				<fmt:message key="about.requests">
					<fmt:param value="<%=getServletContext().getAttribute(PhotoConstants.CONTEXT_REQUESTS) %>"/>
				</fmt:message>
				</td></tr>
			</table>
		</td><td valign="top">
			<table class="form">
				<tr class="formHeaderRow">
				<td><fmt:message key="about.statsPic"/></td></tr>
				<tr><td>
				<fmt:message key="index.totalPics">
						<fmt:param value="<%=photoConfig.getPhotoFactory().getNumberOfPictures() %>"/>
				</fmt:message>
				<fmt:message key="index.lastModification">
						<fmt:param value="<%=photoConfig.getPhotoFactory().getLastModificationTimeAsString(new java.util.Locale(user.getLanguage())) %>"/>
				</fmt:message>
				</td></tr>
				<tr><td>
					<fmt:message key="about.disk"/> 
					<%=photoConfig.getPhotoFactory().getFreeSpace() %>M
				</td></tr>
				<tr><td>
					<%=photoConfig.getPhotoFactory().getStatusString(user.getLanguage()) %>
				</td></tr>
			</table>
		</td></tr>
		</table>
		<BR>
		<div id="changelog">
		<fmt:message key="footer.version"/><BR>
<BR>
1.3 - 31/7/2007<BR><BR>
- bug fixes<BR>
<BR>
1.3beta	- 25/7/2007<BR><BR>
- major refactoring<BR>
- added File user factory<BR>
<BR>
1.2a - 1/4/2006<BR>
<BR>
- added AJAX file upload<BR>
- added folder trees in admin<BR>
- added batch delete<BR>
- improved theme support<BR>
- added show/hide admin button<BR>
- many bug fixes
<BR>
<BR>
1.1	- 11/2/2006<BR><BR>
- added rename, batch move, access logs<BR>
- massive performance enhancement <BR>
- security improvement (added secure login option)<BR>
- HTML improvement to make most pages HTML4.01 strict compliant<BR>
- numerous improvement to administration<BR>
- improved install procedure<BR>
- display of EXIF metadata in picture page, if available<BR>
- a lot of bug fixes!<BR>
<BR>
1.0 - 2/1/2006<BR>
<BR>
First release
		</div>
		<BR>
		<BR>
		<a href="<%=request.getContextPath()+NavigationServlet.URI%>"><fmt:message key="about.back"/></a>
		
	</DIV>
	</DIV>
	<%@include file="/fragments/footer.jspf"%>
</BODY>
</fmt:bundle>
</HTML>