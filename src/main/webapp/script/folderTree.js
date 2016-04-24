// Note: variables imageFolder and idOfFolderTrees must have been initialized by calling page
// (not included in script cause theme dependant!)	
var folderImage = 'dhtmlgoodies_folder.gif';
var plusImage = 'dhtmlgoodies_plus.gif';
var minusImage = 'dhtmlgoodies_minus.gif';

var treeUlCounter = 0;
var nodeId = 1;

function expandAll(treeId) {
	var menuItems = document.getElementById(treeId).getElementsByTagName('LI');
	for(var no=0;no<menuItems.length;no++){
		var subItems = menuItems[no].getElementsByTagName('UL');
		if(subItems.length>0 && subItems[0].style.display!='block'){
			showHideNode(false,menuItems[no].id.replace(/[^0-9]/g,''));
		}			
	}
}

function collapseAll(treeId) {
	var menuItems = document.getElementById(treeId).getElementsByTagName('LI');
	for(var no=0;no<menuItems.length;no++){
		var subItems = menuItems[no].getElementsByTagName('UL');
		if(subItems.length>0 && subItems[0].style.display=='block'){
			showHideNode(false,menuItems[no].id.replace(/[^0-9]/g,''));
		}			
	}		
}
		
function showHideNode(e,inputId) {
	if(inputId){
		if(!document.getElementById('dhtmlgoodies_treeNode'+inputId))return;
		thisNode = document.getElementById('dhtmlgoodies_treeNode'+inputId).getElementsByTagName('IMG')[0]; 
	}else {
		thisNode = this;
		if(this.tagName=='A')thisNode = this.parentNode.getElementsByTagName('IMG')[0];	
		
	}
	if(thisNode.style.visibility=='hidden')return;
	var parentNode = thisNode.parentNode;
	inputId = parentNode.id.replace(/[^0-9]/g,'');
	if(thisNode.src.indexOf(plusImage)>=0){
		thisNode.src = thisNode.src.replace(plusImage,minusImage);
		var ul = parentNode.getElementsByTagName('UL')[0];
		ul.style.display='block';
		
	}else{
		thisNode.src = thisNode.src.replace(minusImage,plusImage);
		parentNode.getElementsByTagName('UL')[0].style.display='none';
	}	
	
	return false;
}


function initTree() {
	
	for(var treeCounter=0;treeCounter<idOfFolderTrees.length;treeCounter++){
		var dhtmlgoodies_tree = document.getElementById(idOfFolderTrees[treeCounter]);
		var menuItems = dhtmlgoodies_tree.getElementsByTagName('LI');	// Get an array of all menu items
		for(var no=0;no<menuItems.length;no++){
			nodeId++;
			var subItems = menuItems[no].getElementsByTagName('UL');
			var img = document.createElement('IMG');
			img.src = imageFolder + plusImage;
			img.onclick = showHideNode;
			if(subItems.length==0)img.style.visibility='hidden';else{
				subItems[0].id = 'tree_ul_' + treeUlCounter;
				treeUlCounter++;
			}
			var aTag = menuItems[no].getElementsByTagName('A')[0];
			//aTag.onclick = document.getElementById('imgDestinationInput').value= aTag.firstChild.value; // modified from original
			menuItems[no].insertBefore(img,aTag);
			menuItems[no].id = 'dhtmlgoodies_treeNode' + nodeId;
			var folderImg = document.createElement('IMG');
			if(menuItems[no].className){
				folderImg.src = imageFolder + menuItems[no].className;
			}else{
				folderImg.src = imageFolder + folderImage;
			}
			menuItems[no].insertBefore(folderImg,aTag);
		}	
	
	}
}

function selectFolderImageMove(folder) {
	document.getElementById('imgDestinationInput').value= folder;
}

function selectFolderDirMove(folder) {
	document.getElementById('dirDestinationInput').value= folder;
}

function selectFolderDirChildrenMove(folder) {
	document.getElementById('dirChildrenDestinationInput').value= folder;
}


window.onload = initTree;	