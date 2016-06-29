<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="examstatistics" var="modelName"/>
<c:set value="${pageContext.request.contextPath}/${moduleName}" var="urlPrefix"/>
<!DOCTYPE html>
<html ng-app="angryCatExamStatisticsListApp">
<head>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>考試成績查詢</title>
	
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
<form class="form-horizontal" name="examStatisticsListForm">
 	<div class="form-group">
		<label class="col-sm-2 control-label" for="pUserId" >
 			受試者帳號
 		</label>
 		<div class="col-sm-6">
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pUserId" id="pUserId" class="form-control" autofocus>
 		</div>
 	</div>
 	<div class="form-group" ng-class="{'has-error': (examStatisticsListForm.pExamDateStart.$dirty && examStatisticsListForm.pExamDateStart.$invalid) || (examStatisticsListForm.pExamDateEnd.$dirty && examStatisticsListForm.pExamDateEnd.$invalid)}">
 		<label class="col-sm-2 control-label" id="createDur">
 			測試起迄日
		</label>
		<div class="col-sm-3">
 			<input id="pExamDateStart" 
 				class="form-control" 
 				ng-model="mainCtrl.conditionConfig.conds.condition_pExamDateStart" 
 				name="pExamDateStart" 
 				bs-datepicker 
 				type="text" 
 				autoclose="1"
 				date-format="yyyy-MM-dd"
 				placeholder="yyyy-MM-dd"
 				date-type="string"
 				aria-labelledby="createDur">			
		</div>
		<div class="col-sm-3">
 			<input id="pExamDateEnd" 
 				class="form-control" 
 				ng-model="mainCtrl.conditionConfig.conds.condition_pExamDateEnd" 
 				name="pExamDateEnd" 
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
 		<button type="submit" ng-click="mainCtrl.query()" class="btn btn-default" ng-disabled="examStatisticsListForm.$dirty && examStatisticsListForm.$invalid">查詢</button>
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
		<td>受試者</td>
		<td query-order-label="測試時間" query-order-by="p.examDate"></td>
		<td>當日最高分</td>
		<td>當日平均分</td>
		<td>當日受測次數</td>
	</tr>
	<tr ng-repeat="result in mainCtrl.conditionConfig.results">
		<td><span ng-bind="result.examinee.userId"></span></td>
		<td><span ng-bind="result.examDate"></span></td>
		<td><span ng-bind="result.maxScore"></span></td>
		<td><span ng-bind="result.avgScore"></span></td>
		<td><span ng-bind="result.examCount"></span></td>
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
</div>

<script type="text/javascript">
	angular.module('angryCatExamStatisticsListApp', ['ui.bootstrap', 'mgcrea.ngStrap'])
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
		.factory('ExamStatisticsService', ['$http', 'urlPrefix', function($http, urlPrefix){
			var queryAllUrl = urlPrefix + '/queryAll.json',
				queryByCondsUrl = urlPrefix + '/queryConditional.json',
				deleteItemsUrl = urlPrefix + '/deleteItems.json',
				copyConditionUrl = urlPrefix + '/copyCondition.json',
				resetConditionUrl = urlPrefix + '/resetConditions.json';
				
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
				copyCondition: function(conds){
					var promise = $http.post(copyConditionUrl, conds);
					return promise;
				}				
			};
		}])
		.controller('MainCtrl', ['$log', '$scope', 'ExamStatisticsService', '$window', 'urlPrefix', 'moduleName', 'currentUserId', function($log, $scope, ExamStatisticsService, $window, urlPrefix, moduleName, currentUserId){			
			var self = this;

			self.isAdmin = function(){
				return currentUserId == 'admin' || currentUserId == 'root';
			};
			ExamStatisticsService.queryAll()
				.then(function(response){
					self.conditionConfig = response.data;
				},function(errResponse){
					$log.log('Error while fetching notes');
				});

			self.query = function(){
				return ExamStatisticsService.queryByConds(self.conditionConfig)
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
				ExamStatisticsService.clearConds(self.conditionConfig);
				$scope.examStatisticsListForm.$setPristine();
				delete $scope.examStatisticsListForm.$error.parse;
				delete $scope.examStatisticsListForm.$error.date;
			};
			self.moduleName = moduleName;
		}])
		.directive('queryOrderBy', ['urlPrefix', 'ExamStatisticsService', function(urlPrefix, ExamStatisticsService){
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
						ExamStatisticsService.queryByConds(mainCtrl.conditionConfig)
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