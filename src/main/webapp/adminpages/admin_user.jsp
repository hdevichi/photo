<%-- Common header for Photo JSP --%>
<%@ page contentType="text/html" pageEncoding="UTF-8" language="java" errorPage="/error.jsp" import="photo.model.user.PhotoUser" import="photo.control.servlet.PhotoConstants" import="java.io.File" import="photo.model.config.PhotoConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 
<%@page import="photo.control.servlet.AdminUserControl"%>
<%@ page import="photo.control.servlet.PhotoServlet" %>
<%@ page import="photo.control.servlet.AdminServlet" %>
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

<%// Verifier que le role est bien admin
if (!user.isAdmin()) throw new RuntimeException("Internal error - user is not admin");
PhotoUser userToUpdate = (PhotoUser)request.getAttribute(AdminServlet.REQUEST_ATTRIBUTE_USER_TO_ADMIN);%>
<script type="text/javascript" src="<%=request.getContextPath()+PhotoConstants.SCRIPTS_URI%>submitNav.js"></script>
<HEAD>
	<TITLE><fmt:message key="adminUser.title">
		<fmt:param value="<%=userToUpdate.getLogin()%>"/>
	</fmt:message></TITLE>
</HEAD>

<BODY>
	<div id="title"></div>
	<%@include file="/fragments/header.jspf"%>
	<DIV id="content"> 
	<div id="content2">
		<form name="application" method="post" action="<%=request.getContextPath()+PhotoServlet.getCommand(request)%>">
			<table id="adminTable" >
				<tr><td class="formContainer" >
					<table class="form" width="100%">
						<tr class="formHeaderRow"><td colspan="2"><fmt:message key="adminUser.body"/></td></tr>
						<tr>
							<td align="left"><input type="checkbox" name="<%=AdminUserControl.FIELD_USER_ADMIN %>" <%if (userToUpdate.isAdmin()) {%>checked<%} %>></td>
							<td><fmt:message key="adminUser.admin"/></td>
						</tr>
						<tr>
							<td align="left"><fmt:message key="adminUser.group"/></td>
							<td><select name="<%=AdminUserControl.FIELD_USER_GROUP %>">	
								<%String[] groups = photoConfig.getPhotoUserFactory().readAllGroups();
								for (int i = 0 ; i < groups.length; i++) {
									String group = groups[i];%>
								<option value="<%=group %>" <%if (userToUpdate.getGroup().equals(group)) {%> selected <%} %>><%=group %></option>
								<%}%>
								</select>
							</td>
						</tr>
						<tr><td colspan="2" align="right"><input type="submit" value="<fmt:message key="adminUser.submit"/>"></td></tr>
					</table>			
				</td></tr>
			</table>
			<input type="hidden" name="<%=AdminUserControl.FIELD_USER_SELECT %>" value="<%=userToUpdate.getLogin() %>">
		</form>
		<a href="<%=request.getContextPath()+request.getAttribute(AdminServlet.REQUEST_ATTRIBUTE_RETURN_URI) %>">
			<fmt:message key="nav.back"/>
		</a>
	</DIV>
	</DIV>
	<%@include file="/fragments/footer.jspf" %>
</BODY>
</fmt:bundle>
</HTML>