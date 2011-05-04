/* Global constants */

// Http request methods
var SPost		= 'POST';
var SGet		= 'GET';

var SDiv		= 'div';

var SUndefined	= 'undefined';
var SIndexOutOfRange = 'Index out of range: ';

var SAuto		= 'auto';
var SPixel		= 'px';

var SNone		= 'none';
var SBlock		= 'block';
var SInline		= 'inline';

var SHidden		= 'hidden';
var SVisible	= 'visible';

var SFocus		= 'focus';
var SBlur		= 'blur';
var SKeyDown	= 'keydown';
var SKeyUp		= 'keyup';

var SClick		= 'click';
var SMouseDown	= 'mousedown';
var SMouseMove	= 'mousemove';
var SMouseOut	= 'mouseout';
var SMouseOver	= 'mouseover';
var SMouseUp	= 'mouseup';
var SMouseWheel	= 'mousewheel';

/* TComponentState */

var csLoading	= 0;
var csLoaded	= 1;

/* Browser info */

var browser = {
	isChrome:	navigator.userAgent.toLowerCase().indexOf('chrome') > -1,
	isIE:		navigator.userAgent.toLowerCase().indexOf('ie') > -1
};

/* Global functions */

function newElement(tagName, className) {
	var result = document.createElement(tagName);
	if (result)
		result.className = className;
	return result;
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

function checkRange(x, min, max) {
	if (!isInRange(x, min, max))
		throw SIndexOutOfRange + x;
}

function ensureRange(x, min, max) {
	if (x < min) return min;
	if (x > max) return max;
	return x;
}
