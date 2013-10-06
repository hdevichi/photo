<%-- Common header for Photo JSP --%>
<%@ page contentType="text/html" pageEncoding="UTF-8" language="java" errorPage="/error.jsp" import="org.devichi.photo.model.photo.PhotoDescription" import="org.devichi.photo.model.user.PhotoUser" import="org.devichi.photo.control.servlet.PhotoConstants" import="java.io.File" import="org.devichi.photo.model.config.PhotoConfiguration" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 
<%@page import="org.devichi.photo.model.config.PhotoConfigurationConstants"%>
<%@page import="org.devichi.photo.control.servlet.AdminImgControl"%>
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
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+user.getTheme()%>/photo.css" >

<%PhotoDescription picture = (PhotoDescription)request.getAttribute(PhotoConstants.REQUEST_OBJECT_TO_DISPLAY);
if (picture.isDirectory())
	throw new RuntimeException("Image admin error: photo object is a directory!");
// Verifier que le role est bien admin
if (!picture.isUserAuthorizedToAdmin(user)) throw new RuntimeException("Image admin error: user not authorized!");%>
<script type="text/javascript" src="<%=request.getContextPath()+PhotoConstants.SCRIPTS_URI%>submitNav.js"></script>
<script type="text/javascript">
var imageFolder = '<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+user.getTheme()%>/images/';	// Path to images
var idOfFolderTrees = ['admin_tree'];
var validationMsg = '<%=org.devichi.photo.i18n.Message.getResource("admin.noChanges",user.getLanguage())%>';
</script>
<script type="text/javascript" src="<%=request.getContextPath()+PhotoConstants.SCRIPTS_URI%>folderTree.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()+PhotoConstants.SCRIPTS_URI%>adminPicControls.js"></script>
<HEAD>
	<TITLE><fmt:message key="adminImage.pageTitle"><fmt:param value="<%=picture.getId()%>"/></fmt:message></TITLE>
</HEAD>

<BODY>
	<div id="title"></div>
	<%@include file="/fragments/header.jspf"%>
	<DIV id="content">
	<div id="content2">
		<form name="picture" method="post" action="<%=request.getContextPath()+PhotoServlet.getCommand(request) %>">
			<%@include file="/fragments/message.jspf"%>
			<table id="adminTable">
			<tr><td class="formContainer">
		
			<table class="form">
				<tr class="formHeaderRow"><td colspan="2"><fmt:message key="adminImage.description"><fmt:param value="<%=picture.getName()%>"/> (id: <%=picture.getId() %>)</fmt:message></td></tr>
				<% for ( int i=0 ; i < photoConfig.getSupportedLocales().length; i++) {
					String loc = photoConfig.getSupportedLocales()[i];%>
				<%if (i != 0 ) {%>
					<tr><td colspan="2">&nbsp;</td></tr>
				<%} %>
				<tr>
					<td><fmt:message key="adminImage.title"><fmt:param value="<%=loc%>"/></fmt:message></td>
					<td><input onkeyup="descriptionChange()" type="text" style="width: 500px;" name="<%=AdminImgControl.FIELD_DESCRIPTION_TITLE_PREFIX %><%=loc %>" value="<%=picture.getTitle(loc)%>"/></td></tr>
				<tr>
					<td valign="top"><fmt:message key="adminImage.text"><fmt:param value="<%=loc%>"/></fmt:message></td>
					<td><textarea  onkeyup="descriptionChange()" rows="3" style="width: 500px;" name="<%=AdminImgControl.FIELD_DESCRIPTION_TEXT_PREFIX %><%=loc %>"><%=picture.getText(loc)%></textarea></td></tr>
				<%} %>
				<tr>
					<td colspan="2" style="text-align: right;" >
						<input id="adminPicSubmitDescription" onclick="return validateDescription()" type="submit" name="<%=AdminImgControl.ACTION_DESCRIPTION %>" value="<fmt:message key="adminImage.submitDescription"/>">
					</td>
				</tr>
			</table>
			
			</td><td class="formContainer">
			
				<table class="form" >
					<tr class="formHeaderRow"><td colspan="2"><fmt:message key="adminImage.imgManagement"/></td></tr>
					<tr>
						<td><input type="radio" name="<%=AdminImgControl.FIELD_MANAGEMENT_RADIO %>" value="<%=AdminImgControl.OPTION_DELETE %>"></td>
						<td align="left"><fmt:message key="adminImage.delete"/></td></tr>
					<tr>
						<td><input type="radio" name="<%=AdminImgControl.FIELD_MANAGEMENT_RADIO %>" value="<%=AdminImgControl.OPTION_MOVE %>" checked="checked"></td>
						<td align="left"><fmt:message key="adminImage.move"/> <input id="imgDestinationInput" type="text" size="25" name="<%=AdminImgControl.FIELD_DESTINATION %>" value="<%=picture.getParent().getId() %>"></td>
					</tr>
					<tr><td></td><td><fmt:message key="adminImage.selectDestination"/> </td>
					</tr><tr>
						<td colspan="2">
						<div style="float:right;">
								<%StringBuffer buffer = new StringBuffer();
								org.devichi.photo.utils.PhotoUtils.generateFolderTreeMarkup("admin_tree","selectFolderImageMove",photoConfig.getPhotoFactory().getRoot(), buffer, user.getLanguage());%>
								<%=buffer.toString() %>
								</div>
						</td>
					</tr>
					<tr>
						<td><input type="radio" name="<%=AdminImgControl.FIELD_MANAGEMENT_RADIO %>" value="<%=AdminImgControl.OPTION_RENAME %>" ></td>
						<%String extension = picture.getName().substring(picture.getName().lastIndexOf(PhotoConstants.SEPARATOR_EXTENSION));
						String name = picture.getName().substring(0, picture.getName().length()-extension.length());%>
						<td align="left"><fmt:message key="adminImage.rename"/> <input type="text" size="21" name="<%=AdminImgControl.FIELD_NEWNAME%>" value="<%=name %>"><%=extension %></td>
					</tr>
					
					<tr><td colspan="2" align="right">
						<input type="submit" name="<%=AdminImgControl.ACTION_MANAGEMENT %>" value="<fmt:message key="adminImage.submitManage"/>">
					</td></tr>
				</table>
			
			</td></tr>
			<tr><td>
			
				<table class="form" width="100%">
					<tr class="formHeaderRow"><td colspan="2"><fmt:message key="adminImage.imgInfo"/></td></tr>
					<tr>
						<td rowspan="7" align="center" class="indexCell">
						
							<div class="thumbPic"><div class="thumb2" align="center">								
								<img class="thumb3" src="
									<%if (PhotoConfigurationConstants.THUMBNAIL_SIZE != PhotoDescription.SCALE_RESIZE_ERROR) {
										if ( PhotoConfigurationConstants.THUMBNAIL_SIZE != PhotoDescription.SCALE_NEVER_RESIZED) {%>
											<%=request.getContextPath()+FileServlet.URI_THUMBNAIL+PhotoConstants.SEPARATOR_PATH+picture.getId()%>" 
										<% } else {%>
											<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+user.getTheme()+"/images/resize.gif" %>"
										<% }
									} else { %>
									<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+user.getTheme()+"/images/missing.gif" %>"
									<%} %>
									alt="<c:out value="<%=picture.getTitle(user.getLanguage())%>"/>">		
							</div></div>
						
							<!--  
							<table><tr><td width="171px" height="172px" align="center" background="<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+user.getTheme()%>/images/border.gif" valign="middle">
								<img src="<%=request.getContextPath()+"thumbnail/"+picture.getId() %>" alt="thumbnail">
							</td></tr></table>
							-->
						</td><td >
							<fmt:message key="adminImage.name"/>&nbsp;<%=picture.getName() %>
							<BR><%=photoConfig.getPhotoFactory().getStorageReference(picture.getId()) %>
						</td>
					</tr><tr>
						<td>
							<fmt:message key="adminImage.size"/>&nbsp;<%=picture.getSize() %>
						</td>
					</tr>
					<tr>
						<td>
							<fmt:message key="adminImage.scale"/> <%=photoConfig.getMaximumSize() %> px
							<%int original = photoConfig.getPhotoFactory().getScale(picture.getId());%>
						</td>
					</tr>
					<tr>
						<td>	
							<fmt:message key="adminImage.originalScale"/> <%=original %> px
							<a href="<%=request.getContextPath()+FileServlet.URI_ORIGINAL+PhotoConstants.SEPARATOR_PATH+picture.getId()%>">
								<fmt:message key="photo.original"/>
							</a>
			
						</td>
					</tr>
					<tr>
						<td>
							<fmt:message key="adminImage.thumbnailScale"/> <%=PhotoConfigurationConstants.THUMBNAIL_SIZE %> px
						</td>
					</tr><tr>
						<td class="right">
							<input type="submit" name="<%=AdminImgControl.ACTION_GENERATE %>" value="<fmt:message key="adminImage.submitRegenerate"/>">
						</td>
					</tr><tr>
						<td class="right">
							<input type="submit" name="<%=AdminImgControl.ACTION_ROTATE %>" value="<fmt:message key="adminImage.submitRotate"/>">
						</td>
					</tr>
				</table>
			
			</td><td>&nbsp;</td></tr></table>
		</form>
		
		<div>
			<a href="<%=request.getContextPath()+request.getAttribute(AdminServlet.REQUEST_ATTRIBUTE_RETURN_URI)%>">
				<fmt:message key="nav.back"/>
			</a>
		</div>
	</DIV>
	</DIV>
	<%@include file="/fragments/footer.jspf" %>
</BODY>
</fmt:bundle>
</HTML>