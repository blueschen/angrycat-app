(function(){'use strict';
	angular.module('erp.date.service', [])
		.factory('DateService', [function(){
			var toDateString = function(d, separator){
					if(!d || !d instanceof Date){
						return null;
					}
					var year = d.getFullYear(),
						month = d.getMonth()+1,
						date = d.getDate(),
						separator = separator ? separator : '-';
					return year + separator + (month > 9 ? month : ('0'+month)) + separator + (date > 9 ? date : ('0'+date));
				},
				getTimeZone = function(){
					var d = new Date(),
						diff = d.getTimezoneOffset()/60*(-1);// how much hours differ between Greenwich Mean Time zone and local time zone
					return diff;
				},
				getTimeZoneId = function(){
					var timezone = getTimeZone();
					timezone = timezone >= 0 ? ('+'+timezone) : timezone;
					return 'GMT'+timezone;
				},
				TIMEZONE_ID = getTimeZoneId();
				Object.freeze(TIMEZONE_ID);
			return {
				toDateString: toDateString,
				toTodayString: function(){
					var d = new Date();
					return toDateString(d);
				},
				getTimeZone: getTimeZone,
				getTimeZoneId: getTimeZoneId,
				TIMEZONE_ID: TIMEZONE_ID
			};
		}]);
})();