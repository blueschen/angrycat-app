<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="purchasebill" var="modelName"/>
<c:set value="${pageContext.request.contextPath}/${moduleName}" var="urlPrefix"/>
<!DOCTYPE html>
<html ng-app="angryCatPurchaseBillListApp">
<head>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>進貨單</title>
	
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap-theme.css"/>'/>
	
	<script type="text/javascript">
		<%@ include file="/vendor/angularjs/1.4.3/angular.min.js" %>
		<%@ include file="/vendor/angularjs/1.4.3/i18n/angular-locale_zh-tw.js" %>
		<%@ include file="/vendor/angular-bootstrap/ui-bootstrap-tpls-0.13.0.min.js" %>
	</script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.tpl.min.js"/>"></script>
	
</head>
<body ng-controller="MainCtrl as mainCtrl">
<jsp:include page="/WEB-INF/views/navigation.jsp"></jsp:include>



<div class="container">
<hr>
<form class="form-horizontal" name="purchaseBillListForm">
 	<div class="form-group">
		<label class="col-sm-2 control-label" for="pNo" >
 			單號
 		</label>
 		<div class="col-sm-6">
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pNo" id="pNo" class="form-control" autofocus>
 		</div>
 	</div>
 	<div class="form-group" ng-class="{'has-error': (purchaseBillListForm.pArriveDateStart.$dirty && purchaseBillListForm.pArriveDateStart.$invalid) || (purchaseBillListForm.pArriveDateEnd.$dirty && purchaseBillListForm.pArriveDateEnd.$invalid)}">
 		<label class="col-sm-2 control-label" id="arriveDateDur">
 			到貨日起迄
		</label>
		<div class="col-sm-3">
 			<input id="pArriveDateStart" 
 				class="form-control" 
 				ng-model="mainCtrl.conditionConfig.conds.condition_pArriveDateStart" 
 				name="pArriveDateStart" 
 				bs-datepicker 
 				type="text" 
 				autoclose="1"
 				date-format="yyyy-MM-dd"
 				placeholder="yyyy-MM-dd"
 				date-type="string"
 				aria-labelledby="arriveDateDur">			
		</div>
		<div class="col-sm-3">
 			<input id="pArriveDateEnd" 
 				class="form-control" 
 				ng-model="mainCtrl.conditionConfig.conds.condition_pArriveDateEnd" 
 				name="pArriveDateEnd" 
 				bs-datepicker 
 				type="text" 
 				autoclose="1"
 				date-format="yyyy-MM-dd"
 				placeholder="yyyy-MM-dd"
 				date-type="string"
 				aria-labelledby="arriveDateDur">			
		</div>
 	</div>
 	<div class="form-group" ng-class="{'has-error': (purchaseBillListForm.pStockDateStart.$dirty && purchaseBillListForm.pStockDateStart.$invalid) || (purchaseBillListForm.pStockDateEnd.$dirty && purchaseBillListForm.pStockDateEnd.$invalid)}">
 		<label class="col-sm-2 control-label" id="stockDateDur">
 			入庫日起迄
		</label>
		<div class="col-sm-3">
 			<input id="pStockDateStart" 
 				class="form-control" 
 				ng-model="mainCtrl.conditionConfig.conds.condition_pStockDateStart" 
 				name="pStockDateStart" 
 				bs-datepicker 
 				type="text" 
 				autoclose="1"
 				date-format="yyyy-MM-dd"
 				placeholder="yyyy-MM-dd"
 				date-type="string"
 				aria-labelledby="stockDateDur">			
		</div>
		<div class="col-sm-3">
 			<input id="pStockDateEnd" 
 				class="form-control" 
 				ng-model="mainCtrl.conditionConfig.conds.condition_pStockDateEnd" 
 				name="pStockDateEnd" 
 				bs-datepicker 
 				type="text" 
 				autoclose="1"
 				date-format="yyyy-MM-dd"
 				placeholder="yyyy-MM-dd"
 				date-type="string"
 				aria-labelledby="stockDateDur">			
		</div>
	</div> 	
 	<div class="form-group">
 	 	<label class="col-sm-2 control-label" for="pNote" >
 			備註
 		</label>
 		<div class="col-sm-6">
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pNote" id="pNote" class="form-control">
 		</div>
 	</div>


  	 	
 	<div class="btn-toolbar" role="toolbar">
 	<div class="btn-group" role="group">
 		<button type="submit" ng-click="mainCtrl.query()" class="btn btn-default" ng-disabled="purchaseBillListForm.$dirty && purchaseBillListForm.$invalid">查詢</button>
 	</div>
 	<div class="btn-group" role="group">
 		<a href="${pageContext.request.contextPath}/purchasebill/add" class="btn btn-default">新增</a>
 	</div>
 	<div class="btn-group" role="group">
 		<input type="button" value="清除" ng-click="mainCtrl.clear()" class="btn btn-default"/>
 	</div>
 	<div style="display:none;">
 		<div class="btn-group" role="group" ng-if="mainCtrl.isAdmin()">
 			<input type="button" ng-click="mainCtrl.downloadExcel()" class="btn btn-default"  ng-disabled="purchaseBillListForm.$dirty && purchaseBillListForm.$invalid" value="下載會員檔案"/>
 		</div>	
 		<div class="btn-group" role="group" ng-if="mainCtrl.isAdmin()">
 			<input type="button" ng-click="mainCtrl.downloadTemplate()" class="btn btn-default" value="下載範本"/>
 		</div>
 	</div>
 </div>
 </form>

<div class="table-responsive">		
<table class="table table-bordered table-hover table-condense">
	<tr>
		<td><input type="checkbox" id="allItems" ng-click="mainCtrl.checkOrUncheckAll($event)"></td>
		<td query-order-label="單號" query-order-by="p.no"></td>
		<td query-order-label="到貨日" query-order-by="p.arriveDate"></td>
		<td query-order-label="入庫日" query-order-by="p.stockDate"></td>
		<td>備註</td>
		<td>內容</td>
		<td>異動紀錄</td>
	</tr>
	<tr ng-repeat="result in mainCtrl.conditionConfig.results">
		<td><input type="checkbox" value="{{result.id}}" name="ids"></td>
		<td><span ng-bind="result.no"></span></td>
		<td><span ng-bind="result.arriveDate"></span></td>
		<td><span ng-bind="result.stockDate"></span></td>
		<td><span ng-bind="result.note"></span></td>
		<td>
			<span to-view="{{result.id}}">
				<i class="glyphicon glyphicon-file"></i>	
			</span>
		</td>
		<td>
			<span to-change-log="{{result.id}}">
				<i class="glyphicon glyphicon-pencil"></i>	
			</span>
		</td>
	</tr>
</table>
</div>		
		<pagination 
			total-items="mainCtrl.conditionConfig.pageNavigator.totalCount" 
			ng-model="mainCtrl.conditionConfig.conds.currentPage" 
			ng-change="mainCtrl.pageChanged()"
			items-per-page="mainCtrl.conditionConfig.conds.countPerPage"
			max-size="10"
			previous-text="&lsaquo;"
			next-text="&rsaquo;"
			first-text="&laquo;"
			last-text="&raquo;"
			boundary-links="true"></pagination>
<div class="btn-toolbar" role="toolbar">
 	<div class="btn-group" role="group">
 		<input type="button" value="刪除" ng-click="mainCtrl.deleteItems()" class="btn btn-default"/>
 	</div>
</div>	
</div>

<script type="text/javascript">
	angular.module('angryCatPurchaseBillListApp', ['ui.bootstrap', 'mgcrea.ngStrap'])
		.constant('urlPrefix', '${urlPrefix}')
		.constant('moduleName', '${moduleName}')
		.constant('currentUserId', '${sessionUser.userId}')
		.factory('AuthInterceptor', ['$q', function($q){
			return {
				responseError: function(responseRejection){
					if(responseRejection.status == 401){
						document.location.href = '${pageContext.request.contextPath}/login.jsp';
					}
					return $q.reject(responseRejection); // make sure to trigger error handler in the next promise
				}
			};
		}])
		.config(['$httpProvider', '$compileProvider', function($httpProvider, $compileProvider){
			$httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest'; // to tell server this is a ajax request
			$httpProvider.interceptors.push('AuthInterceptor');
			$compileProvider.debugInfoEnabled(false); // after set false, angular.element(htmlEle).scope() will return undefined to improve performance
		}])
		.factory('PurchaseBillService', ['$http', 'urlPrefix', function($http, urlPrefix){
			var queryAllUrl = urlPrefix + '/queryAll.json',
				queryByCondsUrl = urlPrefix + '/queryConditional.json',
				deleteItemsUrl = urlPrefix + '/deleteItems.json',
				copyConditionUrl = urlPrefix + '/copyCondition.json',
				resetConditionUrl = urlPrefix + '/resetConditions.json',
				idCheckName = 'ids',
				getCheckedItems = function(){
					var ids = document.getElementsByName(idCheckName),
						checkedItems = [];
					if(!ids){
						return checkedItems;
					}
					if(ids.length == undefined && ids.checked){// check one
						checkedItems.push(ids);
					}else{// check one more
						for(var i = 0; i < ids.length; i++){
							if(ids[i].checked){
								checkedItems.push(ids[i].value);
							}
						}
					}
					return checkedItems;
				};
				
			return {
				queryAll: function(){
					return $http.get(queryAllUrl);
				},
				queryByConds: function(conds){
					return $http.post(queryByCondsUrl, conds);
				},
				clearConds: function(config){
					return $http.get(resetConditionUrl)
						.then(function(response){
							config.conds = response.data.conds;
						},function(responseErr){
							alert('重置查詢條件錯誤');
						});
				},
				getCheckedItems: getCheckedItems,
				validateBeforeDelete: function(checkedItems){
					
					if(checkedItems.length == 0){
						alert('請勾選要刪除的項目');
						return false;
					}
					if(!confirm('確定刪除?')){
						return false;
					}
					return true;
				},
				deleteItems: function(checkedItems){
					return $http.post(deleteItemsUrl, checkedItems);
				},
				copyCondition: function(conds){
					var promise = $http.post(copyConditionUrl, conds);
					return promise;
				},
				checkOrUncheckAll: function($event){
					var isChecked = $event.target.checked,
						ids = document.getElementsByName(idCheckName);
				
					if(ids){
						if(ids.length == undefined){
							ids.checked = isChecked;
						}else{
							for(var i = 0; i < ids.length; i++){
								var item = ids[i];
								item.checked = isChecked;
							}
						}
					}
				}				
			};
		}])
		.controller('MainCtrl', ['$log', '$scope', 'PurchaseBillService', '$window', 'urlPrefix', 'moduleName', 'currentUserId', function($log, $scope, PurchaseBillService, $window, urlPrefix, moduleName, currentUserId){			
			var self = this;
			
			self.isAdmin = function(){
				return currentUserId == 'admin' || currentUserId == 'root';
			};
			PurchaseBillService.queryAll()
				.then(function(response){
					self.conditionConfig = response.data;
				},function(errResponse){
					$log.log('Error while fetching notes');
				});

			self.query = function(){
				return PurchaseBillService.queryByConds(self.conditionConfig)
				.then(function(response){
					self.conditionConfig = response.data;
				},function(errResponse){
					$log.log('failed!!!!!' + JSON.stringify(errResponse));
				});
			};
			self.pageChanged = function(){
				self.query();
			}
			self.clear = function(){
				PurchaseBillService.clearConds(self.conditionConfig);
				$scope.purchaseBillListForm.$setPristine();
				delete $scope.purchaseBillListForm.$error.parse;
				delete $scope.purchaseBillListForm.$error.date;
			};
			self.deleteItems = function(){
				var checkedItems = PurchaseBillService.getCheckedItems();
				if(!PurchaseBillService.validateBeforeDelete(checkedItems)){
					return;
				}
				PurchaseBillService.deleteItems(checkedItems)
				.then(function(response){
					self.conditionConfig = response.data;
					alert('刪除成功' + checkedItems.length + '筆');
				},function(errResponse){
					$log.log('failed!!!!!' + JSON.stringify(errResponse));
				});
			};
			self.downloadExcel = function(){
				PurchaseBillService.copyCondition(self.conditionConfig)
					.then(function(){
						$window.location.href = urlPrefix + '/downloadExcel';
					});
			};
			self.downloadTemplate = function(){
				$window.location.href = urlPrefix + '/downloadTemplate';
			};
			self.checkOrUncheckAll = function($event){
				PurchaseBillService.checkOrUncheckAll($event);
			};
			self.moduleName = moduleName;
			self.months=[{label:'一', value:1},{label:'二', value:2},{label:'三', value:3},{label:'四', value:4},{label:'五', value:5},{label:'六', value:6},{label:'七', value:7},{label:'八', value:8},{label:'九', value:9},{label:'十', value:10},{label:'十一', value:11},{label:'十二', value:12}];
		}])
		.filter('convertBoolean', [function(){
			return function(input){
				return input?'是':'否';
			}
		}])
		.directive('toView', [function(){
			return function(scope, ele, attrs){
				ele.bind('click', function(){
					document.location.href = '${urlPrefix}/view/' + attrs.toView;
				});
			};
		}])
		.directive('toChangeLog', [function(){
			return function(scope, ele, attrs){
				ele.bind('click', function(){
					document.location.href = '${pageContext.request.contextPath}/datachangelog/list?docId=' + attrs.toChangeLog + '&docType=com.angrycat.erp.model.PurchaseBill';
				});
			};
		}])
		.directive('queryOrderBy', ['urlPrefix', 'PurchaseBillService', function(urlPrefix, PurchaseBillService){
			return {
				restrict: 'A',
				template: function(element, attrs){
					var label = attrs.queryOrderLabel;
					return '<span>'+label+'<i></i></span>';
				},
				link: function($scope, element, attrs){
					var asc = null,
						orderBy = attrs.queryOrderBy,
						mainCtrl = $scope.mainCtrl;
					function queryOrderBy(event){
						if(asc){
							asc = false;
						}else{
							asc = true;
						}
						var orderType = orderBy + (asc ? ' ASC' : ' DESC');
						mainCtrl.conditionConfig.conds.orderType = orderType;
						PurchaseBillService.queryByConds(mainCtrl.conditionConfig)
							.then(function(response){
								var target = angular.element(angular.element(element.children()[0]).children()[0]);
								if(asc){
									target.attr('class', 'glyphicon glyphicon-triangle-top');
								}else{
									target.attr('class', 'glyphicon glyphicon-triangle-bottom');
								}
								mainCtrl.conditionConfig = response.data;
							},function(responseErr){
								alert('排序查詢發生錯誤: ' + JSON.stringify(responseErr));
							});
					}
					element.on('click', queryOrderBy);
					$scope.$on('$destroy', function(){
						element.off('click', queryOrderBy);
					});
				}
			};
		}])
		;
</script>

</body>
</html>