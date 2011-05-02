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

/**
 *	Creates an AJAX object.
 *
 *	@param	method	{String}	'GET' or 'POST'.
 *	@param	url		{String}	The request url.
 *	@param	params	{String}	Params to send along, e.g. 'username=abc&password=123'
 *	@param	callback	{Function(xhr, args)}	The function to be called upon receiving the response.
 *	@param	args	{Object}	Arguments to pass to callback.
 */
function AJAX(method, url, params, callback) {
	var xhr = new XMLHttpRequest();
	xhr.onreadystatechange = callback;
	if (method == 'GET') {
		xhr.open(method, url + '?' + params, true);
		xhr.send();
	} else {
		xhr.open(method, url, true);
		xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
		//xhr.setRequestHeader('Content-length', params.length);
		//xhr.setRequestHeader('Connection', 'close');
		xhr.send(params);
	}
}