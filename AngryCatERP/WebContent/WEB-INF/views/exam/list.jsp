<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="exam" var="modelName"/>
<c:set value="${pageContext.request.contextPath}/${moduleName}" var="urlPrefix"/>
<!DOCTYPE html>
<html ng-app="angryCatExamListApp">
<head>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>題庫查詢</title>
	
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap-theme.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/common/spinner/spinner.css"/>'/>
	
	<script type="text/javascript">
		<%@ include file="/vendor/angularjs/1.4.3/angular.min.js" %>
		<%@ include file="/vendor/angularjs/1.4.3/i18n/angular-locale_zh-tw.js" %>
		<%@ include file="/vendor/angular-bootstrap/ui-bootstrap-tpls-0.13.0.min.js" %>
		<%@ include file="/common/spinner/spinner-service.js" %>
		<%@ include file="/common/fileupload/fileupload-service.js" %>
		<%@ include file="/common/fileupload/fileupload-ajax-directive.js" %>
		<%@ include file="/common/ajax/ajax-service.js" %>
	</script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.tpl.min.js"/>"></script>
	
</head>
<body ng-controller="MainCtrl as mainCtrl">
<jsp:include page="/WEB-INF/views/navigation.jsp"></jsp:include>



<div class="container">
<hr>
<form class="form-horizontal" name="examListForm">
 	<div class="form-group">
		<label class="col-sm-2 control-label" for="pDescription" >
 			題目
 		</label>
 		<div class="col-sm-6">
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pDescription" id="pDescription" class="form-control" autofocus>
 		</div>
 	</div>
 	<div class="form-group">
		<label class="col-sm-2 control-label" for="pCategory" >
 			類別
 		</label>
 		<div class="col-sm-6">
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pCategory" id="pCategory" class="form-control">
 		</div>
 	</div>
 	<div class="form-group">
		<label class="col-sm-2 control-label" for="pHint" >
 			提示
 		</label>
 		<div class="col-sm-6">
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pHint" id="pHint" class="form-control" autofocus>
 		</div>
 	</div>
 	<div class="form-group" ng-class="{'has-error': (examListForm.pCreateDateStart.$dirty && examListForm.pCreateDateStart.$invalid) || (examListForm.pCreateDateEnd.$dirty && examListForm.pCreateDateEnd.$invalid)}">
 		<label class="col-sm-2 control-label" id="createDur">
 			新增題庫起迄日
		</label>
		<div class="col-sm-3">
 			<input id="pCreateDateStart" 
 				class="form-control" 
 				ng-model="mainCtrl.conditionConfig.conds.condition_pCreateDateStart" 
 				name="pCreateDateStart" 
 				bs-datepicker 
 				type="text" 
 				autoclose="1"
 				date-format="yyyy-MM-dd"
 				placeholder="yyyy-MM-dd"
 				date-type="string"
 				aria-labelledby="createDur">			
		</div>
		<div class="col-sm-3">
 			<input id="pCreateDateEnd" 
 				class="form-control" 
 				ng-model="mainCtrl.conditionConfig.conds.condition_pCreateDateEnd" 
 				name="pCreateDateEnd" 
 				bs-datepicker 
 				type="text" 
 				autoclose="1"
 				date-format="yyyy-MM-dd"
 				placeholder="yyyy-MM-dd"
 				date-type="string"
 				aria-labelledby="createDur">			
		</div>
 	</div>
 	<div class="btn-toolbar" role="toolbar">
 	<div class="btn-group" role="group">
 		<button type="submit" ng-click="mainCtrl.query()" class="btn btn-default" ng-disabled="examListForm.$dirty && examListForm.$invalid">查詢</button>
 	</div>
 	<div class="btn-group" role="group">
 		<input type="button" value="清除" ng-click="mainCtrl.clear()" class="btn btn-default"/>
 	</div>
 	<div class="btn-group" role="group" ng-if="mainCtrl.isAdmin()">
 		
 	</div>
 </div>
 </form>

<div class="table-responsive">		
<table class="table table-bordered table-hover table-condense">
	<tr>
		<td><input type="checkbox" id="allItems" ng-click="mainCtrl.checkOrUncheckAll($event)"></td>
		<td>題目</td>
		<td>類別</td>
		<td>提示</td>
		<td query-order-label="新增題庫日" query-order-by="p.createDate"></td>
		<td>內容</td>
		<td>異動紀錄</td>
	</tr>
	<tr ng-repeat="result in mainCtrl.conditionConfig.results">
		<td><input type="checkbox" value="{{result.id}}" name="ids"></td>
		<td><span ng-bind="result.description"></span></td>
		<td><span ng-bind="result.category"></span></td>
		<td><span ng-bind="result.hint"></span></td>
		<td><span ng-bind="result.createDate"></span></td>
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
	angular.module('angryCatExamListApp', ['ui.bootstrap', 'mgcrea.ngStrap'])
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
		.factory('ExamService', ['$http', 'urlPrefix', function($http, urlPrefix){
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
		.controller('MainCtrl', ['$log', '$scope', 'ExamService', '$window', 'urlPrefix', 'moduleName', 'currentUserId', function($log, $scope, ExamService, $window, urlPrefix, moduleName, currentUserId){			
			var self = this;

			self.isAdmin = function(){
				return currentUserId == 'admin' || currentUserId == 'root';
			};
			ExamService.queryAll()
				.then(function(response){
					self.conditionConfig = response.data;
				},function(errResponse){
					$log.log('Error while fetching notes');
				});

			self.query = function(){
				return ExamService.queryByConds(self.conditionConfig)
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
				ExamService.clearConds(self.conditionConfig);
				$scope.examListForm.$setPristine();
				delete $scope.examListForm.$error.parse;
				delete $scope.examListForm.$error.date;
			};
			self.deleteItems = function(){
				var checkedItems = ExamService.getCheckedItems();
				if(!ExamService.validateBeforeDelete(checkedItems)){
					return;
				}
				ExamService.deleteItems(checkedItems)
				.then(function(response){
					self.conditionConfig = response.data;
					alert('刪除成功' + checkedItems.length + '筆');
				},function(errResponse){
					$log.log('failed!!!!!' + JSON.stringify(errResponse));
				});
			};
			self.checkOrUncheckAll = function($event){
				ExamService.checkOrUncheckAll($event);
			};
			self.moduleName = moduleName;
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
					document.location.href = '${pageContext.request.contextPath}/datachangelog/list?docId=' + attrs.toChangeLog + '&docType=com.angrycat.erp.model.Exam';
				});
			};
		}])
		.directive('queryOrderBy', ['urlPrefix', 'ExamService', function(urlPrefix, ExamService){
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
						ExamService.queryByConds(mainCtrl.conditionConfig)
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