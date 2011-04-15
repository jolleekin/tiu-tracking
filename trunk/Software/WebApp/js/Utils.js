/* Global constant */

var SPixel		= 'px';

var SNone		= 'none';
var SBlock		= 'block';

var SHidden		= 'hidden';
var SVisible	= 'visible';

var SFocus		= 'focus';
var SBlur		= 'blur';
var SKeyDown	= 'keydown';
var SKeyUp		= 'keyup';

var SClick		= 'click';
var SMouseDown	= 'mousedown';
var SMouseMove	= 'mousemove';
var SMouseUp	= 'mouseup';
var SMouseWheel	= 'mousewheel';

/* Global functions */

function newElement(tagName, className) {
	var result = document.createElement(tagName);
	if (result) {
		result.className = className;
		return result;
	}
	return null;
}

/** A nice alias for document.getElementById() */
function $(idStr) {
	return document.getElementById(idStr);
}

function isZero(x, epsilon) {
	return Math.abs(x) < epsilon;
}

function isInRange(x, min, max) {
	return (x >= min && x <= max);
}

function ensureRange(x, min, max) {
	if (x < min) return min;
	if (x > max) return max;
	return x;
}

function parsePixelString(str) {
	return parseInt(str.replace('px', ''));
}
