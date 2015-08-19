(function(){'use strict';
	angular.module('erp.datepicker.directive', ['erp.date.service', 'erp.datepicker.service'])
		.directive('datepickerPopup', ['DateService', function(DateService){// to overwrite ui.bootstrap datepicker default return value
  			function link(scope, element, attrs, ngModel) {
    			// View -> Model
    			ngModel.$parsers.push(function (value) {
    				if(!value){
    					return null;
    				}
    				var d = new Date(Date.parse(value)),
    					time = DateService.toDateString(d);
    				alert('datepickerPopup directive triggered: ' + time);
      				return time;
    			});
    		}
  			return {
    			restrict: 'A',
    			require: 'ngModel',
    			link: link
  			};
		}])
		.directive('datepickerInput', ['DatepickerService', function(DatepickerService){
			return {
				restrict: 'E',
				controller: function($scope){//此處的scope參數一定要有$
					var self = this;
					self.openCalendar = function($event, opened){
						DatepickerService.openCalendar($event, opened);
					};
				},
				controllerAs: 'datepcikerCtrl',
				template: function(element, attrs){
					var datepickerId = attrs.datepickerId ? attrs.datepickerId : 'defaultId',
						erpModelName = attrs.erpModelName ? attrs.erpModelName : datepickerId,
						format = attrs.format ? attrs.format : 'yyyy-MM-dd';
					
					var template = '<p class="input-group">\
										<input type="text"\
										ng-model="[[erpModelName]]"\
										datepicker-popup="[[format]]"\
										is-open="[[datepickerId]]"\
										readonly="readonly"\
										id="[[datepickerId]]"\
										class="form-control">\
										<span class="input-group-btn">\
											<button type="button" class="btn btn-default" ng-click="datepcikerCtrl.openCalendar($event, \'[[datepickerId]]\')"><i class="glyphicon glyphicon-calendar"></i></button>\
										</span>\
									</p>';
					
					template = template
						.replace('[[datepickerId]]', datepickerId)
						.replace('[[datepickerId]]', datepickerId)
						.replace('[[datepickerId]]', datepickerId)
						.replace('[[datepickerId]]', datepickerId)
						.replace('[[erpModelName]]', erpModelName)
						.replace('[[format]]', format);
					
					return template;
				}
			};
		}]);
})();