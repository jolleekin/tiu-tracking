/**
 *	@author	Man Hoang
 *	@version	1.0
 */
function TMap() {

/* private */
	var ScaleEpsilon = 0.002;
	
	var VelocityEpsilon = 5;	// If he magnitues of both the components of the mouse velocity
								// are less than this value, the mouse is considered not moving.
	var VelocityScale = 6;
	
	var FrameInterval = 40;			// ms
	var LerpFactor = FrameInterval * 0.005; //Some unit / ms^2
	
	var self = this;
	var fComponentState = csLoading;
	
	var fMapImage = null;
	var fMapImageCenter = new TVector2D();
	var fPixelsPerUnitLength = 0;
	
	var fMapCenter = new TVector2D();
	
	var fMinScale = 0;			// The scale at which the scene fits perfectly inside the mapCanvas
	var fMaxScale = 0;		
	
	var fMouse = {
		position: new TVector2D(),
		velocity: new TVector2D(),
		transformedPosition: new TVector2D(),
		isLeftButtonDown: false
	};
	
	var fMapTransform = {
		position: new TVector2D(),
		targetPosition: new TVector2D(),
		scale: 1,						// Current scale of the scene
		targetScale: 1,					// Target scale of the scene
		totalScale: 1					// Scale * METTER_TO_PIXEL
	};
	
	// Timer used for scene animation
	var fTimer = new TTimer(FrameInterval, function () {
		updateMapTransform();
		self.invalidate();
	});

	var fEntities = [];
	
	// Create a map layer on top of the map image to capture events and
	// prevent user from selecting the map image.
	var fMap = document.createElement(SDiv);
	fMap.id = 'map';
	fMap.setAttribute('style', 'position: absolute; z-index: 0; overflow: hidden; background-color: rgba(255, 255, 255, 0);');
	document.body.appendChild(fMap);
	
	var fMapContainer = document.createElement(SDiv);
	fMapContainer.id = 'mapContainer';
	fMapContainer.style.position = 'absolute';
	fMap.appendChild(fMapContainer);

	
	fMapCenter.x = fMap.offsetWidth * 0.5;
	fMapCenter.y = fMap.offsetHeight * 0.5;
	
	mapMouseDown = function (event) {
		if (event.button == 0) {
			fTimer.setEnabled(false);
			fMouse.isLeftButtonDown = true;
			updateMouseInfo(event, false);
			event.preventDefault();
		}
	}
	
	mapMouseMove = function (event) {
		updateMouseInfo(event, true);
		
		if (fMouse.isLeftButtonDown) {
			this.style.cursor = 'move';//'url(images/closedhand_8_8.cur), move';
			fMapTransform.targetPosition.add(fMouse.velocity);
			fMapTransform.position.assign(fMapTransform.targetPosition);
			self.invalidate();
			event.preventDefault();
		} else {
			//self.setHoverObjectIndex(getObjectUnderMouse());
		}
		
	}
	
	mapMouseUp = function (event) {
		if (event.button == 0) {
			this.style.cursor = 'default';
			fMouse.isLeftButtonDown = false;
			updateMouseInfo(event, false);
			
			if ( !fMouse.velocity.equals(ZeroVector2D, VelocityEpsilon) ) {
				fMapTransform.targetPosition.multAddSet(fMapTransform.position, fMouse.velocity, VelocityScale);
				LerpFactor = LerpFactor;
				fTimer.setEnabled(true);
			} else {
				//self.setObjectIndex(getObjectUnderMouse());
			}
		}
	}
	
	mapMouseWheel = function (event) {
		/*var delta = event.detail ? -event.detail * 0.1 : event.wheelDelta * 0.0025;
		var factor = 1 + delta;
		if (delta < 0)
			factor = 1 / (1 - delta);
		*/
		var delta = event.detail ? -event.detail : event.wheelDelta;
		var factor = 0.5;
		if (delta > 0)
			factor = 2;
		
		self.zoom(event, factor);
		event.preventDefault();
	}

	mapDoubleClick = function (event) {
		self.zoom(event, 2);
	}
	
	/* Touch event handlers */
	
	function mapTouchStart(event) {
		fTimer.setEnabled(false);
		var touch = event.touches[0];
		updateMouseInfo({layerX: touch.pageX, layerY: touch.pageY}, false);
		event.preventDefault();
	}
	
	function mapTouchEnd(event) {
		var touch = event.touches[0];
		updateMouseInfo({layerX: touch.pageX, layerY: touch.pageY}, false);
		if ( !mouse.velocity.equals(ZeroVector2D, VELOCITY_THRESHOLD) ) {
			scene.targetPosition.multAddSet(scene.position, mouse.velocity, 2);
			LerpFactor = LerpFactor;
			fTimer.setEnabled(true);
		} else {
			self.setObjectIndex(getObjectUnderMouse());
		}
	}
	
	function mapTouchMove(event) {		
		var touch = event.touches[0];
		updateMouseInfo({layerX: touch.pageX, layerY: touch.pageY}, true);
		scene.targetPosition.add(mouse.velocity);
		scene.position.assign(scene.targetPosition);
		self.invalidate();
		event.preventDefault();
	}
	
	function recalculateScales() {
		if (fMapImage) {
			var sw = fMap.offsetWidth  / fMapImageCenter.x;
			var sh = fMap.offsetHeight / fMapImageCenter.y;
			fMinScale = Math.min(sw, sh) * 0.48;	// 96%
		} else
			fMinScale = 1;

		fMaxScale = 4 * fMinScale;
	}
	
	function show() {
		fMapTransform.scale = 0.001;
		fMapTransform.targetScale = fMinScale;
		fMapTransform.totalScale = fMapTransform.scale * fPixelsPerUnitLength;

		fMapTransform.position.multSubSet(fMapCenter, fMapImageCenter, fMapTransform.scale);
		fMapTransform.targetPosition.multSubSet(fMapCenter, fMapImageCenter, fMapTransform.targetScale);
		LerpFactor = LerpFactor;
		fTimer.setEnabled(true);
	}
	
	function sceneToCanvas(pos) {
		var result = new TVector2D();
		result.multAddSet(fMapTransform.position, pos, fMapTransform.totalScale);
		return result;
	}

	function updateMouseInfo(event, updateVelocity) {
		if (event) {
			// Mouse position w.r.t. the map.
			var x  = event.pageX - fMap.offsetLeft;
			var y  = event.pageY - fMap.offsetTop;
			//console.log(x, y);
			if (updateVelocity) {
				fMouse.velocity.x = x - fMouse.position.x;
				fMouse.velocity.y = y - fMouse.position.y;
			}
			fMouse.position.x = x;
			fMouse.position.y = y;
		} else {
			fMouse.position.assign(fMapCenter);
		}
		var s = 1 / fMapTransform.totalScale;
		fMouse.transformedPosition.x = (fMouse.position.x - fMapTransform.position.x) * s;
		fMouse.transformedPosition.y = (fMouse.position.y - fMapTransform.position.y) * s;
	}
	
	function updateMapTransform() {
		var animationDone = true;
		if (fMapTransform.position.equals(fMapTransform.targetPosition, 1) == false) {
			animationDone = false;
			fMapTransform.position.lerp(fMapTransform.position, fMapTransform.targetPosition, LerpFactor);
		}
		
		var ds = fMapTransform.targetScale - fMapTransform.scale;
		if (isZero(ds, ScaleEpsilon) == false) {
			animationDone = false;
			fMapTransform.scale += ds * LerpFactor;
			fMapTransform.totalScale = fMapTransform.scale * fPixelsPerUnitLength;
			for (var i = 0; i < fEntities.length; i++)
				fEntities[i].draw(fMapTransform.totalScale);
		}
		
		if (animationDone) {
			fTimer.setEnabled(false);
			if (fComponentState == csLoading) {
				fComponentState = csLoaded;
										
				fMap.ondblclick = mapDoubleClick;
				fMap.onmousedown = mapMouseDown;
				fMap.onmousemove = mapMouseMove;
				fMap.onmouseup = mapMouseUp;
				fMap.onmousewheel = mapMouseWheel;
				fMap.addEventListener('DOMMouseScroll', mapMouseWheel, false);	// Firefox shit
				
				if (typeof TouchEvent != SUndefined) {
					fMap.ontouchstart = mapTouchStart;
					fMap.ontouchmove = mapTouchMove;
					fMap.ontouchend = mapTouchEnd;
				}
				
				if (self.onLoad)
					self.onLoad();
			}
		}
	}
	
/* public */

	/**
	 *	Sets the map image.
	 *
	 *	@param	img	{HTMLImageElement}	The map image.
	 *	@param	pixelsPerUnitLength	{Double}	Number of pixels equivalent to 1 unit length.		
	 */
	this.setMapImage = function (img, pixelsPerUnitLength) {
		if (img) {
			fPixelsPerUnitLength = pixelsPerUnitLength;
			fMapImageCenter.x = img.width  * 0.5;
			fMapImageCenter.y = img.height * 0.5;
			img.style.position = 'absolute';
			fMapContainer.insertBefore(img, fMapContainer.firstChild);
			fMapImage = img;
			recalculateScales();
			show();
		}
	}
	
	/**
	 *	Sets the grid.
	 *
	 *	@param	visible	{Boolean}	true = grid on, false = grid off.
	 *	@param	spacing	{Double}	Spacing between two parallel grid lines.
	 */
	this.setGrid = function (visible, spacing) {
	
	}
	
	this.invalidate = function () {
		fMapContainer.style.left = fMapTransform.position.x + SPixel;
		fMapContainer.style.top  = fMapTransform.position.y + SPixel;
		fMapImage.style.width  = 2 * fMapImageCenter.x * fMapTransform.scale + SPixel;
		fMapImage.style.height = 2 * fMapImageCenter.y * fMapTransform.scale + SPixel;
	}

	/**
	 *	Zooms the fMapImage.
	 *
	 *	@param	event	{HTMLEvent}	An object containing layerX and layerY, i.e. mouse's position w.r.t. the screen.
	 *								null means mapCanvas's center
	 *	@param	factor	{Double}	Zoom factor compared to the current scale.
	 *								> 1 means zoom in
	 *								< 1 means zoom out
	 *								= 0 means zoom fit
	 *								< 0 means error (ignored fore now)
	 */
	this.zoom = function (event, factor) {
		if (factor < 0)
			return;

		updateMouseInfo(event, false);
		if (factor > 0) {
			fMapTransform.targetScale = ensureRange(fMapTransform.scale * factor, fMinScale, fMaxScale);
			fMapTransform.targetPosition.lerp(fMouse.position, fMapTransform.position, fMapTransform.targetScale / fMapTransform.scale);
		} else {
			fMapTransform.targetScale = fMinScale;
			fMapTransform.targetPosition.multSubSet(fMouse.position, fMapImageCenter, fMapTransform.targetScale);
		}
		
		if ( !(fMapTransform.position.equals(fMapTransform.targetPosition, VelocityEpsilon) &&
			isZero(fMapTransform.scale - fMapTransform.targetScale, ScaleEpsilon)) ) {
			LerpFactor = LerpFactor;
			fTimer.setEnabled(true);
		}
	}

	this.setRect = function (l, t, r, b) {
		var w = r - l;
		var h = b - t;

		fMapCenter.x = w * 0.5;
		fMapCenter.y = h * 0.5;
		
		fMapTransform.position.x -= l - fMap.offsetLeft;
		fMapTransform.position.y -= t - fMap.offsetTop;
		fMapTransform.targetPosition.assign(fMapTransform.position);
		
		fMap.style.left	= l + SPixel;
		fMap.style.top	= t + SPixel;
		fMap.style.width  = w + SPixel;
		fMap.style.height = h + SPixel;

		recalculateScales();
		
		if (fComponentState == csLoaded)
			self.invalidate();
	}

	/**
	 *	Moves an entity to the center of the map if it is out of bounds.
	 *
	 *	@param	entity	{HTMLElement.TMapEntity}	The entity to be moved.
	 */
	this.bringToCenter = function (entity) {
		if (entity && (entity.parentNode == fMapContainer)) {
			// Don't rely on entity.offsetLeft and entity.offsetTop
			var x = entity.x * fMapTransform.totalScale;
			var y = entity.y * fMapTransform.totalScale;
			if (!isInRange(fMapContainer.offsetLeft + x, 0, fMap.offsetWidth ) ||
				!isInRange(fMapContainer.offsetTop  + y, 0, fMap.offsetHeight)) {
				fMapTransform.targetPosition.x = fMapCenter.x - x;
				fMapTransform.targetPosition.y = fMapCenter.y - y;
				fTimer.setEnabled(true);
			}
		}
	}
	
	this.addEntity = function (entity) {
		if (entity) {
			fEntities.push(entity);
			fMapContainer.appendChild(entity);
			entity.draw(fMapTransform.totalScale);
		}
	}
	
	/**
	 *	Creates a new entity and returns it.
	 *	The entity is a HTMLDivElement.
	 *
	 */
	// this.newEntity = function (className) {
		// var entity = newElement(SDiv, className);
		// entity.appendChild(newElement(SDiv, 'TMarkerIcon'));
		// fEntities.push(entity);
		// fMapContainer.appendChild(entity);
		// entity.x = 0;
		// entity.y = 0;
		// entity.draw = function (scale) {
			// this.style.left = (this.x * scale - 12) + SPixel;
			// this.style.top  = (this.y * scale - this.offsetHeight) + SPixel;
		// }
		// entity.draw(fMapTransform.totalScale);
		// return entity;
	// }
	
	this.removeEntity = function (index) {
		checkRange(index, 0, fEntities.length - 1);
		fMapContainer.removeChild(fEntities.splice(index, 1)[0]);
	}
	
	this.onLoad = null;
}