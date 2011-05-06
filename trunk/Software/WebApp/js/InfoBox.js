/**
 *	@class	TInfoBox
 *	@class	TInfoBoxContent
 *	@class	TPink
 *	@author	Man Hoang	
 *	@version	1.0
 */
function TInfoBox() {
	var box = newElement('div', 'TInfoBox AbsPos');
	box.innerHTML = '<div class="TInfoBoxContent"></div><img class="TPink" src="images/Pink.png" />';
	box.style.visibility = SHidden;
	
	/**
	 *	Sets content of the box.
	 *
	 *	@html	{HTML String}	The string that represents the content of the box.
	 */
	box.setContent = function (html) {
		var content = this.childNodes[0];
		var pointer = this.childNodes[1];
		content.innerHTML = html;
		pointer.style.left = (this.offsetWidth - pointer.offsetWidth) * 0.5 + SPixel;
		this.style.height = (content.offsetHeight + pointer.offsetHeight) + SPixel;
	}

	/**
	 *	Sets the absolute position of the box. Also updates its logical coordinates (x, y).
	 *
	 *	@left and @top are in pixels.
	 *	@scale is used to calculate @x and @y.
	 *	@offsetX, @offsetY are scale independent offsets (in pixels) that are
	 *	added to the box's left and top.
	 *
	 *	By using these offsets, the position of the box relative to some element
	 *	can be maintained while the scale of the map is changing.
	 */
	box.setPosition = function (x, y, offsetX, offsetY, scale) {
		this.mX = x;
		this.mY = y;
		this.mOffsetX = offsetX;
		this.mOffsetY = offsetY;
		this.onScaleChange(scale);
	}
	
	/**
	 *	Updates the absolute position of the box when the scale of the map has changed.
	 */
	box.onScaleChange = function (scale) {
		this.style.left = (this.mX * scale + this.mOffsetX - this.childNodes[1].offsetLeft) + SPixel;
		this.style.top  = (this.mY * scale + this.mOffsetY - this.offsetHeight) + SPixel;
	}
	
	box.onclick = function (event) {
		event.preventDefault();
		event.stopPropagation();
	}
	
	box.mX = 0;
	box.mY = 0;
	
	return box;
}