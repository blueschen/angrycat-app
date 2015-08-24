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

<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.1.1/css/bootstrap.css"/>'/>
<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.1.1/css/bootstrap-theme.css"/>'/>
<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.1.1/css/bootstrap-responsive.css"/>'/>

	<script type="text/javascript">		
		<%@ include file="/vendor/angularjs/1.4.3/angular.min.js" %>
		<%@ include file="/vendor/angularjs/1.4.3/i18n/angular-locale_zh-tw.js" %>
		<%@ include file="/common/ajax/ajax-service.js" %>
		<%@ include file="/common/date/date-service.js" %>
	</script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.tpl.min.js"/>"></script>

	<style type="text/css">
	.form-horizontal .control-label.text-left{
    text-align: left;
}
	</style>
</head>
<body ng-controller="MainCtrl as mainCtrl">
<input type="hidden" value="{{mainCtrl.member.id}}"/>


<div class="container">
	<div class="col-sm-offset-2">
		<h2>會員資料</h2>
	</div>
<form class="form-horizontal" name="memberForm">
 	<div class="form-group">
		<div class="form-group col-sm-5" ng-class="{'has-error': memberForm.birthday.$invalid}">
 			<label class="col-sm-5 control-label" for="birthday" >
 				出生年月日
 			</label>
 			<div class="col-sm-7">
 				<input id="birthday" 
 					class="form-control" 
 					ng-model="mainCtrl.member.birthday" 
 					name="birthday" 
 					bs-datepicker 
 					type="text" 
 					autoclose="1"
 					date-format="yyyy-MM-dd"
 					placeholder="yyyy-MM-dd">			      
 			</div>		
		</div>
		<div class="form-group col-sm-5">
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
 	</div>
	<div class="form-group">
		<div class="form-group col-sm-5">
			<label class="col-sm-5 control-label">
 				Ohm VIP
 			</label>
 			<div class="col-sm-7">
 				<label class="radio-inline">
 					<input type="radio" ng-value="true" ng-model="mainCtrl.member.important" id="importantYes" ng-disabled="true"/>是
 				</label>
 				<label class="radio-inline">
 					<input type="radio" ng-value="false" ng-model="mainCtrl.member.important" id="importantNo" ng-disabled="true"/>否
 				</label>
 			</div>
		</div>
		<div class="form-group col-sm-5" ng-class="{'has-error': memberForm.toVipDate.$invalid}">
 			<label class="col-sm-3 control-label" for="toVipDate">
 				轉VIP日期
 			</label>
 			<div class="col-sm-7">
 				<p class="form-control-static">
 				{{mainCtrl.member.toVipDate}}
 				</p>
 			</div>
		</div>
 	</div>
 	<div class="form-group">
 		<div class="form-group col-sm-5">
			<label class="col-sm-5 control-label" for="fbNickname">
 				FB暱稱
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.member.fbNickname" id="fbNickname" class="form-control" autofocus/>
 			</div>
 		</div>
 		<div class="form-group col-sm-5">
			<label class="col-sm-3 control-label" for="name">
 				姓名
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.member.name" id="name" class="form-control"/>
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="form-group col-sm-5" ng-class="{'has-error': memberForm.idNo.$invalid}">
 			<label class="col-sm-5 control-label" for="idNo">
 				身分證字號
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.member.idNo" id="idNo" name="idNo" required  class="form-control"/>
 			</div> 		
 		</div>
 		<div class="form-group col-sm-5">
 			<label class="col-sm-3 control-label" for="email">
 				電子信箱
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.member.email" id="email" name="email" class="form-control"/>
 			</div> 		
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="form-group col-sm-5">
			<label class="col-sm-5 control-label" for="mobile">
 				聯絡電話	
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.member.mobile" id="mobile" class="form-control"/>
 			</div>	
 		</div>
		<div class="form-group col-sm-5">
			<label class="col-sm-3 control-label" for="postalCode">
 				郵遞區號	
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.member.postalCode" id="postalCode" class="form-control"/>
 			</div>
		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-2 control-label" for="address">
 			地址	
 		</label>
 		<div class="col-sm-5">
 			<input type="text" ng-model="mainCtrl.member.address" id="address" class="form-control"/>
 		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-2 control-label" for="note">
 			備註	
 		</label>
 		<div class="col-sm-5">
 			<textarea ng-model="mainCtrl.member.note" id="note" rows="3" cols="30"  class="form-control"></textarea>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-3">
 			<input type="button" value="{{mainCtrl.member.id | saveOrModify}}" ng-click="mainCtrl.save()" class="btn btn-default" ng-disabled="memberForm.$invalid"/>
 			<input type="button" value="關閉" onclick="document.location.href='${urlPrefix}/list'" class="btn btn-default"/>
 			<button type="button" class="btn btn-default" ng-click="mainCtrl.addMemberDiscount()">
				增加VIP紀錄
			</button>
 		</div>
 	</div>
</form>

<div id="discountDetils" ng-repeat="detail in mainCtrl.member.vipDiscountDetails">
		<form class="form-inline">
			<input type="hidden" ng-value="detail.id" ng-model="detail.id"/>
			<input type="hidden" ng-value="detail.memberIdNo" ng-model="detail.memberIdNo"/>
			<input type="hidden" ng-value="detail.toVipDate" ng-model="detail.toVipDate"/>
			<input type="hidden" ng-value="detail.memberId" ng-model="detail.memberId"/>
			<div class="form-group">
				<label for="effectiveStart{{$index}}">有效起日</label>
				<input id="effectiveStart{{$index}}" type="text" class="form-control" ng-model="detail.effectiveStart" readonly="readonly"/>
			</div>
			<div class="form-group">
				<label for="effectiveEnd{{$index}}">有效迄日</label>
				<input id="effectiveEnd{{$index}}" type="text" class="form-control" ng-model="detail.effectiveEnd" readonly="readonly"/>
			</div>
			<div class="form-group">
				<label for="useStatus{{$index}}">使用狀態</label>
					<select
						ng-model="detail.useStatus"
						ng-options="u.value as u.label for u in mainCtrl.useOptions"
						class="form-control"
						ng-change="mainCtrl.changeDiscountUseDateToToday(detail)"
						ng-disabled="detail.useStatus != '可使用'"
						id="useStatus{{$index}}"
					>
						<option value="">====</option>
					</select>
			</div>
			<div class="form-group">
				<label for="discountUseDate{{$index}}">使用日期</label>
				<input type="text" ng-model="detail.discountUseDate" class="form-control" readonly="readonly" id="discountUseDate{{$index}}"/>
			</div>
			<div class="form-group" ng-show="mainCtrl.showRemoveBtn(detail)">
				<button type="button" class="btn btn-default" ng-click="mainCtrl.removeDetail(detail)"><span class="glyphicon glyphicon-remove"></span></button>
			</div>
		</form>			
</div>

</div>
<script type="text/javascript">
	angular.module('angryCatMemberViewApp', ['erp.date.service', 'erp.ajax.service', 'mgcrea.ngStrap'])
		.controller('MainCtrl', ['$scope', 'DateService', 'AjaxService', function($scope, DateService, AjaxService){
			var self = this,
				saveUrl = '${urlPrefix}/save.json',
				updateMemberDiscountUrl = '${urlPrefix}/updateMemberDiscount.json',
				targetData = '${member}',
				addCount = 0,
				ADD_COUNT_MAX = 2;
			
			if(targetData){
				self.member = JSON.parse(targetData);
			}
			self.actionMsg = function (input){
				return input ? '儲存': '修改';
			};
			self.save = function(){
				var isNew = self.member.id ? false : true;
				AjaxService.post(saveUrl, self.member)
					.then(function(response){
						self.member = response.data;
						alert(self.actionMsg(isNew)+'成功!!');
					},
					function(errResponse){
						alert(self.actionMsg(isNew)+'失敗，錯誤訊息: ' + JSON.stringify(errResponse));
					});	
			};
			self.addMemberDiscount = function(){
				if(!self.member){
					self.member = {};
				}
				if(addCount >= ADD_COUNT_MAX){
					alert('單筆消費VIP最大延續'+ADD_COUNT_MAX+'年，已超過上限!!');
					return;
				}
				self.updateMemberDiscount(function(){
					++addCount;
				});
			};
			self.updateMemberDiscount = function(success, fail){
				if(!self.member || !self.member.birthday){
					alert('生日必填');
					return;
				}
				
				AjaxService.post(updateMemberDiscountUrl, self.member)
					.then(function(response){
						self.member = response.data;
						if(success){
							success();
						}
					}, function(responseErr){
						if(fail){
							fail();
						}
					});
				
			};
			self.changeDiscountUseDateToToday = function(detail){
				if(detail.useStatus != '已使用'){
					detail.discountUseDate = DateService.toTodayString();
				}
			}
			self.useOptions = [{label: '可使用', value: '可使用'}, {label: '已用過', value: '已用過'}, {label: '已過期', value: '已過期'}, {label: '尚未到有效期限', value: '尚未到有效期限'}, {label: '尚未到可用期間', value: '尚未到可用期間'}];
			self.removeDetail = function(detail){
				var idx = self.member.vipDiscountDetails.indexOf(detail);
				self.member.vipDiscountDetails.splice(idx, 1);
				if(self.member.vipDiscountDetails.length > 0){
					self.member.toVipEndDate = self.member.vipDiscountDetails[0].effectiveEnd; 
				}else{
					self.member.toVipDate = null;
					self.member.toVipEndDate = null;
					self.member.important = false;
				}
				addCount--;
			};
			self.showRemoveBtn = function(detail){
				var idx = self.member.vipDiscountDetails.indexOf(detail);
				return idx == 0 && !detail.discountUseDate && new Date(detail.effectiveEnd) >= new Date();
			}
		}])
		.filter('saveOrModify', [function(){
			return function(input){
				return input ? '修改': '儲存';
			};
		}])
		;
</script>
</body>
</html>