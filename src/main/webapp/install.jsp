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
	<TITLE><fmt:message key="install.title"/></TITLE>
</HEAD>
<BODY>
	<div id="content">

	<H1><fmt:message key="install.bodyTitle" /></H1>
	<p align="left"><fmt:message key="install.body1" /></p>
	<p align="left"><fmt:message key="install.body2" /></p>

	<%@include file="/fragments/message.jspf"%>

	<FORM
		action="<%=request.getContextPath()+InstallServlet.URI+PhotoConstants.SEPARATOR_PATH+InstallServlet.COMMAND_STEP1 %>"
		method="post">

	<table id="install">
		<tr>
			<td class="formContainer">

			<table class="form" width="600px">
				<tr class="formHeaderRow">
					<td colspan="2"><fmt:message key="install.imageConfig" /></td>
				</tr>
				<tr>
					<td align="left"><fmt:message key="install.imageRoot" /></td>
					<td align="right" style="width: 380px"><input type="text"
						style="width: 360px;" name="<%=InstallServlet.FIELD_IMAGES %>"
						value=""></td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<td colspan="2" align="left"><fmt:message
						key="install.imageRootDetails" /></td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<td><fmt:message key="install.photoFactory" /></td>
					<td align="right"><select
						name="<%=InstallServlet.FIELD_PHOTO_FACTORY %>">
						<option value="org.devichi.photo.model.photo.FilePhotoFactory"
							selected>Standard (file system)</option>
					</select></td>
				</tr>
			</table>

			</td>
		</tr>

		<tr>
			<td class="formContainer">&nbsp;</td>
		</tr>
		<tr>
			<td class="formContainer">

			<table class="form" width="600px">
				<tr class="formHeaderRow">
					<td colspan="2"><fmt:message key="install.userConfig" /></td>
				</tr>
				<tr>
					<td><fmt:message key="install.userFactory" /></td>
					<td align="right"><select
						name="<%=InstallServlet.FIELD_USER_FACTORY %>">
						<option value="org.devichi.photo.model.user.FilePhotoUserFactory"
							selected>Standard (file system)</option>
						<option value="org.devichi.photo.model.user.JDBCPhotoUserFactory">Advanced
						(database)</option>
					</select></td>
				</tr>
				<tr>
					<td align="left" colspan="2"><fmt:message
						key="install.userConfig2" /></td>
				</tr>
				<tr>
					<td align="left"><fmt:message key="install.url" /></td>
					<td align="right" style="width: 250px"><input type="text"
						style="width: 240px;" name="<%=InstallServlet.FIELD_DBURL %>"
						value="<%=PhotoConfigurationConstants.DEFAULT_JDBC_CONNECTION_URL %>"></td>
				</tr>
				<tr>
					<td align="left"><fmt:message key="install.login" /></td>
					<td align="right" style="width: 250px"><input type="text"
						style="width: 240px;" name="<%=InstallServlet.FIELD_DBLOGIN %>"
						value="<%=PhotoConfigurationConstants.DEFAULT_DATABASE_USER %>"></td>
				</tr>
				<tr>
					<td align="left"><fmt:message key="install.password" /></td>
					<td align="right" style="width: 250px"><input type="text"
						style="width: 240px;" name="<%=InstallServlet.FIELD_DBPASSWORD %>"
						value="<%=PhotoConfigurationConstants.DEFAULT_DATABASE_PASSWORD %>"></td>
				</tr>
				<tr>
					<td align="left"><fmt:message key="install.driver" /></td>
					<td align="right" style="width: 250px"><input type="text"
						style="width: 240px;" name="<%=InstallServlet.FIELD_DBDRIVER %>"
						value="<%=PhotoConfigurationConstants.DEFAULT_JDBC_DRIVER %>"></td>
				</tr>
			</table>

			</td>
		</tr>
		<tr>
			<td class="formContainer">&nbsp;</td>
		</tr>

	</table>

	<input type="submit" name="<%=InstallServlet.ACTION %>"
		value="<fmt:message key="install.submit"/>"> <BR>
	<BR>
	</FORM>
	</div>
</BODY>
</fmt:bundle>
</HTML>