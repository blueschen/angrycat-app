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
			};
			return {
				toDateString: toDateString,
				toTodayString: function(){
					var d = new Date();
					return toDateString(d);
				}
			};
		}]);
})();