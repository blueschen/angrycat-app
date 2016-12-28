<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="purchasebill" var="moduleName"/>
<c:set value="${pageContext.request.contextPath}/${moduleName}" var="urlPrefix"/>
<c:set value="${pageContext.request.contextPath}" var="rootPath"/>
<c:set value="${rootPath}/vendor/kendoui/professional.2016.1.226.trial" var="kendouiRoot"/>
<c:set value="${kendouiRoot}/styles" var="kendouiStyle"/>
<c:set value="${kendouiRoot}/js" var="kendouiJs"/>
<!DOCTYPE html>
<html lang="zh-TW" ng-app="angryCatPurchaseBillViewApp">
<head>
	<meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	
	<title>新增進貨單</title>

	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap-theme.css"/>'/>
    
    <link rel="stylesheet" href="${kendouiStyle}/kendo.common.min.css">
	<link rel="stylesheet" href="${kendouiStyle}/kendo.default.min.css">
    <script type="text/javascript" src="${kendouiJs}/jquery.min.js"></script>
    <script type="text/javascript" src="<c:url value="/vendor/angularjs/1.4.3/angular.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/angularjs/1.4.3/i18n/angular-locale_zh-tw.js"/>"></script>
	<script type="text/javascript">
		<%@ include file="/common/angrycat/js/angrycat.js" %>
		<%@ include file="/common/ajax/ajax-service.js" %>
		<%@ include file="/common/date/date-service.js" %>
	</script>	
	<script type="text/javascript" src="${kendouiJs}/kendo.web.min.js"></script>
	<script type="text/javascript" src="${kendouiJs}/messages/kendo.messages.zh-TW.min.js"></script>
	<script type="text/javascript" src="${rootPath}/common/angrycat/js/angrycat.kendo.grid.js"></script>
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
<jsp:include page="/WEB-INF/views/navigation.jsp"></jsp:include>
<input type="hidden" value="{{mainCtrl.purchaseBill.id}}"/>


<div class="container">
<hr>
<div class="col-sm-offset-2">
	<h2>進貨單資料</h2>
</div>
<form class="form-horizontal" name="purchaseBillForm">
 	<div class="form-group">
 		<div class="form-group col-sm-5" ng-class="{'has-error': purchaseBillForm.no.$error.required || mainCtrl.isNoDuplicated()}">
 			<label class="col-sm-5 control-label" for="no">
 				單號<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.purchaseBill.no" id="no" name="no" class="form-control" no-duplicated="{{mainCtrl.purchaseBill.no}}"  ng-required="true" ng-disabled="mainCtrl.purchaseBill.id != null" autofocus/>
 				<span ng-show="mainCtrl.isNoDuplicated()">
 					單號已重複
 				</span> 				
 			</div> 		
 		</div>
 		<div class="form-group col-sm-5" ng-class="{'has-error': purchaseBillForm.arriveDate.$invalid}">
 			<label class="col-sm-5 control-label" for="arriveDate" >
 				到貨日
 			</label>
 			<div class="col-sm-7">
 				<input id="arriveDate"
 					class="form-control" 
 					ng-model="mainCtrl.purchaseBill.arriveDate" 
 					name="arriveDate" 
 					bs-datepicker 
 					type="text" 
 					autoclose="1"
 					date-format="yyyy-MM-dd"
 					placeholder="yyyy-MM-dd"
 					date-type="string">			      
 			</div>		
		</div>
 	 </div>
 	<div class="form-group">
		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="stockDate" >
 				入庫日
 			</label>
 			<div class="col-sm-7">
 				<span  ng-bind="mainCtrl.purchaseBill.stockDate"></span>			      
 			</div>		
		</div>
		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="note">
 				備註	
 			</label>
 			<div class="col-sm-7">
 				<textarea ng-model="mainCtrl.purchaseBill.note" id="note" rows="3" cols="30"  class="form-control"></textarea>
 			</div>
 		</div>
 	</div>

 	<div class="form-group">
 		<div class="col-sm-offset-3">
 			<input type="submit" value="儲存" ng-click="mainCtrl.save()" ng-disabled="purchaseBillForm.$invalid" class="btn btn-default"/>
 			<input type="button" value="關閉" onclick="document.location.href='${urlPrefix}/list'" class="btn btn-default" />
 			<button type="button" class="btn btn-default" ng-click="mainCtrl.toStock()" ng-if="!mainCtrl.purchaseBill.stockDate" ng-disabled="purchaseBillForm.$invalid">
				入庫歸檔
			</button>
 			<button type="button" class="btn btn-default" ng-click="mainCtrl.addPurchaseBillDetail()" ng-disabled="purchaseBillForm.$invalid">
				增加明細
			</button>
 		</div>
 	</div>
</form>

<div id="detils" ng-repeat="detail in details">
		<form class="form-inline">
			<input type="hidden" ng-value="detail.id" ng-model="detail.id"/>
			<input type="hidden" ng-value="detail.purchaseBillId" ng-model="detail.purchaseBillId"/>
			<div class="form-group">
				<label for="modelId{{$index}}">型號</label>
				<input 
					id="modelId{{$index}}" 
					type="text" 
					class="form-control"
					ng-model="detail.modelId" 
					ng-required="true" 
					kendo-auto-complete 
					k-data-source="ds"
					k-data-text-field="'modelId'"
					k-data-value-field="'modelId'"
					k-filter="'contains'"
					k-select="selectAction"/>
			</div>
			<div class="form-group">
				<input id="nameEng{{$index}}" type="text" class="form-control" placeholder="英文名稱" ng-model="detail.nameEng"/>
			</div>			
			<div class="form-group">
				<input id="name{{$index}}" type="text" class="form-control" placeholder="名稱" ng-model="detail.name"/>
			</div>
			<div class="form-group">
				<input id="count{{$index}}" type="number" class="form-control" placeholder="數量" ng-model="detail.count" ng-required="true"/>
			</div>
			<div class="form-group">
				<input id="note{{$index}}" type="text" class="form-control" placeholder="備註" ng-model="detail.note"/>
			</div>			
			<div class="form-group">
				<button type="button" class="btn btn-default" ng-click="mainCtrl.removeDetail(detail)"><span class="glyphicon glyphicon-remove"></span></button>
			</div>
		</form>			
</div>

</div>
<script type="text/javascript">
	angular.module('angryCatPurchaseBillViewApp', ['erp.date.service', 'erp.ajax.service', 'mgcrea.ngStrap', 'kendo.directives'])
		.constant('urlPrefix', '${urlPrefix}')
		.constant('login', "${sessionScope['sessionUser']}" ? true : false)
		.constant('targetData', ${purchaseBill == null ? "null" : purchaseBill})
		.controller('MainCtrl', ['$scope', 'DateService', 'AjaxService', 'urlPrefix', 'login', 'targetData', '$window', function($scope, DateService, AjaxService, urlPrefix, login, targetData, $window){
			var self = this,
				saveUrl = urlPrefix + '/save.json';
			function assignModel(m){
				self.purchaseBill = m;
				if(self.purchaseBill.purchaseBillDetails){
					$scope.details = self.purchaseBill.purchaseBillDetails;
				}
			}
			if(targetData){
				if(targetData.purchaseBillDetails){
					targetData.purchaseBillDetails.reverse(); // 讓最新的項目出現在最上面
				}
				assignModel(targetData);
			}else{
				assignModel({});
			}
			$scope.ds = 
				angrycat
					.kendoGridService
					.init({
						moduleBaseUrl: urlPrefix
					})
					.getDefaultFieldAutoCompleteDataSource({
						action: "queryProductAutocomplete",
						autocompleteFieldsToFilter: ["modelId", "nameEng", "name"]
					});
			$scope.selectAction = function(e){
				var dataItem = this.dataItem(e.item.index()),
					id = e.sender.element.attr('id'),
					idx = parseInt(id.replace('modelId', ''), 10),
					modelId = dataItem.modelId,
					details = self.purchaseBill.purchaseBillDetails,
					modelIdDuplicated = false;
				$.each(details, function(i, detail){
					if(detail.modelId == modelId){
						modelIdDuplicated = true;
						return false;
					}
				});
				if(modelIdDuplicated && !confirm("型號"+modelId+"已經存在，\n重複型號會造成異動記錄無法識別，是否要繼續下去??")){
					details.shift();
					return;
				}
				details[idx].name = dataItem.name;
				details[idx].nameEng = dataItem.nameEng;
			};
			function successHandler(response, msg){
				assignModel(response.data);
				alert(msg);
				$window.location.href = urlPrefix + '/view/' + response.data.id ;// Google Chrome在離開頁面，以上一頁回返的時候，會cache住舊資料，用reload確保不會發生這種問題
			}
			self.save = function(){
				AjaxService.post(saveUrl, self.purchaseBill)
					.then(function(response){
						successHandler(response, '儲存成功!!');
					},
					function(errResponse){
						alert('儲存失敗，錯誤訊息: ' + JSON.stringify(errResponse));
					});
			};
			self.toStock = function(){
				if(!self.purchaseBill.purchaseBillDetails
				|| self.purchaseBill.purchaseBillDetails.length == 0){
					alert('請新增明細後再入庫');
					return;
				}
				AjaxService.post(urlPrefix + '/toStock.json', self.purchaseBill)
					.then(function(response){
						successHandler(response, '歸檔成功!!');
					},
					function(errResponse){
						alert('歸檔失敗，錯誤訊息: ' + JSON.stringify(errResponse));
					});
			};
			self.addPurchaseBillDetail = function(){
				if(!self.purchaseBill.purchaseBillDetails){
					self.purchaseBill.purchaseBillDetails = [];
				}
				self.purchaseBill.purchaseBillDetails.unshift({purchaseBillId: self.purchaseBill.id, count: 1});
				$scope.details = self.purchaseBill.purchaseBillDetails; // 指定給$scope的物件，才會馬上重新render頁面，指給controller就沒辦法
			};
			self.removeDetail = function(detail){
				var idx = self.purchaseBill.purchaseBillDetails.indexOf(detail);
				self.purchaseBill.purchaseBillDetails.splice(idx, 1);
			};
			self.showRemoveBtn = function(detail){
				var idx = self.purchaseBill.purchaseBillDetails.indexOf(detail);
				return idx == 0;
			};
			self.login = login;
			self.isNoDuplicated = function(){
				return $scope.purchaseBillForm.no.$dirty && $scope.purchaseBillForm.no.$error.noDuplicated;
			};			
		}])
		.factory('ValidateService', ['$log', 'AjaxService', 'urlPrefix', '$window', function($log, AjaxService, urlPrefix, $window){
				return {
					noDuplicated: function(){
						return{
							restrict: 'A',
							require: 'ngModel',
							link: function($scope, ele, attrs, ngModelCtrl){
								$scope.$watch(attrs.ngModel, function(newVal, oldVal){
									if(!newVal || newVal == oldVal){
										return;
									}
									var no = attrs.noDuplicated,
										validationName = 'noDuplicated';
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
				};
			}])
			.directive('noDuplicated', ['ValidateService', function(ValidateService){
				return ValidateService.noDuplicated();
			}])			
			;
</script>
</body>
</html>