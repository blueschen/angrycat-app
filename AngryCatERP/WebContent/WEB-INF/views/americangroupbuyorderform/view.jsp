<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="americangroupbuyorderform" var="moduleName"/>
<c:set value="${pageContext.request.contextPath}/${moduleName}" var="urlPrefix"/>
<!DOCTYPE html>
<html lang="zh-TW" ng-app="angryCatAmericanGroupBuyOrderFormViewApp">
<head>
	<meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	
	<title>{{mainCtrl.americanGroupBuy.activity}}</title>

	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap-theme.css"/>'/>
    
    <script type="text/javascript" src="<c:url value="/vendor/angularjs/1.4.3/angular.min.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/vendor/angularjs/1.4.3/angular-cookies.min.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/vendor/angularjs/1.4.3/angular-animate.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/angularjs/1.4.3/i18n/angular-locale_zh-tw.js"/>"></script>
	<script type="text/javascript">
		<%@ include file="/common/ajax/ajax-service.js" %>
		<%@ include file="/common/date/date-service.js" %>
	</script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.tpl.min.js"/>"></script>

	<!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
	<script type="text/javascript">
	// ref. http://www.nigraphic.com/blog/java-script/how-open-new-window-popup-center-screen
	// 把popup開在螢幕中間
	function PopupCenterDual(url, title, w, h) {
		// Fixes dual-screen position Most browsers Firefox
		var dualScreenLeft = window.screenLeft != undefined ? window.screenLeft : screen.left;
		var dualScreenTop = window.screenTop != undefined ? window.screenTop : screen.top;

		width = window.innerWidth ? window.innerWidth : document.documentElement.clientWidth ? document.documentElement.clientWidth : screen.width;
		height = window.innerHeight ? window.innerHeight : document.documentElement.clientHeight ? document.documentElement.clientHeight : screen.height;

		var left = ((width / 2) - (w / 2)) + dualScreenLeft;
		var top = ((height / 2) - (h / 2)) + dualScreenTop;
		var newWindow = window.open(url, title, 'scrollbars=yes, width=' + w + ', height=' + h + ', top=' + top + ', left=' + left);

		// Puts focus on the newWindow
		if (window.focus) {
			newWindow.focus();
		}
	}
	</script>
	<style type="text/css">
	.form-horizontal .control-label.text-left{
    	text-align: left;
	}
	.alert{
		padding: 5px;
	}
	</style>
</head>
<body ng-controller="MainCtrl as mainCtrl">
<div class="container">
	<div ng-if="!mainCtrl.isOrderFormDisabled()">
		<h2  class="text-center">{{mainCtrl.americanGroupBuy.activity}}</h2>
		<p class="text-center">
		
		</p>
		<p class="text-center">
		
		</p>
		<p class="text-center">
		
		</p>
	</div>
	<div ng-if="mainCtrl.isOrderFormDisabled()">
		<h2  class="text-center">美國團試算(實際價格請以活動期間公布為主)</h2>
	</div>
</div>
<hr>
<div class="container">
<form class="form-horizontal" name="americanGroupBuyOrderFormForm" ng-if="mainCtrl.americanGroupBuy.id">

	
 	<div class="form-group">
 		<div  class="col-sm-offset-1 col-sm-8">
 			<button type="button" class="btn btn-default" ng-click="mainCtrl.addQualify()" id="addQualify">
 				增加一筆正取商品
 			</button>
 		</div>
 	</div>
 	<div id="qualifies" ng-repeat="qualify in mainCtrl.qualifies" class="form-group">
 		<div  class="col-sm-offset-1 col-sm-8">
  			<div class="col-sm-1">
  				<span>{{$index+1}}</span>
  			</div> 		
 			<div class="col-sm-4">
 				<input id="qualify_productName{{$index}}" 
					type="text" 
					class="form-control"
					ng-model="qualify.productName" 
					ng-required="true"
					placeholder="英文名字">
 			</div> 		
 			<div class="col-sm-3">
 				<input id="qualify_modelId{{$index}}" 
					type="text" 
					class="form-control"
					ng-model="qualify.modelId" 
					ng-required="true"
					placeholder="編號">
 			</div>
 			<div class="col-sm-3">
 				<input id="qualify_productAmtUSD{{$index}}" 
					type="number" 
					class="form-control"
					ng-model="qualify.productAmtUSD" 
					ng-required="true"
					placeholder="美金定價"
					ng-blur="mainCtrl.calculateResult()">
 			</div>
 			<div class="col-sm-1">
 				<button type="button" class="btn btn-default" ng-click="mainCtrl.removeDetail(qualify)"><span class="glyphicon glyphicon-remove"></span></button>
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
 		<div  class="col-sm-offset-1 col-sm-8">
 			<button type="button" class="btn btn-default" ng-click="mainCtrl.addWait()">
 				增加一筆備取商品
 			</button>
 		</div>
 	</div>
 	<div id="waits" ng-repeat="wait in mainCtrl.waits" class="form-group">
  		<div class="col-sm-offset-1 col-sm-8">
  			<div class="col-sm-1">
  				<span>{{$index+1}}</span>
  			</div>
 			<div class="col-sm-4">
 				<input id="wait_productName{{$index}}" 
					type="text" 
					class="form-control"
					ng-model="wait.productName" 
					ng-required="true"
					placeholder="英文名字">
 			</div> 		
 			<div class="col-sm-3">
 				<input id="wait_modelId{{$index}}" 
					type="text" 
					class="form-control"
					ng-model="wait.modelId" 
					ng-required="true"
					placeholder="編號">
 			</div>
 			<div class="col-sm-3">
 				<input id="wait_productAmtUSD{{$index}}" 
					type="number" 
					class="form-control"
					ng-model="wait.productAmtUSD" 
					ng-required="true"
					placeholder="美金定價">
 			</div>
 			 <div class="col-sm-1">
 				<button type="button" class="btn btn-default" ng-click="mainCtrl.removeDetail(wait)"><span class="glyphicon glyphicon-remove"></span></button>
 			</div> 			
 		</div>	
 	</div> 	
	<div class="form-group" ng-if="mainCtrl.calCulateQualifyTotalAmt() >= mainCtrl.americanGroupBuy.subAmtUSDThresholdForGift">
		<div class="col-sm-offset-1 col-sm-7">
			<label for="giftItem">選擇贈品</label>
			<select
				ng-model="mainCtrl.selectedGift.productName"
				ng-options="g.value as g.label for g in mainCtrl.gifts"
				class="form-control"
				ng-change="mainCtrl.changeGiftName(mainCtrl.selectedGift.productName)"
				id="giftItem"
				name="giftItem"
			>
			</select>
		</div>
		<div class="col-sm-2">
			<span>加購{{mainCtrl.selectedGift.productAmtUSD ? mainCtrl.selectedGift.productAmtUSD : 0}} USD</span>
		</div>
	</div>
	<div class="form-group" ng-if="mainCtrl.selectedGift.productName && mainCtrl.calCulateQualifyTotalAmt() >= mainCtrl.americanGroupBuy.subAmtUSDThresholdForGift">
		<div class="col-sm-offset-1 col-sm-7">
			<label for="giftSize">選擇尺寸</label>
			<select
				ng-model="mainCtrl.selectedGift.size"
				ng-options="g.value as g.label for g in mainCtrl.giftSizes"
				class="form-control"
				id="giftSize"
				name="giftSize"
			>
			</select>
		</div>
	</div>
	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8">
			<label class="col-sm-3 control-label" for="subAmtUSD">
 				小計USD:
 			</label>
 			<div class="col-sm-4" id="subAmtUSD">
 				<span>{{mainCtrl.calculation.subAmtUSD}}</span>
 			</div>
 		</div>
 	</div>
 		<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8"
 			data-trigger="hover"
 			placement="auto top"
 			bs-tooltip="{title: '美金{{mainCtrl.americanGroupBuy.discountUSDThreshold}}以上總金額折扣{{mainCtrl.americanGroupBuy.discountUSD}}USD'}">
			<label class="col-sm-3 control-label" for="discountUSD">
 				折扣USD:
 			</label>
 			<div class="col-sm-4" id="discountUSD">
 				<span>{{mainCtrl.calCulateQualifyTotalAmt() >= mainCtrl.americanGroupBuy.discountUSDThreshold ? mainCtrl.americanGroupBuy.discountUSD : 0}}</span>
 			</div>
 		</div>
 	</div>
 		<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8">
			<label class="col-sm-3 control-label" for="serviceChargeNTD">
 				代購服務費NTD:
 			</label>
 			<div class="col-sm-4" id="serviceChargeNTD">
 				<span>{{mainCtrl.americanGroupBuy.serviceChargeNTD}}</span>
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8"
 		data-trigger="hover"
 		placement="auto top"
 		bs-tooltip="{title: '代購總金額NTD = (小計USD - 折扣USD) * {{mainCtrl.americanGroupBuy.multiplier}} * {{mainCtrl.americanGroupBuy.rate}} + 代購服務費NTD'}">
			<label class="col-sm-3 control-label" for="totalAmtNTD">
 				代購總金額NTD:
 			</label>
 			<div class="col-sm-4" id="totalAmtNTD">
 				<span>{{mainCtrl.calculation.totalAmtNTD}}</span>
 			</div>
 		</div>
 	</div>
 	
 	
 	<div ng-if="!mainCtrl.isOrderFormDisabled()">
 	
 	
	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8">
			<label class="col-sm-3 control-label" for="salesNo">
 				訂單號碼
 			</label>
 			<div class="col-sm-4" id="salesNo">
 				<span>{{mainCtrl.salesNo}}</span>
 			</div>
 		</div>
 	</div>
	<div class="row" style="height: 60px;">
		<div class="checkbox col-sm-3">
    		<label>
      			<input type="checkbox" ng-model="mainCtrl.trying" ng-change="mainCtrl.tryLastFilled();"> 嘗試帶入最後填寫的基本資料
    		</label>
		</div>
		<div class="alerts-container col-sm-6"></div>
	</div> 	
	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': americanGroupBuyOrderFormForm.fbNickname.$error.required}">
			<label class="col-sm-3 control-label" for="fbNickname">
 				FB顯示名稱<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-4">
 				<input type="text"
 					ng-model="mainCtrl.contact.fbNickname" 
 					id="fbNickname"
 					name="fbNickname"
 					class="form-control"
 					ng-required="true"
 					placeholder="請寫全名 Ex. Ifly Wang"/>
 			</div>
 		</div>
 	</div>	
 	<!-- 
 	<div class="form-group">
 	 	<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': americanGroupBuyOrderFormForm.name.$error.required}">
			<label class="col-sm-3 control-label" for="name">
 				真實姓名<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-4">
 				<input type="text" 
 					ng-model="mainCtrl.transferReply.name"
 					id="name"
 					name="name"
 					class="form-control"
 					ng-required="true"
 					data-trigger="focus"
 					placement="auto top"
 					bs-tooltip="{title: '請一定要寫正確 不然領包裹的時候會很困擾喔'}"/>
 			</div>
 		</div>
 	</div>
 	 -->
 	
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': americanGroupBuyOrderFormForm.mobile.$error.required}">
			<label class="col-sm-3 control-label" for="mobile">
 				手機號碼<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-4">
 				<input type="text"
					ng-model="mainCtrl.contact.mobile"
					id="mobile"
					name="mobile"
					class="form-control"
					ng-required="true"
					placeholder="Ex. 09xx-xxx-xxx"/>
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': americanGroupBuyOrderFormForm.email.$error.required}">
			<label class="col-sm-3 control-label" for="email">
 				Email<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-4">
 				<input type="text"
					ng-model="mainCtrl.contact.email"
					id="email"
					name="email"
					class="form-control"
					ng-required="true">
 			</div>
 		</div>
 	</div>
 	<!-- 
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8">
			<label class="col-sm-3 control-label" for="tel">
 				備用聯絡電話
 			</label>
 			<div class="col-sm-4">
 				<input type="text" ng-model="mainCtrl.transferReply.tel" id="tel" name="tel" class="form-control"/>
 			</div>
 		</div> 	
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': americanGroupBuyOrderFormForm.postalCode.$error.required}">
			<label class="col-sm-3 control-label" for="postalCode">
 				郵遞區號<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-4">
 				<input type="text" ng-model="mainCtrl.transferReply.postalCode" id="postalCode" name="postalCode" class="form-control" ng-required="true"/>
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': americanGroupBuyOrderFormForm.address.$error.required}">
 			<label class="col-sm-3 control-label" for="address">
 				掛號收件地址<span style="color:red;">*</span>	
 			</label>
 			<div class="col-sm-7">
 				<textarea
 					ng-model="mainCtrl.transferReply.address"
 					id="address"
 					name="address"
 					rows="3"
 					cols="30"
 					class="form-control"
 					ng-required="true"
 					placeholder="若有區域 請一定要寫 ex:三重區 內湖區 北屯區...(掛號免運費,其他寄送方式請跟喵娘確認)"
 					data-trigger="focus"
 					placement="auto top"
 					bs-tooltip="{title: '假日郵局不出貨'}"></textarea>
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': americanGroupBuyOrderFormForm.transferDate.$invalid || americanGroupBuyOrderFormForm.transferDate.$error.required}">
 			<label class="col-sm-3 control-label" for="transferDate" >
 				匯款日期<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-7">
 				<input id="transferDate"
 					class="form-control" 
 					ng-model="mainCtrl.transferReply.transferDate" 
 					name="transferDate" 
 					bs-datepicker 
 					type="text" 
 					autoclose="1"
 					date-format="yyyy-MM-dd"
 					placeholder="yyyy-MM-dd"
 					date-type="string"
 					ng-required="true"
 					placeholder="請正確填寫日期，要不然會對不到帳喲"
 					useNative="true">			      
 			</div>		
		</div>
 	</div> 	
 	<div class="form-group">
  		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': americanGroupBuyOrderFormForm.transferAccountCheck.$error.required}">
			<label class="col-sm-3 control-label" for="transferAccountCheck">
 				匯款帳號後5碼<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-7">
 				<input
 					type="text"
 					ng-model="mainCtrl.transferReply.transferAccountCheck"
 					id="transferAccountCheck"
 					name="transferAccountCheck"
 					class="form-control"
 					ng-required="true"
 					data-trigger="focus"
 					placement="auto top"
 					bs-tooltip="{title: '郵局帳戶請提供戶名+後五碼/無摺存款請提供收據上的局號'}"/>
 			</div>
 		</div>	
 	</div>
	<div class="form-group">
		 <div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': americanGroupBuyOrderFormForm.transferAmount.$error.required}">
			<label class="col-sm-3 control-label" for="transferAmount">
 				匯款金額<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-4">
 				<input type="number" ng-model="mainCtrl.transferReply.transferAmount" id="transferAmount" name="transferAmount" class="form-control" ng-required="true"/>
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': americanGroupBuyOrderFormForm.productDetails.$error.required}">
			<label class="col-sm-3 control-label" for="productDetails">
 				購買明細<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-7">
 				<textarea
 					ng-model="mainCtrl.transferReply.productDetails"
 					id="productDetails"
 					name="productDetails"
 					rows="3"
 					cols="30"
 					class="form-control"
 					ng-required="true"
 					placeholder="簡單説明購買的商品即可 有不清楚的請先問我們"></textarea> 					
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8">
 			<label class="col-sm-3 control-label" for="note">
 				其他備註	
 			</label>
 			<div class="col-sm-7">
 				<textarea
 					ng-model="mainCtrl.transferReply.note"
 					id="note"
 					rows="3"
 					cols="30"
 					class="form-control"
 					placeholder="還有話要對喵娘說 有甚麼特別的需求或是要提醒我的 可以寫在這唷~"></textarea>
 			</div>
 		</div>
 	</div>
 	 -->
 	<div class="row">
 		<div class="col-sm-offset-3 col-sm-1">
 			<input type="submit" value="提交" ng-click="mainCtrl.save()" ng-disabled="americanGroupBuyOrderFormForm.$invalid || !mainCtrl.qualifies || mainCtrl.qualifies.length == 0" class="btn btn-primary" id="submitResults"/>
 			<input type="button" value="關閉" onclick="document.location.href='${urlPrefix}/list'" class="btn btn-default" ng-if="mainCtrl.login"/>
 		</div>
 	</div>
 	
 	</div>
</form>

</div>
<script type="text/javascript">
	angular.module('angryCatAmericanGroupBuyOrderFormViewApp', ['erp.date.service', 'erp.ajax.service', 'mgcrea.ngStrap', 'ngCookies', 'ngAnimate'])
		.constant('urlPrefix', '${urlPrefix}')
		.constant('login', "${sessionScope['sessionUser']}" ? true : false)
		.constant('americanGroupBuy', ${americanGroupBuy == null ? "null" : americanGroupBuy})
		.constant('isOrderFormDisabled', ${isOrderFormDisabled == null ? "true" : isOrderFormDisabled})
		.constant('moduleName', '${moduleName == null ? "null" : moduleName}')
		.controller('MainCtrl', ['$scope', 'DateService', 'AjaxService', 'urlPrefix', 'login', 'americanGroupBuy', '$cookies', 'moduleName', '$alert', 'isOrderFormDisabled', function($scope, DateService, AjaxService, urlPrefix, login, americanGroupBuy, $cookies, moduleName, $alert, isOrderFormDisabled){
			var self = this,
				saveUrl = urlPrefix + '/batchSaveOrMerge.json',
				deleteUrl = urlPrefix + '/deleteByIds.json';
			self.addQualify = function(){
				if(!self.qualifies){
					self.qualifies = [];
				}
				self.qualifies.push({salesType: '正取'});
			};
			self.addWait = function(){
				if(!self.waits){
					self.waits = [];
				}
				self.waits.push({salesType: '備取'});
			};
			self.deleteOrderForm = function(orderForm){
				if(!orderForm.id){
					return;
				}
				AjaxService.post(deleteUrl, [orderForm.id])
					.then(function(response){
						var d = response.data;
						alertDeleteSuccess();
					},
					function(errResponse){
						alertDeleteFail(JSON.stringify(errResponse));
					});				
			};
			self.removeDetail = function(detail){
				var isQualify = detail.salesType == '正取';
				var array = isQualify ? self.qualifies : self.waits;
				var idx = array.indexOf(detail);
				var orderForm = array.splice(idx, 1);
				self.deleteOrderForm(orderForm[0]);
				if(isQualify){
					self.calculateResult();	
				}
			};
			function genOptions(values){
				var options = [];
				for(var i = 0; i < values.length; i++){
					var value = values[i];
					options.push({label: value, value: value});
				}
				return options;
			}
			if(americanGroupBuy){
				self.americanGroupBuy = americanGroupBuy;
			}else{
				self.americanGroupBuy = {};
				//assignVal();
			}
			self.isOrderFormDisabled = function (){
				return isOrderFormDisabled;
			};
			// subAmtUSD: 正取小計(美金) / totalAmtNTD:代購總金額(台幣)
			var cal = {subAmtUSD: 0, totalAmtNTD: 0};
			self.calculation = cal;
			function isNumeric(input){
				return !isNaN(parseInt(input, 10));
			}
			// 計算正取總額
			self.calCulateQualifyTotalAmt = function(){
				var totalAmt = 0;
				if(!self.qualifies){
					return totalAmt;
				}
				for(var i = 0; i < self.qualifies.length; i++){
					var qualify = self.qualifies[i],
						productAmtUSD = qualify.productAmtUSD;
					if(!isNumeric(productAmtUSD)){
						totalAmt = 0;
						break;
					}
					totalAmt += parseFloat(productAmtUSD);
				}
				return totalAmt;
			};
			// 計算小計美金總額(正取+贈品補差額)
			self.calCulateSubAmtUSD = function(){
				if(!self.qualifies){
					return 0;
				}
				cal.subAmtUSD = 0;
				// 先計算贈品加購價
				if(self.selectedGift.productAmtUSD > 0){
					cal.subAmtUSD += parseFloat(self.selectedGift.productAmtUSD); 
				}
				// 再計算正取總額
				cal.subAmtUSD += self.calCulateQualifyTotalAmt();
				return cal.subAmtUSD;
			};
			// 計算代購台幣總金額
			self.calCulateTotalAmtNTD = function(subAmtUSD){
				var subTotal = subAmtUSD;
				subTotal = subTotal ? subTotal : self.calCulateSubAmtUSD();
				var result = self.calCulateQualifyTotalAmt() >= americanGroupBuy.discountUSDThreshold ? (subTotal - parseFloat(americanGroupBuy.discountUSD)) : subTotal; // 減掉美金折扣
				cal.totalAmtNTD = result * parseFloat(americanGroupBuy.multiplier) * parseFloat(americanGroupBuy.rate) + parseFloat(americanGroupBuy.serviceChargeNTD);
				cal.totalAmtNTD = Math.ceil(cal.totalAmtNTD);
				return cal.totalAmtNTD;
			};
			// 計算正取美金總額及代購台幣總金額
			self.calculateResult = function(){
				var qualifyTotalAmt = self.calCulateQualifyTotalAmt();
				if(qualifyTotalAmt < americanGroupBuy.subAmtUSDThresholdForGift){
					if(self.selectedGift.id){
						self.deleteOrderForm(self.selectedGift);
					}
					if(self.selectedGift.productName){
						self.selectedGift = {salesType: '贈品', productAmtUSD: 0};	
					}
				}
				var subAmtUSD = self.calCulateSubAmtUSD();
				self.calCulateTotalAmtNTD(subAmtUSD);
			};
			
			// 選擇贈品規則: 正取選擇總金額滿125美金，可選擇一款65美金的手鍊或手環
			var giftPrice = {
				'經典蛇鏈': 65,
				'黑銀蛇鏈': 65,
				'硬手環': 65,
				'Essence手鏈': 65,
				'Essence珠鏈': 65,
				'Essence硬環(春季新品)': 65,
				'愛心叩頭經典蛇鏈': 65,
				'玫瑰金扣純銀蛇鏈(需補差額560NT)': 82,
				'滿鑽釦頭蛇鏈(需補差額560NT)': 82,
				'愛心滿鑽叩頭蛇鏈(需補差額560NT)': 82,
				'2016新款迪士尼蛇鏈(需補差額740NT)': 88,
				'皮繩或其他需求請先跟我們聯絡確認': 65
			};
			var giftData = [];
			for(var p in giftPrice){
				if(giftPrice.hasOwnProperty(p)){
					giftData.push(p);
				}
			}
			self.gifts = genOptions(giftData);
			var sizes = {
				'經典蛇鏈': 
					['17cm',
					'18cm',
					'19cm',
					'20cm',
					'21cm',
					'23cm']
			};
			sizes['黑銀蛇鏈'] = sizes['經典蛇鏈'];
			sizes['玫瑰金釦純銀手鍊'] = sizes['經典蛇鏈'];
			sizes['愛心叩頭經典蛇鏈'] = sizes['經典蛇鏈'];
			sizes['滿鑽釦頭蛇鏈(需補差額560NT)'] = sizes['經典蛇鏈'];
			sizes['硬手環'] = [
				'硬環S (17cm)',
				'硬環M (19cm)',
				'硬環L (21cm)',
			];
			sizes['Essence手鏈'] = [
				'16cm',
				'17cm',
				'18cm',
				'19cm',
				'20cm',
				'21cm',
			];
			sizes['Essence手鏈'] = [
			     'S',
			     'M',
			     'L'
			];
			self.selectedGift = {salesType: '贈品', productAmtUSD: 0};
			self.changeGiftName = function(selectedGiftName){
				var price = giftPrice[selectedGiftName];
				price = parseFloat(price) - parseFloat(self.americanGroupBuy.giftValAmtUSD);
				if(price < 0){
					price = 0;
				}
				self.selectedGift.productAmtUSD = price;
				self.calCulateTotalAmtNTD();
				// 產生尺寸清單
				var size = sizes[selectedGiftName];
				if(size && size.length){
					self.giftSizes = genOptions(size);	
				}else{
					self.giftSizes = [];
				}
			};
			function submitResults(btnVal, btnCss){
				var jqlite = angular.element(document.getElementById('submitResults'));
				var oriVal = jqlite.attr('value'),
					oriCss = jqlite.attr('class');				
				jqlite.attr('value', btnVal);
				jqlite.removeClass(oriCss);
				jqlite.addClass(btnCss);
				setTimeout(function(){
					jqlite.attr('value', oriVal);
					jqlite.removeClass(btnCss);
					jqlite.addClass(oriCss);
				}, 3000);
			}
			function alertSaveSuccess(msg){
				var alertService = 
					$alert({
						title: '儲存成功',
						type: 'success', 
						show: true,
						duration: 3,
						container: '.alerts-container',
						placement: 'top'});
				alertService.init();
				if(msg){
					console.log(msg);	
				}
				submitResults('儲存成功', 'btn btn-success');
			}
			function alertDeleteSuccess(msg){
				var alertService = 
					$alert({
						title: '刪除成功',
						type: 'success', 
						show: true,
						duration: 3,
						container: '.alerts-container',
						placement: 'top'});
				alertService.init();
				if(msg){
					console.log(msg);	
				}
			}
			function alertSaveFail(msg){
				var alertService = 
					$alert({
						title: '儲存失敗', 
						type: 'danger', 
						show: true,
						duration: 3,
						animation: 'am-fade-and-slide-top',
						container: '.alerts-container',
						placement: 'top'});
				alertService.init();
				if(msg){
					console.log(msg);	
				}
				submitResults('儲存失敗', 'btn btn-danger');
			}
			function alertDeleteFail(msg){
				var alertService = 
					$alert({
						title: '刪除失敗', 
						type: 'danger', 
						show: true,
						duration: 3,
						animation: 'am-fade-and-slide-top',
						container: '.alerts-container',
						placement: 'top'});
				alertService.init();
				if(msg){
					console.log(msg);	
				}
			}			
			function alertLastFillNotFound(){
				var alertService = 
					$alert({
						title: '沒有最後填寫記錄', 
						type: 'info', 
						show: true,
						duration: 3,
						animation: 'am-fade-and-slide-top',
						container: '.alerts-container',
						placement: 'top'});
				alertService.init();
			}
			function alertMsg(msg, opts){
				var defaultOps = {
					title: 'default title', 
					type: 'info', 
					show: true,
					duration: 3,
					animation: 'am-fade-and-slide-top',
					container: '.alerts-container',
					placement: 'top'
				};
				if(opts){
					angular.copy(opts, defaultOps);
				}
				var alertService = $alert(defaultOps);
				alertService.init();
				if(msg){
					console.log(msg);
				}
			}
			var cookieFilled = moduleName + '_contact'
			function assignVal(){
				var filled = $cookies.getObject(cookieFilled);
				if(filled){
					if(!self.contact){
						self.contact = {};
					}
					for(var prop in filled){
						var val = filled[prop];
						if(val){
							self.contact[prop] = val;
						}
					}
				}else{
					alertLastFillNotFound();
				}
			}
			function saveFilled(){
				var copy = angular.copy(self.contact);
				$cookies.putObject(cookieFilled, copy);
			}
			function copyTo(dest, ori, props){
				for(var i = 0; i < props.length; i++){
					var prop = props[i];
					dest[prop] = ori[prop];
				}
				return dest;
			}
			function genProductData(items){
				var r = [];
				if(!items || items.length == 0){
					return r;
				}
				for(var i = 0; i < items.length; i++){
					var item = items[i];
					item = copyTo(item, self.contact, ['fbNickname', 'mobile', 'email']);
					item = copyTo(item, self.calculation, ['totalAmtNTD']);
					r.push(item);
				}
				return r;
			}
			self.save = function(){
				if(!self.qualifies || self.qualifies.length == 0){
					return;
				}
				var qualifies = genProductData(self.qualifies),
					waits = genProductData(self.waits),
					selectedGift = null;
				var r = qualifies;
				if(waits.length > 0){
					r = qualifies.concat(waits);
				}
				if(self.selectedGift && self.selectedGift.productName){
					var selectedGift = self.selectedGift;
					r = r.concat(genProductData([selectedGift]));
				}
				AjaxService.post(saveUrl, r)
					.then(function(response){
						var d = response.data;
						if(d && d.length && d.length > 0){
							self.salesNo = d[0].salesNo;
						}else{
							return;
						}
						// TODO 是否不允許送出後改單，這樣設計會簡化很多
						// 考量到客戶重新修改資料，所以要把回傳資料指給頁面的model
						if(qualifies.length > 0){
							self.qualifies = d.splice(0, qualifies.length);
						}
						if(waits.length > 0){
							self.waits = d.splice(0, waits.length);
						}
						if(d.length == 1){
							self.selectedGift = d[0];
						}
						saveFilled();
						alertSaveSuccess();
					},
					function(errResponse){
						alertSaveFail(JSON.stringify(errResponse));
					});
			};
			self.login = login;
			self.tryLastFilled = function(){
				if(self.trying){
					assignVal();
				}
			};
		}]);
</script>
</body>
</html>