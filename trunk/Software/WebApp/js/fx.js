function $fx(c){if(c.nodeType&&c.nodeType==1){var e=c}else{if(String(c).match(/^#([^$]+)$/i)){var e=document.getElementById(RegExp.$1);if(!e){return null}}else{return null}}if(typeof(e.fx)!="undefined"&&e.fx){return e}e.fxVersion=0.1;e.fx={};e.fx.sets=[];e.fx.currentSetIndex=0;var a={"left|top|right|bottom|width|height|margin|padding|spacing|backgroundx|backgroundy":"px",font:"pt",opacity:""};var b={delay:100,step:5,unit:""};var d={opacity:function(g,f){if(g!=null){g=Math.min(100,Math.max(0,g));e.style.opacity=g*0.01}else{return Math.round(parseFloat(e.style.opacity)*100)}},backgroundx:function(i,g){var f=0,j=0;var h=(new RegExp("^(-?\\d+)[^\\d\\-]+(-?\\d+)")).exec(e.style.backgroundPosition);if(h){f=parseInt(h[1]);j=parseInt(h[2])}if(i!=null){e.style.backgroundPosition=i+g+" "+j+g}else{return f}},backgroundy:function(i,g){var f=0,j=0;var h=(new RegExp("^(-?\\d+)[^\\d\\-]+(-?\\d+)")).exec(e.style.backgroundPosition);if(h){f=parseInt(h[1]);j=parseInt(h[2])}if(i!=null){e.style.backgroundPosition=f+g+" "+i+g}else{return j}}};e.fxAddSet=function(){var f=this.fx.sets.length;this.fx.currentSetIndex=f;this.fx.sets[f]={loopCount:1,loopsDone:0,effects:[],effectsDone:0,holdTime:0,isRunning:false};return this};e.fxHold=function(g,f){if(!e.fx.sets[this.fx.currentSetIndex].isRunning){this.fx.sets[isNaN(f)?this.fx.currentSetIndex:f].holdTime=g}return this};e.fxAdd=function(h){var i=this.fx.sets[this.fx.currentSetIndex];if(i.isRunning){return this}for(var g in b){if(!h[g]){h[g]=b[g]}}if(!h.unit){for(var f in a){if((new RegExp(f,"i").test(h.type))){h.unit=a[f];break}}}if(!this.fx[h.type]){if(d[h.type]){this.fx[h.type]=d[h.type]}else{this.fx[h.type]=function(k,j){if(k!=null){e.style[h.type]=k+j}else{return parseInt(e.style[h.type])}}}}if(h.from==null){h.from=this.fx[h.type](null,null)}h.initial=h.from;i.effects.push(h);return this};e.fxRun=function(h,f,g){var i=e.fx.sets[e.fx.currentSetIndex];if(i.isRunning){return this}setTimeout(function(){if(i.isRunning){return e}i.isRunning=true;if(i.effectsDone>0){return e}if(!isNaN(f)){i.loopCount=f}for(var j=0,k;j<i.effects.length;j++){k=i.effects[j];if(k.onstart){k.onstart.call(e)}e.fx.animate(e.fx.currentSetIndex,j)}},i.holdTime);return this};e.fxStop=function(f){this.fx.sets[!isNaN(f)?f:this.fx.currentSetIndex].isRunning=false;return this};e.fxReset=function(){var k=this.fx.sets;for(var h=0;h<k.length;h++){for(var g=0;g<k[h].effects.length;g++){var m=k[h].effects[g];this.fx[m.type](m.initial,m.unit)}}var f=["fx","fxHold","fxAdd","fxAddSet","fxRun","fxPause","fxStop","fxReset"];for(var h=0;h<f.length;h++){try{delete this[f[h]]}catch(l){this[f[h]]=null}}};e.fx.animate=function(f,g){var p=this.sets[f];if(!p||!p.isRunning){return}var r=p.effects[g];var k=this[r.type](null,null);var j=Math.abs(r.step);var o=k+j;var m=k-j;if((o<r.to)||(m>r.to)){if(o<r.to){this[r.type](o,r.unit)}else{this[r.type](m,r.unit)}var q=this;setTimeout(function(){if(q.animate){q.animate(f,g)}},r.delay)}else{this[r.type](r.to,r.unit);p.effectsDone++;if(r.onfinish!=null){r.onfinish.call(e)}if(p.effects.length==p.effectsDone){p.effectsDone=0;p.loopsDone++;if(p.onloop!=null){p.onloop.call(e,p.loopsDone)}if(p.loopCount==-1||p.loopsDone<p.loopCount){for(var n=0;n<p.effects.length;n++){this[r.type](r.from,p.effects[n].unit);p.effects[n].onstart.call(e,p.loopsDone);this.animate(f,n)}}else{if(p.onfinal!=null){p.onfinal.call(e)}p.isRunning=false}}}};e.fxAddSet();return e};