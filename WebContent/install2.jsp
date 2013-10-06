<%-- Common header for Photo JSP --%>
<%@ page contentType="text/html" pageEncoding="UTF-8" language="java" errorPage="/error.jsp" import="org.devichi.photo.control.servlet.PhotoConstants" import="java.io.File" import="org.devichi.photo.model.config.PhotoConfigurationConstants"  import="org.devichi.photo.model.config.PhotoConfiguration" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 
<%@page import="org.devichi.photo.control.servlet.InstallServlet"%>
<HTML>
<%@include file="/fragments/meta.jspf"%>

<fmt:setLocale value="<%=request.getLocale().getLanguage()%>"/>
<fmt:bundle basename="org.devichi.photo.i18n.i18n">
<%-- End of Common header for Photo JSP --%>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+org.devichi.photo.model.config.PhotoConfigurationConstants.DEFAULT_THEME%>/photo.css" >

<HEAD>
	<TITLE><fmt:message key="install2.title"/></TITLE>
</HEAD>
<BODY>
<div id="content">

	<H1><fmt:message key="install2.bodyTitle"/></H1>
	<p><fmt:message key="install2.body1"/></p>
	<p><fmt:message key="install2.body2"/></p>
	
	<%@include file="/fragments/message.jspf"%>
		
	<FORM action="<%=request.getContextPath()+InstallServlet.URI+PhotoConstants.SEPARATOR_PATH+InstallServlet.COMMAND_STEP2 %>" method="post" name="">
	
	<table id="install">
		<tr><td class="formContainer">
	
		<table class="form" width="600px">
			<tr class="formHeaderRow">
				<td colspan="2"><fmt:message key="install2.tableHeader"/></td>
			</tr>
			
			<tr>
				<td><fmt:message key="install2.defaultGroup"/></td>
				<td><input type="text" name="<%=InstallServlet.FIELD_DEFAULT_GROUP %>" value="default"></td>
			</tr>
			<tr>
				<td colspan="2">&nbsp;</td>
			</tr>
			<tr>
				<td><fmt:message key="install2.adminUser"/></td>
				<td><input type="text" name="<%=InstallServlet.FIELD_LOGIN %>" value=""></td>
			</tr>
			<tr>
				<td><fmt:message key="install2.adminPwd"/></td>
				<td><input type="password" name="<%=InstallServlet.FIELD_PASS %>" value=""></td>
			</tr>
			<tr>
				<td><fmt:message key="install2.adminPwd2"/></td>
				<td><input type="password" name="<%=InstallServlet.FIELD_PASS2 %>" value=""></td>
			</tr>
			<tr>
				<td colspan="2">&nbsp;</td>
			</tr>
			<tr>
				<td colspan="2"><fmt:message key="install2.adminUserNote"/></td>
			</tr>
			<tr>
				<td colspan="2">&nbsp;</td>
			</tr>
		</table>
		</td></tr>
	</table>
	
	<input type="submit" name="<%=InstallServlet.ACTION2 %>" value="<fmt:message key="install.submit"/>">
	
	</FORM>
</div>
</BODY>
</fmt:bundle>
</HTML>