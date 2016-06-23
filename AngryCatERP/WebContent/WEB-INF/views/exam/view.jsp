<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="exam" var="moduleName"/>
<c:set value="${pageContext.request.contextPath}/${moduleName}" var="urlPrefix"/>
<!DOCTYPE html>
<html lang="zh-TW" ng-app="angryCatExamViewApp">
<head>
	<meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	
	<title>新增題庫</title>

	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap-theme.css"/>'/>
    
	<script type="text/javascript">		
		<%@ include file="/vendor/angularjs/1.4.3/angular.min.js" %>
		<%@ include file="/vendor/angularjs/1.4.3/i18n/angular-locale_zh-tw.js" %>
		<%@ include file="/common/ajax/ajax-service.js" %>
		<%@ include file="/common/date/date-service.js" %>
	</script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.tpl.min.js"/>"></script>

	<!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->

	<style type="text/css">
	.form-horizontal .control-label.text-left{
    text-align: left;
}
	</style>
</head>
<body ng-controller="MainCtrl as mainCtrl">
<input type="hidden" value="{{mainCtrl.exam.id}}"/>


<div class="container">
	<div class="col-sm-offset-3">
		<h2>題庫資料</h2>
	</div>
<form class="form-horizontal" name="examForm">
 	<div class="form-group">
 		<label class="col-sm-3 control-label" for="description">
 			題目
 		</label>
 		<div class="col-sm-7">
 			<input type="text" ng-model="mainCtrl.exam.description" id="description" class="form-control" autofocus/>
 		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-3 control-label" for="category">
 			類別
 		</label>
 		<div class="col-sm-7">
 			<input type="text" ng-model="mainCtrl.exam.category" id="category" class="form-control"/>
 		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-3 control-label" for="hint">
 			提示
 		</label>
 		<div class="col-sm-7">
 			<input type="text" ng-model="mainCtrl.exam.hint" id="hint" class="form-control"/>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-3">
 			<input type="submit" value="儲存" ng-click="mainCtrl.save()" ng-disabled="examForm.$invalid" class="btn btn-default"/>
 			<input type="button" value="關閉" onclick="document.location.href='${urlPrefix}/list'" class="btn btn-default" ng-if="mainCtrl.login"/>
 			<button type="button" class="btn btn-default" ng-click="mainCtrl.addItem()" ng-if="mainCtrl.login" ng-disabled="examForm.$invalid">
				增加題項
			</button>
 		</div>
 	</div>
</form>

<div id="examItems" ng-repeat="item in mainCtrl.exam.items" class="col-sm-offset-3">
		<form class="form-inline">
			<input type="hidden" ng-value="item.id" ng-model="item.id"/>
			<input type="hidden" ng-value="item.sequence" ng-model="item.sequence"/>
			<input type="hidden" ng-value="item.description" ng-model="item.description"/>
			<input type="hidden" ng-value="item.correct" ng-model="item.correct"/>
			<div class="form-group">
				<label for="description{{$index}}" style="font-size:12px;">描述</label>
				<input id="description{{$index}}" type="text" class="form-control" ng-model="item.description"/>
			</div>			
			<div class="form-group">
				<label for="sequence{{$index}}" style="font-size:12px;">題序</label>
				<input id="sequence{{$index}}" type="number" class="form-control" ng-model="item.sequence"/>
			</div>
			<div class="checkbox">
				<label for="correct{{$index}}">正確答案 <input id="correct{{$index}}" type="checkbox" ng-model="item.correct"/></label>
			</div>
			<div class="form-group" ng-show="mainCtrl.showRemoveBtn(item)">
				<button type="button" class="btn btn-default" ng-click="mainCtrl.removeDetail(item)"><span class="glyphicon glyphicon-remove"></span></button>
			</div>
		</form>			
</div>

</div>
<script type="text/javascript">
	angular.module('angryCatExamViewApp', ['erp.date.service', 'erp.ajax.service', 'mgcrea.ngStrap'])
		.constant('urlPrefix', '${urlPrefix}')
		.constant('login', "${sessionScope['sessionUser']}" ? true : false)
		.constant('targetData', ${exam == null ? "null" : exam})
		.controller('MainCtrl', ['$scope', 'DateService', 'AjaxService', 'urlPrefix', 'login', 'targetData', function($scope, DateService, AjaxService, urlPrefix, login, targetData){
			var self = this,
				saveUrl = urlPrefix + '/save.json',
				updateMemberDiscountUrl = urlPrefix + '/updateMemberDiscount.json',
				addCount = 0,
				ADD_COUNT_MAX = 2;
			self.exam = targetData;
			
			if(!self.exam){
				self.exam = {};
			}
			if(!self.exam.createDate){
				self.exam.createDate = DateService.toTodayString();
			}
			self.save = function(){
				var isNew = self.exam.id ? false : true;
				console.log(JSON.stringify(self.exam.note));
				AjaxService.post(saveUrl, self.exam)
					.then(function(response){
						self.exam = response.data;
						alert('儲存成功!!');
					},
					function(errResponse){
						alert('儲存失敗，錯誤訊息: ' + JSON.stringify(errResponse));
					});
			};
			self.useOptions = [{label: '可使用', value: '可使用'}, {label: '已用過', value: '已用過'}, {label: '已過期', value: '已過期'}, {label: '尚未到有效期限', value: '尚未到有效期限'}, {label: '尚未到可用期間', value: '尚未到可用期間'}];
			self.removeDetail = function(item){
				var items = self.exam.items,
					idx = items.indexOf(item);
				items.splice(idx, 1);
			};
			self.showRemoveBtn = function(item){
				var items = self.exam.items, 
					idx = items.indexOf(item);
				return idx == 0;
			};
			self.login = login;
			self.addItem = function(){
				if(!self.exam.items){
					self.exam.items = [];
				}
				var newItem = {};
				if(self.exam.id){
					newItem["examId"] = self.exam.id;
				}
				self.exam.items.unshift(newItem);
			};
		}])
		.factory('ValidateService', ['$log', 'AjaxService', 'urlPrefix', '$window', function($log, AjaxService, urlPrefix, $window){
				return {
					multiCondsValidateDirectiveDefProto: function(validationName){
						return{
							restrict: 'A',
							require: 'ngModel',
							link: function($scope, ele, attrs, ngModelCtrl){
								$scope.$watch(attrs.ngModel, function(newVal, oldVal){
									if(!newVal || newVal == oldVal || $scope.examForm.name.$error.required){
										return;
									}
									var memberName = $scope.mainCtrl.exam.name;

									AjaxService.get(urlPrefix + '/' + validationName+'/'+newVal+'/'+$window.encodeURIComponent(memberName))
										.then(function(response){
											ngModelCtrl.$setValidity(validationName, response.data.isValid ? true : false);
										},function(responseErr){
											ngModelCtrl.$setValidity(validationName, false);
											alert('後端檢核過程發生錯誤，檢核名稱為: ' + validationName);
										});
								});
							}	
						};
					},
					idNoDuplicated: function(){
						return{
							restrict: 'A',
							require: 'ngModel',
							link: function($scope, ele, attrs, ngModelCtrl){
								$scope.$watch(attrs.ngModel, function(newVal, oldVal){
									if(!newVal || newVal == oldVal){
										return;
									}
									var clientId = attrs.idNoDuplicated;
									if(clientId.indexOf('TW') == 0 && newVal.length != 10){
										return;
									}
									var validationName = 'idNoDuplicated';
									AjaxService.get(urlPrefix + '/' + validationName + '/' + newVal)
										.then(function(response){
											ngModelCtrl.$setValidity(validationName, response.data.isValid ? true : false);
										},function(responseErr){
											ngModelCtrl.$setValidity(validationName, false);
											alert('後端檢核過程發生錯誤，檢核名稱為: ' + validationName);
										});
								});
							}	
						};						
					},
					idNoNotHaveTenChars: function(){
						return{
							restrict: 'A',
							require: 'ngModel',
							link: function($scope, ele, attrs, ngModelCtrl){
								$scope.$watch(attrs.ngModel, function(newVal, oldVal){
									var validationName = 'idNoNotHaveTenChars';
									if(!newVal || newVal == oldVal){
										ngModelCtrl.$setValidity(validationName, true);
										return;
									}
									var re = /^[A-Z]{1}[0-9]{9}$/;
									var clientId = attrs.idNoNotHaveTenChars;
									if(clientId.indexOf('TW') == 0 && !re.test(newVal)){
										ngModelCtrl.$setValidity(validationName, false);
									}else{
										ngModelCtrl.$setValidity(validationName, true);
									}
								});
							}	
						};						
					},
					mobileNotHaveTenNumbers: function(){
						return{
							restrict: 'A',
							require: 'ngModel',
							link: function($scope, ele, attrs, ngModelCtrl){
								$scope.$watch(attrs.ngModel, function(newVal, oldVal){
									var validationName = 'mobileNotHaveTenNumbers';
									if(!newVal || newVal == oldVal){
										ngModelCtrl.$setValidity(validationName, true);
										return;
									}
									var clientId = attrs.mobileNotHaveTenNumbers;
									var re = /^[0]{1}[0-9]{9}$/;
									if(clientId && clientId.indexOf('TW') == 0 && !re.test(newVal)){
										ngModelCtrl.$setValidity(validationName, false);
									}else{
										ngModelCtrl.$setValidity(validationName, true);
									}
								});
							}	
						};						
					}					
				};
			}])
			.directive('mobileDuplicated', ['ValidateService', function(ValidateService){
				return ValidateService.multiCondsValidateDirectiveDefProto('mobileDuplicated');
			}])
			.directive('telDuplicated', ['ValidateService', function(ValidateService){
				return ValidateService.multiCondsValidateDirectiveDefProto('telDuplicated');
			}])
			.directive('idNoDuplicated', ['ValidateService', function(ValidateService){
				return ValidateService.idNoDuplicated();
			}])
			.directive('idNoNotHaveTenChars', ['ValidateService', function(ValidateService){
				return ValidateService.idNoNotHaveTenChars();
			}])
			.directive('mobileNotHaveTenNumbers', ['ValidateService', function(ValidateService){
				return ValidateService.mobileNotHaveTenNumbers();
			}])				
			;
</script>
</body>
</html>