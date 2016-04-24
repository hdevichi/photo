var descriptionChanged = false;
var themeChanged = false;
var rightsChanged = false;

function validateDescription() {
	if (!descriptionChanged) {
		alert(validationMsg);
		return false;
	}
	return true;
}

function validateTheme() {
	if (!themeChanged) {
		alert(validationMsg);
		return false;
	}
	return true;
}

function validateRights() {
	if (!rightsChanged) {
		alert(validationMsg);
		return false;
	}
	return true;
}

function descriptionChange() {
	descriptionChanged = true;
}

function themeChange()  {
	themeChanged = true;
}

function addChildDirChange() {
	addChildDirChanged = true;
}

function addChildPicChange() {
	addChildPicChanged = true;
}

function rightsChange() {
	rightsChanged = true;
}