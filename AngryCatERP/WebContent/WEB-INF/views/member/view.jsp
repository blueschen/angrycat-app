<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<html ng-app="angryCatMemberViewApp">
<head>
<meta content="width=device-width, initial-scale=1.0" name="viewport">
<title><s:message code="model.name.member"/></title>
<script type="text/javascript" src='<c:url value="/jquery/2.1.1/jquery.min.js"/>'></script>
<script type="text/javascript" src='<c:url value="/angularjs/1.3.16/angular.js"/>'></script>
<script type="text/javascript" src='<c:url value="/angularjs/ui-bootstrap-tpls-0.13.0.min.js"/>'></script>
<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap.css"/>'/>
<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap-theme.css"/>'/>
<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap-responsive.css"/>'/>

<c:set value="member" var="modelName"/>
<c:set value="${pageContext.request.contextPath}/${modelName}" var="urlPrefix"/>
</head>
<body ng-controller="MainCtrl as mainCtrl">
<input type="hidden" value="{{mainCtrl.member.id}}"/>


<div class="container-fluid">
<div class="bs-docs-grid">
<div class="row show-grid">
	<div class="span2">
		Ohm VIP
	</div>
	<div class="span10">
		<input type="radio" ng-value="true" ng-model="mainCtrl.member.important"/>是
		<input type="radio" ng-value="false" ng-model="mainCtrl.member.important"/>否
	</div>
</div>
<div class="row show-grid">
	<div class="span2">
		FB暱稱
	</div>
	<div class="span10">
		<input type="text" ng-model="mainCtrl.member.fbNickname"/>
	</div>
</div>
<div class="row show-grid">
	<div class="span2">
		性別
	</div>
	<div class="span10">
		<input type="radio" ng-value="0" ng-model="mainCtrl.member.gender"/>男
		<input type="radio" ng-value="1" ng-model="mainCtrl.member.gender"/>女
	</div>
</div>
<div class="row show-grid">
	<div class="span2">
		姓名
	</div>
	<div class="span10">		
		<input type="text" ng-model="mainCtrl.member.name"/>
	</div>
</div>
<div class="row show-grid">
	<div class="span2">
		身分證字號
	</div>
	<div class="span10">
		<input type="text" ng-model="mainCtrl.member.idNo"/>
	</div>
</div>
<div class="row show-grid">
	<div class="span2">
		電子信箱
	</div>
	<div class="span10">
		<input type="text" ng-model="mainCtrl.member.email"/>
	</div>
</div>
<div class="row show-grid">
	<div class="span2">
		出生年月日
	</div>
	<div class="span10">
		<p class="input-group">
			<input type="text" 
				ng-model="mainCtrl.member.birthday"
				datepicker-popup="yyyy-MM-dd"
				is-open="opened1"
				readonly="readonly"
				class="form-control"/>
			<span class="input-group-btn">
				<button type="button" class="btn btn-default" ng-click="mainCtrl.openCalendar($event, 'opened1')"><i class="glyphicon glyphicon-calendar"></i></button>
			</span>
        </p>
	</div>
</div>
<div class="row show-grid">
	<div class="span2">
		聯絡電話
	</div>
	<div class="span10">
		<input type="text" ng-model="mainCtrl.member.mobile"/>
	</div>
</div>
<div class="row show-grid">
	<div class="span2">
		郵遞區號
	</div>
	<div class="span10">
		<input type="text" ng-model="mainCtrl.member.postalCode"/>
	</div>
</div>
<div class="row show-grid">
	<div class="span2">		
		地址
	</div>
	<div class="span10">
		<input type="text" ng-model="mainCtrl.member.address"/>
	</div>
</div>
<div class="row show-grid">
	<div class="span2">
		轉VIP日期
	</div>
	<div class="span10">
		<p class="input-group">
			<input type="text"
				ng-model="mainCtrl.member.toVipDate"
				datepicker-popup="yyyy-MM-dd"
				is-open="opened2"
				readonly="readonly"
				class="form-control">
			<span class="input-group-btn">
				<button type="button" class="btn btn-default" ng-click="mainCtrl.openCalendar($event, 'opened2')"><i class="glyphicon glyphicon-calendar"></i></button>
			</span>
        </p>
	</div>
</div>
<div class="row show-grid">
	<div class="span2">
		備註
	</div>
	<div class="span10">
		<textarea ng-model="mainCtrl.member.note">
		</textarea>
	</div>
</div>
</div>	
	
<input type="button" value="儲存" ng-click="mainCtrl.save()"/>
<input type="button" value="關閉" onclick="document.location.href='${urlPrefix}/list'"/>	
</div>
<script type="text/javascript">
	angular.module('angryCatMemberViewApp', ['ui.bootstrap'])
		.controller('MainCtrl', ['$http', '$scope', function($http, $scope){
			var self = this,
				saveUrl = '${urlPrefix}/save.json';
			
			self.save = function(){
				var isNew = self.member.id ? false : true;
				$http.post(saveUrl, self.member)
					.then(function(response){
						self.member = response.data;
						alert((isNew ? '儲存' : '修改')+'成功!!');
					},
					function(errResponse){
						alert((isNew ? '儲存' : '修改')+'失敗，錯誤訊息: ' + JSON.stringify(errResponse));
					});	
			};
			// date related
			self.openCalendar = function($event, opened){
			    $event.preventDefault();
			    $event.stopPropagation();
			    
			    $scope[opened] = true;
			};
		}])
		.directive('datepickerPopup', function () {
  			function link(scope, element, attrs, ngModel) {
    			// View -> Model
    			ngModel.$parsers.push(function (value) {
    				if(!value){
    					return null;
    				}
    				var d = new Date(Date.parse(value)),
    					year = d.getFullYear(),
    					month = (d.getMonth()+1),
    					date = d.getDate(),
    					time = year + '-' + (month > 9 ? month : ('0' + month)) + '-' + (date > 9 ? date : ('0' + date));
    				console.log('ori time value: ' + value);
      				return time;
    			});
    		}
  			return {
    			restrict: 'A',
    			require: 'ngModel',
    			link: link
  			};
		});
</script>
</body>
</html>