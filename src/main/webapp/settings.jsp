<%-- Common header for Photo JSP --%>
<%@ page contentType="text/html" pageEncoding="UTF-8" language="java" errorPage="/error.jsp" import="org.devichi.photo.model.user.PhotoUser" import="org.devichi.photo.control.servlet.PhotoConstants" import="java.io.File" import="org.devichi.photo.model.config.PhotoConfiguration" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 
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

<%if (user.isGuest()) 
	throw new RuntimeException("Guest user cannot access settings page!");%>

<head>
	<title>
		<fmt:message key="settings.title">
			<fmt:param value="<%=user.getLogin()%>"/>
		</fmt:message>
	</title>
</head>

<body>

	<div id="title">
		<fmt:message key="settings.bodyTitle"/>
	</div>
	
	<%@include file="/fragments/header.jspf"%>
	<DIV id="content">
	<DIV id="content2">
			<FORM action="<%=request.getContextPath()+PhotoServlet.getCommand(request) %>" name="preferences" method="post">
			<div>
			<%@include file="/fragments/message.jspf"%>
			</div>
			<table id="settingsTable">
				<tr><td class="formContainer">
					<table class="form" >
						<tr class="formHeaderRow"><td colspan="2"><fmt:message key="settings.userSettings"/></td></tr>
						<tr>
							<td><fmt:message key="settings.columns"/></td>
							<td><input type="text" name="<%=SettingsServlet.FIELD_COLUMNS %>" size="3" value="<%= user.getColumns()%>"></td>
						</tr>
						<tr>
							<td><fmt:message key="settings.theme"/></td>
							<td><select name="<%=SettingsServlet.FIELD_THEME %>">
								<%String[] themes = photoConfig.getAvailableThemes();
								for (int i = 0 ; i < themes.length; i++) {%>
									<option <%if ( themes[i].equals(user.getTheme()) ) {%>SELECTED <%} %>><%=themes[i] %></option>
								<%} %>
								</select>
							</td>
						</tr>
						<tr>
							<td><fmt:message key="settings.language"/></td>
							<td><select  name="<%=SettingsServlet.FIELD_LANGUAGE %>" >
								<%String[] locales = photoConfig.getSupportedLocales();
								for (int i = 0 ; i < locales.length; i++) {%>
									<option <%if ( locales[i].equals(user.getLanguage()) ) {%>SELECTED <%} %>><%=locales[i] %></option>
								<%} %>
								</select>
							</td>
						</tr>
						<tr><td colspan="2">&nbsp;</td></tr>
						<tr>
							<td colspan="2" class="right"><input type="submit" name="<%=SettingsServlet.ACTION_SETTINGS %>" value="<fmt:message key="settings.submitSettings"/>"></td>
						</tr>
					</TABLE>
				
				</td><td class="formContainer">
					<table class="form">
						<tr class="formHeaderRow"><td colspan="2"><fmt:message key="settings.changePassword"/></td></tr>
						<tr><td><fmt:message key="settings.oldPassword"/></td><td><input  type="password" name="<%=SettingsServlet.FIELD_PASSWORD_OLD %>"></td></tr>
						<tr><td><fmt:message key="settings.newPassword"/></td><td><input  type="password" name="<%=SettingsServlet.FIELD_PASSWORD_NEW %>"></td></tr>
						<tr><td><fmt:message key="settings.repeatPassword"/></td><td><input  type="password" name="<%=SettingsServlet.FIELD_PASSWORD_CONFIRM %>"></td></tr>
						<tr><td colspan="2">&nbsp;</td></tr>
						<tr><td colspan="2" class="right"><input type="submit" name="<%=SettingsServlet.ACTION_PASSWORD %>" value="<fmt:message key="settings.submitPassword"/>"></td></tr>
					</table>
				</td></tr>
			</table>
		</FORM>
		<BR>
		<%String settingsCommand = (String)request.getAttribute(SettingsServlet.REQUEST_SETTINGS_COMMAND_ATTRIBUTE);%>
		<a href="<%=request.getContextPath()+settingsCommand%>">
			<fmt:message key="nav.back"/>
		</a>
	</DIV>
	
	</DIV>
</body>
</fmt:bundle>
</html>