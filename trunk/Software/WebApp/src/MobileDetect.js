(function() {
	var mobilePlatforms=["midp","240x320","blackberry","netfront","nokia","panasonic","portalmmm","sharp","sie-","sonyericsson","symbian","windows ce","benq","mda","mot-","opera mini","philips","pocket pc","sagem","samsung","sda","sgh-","vodafone","xda","iphone","ipad","android"],
		userAgent,
		isMobile = !1,
		a;
	if (!/full_io_site=1/.test(document.cookie||"")) {
		userAgent = navigator.userAgent.toLowerCase();
		for(var i = 0; i < mobilePlatforms.length; i++)
			if (userAgent.indexOf(mobilePlatforms[i]) != -1)
				isMobile = !0,
				a = window.location.pathname,
				a.indexOf("agenda.html") !== -1 ?
					a = "/mobile/agenda.html" :
						a.indexOf("sessions.html") !== -1 ?
							a = "/mobile/tracks.html" :
								/sessions\/(.*?\.html)/.test(a) ? (
									a = a.match(/sessions\/(.*?\.html)/),
									a = "/mobile/sessions/all-tracks/" + a[1]) :
										a = "/mobile/index.html",
										window.location.replace("/events/io/2011" + a);
		if (!isMobile)
			document.cookie = "full_io_site=1; max-age=3600; path=/";
		window.onload = function() {
			function setCookie() {
				document.cookie = "full_io_site=0; max-age=0; path=/"
			}
			var e = document.getElementById("mobile-site");
			if (e)
				e.onclick = setCookie
		}
	}
})();
