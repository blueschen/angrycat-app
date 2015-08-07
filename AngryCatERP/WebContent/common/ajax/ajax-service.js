/**
 * 
 */
(function(){
	angular.module('erp.ajax.service', [])
		.factory('AjaxService', ['$http', function($http){
			return {
				get: function(url, config){
					return $http.get(url, config);
				},
				post: function(url, data, config){
					return $http.post(url, data, config);
				}
			};
		}]);
})()
	