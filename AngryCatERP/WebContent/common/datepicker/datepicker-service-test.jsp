<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>    
<!DOCTYPE html>
<html ng-app="datepickerTest">
<head>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>Insert title here</title>

	<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap-theme.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap-responsive.css"/>'/>

</head>
<body ng-controller="MainCtrl as mainCtrl">
	<div class="container">
		<div class="row">
			<div class="col-sm-2"><label class="label-control">直接使用AngularJS ui</label></div>
			<div class="col-sm-5">
				<p class="input-group">
            		<input type="text" 
						ng-model="mainCtrl.conditionConfig.conds.condition_pBirthdayEnd"
						datepicker-popup="yyyy-MM-dd"
						is-open="pBirthdayEnd"
						readonly="readonly"
						id="pBirthdayEnd"
						class="form-control">
					<span class="input-group-btn">
						<button type="button" class="btn btn-default" ng-click="mainCtrl.openCalendar($event, 'pBirthdayEnd')"><i class="glyphicon glyphicon-calendar"></i></button>
            		</span>
				</p>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-2"><label class="label-control">用指令封裝AngularJS ui相關邏輯</label></div>
			<div class="col-sm-5">
				<datepicker-input 
					datepicker-id="pBirthdayStart" 
					erp-model-name="mainCtrl.conditionConfig.conds.condition_pBirthdayStart"
					format="yyyy-MM-dd"></datepicker-input>
			</div>
			<div class="col-sm-2">
				<span ng-bind="mainCtrl.conditionConfig.conds.condition_pBirthdayStart"></span>
			</div>
		</div>		
	</div>


	<script type="text/javascript" src='<c:url value="/angularjs/1.4.3/angular.js"/>'></script>
	<script type="text/javascript" src='<c:url value="/angularjs/ui-bootstrap-tpls-0.13.0.min.js"/>'></script>
	<script type="text/javascript" src='<c:url value="/common/datepicker/datepicker-service.js"/>'></script>
	<script type="text/javascript" src='<c:url value="/common/datepicker/datepicker-directive.js"/>'></script>
	<script type="text/javascript">
		angular.module('datepickerTest', ['ui.bootstrap', 'erp.datepicker.service', 'erp.datepicker.directive'])
			.controller('MainCtrl', ['DatepickerService', '$scope', function(DatepickerService, $scope){
				var self = this;
				self.openCalendar = function($event, opened){
					DatepickerService.openCalendar($event, opened);
				};
			}]);
	</script>
</body>
</html>