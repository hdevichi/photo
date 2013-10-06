var descriptionChanged = false;
var nameChanged = false;

function descriptionChange() {
	descriptionChanged = true;
}

function validateDescription() {
	if (!descriptionChanged) {
		alert(validationMsg);
		return false;
	}
	return true;
}