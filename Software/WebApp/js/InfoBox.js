/**
 *	@class	TInfoBox
 *	@class	TInfoBoxContent
 *	@class	TPink
 *	@author	Man Hoang	
 *	@version	1.0
 */
function TInfoBox() {
	var element = newElement('div', 'TInfoBox ShowOnReady AbsPos');
	element.innerHTML = '<div class="TInfoBoxContent"></div><img class="TPink" src="images/Pink.png" />';

	document.body.appendChild(element);
	
	var content = element.childNodes[0];
	var pink = element.childNodes[1];
	var cx = (element.offsetWidth - pink.width) * 0.5;
	var cy = element.offsetHeight;

	this.getElement = function () {
		return element;
	}
	
	this.setVisible = function (value) {
		element.style.visibility = value ? SVisible : SHidden;
	}
	
	this.setContent = function (html) {
		content.innerHTML = html;
		cx = (element.offsetWidth - pink.width) * 0.5;
		cy = element.offsetHeight;
		pink.style.left = cx + SPixel;
	}
	
	this.setPosition = function (x, y) {
		element.style.left = (x - cx) + SPixel;
		element.style.top  = (y - cy) + SPixel;
	}
}