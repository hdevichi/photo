<%-- Common header for Photo JSP --%>
<%@ page contentType="text/html" pageEncoding="UTF-8" language="java" errorPage="/error.jsp" import="photo.model.photo.PhotoDescription" import="photo.model.user.PhotoUser"
		 import="java.io.File" import="photo.model.config.PhotoConfiguration" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page import="photo.model.config.PhotoConfigurationConstants" %>
<%@ page import="photo.utils.PhotoUtils" %>
<%@ page import="photo.i18n.Message" %>
<%@ page import="photo.control.servlet.*" %>
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

<%PhotoDescription directory = (PhotoDescription)request.getAttribute(PhotoConstants.REQUEST_OBJECT_TO_DISPLAY);
if (!directory.isDirectory())
	throw new RuntimeException("Admin directory Page error: photo object is not a directory!");
// Verifier que le role est bien admin
if (!directory.isUserAuthorizedToAdmin(user)) throw new RuntimeException("Admin directory: unauthorized access!"); %>
<script type="text/javascript" src="<%=request.getContextPath()+PhotoConstants.SCRIPTS_URI%>AjaxRequest.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()+PhotoConstants.SCRIPTS_URI%>ajaxUpload.js"></script>
<script type="text/javascript">
var imageFolder = '<%=request.getContextPath()+PhotoConstants.PATH_TO_THEMES+user.getTheme()%>/images/';	// Path to images
var idOfFolderTrees = ['admin_tree' , 'admin_tree2'];
var validationMsg = '<%=Message.getResource("admin.noChanges",user.getLanguage())%>';
var contextRoot='<%=request.getContextPath()+PhotoConstants.SEPARATOR_PATH %>';
</script>
<script type="text/javascript" src="<%=request.getContextPath()+PhotoConstants.SCRIPTS_URI%>folderTree.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()+PhotoConstants.SCRIPTS_URI%>adminDirControls.js"></script>

<HEAD>
	<TITLE><fmt:message key="adminDir.pageTitle"><fmt:param value="<%=directory.getId() %>"/></fmt:message></TITLE>
</HEAD>

<BODY>
	<div id="title"><fmt:message key="adminDir.bodyTitle"><fmt:param value="<%=directory.getPath() %>"/></fmt:message> (id: <%=directory.getId() %>)</div>
	
	<%@include file="/fragments/header.jspf"%>
	<DIV id="content">
	<div id="content2">
		<form id="adminDirForm" name="directory" method="post" action="<%=request.getContextPath()+PhotoServlet.getCommand(request) %>" enctype="multipart/form-data">
		<%@include file="/fragments/message.jspf"%>	
		
		<%-- Image upload result --%>
		<div id="uploadLinesResult" class="alert">
			<%if (request.getSession().getAttribute(AjaxServlet.SESSION_ATTRIBUTE_UPLOAD_STATUS) != null) {%> 
				<%=request.getSession().getAttribute(AjaxServlet.SESSION_ATTRIBUTE_UPLOAD_STATUS)%>
				<%request.getSession().setAttribute(AjaxServlet.SESSION_ATTRIBUTE_UPLOAD_STATUS, null);
			} %>
		</div>
		
		<table id="adminTable">
			<tr><td class="formContainer" rowspan="2">
				<table class="form">
					<tr class="formHeaderRow"><td colspan="2"><fmt:message key="adminDir.description"><fmt:param value="<%=directory.getName() %>"/></fmt:message></td></tr>
					<% for ( int i=0 ; i < photoConfig.getSupportedLocales().length; i++) {
						String loc = photoConfig.getSupportedLocales()[i];%>
					<%if (i!= 0 ) {%>
						<tr><td colspan="2">&nbsp;</td></tr>
					<%} %>
					<tr>
						<td><fmt:message key="adminDir.title"><fmt:param value="<%=loc%>"/></fmt:message></td>
						<td><input  onkeyup="descriptionChange()" type="text" style="width: 500px;" name="<%=AdminDirControl.FIELD_DESCRIPTION_TITLE_PREFIX %><%=loc %>" value="<%=directory.getTitle(loc)%>"/></td></tr>
					<tr>
						<td valign="top"><fmt:message key="adminDir.text"><fmt:param value="<%=loc%>"/></fmt:message></td>
						<td><textarea onkeyup="descriptionChange()" rows="3" style="width: 500px;" name="<%=AdminDirControl.FIELD_DESCRIPTION_TEXT_PREFIX %><%=loc %>"><%=directory.getText(loc)%></textarea></td></tr>
					<%} %>
					<tr><td colspan="2" style="text-align: right;" ><input onclick="return validateDescription()" type="submit" name="<%=AdminDirControl.ACTION_DESCRIPTION %>" value="<fmt:message key="adminDir.submitDescription"/>"></td></tr>
				</table>
			
			</td><td valign="top">
			
				<table class="form" width="100%">
					<tr  class="formHeaderRow"><td colspan="3"><fmt:message key="adminDir.access"/></td></tr>
					<tr><td><fmt:message key="adminDir.group"/></td><td><fmt:message key="adminDir.authorized"/></td><td><fmt:message key="adminDir.admin"/></td></tr>
					<%String[] groups = photoConfig.getPhotoUserFactory().readAllGroups();
					for (int i = 0 ; i < groups.length; i++) {
						String group = groups[i];
						if (group.equals(PhotoConstants.GUEST_GROUP_NAME)) { continue; }%>
					<tr>
						<td><%=group %></td>
						<td><input type="checkbox" onchange="rightsChange()" name="<%=AdminDirControl.FIELD_RIGHTS_GROUP %><%=group%>" <%if (directory.isGroupAuthorized(group)) {%>checked="checked"<% } %>></td>
						<td><input type="checkbox" onchange="rightsChange()" name="<%=AdminDirControl.FIELD_RIGHTS_ADMIN %><%=group%>" <%if (directory.isGroupAuthorizedToAdmin(group)) {%>checked="checked"<% }%>></td>
					</tr>
					<%}%>
					<tr><td><%=PhotoConstants.GUEST_GROUP_NAME %></td>
						<td><input type="checkbox" onchange="rightsChange()" name="<%=AdminDirControl.FIELD_RIGHTS_GROUP %><%=PhotoConstants.GUEST_GROUP_NAME%>"  <%if (directory.isGroupAuthorized(PhotoConstants.GUEST_GROUP_NAME)) {%>checked="checked"<% } %>></td>
						<td></td>
					</tr>
					<tr>
						<td colspan="3">&nbsp;</td>
					</tr>
					<tr>
						<td class="right"><input type="checkbox" name="<%=AdminDirControl.FIELD_RIGHTS_RECURSIVE%>"></td>
						<td colspan="2"><fmt:message key="adminDir.recursive"/></td>
					</tr>
					<tr><td colspan="3" align="right"><input onclick="return validateRights()" type="submit" name="<%=AdminDirControl.ACTION_RIGHTS %>" value="<fmt:message key="adminDir.submitRights"/>" ></td></tr>
				</table>
			
			</td></tr>
			
			<tr>
				<td valign="top">
			
				<table class="form" width="100%" >
					<tr class="formHeaderRow"><td><fmt:message key="adminDir.theme"/></td></tr>
					<tr><td>
						<select  onchange="themeChange()" name="<%=AdminDirControl.FIELD_THEME %>">
								<option value="<%=AdminDirControl.FIELD_THEME_DEFAULT %>"><fmt:message key="adminDir.defaultTheme"/></option>
							<%String[] themes = photoConfig.getAvailableThemes();
							for (int i = 0 ; i < themes.length; i++) {%>
								<option <%if ( themes[i].equals(directory.getTheme()) ) {%>SELECTED <%} %>><%=themes[i] %></option>
							<%} %>
						</select>
					</td></tr>
					<tr><td class="right"><input onclick="return validateTheme()" type="submit" name="<%=AdminDirControl.ACTION_THEME %>" value="<fmt:message key="adminDir.themeSubmit"/>"></td></tr>
				</table>
			
				</td>
			</tr>
			<tr><td valign="top">
				<table class="form" >
					<tr class="formHeaderRow"><td colspan="2"><fmt:message key="adminDir.management"/></td></tr>
					
					<%-- Image upload form controls --%>
					<tbody id="uploadLines">
						<tr>
							<td align="left"><fmt:message key="adminDir.addPic"/></td>
							<td align="left" >
								<input id="firstUpload" type="file" onchange="addUploadLine(1);" size="50" name="<%=AdminDirControl.FIELD_IMAGE %>1" <%if (photoConfig.getPhotoFactory().getFreeSpace() < PhotoConfigurationConstants.MINIMUM_DISK_SPACE) {%> disabled <%}%>>
								<input type="hidden" id="uploadLinesCounter" value="1">
								<input type="hidden" id="jsLabel" value="<fmt:message key="adminDir.addPicRemove"/>">
							</td>
						</tr>
					</tbody>
					
					<%-- Image upload progress bar --%>
					<tbody id="uploadLinesProgress" style="display: none;">
						<tr><td colspan="2">
							<div id="progressBarID" class="progressBar">
								<div id="progressBarProgress"></div>
							</div>
							<div id="progressBarLabel">en attente...</div>
						</td></tr>
					</tbody>

					<%-- Iframe to upload to --%>
					<tbody id="uploadLinesTarget" style="display: none;">
						<tr><td colspan="2"><iframe id="uploadFrame" name="uploadFrame"></iframe>
						</td></tr>
					</tbody>
					<tbody id="uploadLinesSubmit">
					<tr>
						<td colspan="2" align="right">
						 	<%if (photoConfig.getPhotoFactory().getFreeSpace() < PhotoConfigurationConstants.MINIMUM_DISK_SPACE) {%>
						 	<fmt:message key="adminDir.addPicImpossible"/>
						 	<%} %>
							<input id="uploadButton" type="button" onclick="doUpload();" name="uploadButton" value="<fmt:message key="adminDir.submitAddImage"/>" <%if (photoConfig.getPhotoFactory().getFreeSpace() < PhotoConfigurationConstants.MINIMUM_DISK_SPACE) {%> disabled <%}%>>
							<input type="hidden" id="ajaxSubmitUpload" name="<%=AdminDirControl.ACTION_UPLOAD_IMAGES%>" value="">
							<%-- Set context root JS variable in order for upload JS to work ! (see header) --%>
						</td>
					</tr>
					</tbody>
					<tr>
						<td align="left"><fmt:message key="adminDir.addDir"/></td>
						<td colspan="2" align="left"><input type="text" size="50" name="<%=AdminDirControl.FIELD_CHILD_DIR %>"></td>
					</tr>
					<tr>
						<td colspan="2" class="right">
							<input type="checkbox" name="<%=AdminDirControl.FIELD_CHILD_DIR_RIGHTS %>"><fmt:message key="adminDir.addChildWithRights"/>
							<input type="submit" name="<%=AdminDirControl.ACTION_ADD_CHILD_DIR %>" value="<fmt:message key="adminDir.submitAddChildDir"/>">
							
						</td>
					</tr>
				</table>
			
				</td>
			
				<td valign="top">
			
				<table class="form" >
					<tr class="formHeaderRow"><td colspan="3"><fmt:message key="adminDir.manage"/></td></tr>
					<%boolean disabled = false;
					if (directory.getId().equals("") ) {
						 disabled = true;%>
						 <tr><td colspan="3"><fmt:message key="adminDir.noManageRoot"/></td></tr>
					<%}
					if (!disabled && !directory.getParent().isUserAuthorizedToAdmin(user) ) {
						disabled = true;%>
						<tr><td colspan="3"><fmt:message key="adminDir.noManageExplanation"/></td></tr>
					<%}%>
					<%if (!disabled) { %>

					<tr><td>
						<fmt:message key="adminDir.move"/> 
						</td><td>
						<input id="dirDestinationInput" type="text" name="<%=AdminDirControl.FIELD_MOVE_DESTINATION %>" value="<%=directory.getParent().getPath() %>" <%if (disabled ) {%> disabled <%}%>>
						
						<td class="right"><input type="submit" name="<%=AdminDirControl.ACTION_MOVE %>" value="<fmt:message key="adminDir.submitMove"/>"  <%if (disabled ) {%> disabled <%}%>>
						</td>
					</tr>
					<tr><td>&nbsp;</td><td colspan="2">
						<div >
								<%StringBuffer buffer = new StringBuffer();
								PhotoUtils.generateFolderTreeMarkup("admin_tree","selectFolderDirMove",photoConfig.getPhotoFactory().getRoot(), buffer, user.getLanguage());%>
								<%=buffer.toString() %>
						</div>
					</td></tr>
					<tr><td colspan="3">&nbsp;</td></tr>
					<tr><td>
						<fmt:message key="adminDir.rename"/> 
						</td><td>
						<input type="text" name="<%=AdminDirControl.FIELD_RENAME_NEWNAME %>" value="<%=directory.getName() %>" <%if (disabled ) {%> disabled <%}%>>
						</td><td class="right">
						<input type="submit" name="<%=AdminDirControl.ACTION_RENAME %>" value="<fmt:message key="adminDir.submitRename"/>"  <%if (disabled ) {%> disabled <%}%>>
						</td>
					</tr>
					<%} else {%><tr><td><div style="display: none;"><ul id="admin_tree"></ul></div></td></tr><%} %>
					<%boolean deleteDisabled = disabled;
					if ( !disabled && (directory.getNumberOfChildDirectories() > 0) ) {
						deleteDisabled = true;%>
						<tr><td colspan="3"><fmt:message key="adminDir.deleteExplanation"/></td></tr>
					<%}%>
					<tr><td class="right" colspan="3">
						<input type="submit" name="<%=AdminDirControl.ACTION_DELETE %>" 
											value="<fmt:message key="adminDir.submitDelete"/>" 
											<%String[] names = photoConfig.getPhotoFactory().listAllChildren(directory.getId());
											if ( names.length > 0 ) {
												String namesToDisplay= "";
												for (int i = 0 ; i < names.length ; i++) {
													namesToDisplay += names[i]+"\\n";
													if (i == 9 && names.length > 10) {
														namesToDisplay += "...("+names.length+" files)\\n";
														break;
													}
												}%>
											onclick="return confirm('Directory not empty!\nYou are about to delete:\n<%=namesToDisplay %>Are you sure?');"
											<%} %>
											<%if (deleteDisabled ) {%> disabled <%}%>>
					</td></tr>
				</table>
			
			</td>	
			</tr>
			
			<tr><td colspan="2" width="100%">
				<table class="form"  width="100%">
					<tr class="formHeaderRow"><td colspan="5"><fmt:message key="adminDir.content"/></td></tr>
					<tr><td colspan="5"><fmt:message key="adminDir.childDirs"/></td></tr>
					<%int dirs = directory.getNumberOfChildDirectories();
					if (dirs > 0) {%>
						<tr><td></td>
						<td><fmt:message key="adminDir.childId"/></td>
						<td><fmt:message key="adminDir.childTitle"/></td>
						<td><fmt:message key="adminDir.childText"/></td>
						<td></td>
						</tr>
						<%for (int i = 0 ; i < dirs ; i++) { 
							PhotoDescription child = directory.getChildDirectory(i);
							String name = child.getName();
							String prefix = child.getParent().getId();
							%>
							<tr><td width="10%" align="center"><input type="checkbox" name="<%=child.getName() %>"></td>
							<td><%if (!child.getId().equals("")) {%><%=prefix+PhotoConstants.SEPARATOR_PATH %><%} %><INPUT type="text" name="<%=AdminDirControl.FIELD_BATCH_UPDATE_NAME+i %>" value="<%=name%>"></td>
							<td><INPUT type="text" name="<%=AdminDirControl.FIELD_BATCH_UPDATE_TITLE+i %>" value="<%=child.getTitle(user.getLanguage())%>"></td>
							<td><INPUT type="text" name="<%=AdminDirControl.FIELD_BATCH_UPDATE_TEXT+i %>" value="<%=child.getTitle(user.getLanguage())%>"></td>
							<td><a href="<%=request.getContextPath()+AdminServlet.URI+PhotoConstants.SEPARATOR_PATH+child.getId()+request.getAttribute(AdminServlet.REQUEST_ATTRIBUTE_RETURN_URI)%>"><fmt:message key="adminDir.adminChild"/></a></td>
							</tr>
					<%	}
					} else { %>
						<tr><td colspan="5"><fmt:message key="adminDir.noChildDirs"/></td></tr>
					<%} %>
					
					<tr><td colspan="5"><fmt:message key="adminDir.childPics"/></td></tr>
					<%int pics = directory.getNumberOfChildImages();
					if (pics > 0) {%>
						<tr><td></td><td><fmt:message key="adminDir.childId"/></td><td><fmt:message key="adminDir.childTitle"/></td><td><fmt:message key="adminDir.childText"/></td></tr>
						<%for (int i = 0 ; i < pics ; i++) { 
							PhotoDescription child = directory.getChildImage(i);
							String extension = child.getName().substring(child.getName().lastIndexOf(PhotoConstants.SEPARATOR_EXTENSION));
							String name = child.getName().substring(0, child.getName().length()-extension.length());
							String prefix = child.getParent().getId();%>
							<tr><td width="10%" align="center"><input type="checkbox" name="<%=child.getName() %>"></td>
							<td><%=prefix+PhotoConstants.SEPARATOR_PATH %><INPUT type="text" name="<%=AdminDirControl.FIELD_BATCH_UPDATE_NAME+(dirs+i) %>" value="<%=name%>"><%=extension%></td>
							<td><INPUT type="text" name="<%=AdminDirControl.FIELD_BATCH_UPDATE_TITLE+(dirs+i) %>" value="<%=child.getTitle(user.getLanguage())%>"></td>
							<td><INPUT type="text" name="<%=AdminDirControl.FIELD_BATCH_UPDATE_TEXT+(dirs+i) %>" value="<%=child.getText(user.getLanguage())%>"></td>
							<td><a href="<%=request.getContextPath()+AdminServlet.URI+PhotoConstants.SEPARATOR_PATH+child.getId()+request.getAttribute(AdminServlet.REQUEST_ATTRIBUTE_RETURN_URI)%>"><fmt:message key="adminDir.adminChild"/></a></td>
							</tr>
					<%	}
					} else { %>
						<tr><td colspan="5"><fmt:message key="adminDir.noChildPics"/></td></tr>
					<%} %>
					<tr><td colspan="5">&nbsp;</tr>
					<%boolean batchDisabled = ( (dirs == 0) && (pics == 0)); %>
					<tr>
					<td><input type="submit" name="<%=AdminDirControl.ACTION_BATCH_MOVE %>" value="<fmt:message key="adminDir.batchMove"/>" <%if (batchDisabled) {%> disabled <%} %>></td>
					<td><fmt:message key="adminDir.batchMoveDest"/></td>
					<td colspan="3"> 
					<input id="dirChildrenDestinationInput" type="text" name="<%=AdminDirControl.FIELD_BATCH_MOVE_DESTINATION %>" value="<%=directory.getId() %>" <%if (batchDisabled) {%> disabled <%} %>> 
					</td></tr>
					<tr><td colspan="2">&nbsp;</td>
					<td colspan="3">
						<div >
								<%StringBuffer buffer = new StringBuffer();
								PhotoUtils.generateFolderTreeMarkup("admin_tree2", "selectFolderDirChildrenMove",photoConfig.getPhotoFactory().getRoot(), buffer, user.getLanguage());%>
								<%=buffer.toString() %>
						</div>
					</td></tr>
					<tr>
					<td><input type="submit" name="<%=AdminDirControl.ACTION_BATCH_DELETE%>" value="<fmt:message key="adminDir.batchDeleteAction"/>" <%if (batchDisabled) {%> disabled <%} %>></td>
					<td colspan="4"><fmt:message key="adminDir.batchDelete"/> 
					</td></tr>
					<tr><td><input type="submit" name="<%=AdminDirControl.ACTION_BATCH_UPDATE%>" value="<fmt:message key="adminDir.batchUpdateAction"/>" <%if (batchDisabled) {%> disabled <%} %>></td>
					<td colspan="4"><fmt:message key="adminDir.batchUpdate"/>
					</td></tr>
				</table>
			
			</td>
			</tr>
			
		</table>
		</form>
		<a href="<%=request.getContextPath()+request.getAttribute(AdminServlet.REQUEST_ATTRIBUTE_RETURN_URI)%>"><fmt:message key="nav.back"/></a>
		
	</DIV>
	</DIV>
	<%@include file="/fragments/footer.jspf" %>
</BODY>
</fmt:bundle>
</HTML>