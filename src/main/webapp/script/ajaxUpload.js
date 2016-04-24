// This assumes that caller JSP has set a contextRoot variable

// called by onclick on button
function doUpload() {

	document.getElementById('adminDirForm').target = 'uploadFrame';
	document.getElementById('ajaxSubmitUpload').value = 'true'; // What matters is that it's not empty
	document.getElementById('adminDirForm').submit();
   	document.getElementById('adminDirForm').target='_self';
    setTimeout('requestRefreshProgress();',500);
}

// This function is called after upload form submission 
function requestRefreshProgress(){ 
 
 	// We are entering stage 2: hide form, display progress
  	document.getElementById('uploadLines').style.display = 'none';
  	document.getElementById('uploadLinesSubmit').style.display = 'none';
  	document.getElementById('uploadLinesProgress').style.display = '';
  	document.getElementById('uploadLinesResult').style.display = 'none';

  	// Create the AJAX post for progress retrieval
  	AjaxRequest.post( 
		   {
		   'url':contextRoot+'ajax',
    		//Specify the correct parameters so that 
    		//the component is correctly handled on 
    		//the server side.
     		'parameters':{ 'ajaxAction':'ajaxProgress'},
 		    // Specify the callback method for successful processing.
    		'onSuccess':refreshProgress
    		});
}
    		 
// Callback for the request made by requestRefreshProgress   		
function refreshProgress(req) { 
	
	var xml = req.responseXML;
 
    //Get the data from the XML
    var percentage = xml.getElementsByTagName('progress')[0].firstChild.nodeValue;
    var status = xml.getElementsByTagName('status')[0].firstChild.nodeValue;
    var isComplete = xml.getElementsByTagName('complete')[0].firstChild.nodeValue;
 
    var innerProgress = document.getElementById('progressBarProgress');
    document.getElementById('progressBarLabel').innerHTML = status;
 
    // Set the style classes of the spans based on the current progress.
    innerProgress.style.width = 3*percentage +'px';
 
	// If not complete, we need to carry
	// on polling the server for updates.
    if(isComplete == 0){ 
    	setTimeout('requestRefreshProgress()',400);
    } else { 
	  
	    // Force a refresh to see the new actions
	     resetUpload();
	    window.location.reload();
	    
  		// The file upload is done - display upload complete.
  		// document.getElementById('uploadLines').style.display = '';
  		// document.getElementById('uploadLinesSubmit').style.display = '';
  		// document.getElementById('uploadLinesProgress').style.display = 'none'; 
  		// document.getElementById('uploadLinesResult').style.display = '';
   		// document.getElementById('uploadLinesResultDiv').innerHTML = status;
    	// resetUpload();
    } 
}

function addUploadLine( line ) {
  var parent = document.getElementById('uploadLines');
  var uploadLinesCounter = document.getElementById('uploadLinesCounter');
  var newNumberOfLines = (document.getElementById('uploadLinesCounter').value -1)+ 2;
  if (newNumberOfLines != line + 1)
  	return;
  uploadLinesCounter.value = newNumberOfLines;
  var newRow = document.createElement('tr');
  parent.appendChild(newRow);
  var newCell = document.createElement('td');
  var label = document.getElementById('jsLabel').value;
  newCell.innerHTML = '<a style="float:right;" href="#" onclick="removeLastUploadLine()">'+label+'</a>';
  newRow.appendChild(newCell);
  var newCell2 = document.createElement('td');
  newCell2.innerHTML = '<input type="file" size="50" onchange="addUploadLine('+newNumberOfLines+');" name="addPicture'+newNumberOfLines+'">';
  newRow.appendChild(newCell2);
  newRow.id = 'uploadRow'+newNumberOfLines;
}

function removeLastUploadLine( ) {

  var uploadLinesCounter = document.getElementById('uploadLinesCounter');
  var numberOfLines = (document.getElementById('uploadLinesCounter').value -1)+1;
  var lineToRemove = document.getElementById('uploadRow'+numberOfLines);
  uploadLinesCounter.value = numberOfLines - 1;
  var parent = document.getElementById('uploadLines');
  parent.removeChild(lineToRemove);
}

function resetUpload() {
	var uploadLinesCounter = document.getElementById('uploadLinesCounter').value;
 	for (var i=1 ; i < uploadLinesCounter; i++)
 		removeLastUploadLine();
 
 	document.getElementById('firstUpload').value = '';	
}
   