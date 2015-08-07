(function(){'use strict';
	angular.module('erp.spinner.service', [])
		.factory('ElementSpin', ['$document', function($document){
			var defaultMask = 'masked-partial-taiji';
			return {
				startMask: function(config){
					var ele = config.element ? config.element : (config.eleId ? $document[0].getElementById(config.eleId) : null),
						$jq = angular.element(ele),
						maskClass = config.maskClass ? config.maskClass : defaultMask;
					if(!$jq.hasClass(maskClass)){
						$jq.addClass(maskClass);
					}	
				},
				stopMask: function(config){
					var ele = config.element ? config.element : (config.eleId ? $document[0].getElementById(config.eleId) : null),
						$jq = angular.element(ele),
						maskClass = config.maskClass ? config.maskClass : defaultMask;
					if($jq.hasClass(maskClass)){
						$jq.removeClass(maskClass);
					}			
				}
			};
		}])
		.factory('GlobalSpin', ['ElementSpin', '$document', function(ElementSpin, $document){
			var defaultMask = 'masked-full-ajax-loader';
			return {
				startMask: function(maskClass){
					maskClass = maskClass ? maskClass : defaultMask;
					var config = {element: $document[0].body, maskClass: maskClass};
					ElementSpin.startMask(config);
				},
				stopMask: function(maskClass){
					maskClass = maskClass ? maskClass : defaultMask;
					var config = {element: $document[0].body, maskClass: maskClass};
					ElementSpin.stopMask(config);
				}
			};
		}])
		.factory('AjaxGlobalSpinInterceptor', ['$q', 'GlobalSpin', function($q, GlobalSpin){
			var numLoadings = 0,
				maskClass = 'masked-full-ajax-loader';
			return {
				request: function(config){						
					numLoadings++;						
					GlobalSpin.startMask(maskClass);						
					return config || $q.when(config);
				},
				requestError: function(rejection){
					return $q.reject(rejection);
				},
				response: function(response){						
					if((--numLoadings) === 0){
						//remove mask
						GlobalSpin.stopMask(maskClass);
					}						
					return response || $q.when(response);
				},
				responseError: function(rejection){						
					if(!(--numLoadings)){
						//remove mask
						GlobalSpin.stopMask(maskClass);
					}						
					return $q.reject(rejection);
				}
			};
		}]);
})();