function $fx(initElm){
	
	var csPaused = 0;
	var csRunning = 1;
	var csStopped = 2;
	
	if (initElm.nodeType && initElm.nodeType==1)
		var elm = initElm;
	else if (String(initElm).match(/^#([^$]+)$/i)){
		var elm = document.getElementById(RegExp.$1+'');
		if(!elm)
			return null;
	}else 
		return null;
	
	if (typeof(elm.fx) != 'undefined' && elm.fx){
		return elm;
	};
	
	elm.fxVersion = 0.1;
	elm.fx = {};
	elm.fx.sets = [];
	elm.fx.currentSetIndex = 0;
	
	var units = {
		'left|top|right|bottom|width|height|margin|padding|spacing|backgroundx|backgroundy': 'px',
		'font': 'pt',
		'opacity': ''
	};
	
	var requiredParams = {delay: 100, step: 5, unit: ''};
	
	var specialHandlers = {
		opacity: function (value, unit) {
			if (value != null) {
				value = Math.min(100, Math.max(0, value));
				elm.style.opacity = value * 0.01;
			} else
				return Math.round(parseFloat(elm.style.opacity) * 100);
		},
		
		backgroundx: function (value, unit) {
			var x = 0, y = 0;
			var matches = (new RegExp('^(-?\\d+)[^\\d\\-]+(-?\\d+)')).exec(elm.style.backgroundPosition + '');
			if (matches){
				x = parseInt(matches[1]);
				y = parseInt(matches[2]);
			}
			if (value != null)
				elm.style.backgroundPosition = value + unit + ' ' + y + unit;
			else
				return x;
		},
		
		backgroundy: function (value, unit) {
			var x = 0, y = 0;
			var matches = (new RegExp('^(-?\\d+)[^\\d\\-]+(-?\\d+)')).exec(elm.style.backgroundPosition + '');
			if (matches){
				x = parseInt(matches[1]);
				y = parseInt(matches[2]);
			}
			if (value != null)
				elm.style.backgroundPosition = x + unit + ' ' + value + unit;  
			else
				return y;
		}
	};
	
	var defaults = {
		width: function(){
			return elm.offsetWidth;
		},
		height: function(){
			return elm.offsetHeight;
		},
		left: function(){
			var left = 0;
			for (var el=elm; el; el=el.offsetParent) left += el.offsetLeft;
			return left;
			
			/*return elm.offsetLeft;*/
		},
		top: function(){
			/*var top = 0;
			for (var el=elm; el; el=el.offsetParent) top += el.offsetTop;
			return top;
			*/
			return elm.offsetTop;
		}
	};
	
	elm.fxAddSet = function(){
		var currSetIndex = this.fx.sets.length;
		this.fx.currentSetIndex = currSetIndex;
		this.fx.sets[currSetIndex] = {
			loopCount: 1,
			loopsDone: 0,
			effects: [],
			effectsDone: 0,
			holdTime: 0,
			isRunning: false,
			onfinal: function(){}
		}
		return this;
	};
	
	elm.fxHold = function(time, setIndex){
		if (!elm.fx.sets[this.fx.currentSetIndex].isRunning)
			this.fx.sets[isNaN(setIndex) ? this.fx.currentSetIndex : setIndex].holdTime = time;
		return this; 
	};
	
	elm.fxAdd = function(params) {
		var currSetIndex = this.fx.currentSetIndex;
		if (this.fx.sets[currSetIndex].isRunning)
			return this;
		
		for (var p in requiredParams) {
			if (!params[p])
				params[p] = requiredParams[p]
		}
		
		if (!params.unit) {
			for (var mask in units)
				if ((new RegExp(mask,'i').test(params.type))){
					params.unit = units[mask];
					break;
				}
		};
		
		params.onstart = (params.onstart && params.onstart.call) ? params.onstart : function(){}; 
		
		if (!this.fx[params.type]){
			if (specialHandlers[params.type])
				this.fx[params.type] = specialHandlers[params.type];
			else{
				//var elm = this;
				this.fx[params.type] = function (value, unit) {
					if (value != null) {
					elm.style[params.type] = value + unit;
					}
					else {
						return parseInt(elm.style[params.type]);
					}
				}
			}
		};
		if (isNaN(params.from)) {
			if (isNaN(this.fx[params.type](null, ''))) {
				if (defaults[params.type])
					params.from = defaults[params.type](); 
				else
					params.from = 0;
			} else
				params.from = this.fx[params.type](null, '');
		}
		params.initial = params.from;
		//this.fx[params.type](params.from, params.unit);
		this.fx.sets[currSetIndex].effects.push(params);
		return this;
	};
	
	elm.fxRun = function(finalCallback, loops, loopCallback){
		var set = elm.fx.sets[elm.fx.currentSetIndex];		
		
		if (set.isRunning){
			return this;
		}
		
		setTimeout(function(){

			if (set.isRunning)
				return elm;
			
			set.isRunning = true;
			
			if (set.effectsDone > 0)
				return elm;
			set.onfinal = (finalCallback && finalCallback.call) ? finalCallback : function(){};
			set.onloop = (loopCallback && loopCallback.call) ? loopCallback : function(){};
			if(!isNaN(loops))
				set.loopCount = loops;
			 		
			for (var i=0; i<set.effects.length; i++){
				set.effects[i].onstart.call(elm);
				elm.fx.animate(elm.fx.currentSetIndex, i);
			}
		}, set.holdTime);
		
		return this;
	};
	
	elm.fxStop = function (setNum) {
		this.fx.sets[!isNaN(setNum) ? setNum : this.fx.currentSetIndex].isRunning = false;
		return this;
	};
	
	elm.fxReset = function(){
			for (var i = 0; i < this.fx.sets.length; i++){
				for (var j = 0; j < this.fx.sets[i].effects.length; j++){
					var params = this.fx.sets[i].effects[j];
					this.fx[params.type](params.initial, params.unit);
				}
			}
			var del = ['fx','fxHold','fxAdd','fxAddSet','fxRun','fxPause','fxStop','fxReset'];
			for (var i = 0; i < del.length; i++)
				try {
					delete this[del[i]];
				} catch (e) {
					this[del[i]] = null;
				}
	};
	
	elm.fx.animate = function(setIndex, effectIndex){
		var set = this.sets[setIndex];
		if(!set || (!set.isRunning))
			return;
		
		var ef = set.effects[effectIndex];
		var param = this[ef.type](null, '');

		if ((ef.step > 0 && param + ef.step < ef.to) || (ef.step < 0 && param + ef.step > ef.to)){
			this[ef.type](param + ef.step, ef.unit);
			var self = this;
			setTimeout(function(){if (self.animate) self.animate(setIndex, effectIndex)}, ef.delay);
		} else {
			this[ef.type](ef.to, ef.unit);
			set.effectsDone++;
			if (ef.onfinish)
				ef.onfinish.call(elm);
			if (set.effects.length == set.effectsDone){
				set.effectsDone = 0;
				set.loopsDone++;
				set.onloop.call(elm, set.loopsDone);
				if (set.loopsDone < set.loopCount || set.loopCount == -1){
					for (var i = 0; i < set.effects.length; i++){
						this[ef.type](ef.from, set.effects[i].unit);
						set.effects[i].onstart.call(elm, set.loopsDone);
						this.animate(setIndex, i);
					}
				} else {
					set.onfinal.call(elm);
					set.isRunning = false;
				}
			}
		}
	};
	
	elm.fxAddSet();
	
	return elm;
}
