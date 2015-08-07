(function(){'use strict';
	angular.module('erp.fileupload.service', [])
		.factory('FileUploader', ['$http', function($http){
			return {
				uploadForm: function(config){
					var url = config.url,
						data = config.data,
						fd = new FormData();
					
					for(var prop in data){
						if(!data.hasOwnProperty(prop)){
							continue;
						}
						var inputVal = data[prop];
						if(inputVal instanceof FileList){
							var i = 0,
								len = inputVal.length;
							for(; i < len; i++){
								var file = inputVal[i];
								fd.append(prop, file, file.name);	
							}
						}else if(inputVal instanceof File){
							fd.append(prop, inputVal, inputVal.name);
						}else{
							var dataModel = JSON.stringify(inputVal);
							fd.append(prop, new Blob([dataModel], {type: 'application/json'}));		
						}
					}
					
					var promise = $http.post(url, fd, {
						transformRequest: angular.identity,
						headers: {'Content-Type': undefined}
					});
					return promise;
				}
			};
		}])
		;
})();