(function(){'use strict';	
	angular.module('erp.fileupload.ajax.directive',['erp.spinner.service', 'erp.fileupload.service'])
		.directive('erpFileChange', ['GlobalSpin', 'FileUploader', '$log', function(GlobalSpin, FileUploader, $log){
			return {
				restrict: 'A',
				controller: function($scope){
					var self = this;
					self.callback = {};
				},
				controllerAs: 'fileChangeCtrl',
				link: function($scope, element, attrs, fileChangeCtrl){
					var inputName = attrs.name ? attrs.name : 'defaultName',
						requestUrl = attrs.requestUrl,
						success = fileChangeCtrl.callback.success ? fileChangeCtrl.callback.success : angular.noop,
						fail = fileChangeCtrl.callback.fail ? fileChangeCtrl.callback.fail : angular.noop;
					
					if(requestUrl){
						var fileUpload = function(){
							$scope.$apply(function(){
								var config = {
										data:{},
										url: requestUrl};
								config.data[inputName] = element[0].files;
								
								GlobalSpin.startMask();
								FileUploader.uploadForm(config)
									.then(function(res){
										success(res);
										GlobalSpin.stopMask();
									},function(resErr){
										fail(resErr);
										GlobalSpin.stopMask();
									});
							});
						};
						element.on('change', fileUpload);
						$scope.$on('$destroy', function(){
							element.off('change', fileUpload);
						});
					}
				}
			};
		}])
		.directive('erpFileAjaxBtn', [function(){
			return {
				restrict: 'E',
				transclude: true,
				template: function(element, attrs){
					//supporting attributes
					var fileId = attrs.fileId ? attrs.fileId : 'fileId',
						btn = attrs.btn ? attrs.btn : '上傳',
						isMultiple = (attrs.isMultiple && attrs.isMultiple == "true") ? 'multiple' : '',
						accept = attrs.acceptType ? attrs.acceptType : '',
						inputName = attrs.inputName ? attrs.inputName : '',
						requestUrl = attrs.requestUrl ? attrs.requestUrl : ''; 
					
					var template = '<label for="[[fileId]]" class="btn btn-default">\
										<input type="file" style="display:none;" id="[[fileId]]" erp-file-change accept="[[accept]]" name="[[inputName]]" request-url="[[requestUrl]]" ng-transclude/>\
										[[btn]]\
									</label>';
					template = template
								.replace('[[fileId]]', fileId)
								.replace('[[fileId]]', fileId)
								.replace('[[btn]]', btn)
								.replace('[[multiple]]', isMultiple)
								.replace('[[accept]]', accept)
								.replace('[[inputName]]', inputName)
								.replace('[[requestUrl]]', requestUrl);
					return template;
				}
			};			
		}]);
})();