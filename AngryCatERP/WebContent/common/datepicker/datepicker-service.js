/**
 * 
 */
(function(){'use strict';
	angular.module('erp.datepicker.service', [])
		.factory('DatepickerService', [function(){
			return {
				openCalendar: function($event, opened){
				    $event.preventDefault();
				    $event.stopPropagation();
				    
				    angular.element($event.target).scope()[opened] = true;
				}
			};
		}]);
})();