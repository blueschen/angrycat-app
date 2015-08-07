(function(){'use strict';	
	angular.module('erp.fileupload.model.directive',['erp.spinner.service', 'erp.fileupload.service'])
		.directive('erpFileModel', ['$parse', function($parse){//binding input file to assigned ng-model
			return {
				restrict: 'A',
				link: function($scope, element, attrs){
					var getter = $parse(attrs.fileModel);
					var setter = getter.assign;
					var bindModel = function(){
						$scope.$apply(function(){
							setter($scope, element[0].files);
						});
					}
					element.on('change', bindModel);
					$scope.$on('$destroy', function(){
						element.off(bindModel);
					});
				}
			};
		}])// template or templateUrl--> compile:pre--> controller--> compile:post
		.directive('erpFileBtn', [function(){
			return {
				restrict: 'E',
				template: function(element, attrs){
					//supporting attributes
					var fileId = attrs.fileId ? attrs.fileId : 'fileId',
						fileModel = attrs.fileModelAssign ? attrs.fileModelAssign : '',
						btn = attrs.btn ? attrs.btn : '上傳',
						isMultiple = (attrs.isMultiple && attrs.isMultiple == "true") ? 'multiple' : ''
						accept = attrs.acceptType ? attrs.acceptType : '';
					
					var template = '<label for="[[fileId]]" class="btn btn-default">\
										<input type="file" style="display:none;" id="[[fileId]]" erp-file-model="[[fileModel]]" [[multiple]] accept="[[accept]]"/>\
										[[btn]]\
									</label>';
					template = template
								.replace('[[fileId]]', fileId)
								.replace('[[fileId]]', fileId)
								.replace('[[fileModel]]', fileModel)
								.replace('[[btn]]', btn)
								.replace('[[multiple]]', isMultiple)
								.replace('[[accept]]', accept);
					
					return template;
				},
				compile:function(tEle, tAttrs){
					return {
						pre: angular.noop,
						post: angular.noop
					};
				}
			};
		}]);	
})();