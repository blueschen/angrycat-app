(function(window){"use strict"
	var angrycat = window.angrycat = window.angrycat || {}; 

	function once(func){
		function empty(){}
		return function(){
			var f = func;
			func = empty;
			f.apply(this, arguments);
		};
	}
	function minusTimezoneOffset(d){
		var hours = d.getHours(),
			mins = d.getMinutes(),
			secs = d.getSeconds(),
			milliSecs = d.getMilliseconds();
		d.setHours(hours, mins-d.getTimezoneOffset(), secs, milliSecs); // GMT+0800 timezoneoffset is -480
		return d;
	}
	function plusTimezoneOffset(d){
		var hours = d.getHours(),
			mins = d.getMinutes(),
			secs = d.getSeconds(),
			milliSecs = d.getMilliseconds();
		d.setHours(hours, mins+d.getTimezoneOffset(), secs, milliSecs);
		return d;				
	}
	function assert(value, desc){
		var li = document.createElement("li"),
			passColor = "green",
			failColor = "red";
		li.style.color = value ? passColor : failColor;
		li.appendChild(document.createTexeNode(desc));
		var results = document.getElementById("results");
		if(!results){
			results = document.createElement("ul");
			results.id = "results";
		}
		results.appendChild(li);
	}
	function getCookie(cname) {
	    var name = cname + "=";
	    var decodedCookie = decodeURIComponent(document.cookie);
	    var ca = decodedCookie.split(';');
	    for(var i = 0; i <ca.length; i++) {
	        var c = ca[i];
	        while (c.charAt(0) == ' ') {
	            c = c.substring(1);
	        }
	        if (c.indexOf(name) == 0) {
	            return c.substring(name.length, c.length);
	        }
	    }
	    return "";
	}	
	function setCookie(cname, cvalue, exdays) {
	    var d = new Date();
	    d.setTime(d.getTime() + (exdays*24*60*60*1000));
	    var expires = "expires="+ d.toUTCString();
	    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
	}
	function reloadIfVerionNotMatched(pageName, version) {
		var cName = pageName + "_deploy_version",
			v = getCookie(cName);
		if(version !== v){
			setCookie(cName, version, 365);
			window.location.reload(true);
		}
	}
	angrycat.core = {
		once: once,
		minusTimezoneOffset: minusTimezoneOffset,
		plusTimezoneOffset: plusTimezoneOffset,
		getCookie: getCookie,
		setCookie: setCookie,
		reloadIfVerionNotMatched: reloadIfVerionNotMatched
	};
})(window);