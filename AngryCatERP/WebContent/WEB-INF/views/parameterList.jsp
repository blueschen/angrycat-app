<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<html ng-app="parameterListApp">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>eeeee</title>
<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap.css"/>'/>
<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap-theme.css"/>'/>
<script type="text/javascript" src='<c:url value="/angularjs/1.3.16/angular.js"/>'></script>
<script type="text/javascript" src='<c:url value="/angularjs/ui-bootstrap-tpls-0.13.0.min.js"/>'></script>
</head>
<body ng-controller="MainCtrl as mainCtrl">
    



	<h1>Hello Services!</h1>
	<button ng-click="mainCtrl.logStuff()">Log Something!</button><br>	
		代碼:
		<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pCode"><br>
		序號:
		<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pSeq"><br>
		<input type="button" value="查詢" ng-click="mainCtrl.query()"/>
		<table class="table">
			<tr ng-repeat="result in mainCtrl.conditionConfig.results">
				<td>{{result.id}}</td>
				<td>{{result.nameDefault}}</td>
				<td>{{result.code}}</td>
				<td>{{result.sequence}}</td>
			</tr>
		</table>
		
		<br>
		<pagination 
			total-items="mainCtrl.conditionConfig.pageNavigator.totalCount" 
			ng-model="mainCtrl.conditionConfig.conds.currentPage" 
			ng-change="mainCtrl.pageChanged()"
			items-per-page="mainCtrl.conditionConfig.conds.countPerPage"
			class="pagination-sm"></pagination>
	
	<button ng-click="mainCtrl.testPost()">測試按鈕</button>

<script type="text/javascript">
	angular.module('parameterListApp', ['ui.bootstrap'])
		.controller('MainCtrl', ['$log', '$http', function($log, $http){
			var self = this;
			self.logStuff = function(){
				$log.log('The button was pressed');
			};
			self.content = {};
			$http.get('${pageContext.request.contextPath}/parameter/getConditionConfig.json')
				.then(function(response){
					self.conditionConfig = response.data;
					$log.log("getting: " + JSON.stringify(response.data));
				},function(errResponse){
					$log.log('Error while fetching notes');
				});
			self.testPost = function(){
				$log.log('before posting, conditionConfig: ' + JSON.stringify(self.conditionConfig));
				
				$http.post('${pageContext.request.contextPath}/parameter/getConditionConfig.json', self.conditionConfig)
					.then(function(response){
						$log.log('successfully return: ' + JSON.stringify(response.data));
					},function(){});
			};
			self.query = function(){
				$http.post('${pageContext.request.contextPath}/parameter/getConditionConfig.json', self.conditionConfig)
				.then(function(response){
					$log.log('successfully return: ' + JSON.stringify(response.data));
					self.conditionConfig = response.data;
				},function(errResponse){
					$log.log('failed!!!!!' + JSON.stringify(errResponse));
				});
			};
			self.pageChanged = function(){
				self.query();
			}
		}]);
</script>

</body>
</html>