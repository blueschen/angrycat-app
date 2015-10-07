<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="${pageContext.request.contextPath}/${moduleName}" var="urlPrefix"/>
<!DOCTYPE html>
<html ng-app="datachangelogListApp">
<head>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>異動紀錄查詢</title>
	
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.1.1/css/bootstrap.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.1.1/css/bootstrap-theme.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.1.1/css/bootstrap-responsive.css"/>'/>
	
	<script type="text/javascript">
		<%@ include file="/vendor/angularjs/1.4.3/angular.min.js" %>
		<%@ include file="/vendor/angularjs/1.4.3/i18n/angular-locale_zh-tw.js" %>
		<%@ include file="/vendor/angular-bootstrap/ui-bootstrap-tpls-0.13.0.min.js" %>
		<%@ include file="/common/ajax/ajax-service.js" %>
		<%@ include file="/common/date/date-service.js" %>
	</script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.tpl.min.js"/>"></script>
	
	<style type="text/css">
		.extra-row{cursor: pointer;}
		.extra-row:hover{background-color:#ddd;}
		.main-row{background-color:#aea !important;}
		.main-row:hover{background-color:#52CCCC;}
	</style>
	
</head>
<body ng-controller="MainCtrl as mainCtrl">

<div>
	<nav role="navigation" class="navbar navbar-default navbar-fixed-top">
		<div class="container">
			<div class="navbar-header">
				<button type="button" data-target="#navbarCollapse" data-toggle="collapse" class="navbar-toggle">
					<span class="sr-only">Toggle navigation</span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
				</button>
				<a href="#" class="navbar-brand">Angrycat</a>
			</div>
			<div id="navbarCollapse" class="collapse navbar-collapse">
				<ul class="nav navbar-nav">
					<li ng-class="{'active': mainCtrl.moduleName == 'member'}">
						<a href="${pageContext.request.contextPath}/member/list">會員查詢</a>
					</li>
					<li ng-class="{'active': mainCtrl.moduleName == 'datachangelog'}">
						<a href="${pageContext.request.contextPath}/datachangelog/list">異動紀錄查詢</a>
					</li>
					<li ng-class="{'active': mainCtrl.moduleName == 'datadeletelog'}">
						<a href="${pageContext.request.contextPath}/datadeletelog/list">刪除紀錄查詢</a>
					</li>					
				</ul>
				<ul class="nav navbar-nav navbar-right">
					<li>
						<a href="${pageContext.request.contextPath}/logout"><span class="glyphicon glyphicon-user"></span>登出</a>
					</li>
				</ul>
			</div>		
		</div>
	</nav>
</div>
  
<div class="container">

<div class="jumbotron">
<h4>異動紀錄查詢</h4>
</div>
<input type="hidden" ng-model="mainCtrl.conditionConfig.conds.condition_dAction"/>
<form class="form-horizontal" name="datachangelogListForm" >
 	<div class="form-group">
		<label class="col-sm-2 control-label" for="pAction">
 			操作
 		</label>
 		<div class="col-sm-6">
 			<select 
				ng-model="mainCtrl.conditionConfig.conds.condition_pAction" 
				ng-options="a.value as a.label for a in mainCtrl.actionTypes"
				id="pAction"
				 class="form-control">
				<option value="">==請選擇==</option>	
			</select>
 		</div>
 	</div>
 	<div class="form-group" ng-class="{'has-error': (datachangelogListForm.pLogTimeStart.$dirty && datachangelogListForm.pLogTimeStart.$invalid) || (datachangelogListForm.pLogTimeEnd.$dirty && datachangelogListForm.pLogTimeEnd.$invalid)}">
 		<label class="col-sm-2 control-label" id="logTimeDur">
 			更動時間範圍
		</label>
		<div class="col-sm-3">
 			<input id="pLogTimeStart" 
 				class="form-control" 
 				ng-model="mainCtrl.conditionConfig.conds.condition_pLogTimeStart" 
 				name="pLogTimeStart" 
 				bs-datepicker 
 				type="text" 
 				autoclose="1"
 				date-format="yyyy-MM-dd"
 				placeholder="yyyy-MM-dd"
 				date-type="string"
 				aria-labelledby="logTimeDur">
 			
		</div>
		<div class="col-sm-3">
 			<input id="pLogTimeEnd" 
 				class="form-control" 
 				ng-model="mainCtrl.conditionConfig.conds.condition_pLogTimeEnd" 
 				name="pLogTimeEnd" 
 				bs-datepicker 
 				type="text" 
 				autoclose="1"
 				date-format="yyyy-MM-dd"
 				placeholder="yyyy-MM-dd"
 				date-type="string"
 				aria-labelledby="logTimeDur">			
		</div>
 	</div>
 	<div class="form-group">
 	 	<label class="col-sm-2 control-label" for="pUserName">
 			使用者姓名
 		</label>
 		<div class="col-sm-6">
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pUserName" id="pUserName" class="form-control" autofocus>
 		</div>
 	</div>
 	<div class="form-group">
 		 <label class="col-sm-2 control-label" for="pUserId" >
 			使用者帳號
 		</label>
 		<div class="col-sm-6">
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pUserId" id="pUserId" class="form-control">
 		</div>
 	</div>
 	 <div class="btn-toolbar" role="toolbar">
 	<div class="btn-group" role="group">
 		<input type="submit" ng-click="mainCtrl.queryByConds()" class="btn btn-default" value="查詢" ng-disabled="datachangelogListForm.$dirty && datachangelogListForm.$invalid"/>
 	</div>
 	<div class="btn-group" role="group">
 		<input type="button" ng-click="mainCtrl.clearConds()" class="btn btn-default"value="清除"/>
 	</div>
 </div>
 </form>

<div class="table-responsive">
	<div class="row">
		<div class="col-sm-3"></div>
	</div>

		
<table class="table table-bordered table-hover table-condense">
	<colgroup>
		<col class="col-sm-2">
		<col class="col-sm-2">
		<col class="col-sm-4">
		<col class="col-sm-4">
	</colgroup>
	<thead>
	<tr>
		<th>更動時間</th>
		<th>更動模組</th>
		<th>使用者帳號</th>
		<th>使用者姓名</th>
	</tr>	
	</thead>
	<tbody ng-repeat="result in mainCtrl.conditionConfig.results">
		<tr class="main-row" ng-click="mainCtrl.toggleDetail($index)">
			<td><span ng-bind="result.logTime | date : 'yyyy-MM-dd HH:mm:ss' : mainCtrl.TIMEZONE_ID"></span></td>
			<td>
				<span ng-bind="result.docId"></span>	
			</td>
			<td><span ng-bind="result.userId"></span></td>
			<td><span ng-bind="result.userName"></span></td>				
		</tr>
		<tr class="extra-row">
			<td colspan="4" style="padding: 0px;">
				<table style="width:100%;" class="table-bordered">
					<colgroup>
						<col class="col-sm-2">
						<col class="col-sm-5">
						<col class="col-sm-5">
					</colgroup>
					<thead>
						<tr>
							<th>欄位名稱</th>
							<th>原始內容</th>
							<th>變更內容</th>
						</tr>					
					</thead>
					<tbody  ng-repeat="detail in result.details">
						<tr>
							<td><span ng-bind="detail.fieldName"></span></td>
							<td><span ng-bind="detail.originalContent"></span></td>
							<td><span ng-bind="detail.changedContent"></span></td>				
						</tr>					
					</tbody>
				</table>
			</td>		
		</tr>
	</tbody>
	<tr>

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
	angular.module('datachangelogListApp', ['ui.bootstrap', 'mgcrea.ngStrap', 'erp.ajax.service', 'erp.date.service'])
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
		.config(['$httpProvider', function($httpProvider){
			$httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest'; // to tell server this is a ajax request
			$httpProvider.interceptors.push('AuthInterceptor');
		}])
		.controller('MainCtrl', ['$log', '$scope', '$window', 'AjaxService', 'DateService', function($log, $scope, $window, AjaxService, DateService){			
			var self = this,
				queryAllUrl = '${urlPrefix}/queryAll.json'
				queryConditionalUrl = '${urlPrefix}/queryConditional.json',
				condPrefix = 'condition_',
				initialState = null;
				
			self.actionTypes = [{label: '刪除', value: 'DELETE'}, {label: '修改', value: 'UPDATE'}, {label: '新增', value: 'ADD'}];
			
			self.queryAll = function(){
				AjaxService.post(queryAllUrl)
					.then(function(response){
						self.conditionConfig = response.data;
						initialState = angular.copy(self.conditionConfig.conds);
					},function(responseErr){
						alert('queryAll failed');
						$log.log('queryAll failed err msg: ' + JSON.stringify(responseErr));
					});
			};
			self.queryByConds = function(){
				AjaxService.post(queryConditionalUrl, self.conditionConfig)
					.then(function(response){
						self.conditionConfig = response.data;
					},function(responseErr){
						alert('queryConditional failed');
						$log.log('queryConditional failed err msg: ' + JSON.stringify(responseErr));	
					})
			};
			self.clearConds = function(){
				self.conditionConfig.conds = angular.copy(initialState);
				$scope.datachangelogListForm.$setPristine();
				delete $scope.datachangelogListForm.$error.parse;
				delete $scope.datachangelogListForm.$error.date;
			};
			self.pageChanged = function(){
				self.queryByConds();
			};
			self.toggleDetail = function($index){
				self.active = self.active == $index ? -1 : $index;
			};
			self.TIMEZONE_ID = DateService.TIMEZONE_ID;
			self.queryAll();
			self.moduleName = '${moduleName}';
		}])
		.filter('convertModule', [function(){
			return function(clz){
				var name = '';
				if(clz == 'com.angrycat.erp.model.Member'){
					name = '會員';
				}
				return name;
			}
		}]);
</script>

</body>
</html>