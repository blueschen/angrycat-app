<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>


    
<html ng-app="loginApp">
<c:set value="login" var="modelName"/>
<c:set value="${pageContext.request.contextPath}/${modelName}" var="actionUrl"/>
<head>
<meta content="width=device-width, initial-scale=1.0" name="viewport">
<title>登錄頁</title>

<script type="text/javascript" src='<c:url value="/jquery/2.1.1/jquery.min.js"/>'></script>
<script type="text/javascript" src='<c:url value="/angularjs/1.3.16/angular.js"/>'></script>
<script type="text/javascript" src='<c:url value="/angularjs/ui-bootstrap-tpls-0.13.0.min.js"/>'></script>
<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap.css"/>'/>
<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap-theme.css"/>'/>
<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap-responsive.css"/>'/>

</head>
<body ng-controller="MainCtrl as mainCtrl">

<div class="container">
	<alert ng-repeat="alert in alerts" type="{{alert.type}}" close="closeAlert($index)">{{alert.msg}}</alert>
	<form class="form-horizontal" name="loginForm" action="${actionUrl}">
		<div class="control-group">
			<label class="control-label" for="userId">帳號</label>
			<div class="controls">
				<input type="text" id="userId" ng-model="mainCtrl.user.userId" name="userId" required autofocus/>
				<span ng-show="loginForm.userId.$error.required">必填</span>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="password">密碼</label>
			<div class="controls">
				<input type="password" id="password" ng-model="mainCtrl.user.password" name="password" required/>
				<span ng-show="loginForm.password.$error.required">必填</span>
			</div>
		</div>
		<div class="control-group">
			<div class="controls">
				<button type="submit" class="btn btn-default" ng-disabled="loginForm.$invalid">登入</button>
			</div>
		</div>
	</form>
</div>



<script type="text/javascript">
	angular.module('loginApp',['ui.bootstrap'])
		.controller('MainCtrl',['$scope', '$http', function($scope, $http){
			var self = this,
				loginErrMsg = '${loginErrMsg}',
				user = '${user}';
				
			if(loginErrMsg){
				$scope.alerts = [{msg: loginErrMsg, type: 'danger'}];
				$scope.closeAlert = function(idx){
					$scope.alerts.splice(idx, 1);
				};
			}
			if(user){
				self.user = JSON.parse(user);	
			}			
		}]);
</script>
</body>
</html>