<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="member" var="modelName"/>
<c:set value="${pageContext.request.contextPath}/${modelName}" var="urlPrefix"/>

<html ng-app="angryCatMemberViewApp">
<head>
<meta content="width=device-width, initial-scale=1.0" name="viewport">
<title><s:message code="model.name.${modelName}"/></title>
<script type="text/javascript" src='<c:url value="/jquery/2.1.1/jquery.min.js"/>'></script>
<script type="text/javascript" src='<c:url value="/angularjs/1.3.16/angular.js"/>'></script>
<script type="text/javascript" src='<c:url value="/angularjs/ui-bootstrap-tpls-0.13.0.min.js"/>'></script>
<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap.css"/>'/>
<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap-theme.css"/>'/>
<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap-responsive.css"/>'/>

<style type="text/css">
	.my-control-label .control-label{
		text-align: left; 
		width: 10%;
	}
	label {
  		display: inline-block;
  		font-size: 18px;
	}
	input[type='text']{
		width: 20%;
	}
	textarea{
		width: 20%;
	}
</style>
</head>
<body ng-controller="MainCtrl as mainCtrl">
<input type="hidden" value="{{mainCtrl.member.id}}"/>


<div class="container-fluid">

<form class="form-horizontal" name="memberForm">
	<div class="control-group">
 		<div class="controls my-control-label">
 			<label class="control-label label-important">
 				Ohm VIP
 			</label>
 			<input type="radio" ng-value="true" ng-model="mainCtrl.member.important" id="importantYes"/>是
 			<input type="radio" ng-value="false" ng-model="mainCtrl.member.important" id="importantNo"/>否		
 		</div>
 	</div>
 	<div class="control-group">
 		<div class="controls my-control-label">
 			<label class="control-label label-important" for="fbNickname">
 				FB暱稱
 			</label>
 			<input type="text" ng-model="mainCtrl.member.fbNickname" id="fbNickname"/>
 		</div>
 	</div>
 	<div class="control-group">
 		<div class="controls my-control-label">
 			<label class="control-label label-important">
 				性別
 			</label>
 			<input type="radio" ng-value="0" ng-model="mainCtrl.member.gender" id="genderMale"/>男
 			<input type="radio" ng-value="1" ng-model="mainCtrl.member.gender" id="genderFemale"/>女
 		</div>
 	</div>
 	<div class="control-group">
 		<div class="controls my-control-label">
 			<label class="control-label label-important" for="name">
 				姓名
 			</label>
 			<input type="text" ng-model="mainCtrl.member.name" id="name"/>
 		</div>
 	</div>
 	<div class="control-group">
 		<div class="controls my-control-label">
 			<label class="control-label label-important" for="idNo">
 				身分證字號
 			</label>
 			<input type="text" ng-model="mainCtrl.member.idNo" id="idNo" name="idNo" required/>
 			<span ng-show="memberForm.idNo.$error.required">必填</span>
 		</div>
 	</div>
 	<div class="control-group">
 		<div class="controls my-control-label">
 			<label class="control-label label-important" for="email">
 				電子信箱
 			</label>
 			<input type="email" ng-model="mainCtrl.member.email" id="email" name="email"/>
 			<span ng-show="memberForm.email.$invalid">email格式不正確</span>
 		</div>
 	</div>
 	<div class="control-group">
 		<div class="controls my-control-label">
 			<label class="control-label label-important" for="birthday">
 				出生年月日
 			</label>			
			<input type="text" 
				ng-model="mainCtrl.member.birthday"
				datepicker-popup="yyyy-MM-dd"
				is-open="opened1"
				readonly="readonly"
				id="birthday"
				name="birthday"/>
			<span>
				<button type="button" class="btn btn-default" ng-click="mainCtrl.openCalendar($event, 'opened1')"><i class="glyphicon glyphicon-calendar"></i></button>
			</span>        
 		</div>
 	</div>
 	<div class="control-group">
 		<div class="controls my-control-label">
 			<label class="control-label label-important" for="mobile">
 				聯絡電話	
 			</label>
 			<input type="text" ng-model="mainCtrl.member.mobile" id="mobile"/>
 		</div>
 	</div>
 	<div class="control-group">
 		<div class="controls my-control-label">
 			<label class="control-label label-important" for="postalCode">
 				郵遞區號	
 			</label>
 			<input type="text" ng-model="mainCtrl.member.postalCode" id="postalCode"/>
 		</div>
 	</div>
 	<div class="control-group">
 		<div class="controls my-control-label">
 			<label class="control-label label-important" for="address">
 				地址	
 			</label>
 			<input type="text" ng-model="mainCtrl.member.address" id="address"/>
 		</div>
 	</div>
 	<div class="control-group">
 		<div class="controls my-control-label">
 			<label class="control-label label-important" for="toVipDate">
 				轉VIP日期
 			</label>
 			<input type="text"
				ng-model="mainCtrl.member.toVipDate"
				datepicker-popup="yyyy-MM-dd"
				is-open="opened2"
				readonly="readonly"
				id="toVipDate"
				name="toVipDate">
			<span>
				<button type="button" class="btn btn-default" ng-click="mainCtrl.openCalendar($event, 'opened2')"><i class="glyphicon glyphicon-calendar"></i></button>
			</span>
 		</div>
 	</div>
 	<div class="control-group">
 		<div class="controls my-control-label">
 			<label class="control-label label-important" for="note">
 				備註	
 			</label>
 			<textarea ng-model="mainCtrl.member.note" id="note" rows="5" cols="30"></textarea>
 		</div>
 	</div>
 	<div class="control-group">
 		<input type="button" value="{{mainCtrl.member.id | saveOrModify}}" ng-click="mainCtrl.save()" class="btn btn-default" ng-disabled="memberForm.$invalid && !memberForm.$error.date"/>
		<input type="button" value="關閉" onclick="document.location.href='${urlPrefix}/list'" class="btn btn-default"/>	
 	</div>
</form>

	

</div>
<script type="text/javascript">
	angular.module('angryCatMemberViewApp', ['ui.bootstrap'])
		.controller('MainCtrl', ['$http', '$scope', function($http, $scope){
			var self = this,
				saveUrl = '${urlPrefix}/save.json',
				targetData = '${member}';
			if(targetData){
				self.member = JSON.parse(targetData);
			}
			self.actionMsg = function (input){
				return input ? '儲存': '修改';
			};
			self.save = function(){
				var isNew = self.member.id ? false : true;
				$http.post(saveUrl, self.member)
					.then(function(response){
						self.member = response.data;
						alert(self.actionMsg(isNew)+'成功!!');
					},
					function(errResponse){
						alert(self.actionMsg(isNew)+'失敗，錯誤訊息: ' + JSON.stringify(errResponse));
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
      				return time;
    			});
    		}
  			return {
    			restrict: 'A',
    			require: 'ngModel',
    			link: link
  			};
		})
		.filter('saveOrModify', function(){
			return function(input){
				return input ? '修改': '儲存';
			};
		});
</script>
</body>
</html>