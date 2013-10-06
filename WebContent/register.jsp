<%-- Common header for Photo JSP --%>
<%@ page contentType="text/html" pageEncoding="UTF-8" language="java" errorPage="/error.jsp" import="org.devichi.photo.model.user.PhotoUser" import="org.devichi.photo.control.servlet.PhotoConstants" import="java.io.File" import="org.devichi.photo.model.config.PhotoConfiguration" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 
<%@page import="org.devichi.photo.control.servlet.RegisterServlet"%>
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

<HEAD>
	<TITLE>
		<fmt:message key="register.title"/>
	</TITLE>
</HEAD>

<BODY>
<%@include file="/fragments/header.jspf"%>
<DIV id="content">
	<div id="content2">
		<H2>
		<fmt:message key="register.body"/>
		</H2>
		<%@include file="/fragments/message.jspf"%>
		<form method="POST" action="<%=request.getContextPath()+RegisterServlet.URI+PhotoServlet.getCommand(request) %>">
			<table id="settingsTable">
				<tr>
					<td align="left"><fmt:message key="register.login"/></td>
					<td align="left"><input type="text" name="<%=RegisterServlet.FIELD_LOGIN %>" value=""></td>
				</tr>
				<tr>
					<td align="left"><fmt:message key="register.password"/></td>
					<td align="left"><input type="password" name="<%=RegisterServlet.FIELD_PASSWORD %>" value=""></td>
				</tr>
				<tr>
					<td align="left"><fmt:message key="register.repeatPassword"/></td>
					<td align="left"><input type="password" name="<%=RegisterServlet.FIELD_PASSWORD_CONFIRM %>" value=""></td>
				</tr>
				<tr>
					<td align="left"><fmt:message key="register.language"/></td>
					<td align="left">
						<select name="<%=RegisterServlet.FIELD_LANGUAGE %>">
							<%String[] locales = photoConfig.getSupportedLocales();
							for (int i = 0 ; i < locales.length; i++) {%>
								<option <%if ( locales[i].equals(request.getLocale().getLanguage()) ) {%>SELECTED <%} %>><%=locales[i] %></option>
							<%} %>
						</select>
					</td>
				</tr>
				<tr>
					<td align="left"><fmt:message key="register.theme"/></td>
					<td align="left">
						<select name="<%=RegisterServlet.FIELD_THEME %>">
							<%String[] themes = photoConfig.getAvailableThemes();
							for (int i = 0 ; i < themes.length; i++) {%>
								<option <%if ( themes[i].equals(photoConfig.getDefaultTheme()) ) {%>SELECTED <%} %>><%=themes[i] %></option>
							<%} %>
						</select>
					</td>
				</tr>
				<tr>
					<td align="left"><fmt:message key="register.columns"/></td>
					<td align="left"><input type="text" name="<%=RegisterServlet.FIELD_COLUMNS %>" value="<%=photoConfig.getDefaultColumns() %>"></td>
				</tr>
				<tr>
					<td align="left"><fmt:message key="register.email"/></td>
					<td align="left"><input type="text" name="<%=RegisterServlet.FIELD_EMAIL %>" value=""></td>
				</tr>
				<tr><td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<td colspan="2" align="right"><input type="submit" value="<fmt:message key="register.submit"/>"></td>
				</tr>
			</table>
			
			<BR>
			<p>
			<fmt:message key="register.mailRemark"/>
			</p>
			<p>
			<fmt:message key="register.createInGroup">
				<fmt:param value="<%=photoConfig.getDefaultGroup() %>"/>
			</fmt:message>
			</p>
			<BR>
		</form>
	</div>
</DIV>
<%@include file="/fragments/footer.jspf" %>
</BODY>
</fmt:bundle>
</HTML>