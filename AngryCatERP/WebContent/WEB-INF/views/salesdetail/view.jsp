<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="${pageContext.request.contextPath}/${modelName}" var="urlPrefix"/>
<!DOCTYPE html>
<html lang="zh-TW" ng-app="angryCatSalesDetailViewApp">
<head>
	<meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	
	<title>銷售明細</title>

	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap-theme.css"/>'/>
    
	<script type="text/javascript">		
		<%@ include file="/vendor/angularjs/1.4.3/angular.min.js" %>
		<%@ include file="/vendor/angularjs/1.4.3/i18n/angular-locale_zh-tw.js" %>
		<%@ include file="/common/ajax/ajax-service.js" %>
		<%@ include file="/common/date/date-service.js" %>
	</script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.tpl.min.js"/>"></script>

	<!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->

	<style type="text/css">
	.form-horizontal .control-label.text-left{
    text-align: left;
}
	</style>
</head>
<body ng-controller="MainCtrl as mainCtrl">
<input type="hidden" value="{{mainCtrl.salesDetail.id}}"/>


<div class="container">
	<div class="col-sm-offset-2">
		<h2>銷售明細資料</h2>
	</div>
<form class="form-horizontal" name="salesDetailForm">
 	<div class="form-group">
		<div class="form-group col-sm-5" ng-class="{'has-error': salesDetailForm.birthday.$invalid}">
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
 					placeholder="yyyy-MM-dd"
 					date-type="string">			      
 			</div>		
		</div>
		<div class="form-group col-sm-5">
 			<label class="col-sm-3 control-label">
 				性別
 			</label>
			<div class="col-sm-7" ng-init="mainCtrl.member.gender=1">
				<label class="radio-inline">
 					<input type="radio" ng-value="0" ng-model="mainCtrl.member.gender" id="genderMale"/>男
 				</label>
 				<label class="radio-inline">
 					<input type="radio" ng-value="1" ng-model="mainCtrl.member.gender" id="genderFemale"/>女
 				</label>
			</div>		
		</div>
 	</div>
	<div class="form-group" ng-if="mainCtrl.login">
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
		<div class="form-group col-sm-5">
 			<label class="col-sm-3 control-label" for="toVipDate">
 				轉VIP日期
 			</label>
 			<div class="col-sm-7">
 				<p class="form-control-static">
 					<span ng-bind="mainCtrl.member.toVipDate"></span>
 				</p>
 			</div>
		</div>
 	</div>
 	<div class="form-group">
 		<div class="form-group col-sm-5" ng-if="mainCtrl.login && mainCtrl.member.id">
 			<label class="col-sm-5 control-label" for="clientId">
 				客戶編號
 			</label>
 			<div class="col-sm-7">
 				<p class="form-control-static">
 					<span ng-bind="mainCtrl.member.clientId" id="clientId"></span>
 				</p>
 			</div> 		
 		</div>
 		<div class="form-group col-sm-5" ng-if="!mainCtrl.member.id">
  			<label class="col-sm-5 control-label" for="country">
 				country
 			</label>
 			<div class="col-sm-7">
 				<select 
					ng-model="mainCtrl.member.clientId" 
					ng-options="a.value as a.label for a in mainCtrl.countries"
					id="country"
				 	class="form-control">
			</select>
 			</div> 			
 		</div>
 		<!--
 		 <div class="col-sm-5">
 			<div ng-if="!mainCtrl.member.clientId"><span style="color:red">請輸入國碼為大寫英文兩碼，阿拉伯數字四碼</span></div>
 			<div ng-if="hintClientId">
 				輸入提示:<span ng-bind="hintClientId"></span>
 			</div>
 			<div ng-if="clientIdDuplicatedWarning">
 				<span style="color:red" ng-bind="clientIdDuplicatedWarning"></span>
 			</div>
 		</div>
 		 -->
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
 		<div class="form-group col-sm-5" ng-class="{'has-error': salesDetailForm.name.$error.required}">
			<label class="col-sm-3 control-label" for="name">
 				姓名<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.member.name" id="name" name="name" class="form-control" ng-required="true"/>
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="idNo">
 				身分證字號
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.member.idNo" id="idNo" name="idNo" class="form-control"/>
 			</div> 		
 		</div>
 		<div class="form-group col-sm-5" ng-class="{'has-error': salesDetailForm.email.$invalid}">
 			<label class="col-sm-3 control-label" for="email">
 				電子信箱
 			</label>
 			<div class="col-sm-7">
 				<input type="email" ng-model="mainCtrl.member.email" id="email" name="email" class="form-control"/>
 			</div> 		
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="form-group col-sm-5" ng-class="{'has-error': mainCtrl.isTelAndMobileNotExisted() || mainCtrl.isNameAndMobileDuplicate() || mainCtrl.isMobileNotNumeric()}">
			<label class="col-sm-5 control-label" for="mobile">
 				手機電話<span style="color:red;">*</span>	
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.member.mobile" ng-model-options="{updateOn: 'default'}" id="mobile" name="mobile" class="form-control" mobile-duplicated ng-pattern="/^[0-9]+$/" ng-disabled="salesDetailForm.name.$error.required"/>
 				<span ng-show="mainCtrl.isNameAndMobileDuplicate()">
 					姓名和手機電話重複
 				</span>
 				 <span ng-show="mainCtrl.isMobileNotNumeric()">
 					手機電話應為數字
 				</span>
 			</div>
 		</div>
		<div class="form-group col-sm-5" ng-class="{'has-error': mainCtrl.isTelAndMobileNotExisted() || mainCtrl.isNameAndTelDuplicate()}">
			<label class="col-sm-3 control-label" for="tel">
 				室內電話<span style="color:red;">*</span>	
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.member.tel" ng-model-options="{updateOn: 'default'}" id="tel" name="tel" class="form-control" tel-duplicated ng-disabled="salesDetailForm.name.$error.required"/>
 				<span class="has-error" ng-show="salesDetailForm.name.$dirty && salesDetailForm.tel.$dirty && salesDetailForm.tel.$error.telDuplicated">
 					姓名和室內電話重複
 				</span>
 			</div>
		</div>
 	</div>
 	<div class="form-group">
 		<div class="form-group col-sm-5" ng-class="{'has-error': salesDetailForm.batchStartDate.$invalid}">
			<label class="col-sm-5 control-label" for="batchStartDate" ng-if="mainCtrl.login">
 				調整VIP起始日	
 			</label>
 			<div class="col-sm-7" ng-if="mainCtrl.login">
 				<input id="batchStartDate" 
 					class="form-control" 
 					ng-model="mainCtrl.discount.batchStartDate" 
 					name="batchStartDate" 
 					bs-datepicker 
 					type="text" 
 					autoclose="1"
 					date-format="yyyy-MM-dd"
 					placeholder="yyyy-MM-dd"
 					date-type="string"> 				
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
 	<div class="form-group" ng-if="false">
 		<div class="form-group col-sm-5" ng-class="{'has-error': salesDetailForm.today.$invalid}">
			<label class="col-sm-5 control-label" for="today">
 				假如今天是	
 			</label>
 			<div class="col-sm-7">
 				<input id="today" 
 					class="form-control" 
 					ng-model="mainCtrl.discount.today" 
 					name="today" 
 					bs-datepicker 
 					type="text" 
 					autoclose="1"
 					date-format="yyyy-MM-dd"
 					placeholder="yyyy-MM-dd"
 					date-type="string"> 				
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
 			<input type="submit" value="儲存" ng-click="mainCtrl.save()" ng-disabled="salesDetailForm.$invalid || mainCtrl.isTelAndMobileNotExisted()" class="btn btn-default"/>
 			<input type="button" value="關閉" onclick="document.location.href='${urlPrefix}/list'" class="btn btn-default" ng-if="mainCtrl.login"/>
 			<button type="button" class="btn btn-default" ng-click="mainCtrl.addMemberDiscount()" ng-if="mainCtrl.login" ng-disabled="salesDetailForm.$invalid || mainCtrl.isTelAndMobileNotExisted()">
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
				<label for="effectiveStart{{$index}}" style="font-size:12px;">VIP有效起始日</label>
				<input id="effectiveStart{{$index}}" type="text" class="form-control" ng-model="detail.effectiveStart" readonly="readonly"/>
			</div>
			<div class="form-group">
				<label for="effectiveEnd{{$index}}" style="font-size:12px;">VIP有效結束日</label>
				<input id="effectiveEnd{{$index}}" type="text" class="form-control" ng-model="detail.effectiveEnd" readonly="readonly"/>
			</div>
			<div class="form-group">
				<label for="useStatus{{$index}}" style="font-size:12px;">生日折扣使用狀態</label>
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
				<label for="discountUseDate{{$index}}" style="font-size:12px;">生日折扣使用日期</label>
				<input type="text" ng-model="detail.discountUseDate" class="form-control" readonly="readonly" id="discountUseDate{{$index}}"/>
			</div>
			<div class="form-group" ng-show="mainCtrl.showRemoveBtn(detail)">
				<button type="button" class="btn btn-default" ng-click="mainCtrl.removeDetail(detail)"><span class="glyphicon glyphicon-remove"></span></button>
			</div>
		</form>			
</div>

</div>
<script type="text/javascript">
	angular.module('angryCatSalesDetailViewApp', ['erp.date.service', 'erp.ajax.service', 'mgcrea.ngStrap'])
		.constant('urlPrefix', '${urlPrefix}')
		.constant('login', "${sessionScope['sessionUser']}" ? true : false)
		.constant('targetData', ${member == null ? "null" : member})
		.constant('displayJsonCountries', ${displayJsonCountries == null ? "null" : displayJsonCountries})
		.controller('MainCtrl', ['$scope', 'DateService', 'AjaxService', 'urlPrefix', 'login', 'targetData', 'displayJsonCountries', function($scope, DateService, AjaxService, urlPrefix, login, targetData, displayJsonCountries){
			var self = this,
				saveUrl = urlPrefix + '/save.json',
				updateMemberDiscountUrl = urlPrefix + '/updateMemberDiscount.json',
				addCount = 0,
				ADD_COUNT_MAX = 2;
			self.discount = {today: DateService.toTodayString()};
			self.member = {};
			
			if(targetData){
				self.member = targetData;
			}
			if(displayJsonCountries){
				self.countries = displayJsonCountries;
				if(!targetData){
					self.member = {};
					self.member.clientId = 'TW';
				}else if(!self.member.id){
					self.member.clientId = 'TW';
				}
			}
			self.isInVipEffectiveDur = function(){
				if(!self.member.toVipDate || !self.member.toVipEndDate){
					self.member.important = false;
					return;
				}
				if(new Date(self.member.toVipEndDate) >= new Date(DateService.toTodayString())){
					self.member.important = true;
				}else{
					self.member.important = false;
				}
			};
			self.isInVipEffectiveDur();
			self.save = function(){
				var isNew = self.member.id ? false : true;
				console.log(JSON.stringify(self.member.note));
				AjaxService.post(saveUrl, self.member)
					.then(function(response){
						self.member = response.data;
						alert('儲存成功!!');
					},
					function(errResponse){
						alert('儲存失敗，錯誤訊息: ' + JSON.stringify(errResponse));
					});
			};
			self.addMemberDiscount = function(){
				if(addCount >= ADD_COUNT_MAX){
					alert('單筆消費VIP最大延續'+ADD_COUNT_MAX+'年，已超過上限!!');
					return;
				}
				self.updateMemberDiscount()
					.then(function(){
						++addCount;
					});
			};
			self.updateMemberDiscount = function(){
				if(!self.member.birthday){
					alert('生日必填');
					return;
				}
				
				return AjaxService.post(urlPrefix + '/updateDiscountParam.json', self.discount)
					.then(function(response){
						return AjaxService.post(updateMemberDiscountUrl, self.member);
					})
					.then(function(response){
						self.member = response.data;
					});
				
			};
			self.getMockTodayOrNew = function(){return self.discount.today ? self.discount.today : DateService.toTodayString()};
			self.changeDiscountUseDateToToday = function(detail){
				if(detail.useStatus != '已使用'){
					detail.discountUseDate = self.getMockTodayOrNew();
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
				if(addCount > 0){
					addCount--;	
				}
			};
			self.showRemoveBtn = function(detail){
				var idx = self.member.vipDiscountDetails.indexOf(detail);
				return idx == 0 && !detail.discountUseDate && new Date(detail.effectiveEnd) >= new Date(self.getMockTodayOrNew());
			};
			self.login = login;
			self.isTelAndMobileNotExisted = function(){
				return !$scope.mainCtrl.member.mobile && !$scope.mainCtrl.member.tel;
			};
			self.isNameAndMobileDuplicate = function(){
				return $scope.salesDetailForm.name.$dirty && $scope.salesDetailForm.mobile.$dirty && $scope.salesDetailForm.mobile.$error.mobileDuplicated;
			};
			self.isNameAndTelDuplicate = function(){
				return $scope.salesDetailForm.name.$dirty && $scope.salesDetailForm.tel.$dirty && $scope.salesDetailForm.tel.$error.telDuplicated;
			};
			self.isMobileNotNumeric = function(){
				return $scope.salesDetailForm.mobile.$dirty && $scope.salesDetailForm.mobile.$error.pattern;
			};
		}])
		.factory('ValidateService', ['$log', 'AjaxService', 'urlPrefix', '$window', function($log, AjaxService, urlPrefix, $window){
				return {
					multiCondsValidateDirectiveDefProto: function(validationName){
						return{
							restrict: 'A',
							require: 'ngModel',
							link: function($scope, ele, attrs, ngModelCtrl){
								$scope.$watch(attrs.ngModel, function(newVal, oldVal){
									if(!newVal || newVal == oldVal || $scope.salesDetailForm.name.$error.required){
										return;
									}
									var memberName = $scope.mainCtrl.member.name;

									AjaxService.get(urlPrefix + '/' + validationName+'/'+newVal+'/'+$window.encodeURIComponent(memberName))
										.then(function(response){
											ngModelCtrl.$setValidity(validationName, response.data.isValid ? true : false);
										},function(responseErr){
											ngModelCtrl.$setValidity(validationName, false);
											alert('後端檢核過程發生錯誤，檢核名稱為: ' + validationName);
										});
								});
							}	
						};
					}
				};
			}])
			.directive('mobileDuplicated', ['ValidateService', function(ValidateService){
				return ValidateService.multiCondsValidateDirectiveDefProto('mobileDuplicated');
			}])
			.directive('telDuplicated', ['ValidateService', function(ValidateService){
				return ValidateService.multiCondsValidateDirectiveDefProto('telDuplicated');
			}])
			;
</script>
</body>
</html>