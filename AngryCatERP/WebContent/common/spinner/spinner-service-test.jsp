<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>    
<!DOCTYPE html>
<html ng-app="spinnerServiceTest">
<head>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>Insert title here</title>

	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.1.1/css/bootstrap.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.1.1/css/bootstrap-theme.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.1.1/css/bootstrap-responsive.css"/>'/>

	<link rel="stylesheet" href='<c:url value="/common/spinner/spinner.css"/>'/>
</head>
<body ng-controller="MainCtrl as mainCtrl">
	<div class="container">
		<div class="row">
			<div class="col-sm-4">
				<div class="page-header">
					<h1>First Row</h1>
				</div>
			</div>
		</div>
		<div class="row" id="eleMaskBlock">
			<div class="col-sm-4">
				<div class="page-header">
					<h1>Element Mask Block</h1>
				</div>
				<p>dddddddddddssssssssssffffffffff</p>
				<p>ffffffffffffggggggggggggggggggg</p>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-3">
				<button type="button" ng-click="mainCtrl.testStartMask()">Start Global Mask</button>
			</div>
			<div class="col-sm-3">
				<button type="button" ng-click="mainCtrl.testStopMask()">Stop Global Mask</button>
			</div>
			<div class="col-sm-3">
				<button type="button" ng-click="mainCtrl.testAjaxGlobalMask()">Ajax Global Mask</button>
			</div>
			<div class="col-sm-3">
				<div class="col-sm-6">
					<button type="button" ng-click="mainCtrl.testStartElementMask()">Start Element Mask</button>
				</div>
				<div class="col-sm-6">
					<button type="button" ng-click="mainCtrl.testStopElementMask()">Stop Element Mask</button>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-3">
				<button type="button" class="btn btn-default" ng-click="mainCtrl.testAjaxGlobalSpinInterceptor()">Start Global Mask Through Interceptor</button>
			</div>
		</div>
	</div>


	<script type="text/javascript" src='<c:url value="/vendor/angularjs/1.4.3/angular.js"/>'></script>
	<script type="text/javascript" src='<c:url value="/common/spinner/spinner-service.js"/>'></script>
	<script type="text/javascript">
		angular.module('spinnerServiceTest', ['erp.spinner'])
			.config(['$httpProvider', function($httpProvider){
				$httpProvider.interceptors.push('AjaxGlobalSpinInterceptor');
			}])
			.controller('MainCtrl', ['GlobalSpin', '$http', 'ElementSpin', function(GlobalSpin, $http, ElementSpin){
				var self = this,
					testUrl = '${pageContext.request.contextPath}/test';
				self.testStartMask = function(){
					GlobalSpin.startMask();
				};
				self.testStopMask = function(){
					GlobalSpin.stopMask();
				};
				self.testAjaxGlobalMask = function(){
					GlobalSpin.startMask();
					$http.post(testUrl)
						.then(function(){
							GlobalSpin.stopMask();
						},function(){
							GlobalSpin.stopMask();
						});
				};
				var eleId = 'eleMaskBlock';
				self.testStartElementMask = function(){
					ElementSpin.startMask({eleId: eleId});
				};
				self.testStopElementMask = function(){
					ElementSpin.stopMask({eleId: eleId});
				};
				self.testAjaxGlobalSpinInterceptor = function(){
					$http.post(testUrl)
						.then(function(){
							
						},function(){
							
						})
				};
			}]);
	</script>
</body>
</html>