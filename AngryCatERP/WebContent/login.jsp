<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<c:set value="login" var="modelName"/>
<c:set value="${pageContext.request.contextPath}/${modelName}" var="actionUrl"/>
<c:set value="${actionUrl}/test" var="testUrl"/>
<c:set value="${actionUrl}/admin" var="adminUrl"/>
<!DOCTYPE html>   
<html ng-app="loginApp">
<head>
<meta content="width=device-width, initial-scale=1.0" name="viewport">
<title>登錄頁</title>


<script type="text/javascript" src='<c:url value="/vendor/angularjs/1.4.3/angular.min.js"/>'></script>
<script type="text/javascript" src='<c:url value="/vendor/angular-bootstrap/ui-bootstrap-tpls-0.13.0.min.js"/>'></script>


<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.1.1/css/bootstrap.css"/>'/>
<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.1.1/css/bootstrap-theme.css"/>'/>
<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.1.1/css/bootstrap-responsive.css"/>'/>

<style type="text/css">
	.align-middle{
		margin-top:10%;
	}
</style>

</head>
<body ng-controller="MainCtrl as mainCtrl">

<div class="container align-middle">
	<alert ng-repeat="alert in alerts" type="{{alert.type}}" close="closeAlert($index)">{{alert.msg}}</alert>
	<form class="form-horizontal" name="loginForm" action="${actionUrl}" method="POST">
		<div class="form-group">
			<label class="control-label col-sm-2 col-sm-offset-2" for="userId">帳號</label>
			<div class="col-sm-4">
				<input type="text" id="userId" ng-model="mainCtrl.user.userId" name="userId" autofocus class="form-control" placeholder="帳號"/>
			</div>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-2 col-sm-offset-2" for="password">密碼</label>
			<div class="col-sm-4">
				<input type="password" id="password" ng-model="mainCtrl.user.password" name="password" class="form-control" placeholder="密碼"/>
			</div>
		</div>
		<div class="form-group">
			<div class="col-sm-4 col-sm-offset-4">
				<button type="submit" class="btn btn-default btn-block" ng-disabled="loginForm.$invalid">登入</button>
			</div>
		</div>
	</form>
	<form  class="form-horizontal" name="testForm" action="${testUrl}" method="POST">
		<div class="form-group">
			<input type="text" name="examineeId" ng-model="mainCtrl.user.userId" class="hidden">
			<input type="password" name="examineePwd" ng-model="mainCtrl.user.password" class="hidden">
			<div class="col-sm-4 col-sm-offset-4">
				<button type="submit" class="btn btn-default btn-block" ng-disabled="testForm.$invalid">考試</button>
			</div>
		</div>
	</form>
	<form class="form-horizontal" name="adminForm" action="${adminUrl}" method="POST">
		<div class="form-group">
			<input type="text" name="adminId" ng-model="mainCtrl.user.userId" class="hidden">
			<input type="password" name="adminPwd" ng-model="mainCtrl.user.password" class="hidden">
			<div class="col-sm-4 col-sm-offset-4">
				<button type="submit" class="btn btn-default btn-block" ng-disabled="adminForm.$invalid">參數設定</button>
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