/**
 *	@class	TInfoBox
 *	@class	TInfoBoxContent
 *	@class	TPink
 *	@author	Man Hoang	
 *	@version	1.0
 */
function newInfoBox() {
	var element = newElement('div', 'TInfoBox AbsPos');
	element.innerHTML = '<div class="TInfoBoxContent"></div><img class="TPink" src="images/Pink.png" />';
	element.style.visibility = SHidden;
	
	element.setContent = function (html) {
		var pointer = this.childNodes[1];
		this.childNodes[0].innerHTML = html;
		pointer.style.left = (this.offsetWidth - pointer.offsetWidth) * 0.5 + SPixel;
	}

	element.setPosition = function (x, y) {
		this.style.left = (x - this.offsetWidth * 0.5 + this.childNodes[1].offsetWidth) + SPixel;
		this.style.top  = (y - this.offsetHeight + 7) + SPixel;
	}
	
	element.draw = function (scale) {
		this.style.left = (this.x * scale - this.offsetWidth * 0.5 + this.childNodes[1].offsetWidth) + SPixel;
		this.style.top  = (this.y * scale - this.offsetHeight) + SPixel;
	}
	
	return element;
}