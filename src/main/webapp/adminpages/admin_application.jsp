<%-- Common header for Photo JSP --%>
<%@ page contentType="text/html" pageEncoding="UTF-8" language="java" errorPage="/error.jsp" import="photo.model.user.PhotoUser" import="photo.control.servlet.PhotoConstants" import="java.io.File" import="photo.model.config.PhotoConfiguration" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 
<%@page import="photo.control.servlet.AdminAppControl"%>
<%@ page import="photo.model.config.PhotoConfigurationConstants" %>
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

<script type="text/javascript" src="<%=request.getContextPath()+PhotoConstants.SCRIPTS_URI%>submitNav.js"></script>
<%// Verifier que le role est bien admin
if (!user.isAdmin()) throw new RuntimeException("Admin application error: not authorized!");
String[] groups = photoConfig.getPhotoUserFactory().readAllGroups(); 
String[] supLocs = photoConfig.getSupportedLocales();
String formattedSupLocs = "";
for (int i = 0 ; i < supLocs.length ; i++) {
	formattedSupLocs += supLocs[i]+ PhotoConfigurationConstants.SUPPORTED_LOCALES_SEPARATOR;
}%>
<HEAD>
	<TITLE><fmt:message key="adminApp.title"/></TITLE>
</HEAD>

<BODY>
	<div id="title"></div>
	
	<%@include file="/fragments/header.jspf"%>
	<DIV id="content">
	<div id="content2">
		<form name="application" method="post" action="<%=request.getContextPath()+PhotoServlet.getCommand(request) %>">
		<%@include file="/fragments/message.jspf"%>
		<table id="adminTable" >
			<tr><td class="formContainer" rowspan="3">
				<table class="form" width="100%">
					<tr class="formHeaderRow"><td colspan="2"><fmt:message key="adminApp.appConfig"/></td></tr>
					<tr>
						<td align="left"><fmt:message key="adminApp.security"/></td>
						<td align="left"><input type="checkbox" name="<%=AdminAppControl.FIELD_CONFIG_SECURITY %>" <%if (photoConfig.isSecurityMode()) {%> checked="checked" <%} %>></td>
					</tr>
					<tr>
						<td colspan="2" align="center"><fmt:message key="adminApp.newUsers"/></td>
					</tr><tr>
						<td align="left"><fmt:message key="adminApp.defaultTheme"/></td>
						<td align="left"><select style="width: 100%;" name="<%=AdminAppControl.FIELD_CONFIG_DEFAULT_THEME %>">
							<%String[] themes = photoConfig.getAvailableThemes();
							for (int i = 0 ; i < themes.length; i++) {%>
								<option <%if ( themes[i].equals(photoConfig.getDefaultTheme()) ) {%>SELECTED <%} %>><%=themes[i] %></option>
							<%} %>
						</select></td>	
					</tr><tr>
						<td align="left"><fmt:message key="adminApp.defaultColumns"/></td>
						<td>
							<input size="30" type="text" name="<%=AdminAppControl.FIELD_CONFIG_DEFAULT_COLUMNS %>" value="<%=photoConfig.getDefaultColumns() %>">
						</td>
					</tr><tr>
						<td align="left"><fmt:message key="adminApp.defaultGroup"/></td>
						<td align="left">
							<select style="width: 100%;" name="<%=AdminAppControl.FIELD_CONFIG_DEFAULT_GROUP%>">
								<%for (int i = 0 ; i < groups.length; i++) {%>
								<option <%if ( groups[i].equals(photoConfig.getDefaultGroup()) ) {%>SELECTED <%} %>><%=groups[i] %></option>
								<%} %>
							</select>
							
						</td>
					</tr><tr>
						<td colspan="2" align="center"><fmt:message key="adminApp.appSettings"/></td>
					</tr><tr>
						<td align="left"><fmt:message key="adminApp.pathToImages"/></td>
						<td>
							<input size="30" type="text" name="<%=AdminAppControl.FIELD_CONFIG_PATH %>" value="<%=photoConfig.getPathToImages() %>">
						</td>
					</tr><tr>
						<td align="left"><fmt:message key="adminApp.imageTimeout"/></td>
						<td>
							<input size="30" type="text" name="<%=AdminAppControl.FIELD_CONFIG_IMAGE_TIMEOUT %>" value="<%=photoConfig.getImageTimeout() %>">
						</td>
					</tr><tr>
						<td align="left"><fmt:message key="adminApp.locales"/></td>
						<td>
							<input size="30" type="text" name="<%=AdminAppControl.FIELD_CONFIG_LOCALES %>" value="<%=formattedSupLocs%>">
						</td>
					</tr><tr>
						<td align="left"><fmt:message key="adminApp.modelFactory"/></td>
						<td>
							<input size="30" type="text" name="<%=AdminAppControl.FIELD_CONFIG_FACTORY %>" value="<%=photoConfig.getPhotoFactoryClass() %>">
						</td>
					</tr><tr>
						<td colspan="2" align="center"><fmt:message key="adminApp.userSettings"/></td>
					</tr><tr>
						<td align="left"><fmt:message key="adminApp.userFactory"/></td>
						<td>
							<input size="30" type="text" name="<%=AdminAppControl.FIELD_CONFIG_USER_FACTORY %>" value="<%=photoConfig.getPhotoUserFactoryClass() %>">
						</td>
					</tr><tr>
						<td align="left"><fmt:message key="adminApp.userTimeout"/></td>
						<td>
							<input size="30" type="text" name="<%=AdminAppControl.FIELD_CONFIG_USER_TIMEOUT %>" value="<%=photoConfig.getUserTimeout() %>">
						</td>
					</tr><tr>
						<td align="left"><fmt:message key="adminApp.userDBLogin"/></td>
						<td>
							<input size="30" type="text" name="<%=AdminAppControl.FIELD_CONFIG_DB_LOGIN %>" value="<%=photoConfig.getJDBCLogin() %>">
						</td>
					</tr><tr>
						<td align="left"><fmt:message key="adminApp.userDBPassword"/></td>
						<td>
							<input size="30" type="text" name="<%=AdminAppControl.FIELD_CONFIG_DB_PASSWORD %>" value="<%=photoConfig.getJDBCPassword() %>">
						</td>
					</tr><tr>
						<td align="left"><fmt:message key="adminApp.userDBURL"/></td>
						<td>
							<input size="30" type="text" name="<%=AdminAppControl.FIELD_CONFIG_DB_URL %>" value="<%=photoConfig.getJDBCConnectionUrl() %>">
						</td>
					</tr><tr>
						<td align="left"><fmt:message key="adminApp.userDBDriver"/></td>
						<td>
							<input size="30" type="text" name="<%=AdminAppControl.FIELD_CONFIG_DB_DRIVER %>" value="<%=photoConfig.getJDBCDriverName() %>">
						</td>
					</tr><tr>
						<td colspan="2" align="center"><fmt:message key="adminApp.mailSettings"/></td>
					</tr><tr>
						<td align="left"><fmt:message key="adminApp.mailServer"/></td>
						<td>
							<input size="30" type="text" name="<%=AdminAppControl.FIELD_CONFIG_MAIL_SERVER %>" value="<%=photoConfig.getSMTPServer() %>">
						</td>
					</tr><tr>
						<td align="left"><fmt:message key="adminApp.adminMail"/></td>
						<td>
							<input size="30" type="text" name="<%=AdminAppControl.FIELD_CONFIG_ADMIN_MAIL %>" value="<%=photoConfig.getAdminEMailAdress() %>">
						</td>
					</tr><tr>
						<td align="left"><fmt:message key="adminApp.adminName"/></td>
						<td>
							<input size="30" type="text" name="<%=AdminAppControl.FIELD_CONFIG_ADMIN_NAME %>" value="<%=photoConfig.getAdminEMailName() %>">
						</td>
					</tr>
					<tr><td colspan="2" align="right">
						<input size="30" type="submit" name="<%=AdminAppControl.ACTION_CONFIG%>" value="<fmt:message key="adminApp.submitConfig"/>">
					</td></tr>
				</table>
				
			</td><td class="formContainer" rowspan="3">
			
				<table class="form" width="100%">
					<tr class="formHeaderRow"><td colspan="2"><fmt:message key="adminApp.manageUsers"/></td></tr>
					<tr><td><table >
						<tr>
							<td>&nbsp;</td>
							<td><fmt:message key="adminApp.login"/></td>
							<td><fmt:message key="adminApp.group"/></td>
							<td><fmt:message key="adminApp.mail"/></td>
							<td><fmt:message key="adminApp.lang"/></td>
							<td><fmt:message key="adminApp.theme"/></td>
							<td><fmt:message key="adminApp.cols"/></td>
						</tr>
						<%String[] userList = photoConfig.getPhotoUserFactory().readAllLogins();
						for (int i = 0 ; i < userList.length; i++) { %>
							<%PhotoUser u = photoConfig.getPhotoUserFactory().readUser(userList[i]);%>
							<tr><td><input type="radio" name="<%=AdminAppControl.FIELD_USER_SELECT %>" value="<%=u.getLogin() %>"></td>
							<td align="left">
								<a href="<%=request.getContextPath()+AdminServlet.URI+PhotoConstants.SEPARATOR_PATH+AdminServlet.COMMAND_USERS+u.getLogin()+request.getAttribute(AdminServlet.REQUEST_ATTRIBUTE_RETURN_URI) %>"><%=u.getLogin() %></a>
							</td><td  align="left"<%if (u.isAdmin()) {%>style="font-weight:bold;"<%} %>>
								<%=u.getGroup() %></td><td align="left"><%=u.getEmail() %>
							</td><td align="left">
								<%=u.getLanguage() %>
							</td><td align="left">
								<%=u.getTheme() %>
							</td><td align="left">
								<%=u.getColumns() %>
							</td>
						<%} %>
					</table></td>
					</tr><tr><td colspan="2" align="right">
						<input type="submit" name="<%=AdminAppControl.ACTION_USER_DELETE%>" value="<fmt:message key="adminApp.submitUserDelete"/>">
					</td></tr>
				</table>
							
			</td><td class="formContainer" >
			
				<table class="form" width="100%">
					<tr class="formHeaderRow"><td><fmt:message key="adminApp.listGroup"/></td></tr>
					<%for (int i = 0 ; i <  groups.length ; i++) {%>
					<tr><td><%=groups[i]%><%if (groups[i].equals(photoConfig.getDefaultGroup())) {%> (default)<%} %></td></tr>
					<%} %>
				</table>
				
			</td></tr><tr><td class="formContainer">
			
				<table class="form"  width="100%">
					<tr class="formHeaderRow"><td colspan="2"><fmt:message key="adminApp.deleteGroup"/></td></tr>
					<%
					if (groups.length > 0 ) {	
						for (int i = 0 ; i < groups.length; i++) {
						String group = groups[i];
						if (group.equals(PhotoConstants.GUEST_GROUP_NAME)) { continue;}%>
								<tr><td><input type="radio" name="<%=AdminAppControl.FIELD_GROUP_DELETE_GROUP %>" value="<%=group %>"></td><td><%=group %></td></tr>
								
					<%}%>
						<tr><td colspan="2" align="right"><input type="submit" name="<%=AdminAppControl.ACTION_GROUP_DELETE%>" value="<fmt:message key="adminApp.submitDeleteGroup"/>"></td></tr>
					<% } else { %>
						<tr><td><fmt:message key="adminApp.noGroups"/></td></tr>
					<%} %>
					
				</table>
					
			</td></tr><tr><td class="formContainer">
			
				<table class="form" >
					<tr class="formHeaderRow"><td colspan="2"><fmt:message key="adminApp.addGroup"/></td></tr>
					<tr><td>Nom</td><td><input type="text" name="<%=AdminAppControl.FIELD_GROUP_ADD_NAME %>" value=""></td></tr>
					<tr><td colspan="2" align="right"><input type="submit" name="<%=AdminAppControl.ACTION_GROUP_ADD%>" value="<fmt:message key="adminApp.submitAddGroup"/>"></td></tr>
				</table>					
			</td></tr>
		</table>
		</form>
		<a href="<%=request.getContextPath()+request.getAttribute(AdminServlet.REQUEST_ATTRIBUTE_RETURN_URI) %>"><fmt:message key="nav.back"/></a>
	</div>
	</DIV>
	<%@include file="/fragments/footer.jspf" %>
</BODY>
</fmt:bundle>
</HTML>