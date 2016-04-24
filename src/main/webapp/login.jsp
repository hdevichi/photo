<%-- Common header for Photo JSP --%>
<%@ page contentType="text/html" pageEncoding="UTF-8" language="java" errorPage="/error.jsp" import="org.devichi.photo.model.user.PhotoUser" import="org.devichi.photo.control.servlet.PhotoConstants" import="java.io.File" import="org.devichi.photo.model.config.PhotoConfiguration" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 
<%@page import="org.devichi.photo.control.servlet.LoginServlet"%>
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
	<TITLE><fmt:message key="login.title"/></TITLE>
</HEAD>

<BODY>
<%@include file="/fragments/header.jspf"%>

<div id="title"></div>

<DIV id="content">
<div id="content2">
	<H2>
	<fmt:message key="login.body" />
	</H2>
	<%@include file="/fragments/message.jspf"%>
	
	<%String loginCommand = (String)request.getAttribute(LoginServlet.REQUEST_LOGIN_COMMAND_ATTRIBUTE);%>
	<form method="POST" name="login" action="<%=request.getContextPath()+LoginServlet.URI+loginCommand%>">
		<table align="center">
		<tr><td>
			<table id="login">
				<tr>
					<td><fmt:message key="login.user" /></td>
					<td><input class="input" type="text" name="<%=LoginServlet.FIELD_LOGIN %>"></td>
				</tr>
				<tr>
					<td><fmt:message key="login.password" /></td>
					<td><input class="input" type="password" name="<%=LoginServlet.FIELD_PASSWORD %>"></td>
				</tr>
				<tr>
					<td class="right"><fmt:message key="login.remenberMe" /></td>
					<td><input type="checkbox" name="<%=LoginServlet.FIELD_REMENBER %>"></td>
				</tr>
				<tr>
					<td colspan="2" class="right"><BR>
					<!--input type="reset" value="Reset" -->
					<input type="submit" name="<%=LoginServlet.ACTION_LOGIN %>"
						value="<fmt:message key="login.submit" />"></td>
				</tr>
			</table>
		</td></tr>		
		<tr><td>
			<div align="left">
			<%if (photoConfig.isSendMail()) {%>
				<BR><BR>
				<fmt:message key="login.forgot" /><BR>
				<input type="submit" name="<%=LoginServlet.ACTION_REMIND %>" value="<fmt:message key="login.clickHere" />">
				<fmt:message key="login.remind" />
			<%} %>
			<BR><BR>
			<fmt:message key="login.unRegistered" /><BR>
			<a href="<%=request.getContextPath()+RegisterServlet.URI+loginCommand%>">
			<fmt:message key="login.clickHere"/></a> <fmt:message key="login.register" />
			</div>		
		</td></tr>
		</table>
	</form>
	<BR><BR>
	<a href="<%=request.getContextPath()+loginCommand%>"/></a>
</DIV>
</DIV>
<%@include file="/fragments/footer.jspf" %>
</BODY>
</fmt:bundle>
</HTML>