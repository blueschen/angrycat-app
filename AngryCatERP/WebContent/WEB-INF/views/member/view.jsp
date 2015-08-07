<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="member" var="modelName"/>
<c:set value="${pageContext.request.contextPath}/${modelName}" var="urlPrefix"/>
<!DOCTYPE html>
<html ng-app="angryCatMemberViewApp">
<head>
<meta content="width=device-width, initial-scale=1.0" name="viewport">
<title><s:message code="model.name.${modelName}"/></title>

<script type="text/javascript" src='<c:url value="/vendor/angularjs/1.4.3/angular.min.js"/>'></script>
<script type="text/javascript" src='<c:url value="/vendor/angular-bootstrap/ui-bootstrap-tpls-0.13.0.min.js"/>'></script>

<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.1.1/css/bootstrap.css"/>'/>
<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.1.1/css/bootstrap-theme.css"/>'/>
<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.1.1/css/bootstrap-responsive.css"/>'/>

</head>
<body ng-controller="MainCtrl as mainCtrl">
<input type="hidden" value="{{mainCtrl.member.id}}"/>


<div class="container">
	<div class="col-sm-offset-2">
		<h2>會員資料</h2>
	</div>
<form class="form-horizontal" name="memberForm">
	<div class="form-group">
		<label class="col-sm-3 control-label" for="toVipDate">
 			轉VIP日期
 		</label>
 		<div class="col-sm-5">
 			<p class="input-group">
				<input type="text"
					ng-model="mainCtrl.member.toVipDate"
					datepicker-popup="yyyy-MM-dd"
					is-open="opened2"
					readonly="readonly"
					id="toVipDate"
					name="toVipDate"
					class="form-control">
				<span class="input-group-btn">
					<button type="button" class="btn btn-default" ng-click="mainCtrl.openCalendar($event, 'opened2')"><i class="glyphicon glyphicon-calendar"></i></button>
				</span>
 			</p>
 		</div>
 		<div class="col-sm-2">
 			<span>
				VIP有效時間:&nbsp;&nbsp;{{mainCtrl.member.toVipDate | oneYearLater}}
			</span>
 		</div>
 	</div>
	<div class="form-group">
		<label class="col-sm-3 control-label">
 			Ohm VIP
 		</label>
 		<div class="col-sm-7">
 			<label class="radio-inline">
 				<input type="radio" ng-value="true" ng-model="mainCtrl.member.important" id="importantYes"/>是
 			</label>
 			<label class="radio-inline">
 				<input type="radio" ng-value="false" ng-model="mainCtrl.member.important" id="importantNo"/>否
 			</label>
 		</div>
 	</div>
 	<div class="form-group">
 		 <label class="col-sm-3 control-label" for="vipDiscountUseDate">
 			VIP優惠使用時間
 		</label>
 		<div class="col-sm-5">
			<p class="input-group">
				<input type="text" 
					ng-model="mainCtrl.member.vipDiscountUseDate"
					datepicker-popup="yyyy-MM-dd"
					is-open="opened3"
					readonly="readonly"
					id="vipDiscountUseDate"
					name="vipDiscountUseDate"
					class="form-control"/>
			<span class="input-group-btn">
				<button type="button" class="btn btn-default" ng-click="mainCtrl.openCalendar($event, 'opened3')"><i class="glyphicon glyphicon-calendar"></i></button>
			</span>
			</p>       
 		</div>
 		<div class="col-sm-2">
 			<span>
				VIP優惠使用狀態:&nbsp;&nbsp;{{mainCtrl.member.vipDiscountUseDate | vipDiscountUseStatus: mainCtrl.member.toVipDate}}
			</span>
 		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-3 control-label" for="fbNickname">
 			FB暱稱
 		</label>
 		<div class="col-sm-7">
 			<input type="text" ng-model="mainCtrl.member.fbNickname" id="fbNickname" class="form-control"/>
 		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-3 control-label">
 			性別
 		</label>
		<div class="col-sm-7">
			<label class="radio-inline">
 				<input type="radio" ng-value="0" ng-model="mainCtrl.member.gender" id="genderMale"/>男
 			</label>
 			<label class="radio-inline">
 				<input type="radio" ng-value="1" ng-model="mainCtrl.member.gender" id="genderFemale"/>女
 			</label>
		</div>
 	</div>
 	<div class="form-group">
 		 <label class="col-sm-3 control-label" for="name">
 			姓名
 		</label>
 		<div class="col-sm-7">
 			<input type="text" ng-model="mainCtrl.member.name" id="name" class="form-control"/>
 		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-3 control-label" for="idNo">
 			身分證字號
 		</label>
 		<div class="col-sm-7">
 			<input type="text" ng-model="mainCtrl.member.idNo" id="idNo" name="idNo" required  class="form-control"/>
 			<span ng-show="memberForm.idNo.$error.required">必填</span>
 		</div>
 	</div>
 	<div class="form-group">
 		 <label class="col-sm-3 control-label" for="email">
 			電子信箱
 		</label>
 		<div class="col-sm-7">
 			<input type="email" ng-model="mainCtrl.member.email" id="email" name="email" class="form-control"/>
 			<span ng-show="memberForm.email.$invalid">email格式不正確</span>
 		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-3 control-label" for="birthday">
 			出生年月日
 		</label>
 		<div class="col-sm-5">			
			<p class="input-group">
				<input type="text" 
					ng-model="mainCtrl.member.birthday"
					datepicker-popup="yyyy-MM-dd"
					is-open="opened1"
					readonly="readonly"
					id="birthday"
					name="birthday"
					class="form-control"/>
				<span class="input-group-btn">
					<button type="button" class="btn btn-default" ng-click="mainCtrl.openCalendar($event, 'opened1')"><i class="glyphicon glyphicon-calendar"></i></button>
				</span> 
			</p>       
 		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-3 control-label" for="mobile">
 			聯絡電話	
 		</label>
 		<div class="col-sm-7">
 			<input type="text" ng-model="mainCtrl.member.mobile" id="mobile" class="form-control"/>
 		</div>
 	</div>
 	<div class="form-group">
 		 <label class="col-sm-3 control-label" for="postalCode">
 			郵遞區號	
 		</label>
 		<div class="col-sm-7">
 			<input type="text" ng-model="mainCtrl.member.postalCode" id="postalCode" class="form-control"/>
 		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-3 control-label" for="address">
 			地址	
 		</label>
 		<div class="col-sm-7">
 			<input type="text" ng-model="mainCtrl.member.address" id="address" class="form-control"/>
 		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-3 control-label" for="note">
 			備註	
 		</label>
 		<div class="col-sm-7">
 			<textarea ng-model="mainCtrl.member.note" id="note" rows="3" cols="30"  class="form-control"></textarea>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-3">
 			<input type="button" value="{{mainCtrl.member.id | saveOrModify}}" ng-click="mainCtrl.save()" class="btn btn-default" ng-disabled="memberForm.$invalid && !memberForm.$error.date"/>
 			<input type="button" value="關閉" onclick="document.location.href='${urlPrefix}/list'" class="btn btn-default"/>
 		</div>
 	</div>
</form>

</div>
<script type="text/javascript">
	angular.module('angryCatMemberViewApp', ['ui.bootstrap'])
		.factory('DateService', [function(){
			var addYear = function(date, yearCount){
				if(!date || !yearCount){
					return null;
				}
				var time = new Date(date.setFullYear(date.getFullYear() + yearCount));
				return time;
			};
			return {
				parseDateSplittedByDash: function(d){
					year = d.getFullYear(),
					month = (d.getMonth()+1),
					date = d.getDate(),
					time = year + '-' + (month > 9 ? month : ('0' + month)) + '-' + (date > 9 ? date : ('0' + date));
					return time;
				},
				addYear: addYear,
				addOneYear: function(date){
					return addYear(date, 1)
				}
			};
		}])
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
		.directive('datepickerPopup', ['DateService', function (DateService) {
  			function link(scope, element, attrs, ngModel) {
    			// View -> Model
    			ngModel.$parsers.push(function (value) {
    				if(!value){
    					return null;
    				}
    				var d = new Date(Date.parse(value)),
    					time = DateService.parseDateSplittedByDash(d);
      				return time;
    			});
    		}
  			return {
    			restrict: 'A',
    			require: 'ngModel',
    			link: link
  			};
		}])
		.filter('saveOrModify', [function(){
			return function(input){
				return input ? '修改': '儲存';
			};
		}])
		.filter('oneYearLater', ['DateService', function(DateService){
			return function(dateStr){
				if(!dateStr){
					return null;
				}
				var baseDate = new Date(Date.parse(dateStr));
					deadLine = new Date(baseDate.setFullYear(baseDate.getFullYear()+1)),
					time = DateService.parseDateSplittedByDash(deadLine);
				return time;
			};
		}])
		.filter('vipDiscountUseStatus', ['DateService', function(DateService){
			return function(vipDiscountUseDateStr, baseDateStr){
				if(!baseDateStr){
					return '尚未成為VIP!!';
				}
				var baseDate = new Date(baseDateStr),
					deadLine = DateService.addOneYear(baseDate);
				if(!vipDiscountUseDateStr){
					var today = new Date();
					if(today > deadLine){
						return 'VIP優惠已過期';
					}else{
						return '該年度VIP優惠尚未使用';
					}
				}
				var vipDiscountUseDate = new Date(vipDiscountUseDateStr);
				// alert('vipDiscountUseDate: ' + vipDiscountUseDate + ', baseDate: ' + baseDate);
				var msg = '';
				if(vipDiscountUseDate <= baseDate){
					msg = '該年度VIP優惠尚未使用';
				}else if(vipDiscountUseDate > baseDate && vipDiscountUseDate < deadLine){
					msg = '該年度VIP優惠已使用!!';
				}else{// vipDiscountUseDate >= dearLine
					msg = '超過VIP優惠使用期限!!'
				}
				return msg;
			};
		}]);
</script>
</body>
</html>