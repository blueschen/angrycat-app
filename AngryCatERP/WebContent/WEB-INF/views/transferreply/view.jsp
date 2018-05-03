<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="transferreply" var="moduleName"/>
<c:set value="${pageContext.request.contextPath}/${moduleName}" var="urlPrefix"/>
<!DOCTYPE html>

<html lang="zh-TW" ng-app="angryCatTransferReplyViewApp">
<head>
	<meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,Chrome=1">
    <meta http-equiv="X-UA-Compatible" content="IE=9" />
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	
	<title>安格卡特Angrycat匯款回條</title>

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
	/*fix angularstrap modal background not transparent*/
	.modal-backdrop{
		opacity: .5;
	}
	/*
		by default, angularstrap alert service don't support center position,
		with this css definition, it can adjust position to center.
		this css would exclude the placement config.
	*/
	.modal.center .modal-dialog{
    	position:fixed;
    	top:40%;
    	left:50%;
    	width:30%;
    	-webkit-transform:translateX(-50%) translateY(-50%);
    	transform:translateX(-50%) translateY(-50%)
	}	
	</style>
</head>
<body ng-controller="MainCtrl as mainCtrl" ng-cloak>
<div class="container">
	<div>
		<h2  class="text-center">安格卡特Angrycat匯款回條</h2>
		<p style="color:red;">請務必在匯款完畢之後再填寫以利核對款項</p>
		<br>
		<p>
		<b>Pandora</b> 代購商品我們會用氣泡袋包裝之後出貨。
		</p>
		<p>
		<b>OHM Beads</b> 專櫃商品為完整盒裝，附商品保固卡。
		</p>
		<p>
		<b>其他商品如Agete則以社團公告爲主。</b>
		</p>
		<br>
		<p>
		我們統一週日至週四晚上9點對帳，現貨商品安排下一個工作天出貨，預定商品則需依照到貨時間等待2-3週不等時間寄送期間還麻煩耐心等待，有任何問題可以在粉絲團或社團留言，或是直接來信<b><a href="mailto:info@ohmbeads.com.tw" target="_blank">info@ohmbeads.com.tw</a></b>聯絡。		
		</p>
		<p>
		所有款項統一匯款至郵局帳號，無折存款請務必提供<b>郵局局號</b> (請參考郵局提供收據上的印章)		
		</p>
		<h5><b>郵局700</b></h5>
		<h5><b>戶名:王逸凡</b></h5>
 		<h5><b>帳號: 0002123-0169388</b></h5>
 		 				
		<div class="row">
			<div class="col-sm-offset-5">
				<a href="javascript:void(0);" onclick="PopupCenterDual('http://www.post.gov.tw/post/internet/Postal/index.jsp?ID=208', 'postPage', '800', '500');">郵遞區號查詢</a>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-offset-5">
				<a href="javascript:void(0);" onclick="PopupCenterDual('http://www.family.com.tw/marketing/inquiry.aspx', 'familyMartPage', '800', '500');">全家門市名稱/代號查詢</a>
			</div>
		</div>
	</div>
</div>
<hr>
<div id="warning-compatibility" style="display: block;">
	<h1 style="color:red;">
		注意!!&nbsp;&nbsp;您的瀏覽器版本不支援本頁面服務，請採用Google Chrome或Firefox填寫匯款回條
	</h1>
	<br>
	<h3>
		若有任何回條填寫問題請來信<b><a href="mailto:info@ohmbeads.com.tw" target="_blank">info@ohmbeads.com.tw</a></b>或洽詢02-2776-1505
	</h3>
</div>
<div class="container" id="container" style="display: none;">
<form class="form-horizontal" name="transferReplyForm">
	<div class="row" style="height: 60px;">
		<div class="checkbox col-sm-3">
    		<label>
      			<input type="checkbox" ng-model="mainCtrl.trying" ng-change="mainCtrl.tryLastFilled();"> 嘗試帶入上次填寫的基本資料
    		</label>
		</div>
		<div class="alerts-container col-sm-6"></div>
	</div>
<div id="salesInfo">	
	<div class="form-group">
		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.brand.$invalid || transferReplyForm.brand.$error.required}">
			<label for="brand" class="col-sm-3 control-label">購買商品品牌<span style="color:red;">*</span></label>
			<div class="col-sm-4">
				<select
					ng-model="mainCtrl.brand"
					ng-options="g.value as g.label for g in mainCtrl.selectOpts.brands"
					class="form-control"
					id="brand"
					name="brand"
					ng-change="mainCtrl.changeItem('brand', mainCtrl.brand)"
					ng-required="true">
				</select>
			</div>
			<div class="col-sm-3" ng-if="mainCtrl.brand == '其他'" ng-class="{'has-error': transferReplyForm.brandInput.$error.required}">
				<input type="text" ng-model="mainCtrl.transferReply.brand" class="form-control" ng-required="true" name="brandInput">
			</div>			
		</div>
	</div>	
	<div class="form-group">
		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.salePoint.$invalid || transferReplyForm.salePoint.$error.required}">
			<label for="salePoint" class="col-sm-3 control-label">訂購管道<span style="color:red;">*</span></label>
			<div class="col-sm-4">
				<select
					ng-model="mainCtrl.salePoint"
					ng-options="g.value as g.label for g in mainCtrl.selectOpts.salePoints"
					class="form-control"
					id="salePoint"
					name="salePoint"
					ng-change="mainCtrl.changeItem('salePoint', mainCtrl.salePoint)"
					ng-required="true">
				</select>
			</div>
		</div>
	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.productDetails.$error.required}">
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
 					placeholder="簡單説明購買的商品即可 有不清楚的請先問我們"
 					ng-disabled="mainCtrl.fieldsDisabled"></textarea> 					
 			</div>
 		</div>
 	</div>
	<div class="form-group">
		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.shipment.$error.required}">
			<label for="shipment" class="col-sm-3 control-label">寄送方式<span style="color:red;">*</span></label>
			<div class="col-sm-4">
				<select
					ng-model="mainCtrl.shipment"
					ng-options="g.value as g.label for g in mainCtrl.selectOpts.shipments"
					class="form-control"
					id="shipment"
					name="shipment"
					ng-change="mainCtrl.changeItem('shipment', mainCtrl.shipment)"
					ng-required="true">
				</select>
			</div>
			<div>
				<span class="control-label">{{mainCtrl.shipMemo[mainCtrl.shipment]}}</span>
			</div>
		</div>
	</div> 	 	
</div> 	
<div id="transferInfo">
	<!-- 
	<div class="form-group">
		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.transferTo.$invalid || transferReplyForm.transferTo.$error.required}">
			<label class="col-sm-3 control-label" for="transferTo">匯款至<span style="color:red;">*</span></label>
			<div class="col-sm-4">
				<select
					ng-model="mainCtrl.transferTo"
					ng-options="g.value as g.label for g in mainCtrl.selectOpts.transferTos"
					class="form-control"
					id="transferTo"
					name="transferTo"
					ng-change="mainCtrl.changeItem('transferTo', mainCtrl.transferTo)"
					ng-required="true">
				</select>
			</div>
		</div>
	</div> -->
 	<div class="form-group">
  		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.transferAccountCheck.$error.required}">
			<label class="col-sm-3 control-label" for="transferAccountCheck">
 				匯款帳號後5碼<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-4">
 				<input
 					type="text"
 					ng-model="mainCtrl.transferReply.transferAccountCheck"
 					id="transferAccountCheck"
 					name="transferAccountCheck"
 					class="form-control"
 					ng-required="true"
 					data-trigger="focus"
 					placement="auto top"
 					bs-tooltip="{title: '郵局帳戶請提供戶名+後五碼/無摺存款請提供收據上的局號'}"
 					ng-disabled="mainCtrl.fieldsDisabled"/>
 			</div>
 		</div>	
 	</div>	
	<div class="form-group">
		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.transferDate.$invalid || transferReplyForm.transferDate.$error.required}">
 			<label class="col-sm-3 control-label" for="transferDate" >
 				匯款日期<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-4">
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
 					useNative="true"
 					ng-disabled="mainCtrl.fieldsDisabled">			      
 			</div>		
		</div>
 	</div>
	<div class="form-group">
		 <div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.transferAmount.$error.required || transferReplyForm.transferAmount.$error.number}">
			<label class="col-sm-3 control-label" for="transferAmount">
 				匯款金額<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-4">
 				<input type="number" ng-model="mainCtrl.transferReply.transferAmount" id="transferAmount" name="transferAmount" class="form-control" ng-required="true" ng-disabled="mainCtrl.fieldsDisabled"/>	
 			</div>
 			<div class="col-sm-4">
 				<span  style="color:red;" ng-show="transferReplyForm.transferAmount.$error.number">
      				請填入大於0整數!</span>
 			</div>
 		</div>
 	</div>	
</div>
			
<div id="shipInfo">
	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.fbNickname.$error.required}">
			<label class="col-sm-3 control-label" for="fbNickname">
 				FB顯示名稱<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-4">
 				<input type="text"
 					ng-model="mainCtrl.transferReply.fbNickname" 
 					id="fbNickname"
 					name="fbNickname"
 					class="form-control"
 					ng-required="true"
 					placeholder="請寫全名 Ex. Ifly Wang"/>
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.mobile.$error.required || transferReplyForm.mobile.$error.pattern}">
			<label class="col-sm-3 control-label" for="mobile">
 				手機號碼<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-4">
 				<input type="text"
					ng-model="mainCtrl.transferReply.mobile"
					id="mobile"
					name="mobile"
					class="form-control"
					ng-required="true"
					ng-pattern="/^09[0-9]{8}$/"
					placeholder="Ex. 09xxxxxxxx"/>
 			</div>
 			<div class="col-sm-4">
 				<span  style="color:red;" ng-show="transferReplyForm.mobile.$error.pattern">
      				請填入手機格式09xxxxxxxx共十碼</span>
 			</div>
 		</div>
 	</div>
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
 	 	<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.name.$error.required}">
			<label class="col-sm-3 control-label" for="name">
 				收件人真實姓名<span style="color:red;">*</span>
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
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.postalCode.$error.required || transferReplyForm.postalCode.$error.pattern}">
			<label class="col-sm-3 control-label" for="postalCode">
 				郵遞區號<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-4">
 				<input type="text" ng-model="mainCtrl.transferReply.postalCode" id="postalCode" name="postalCode" class="form-control" ng-required="true" ng-pattern="/^([0-9]{3}|[0-9]{5})$/"/>
 			</div>
 			<div class="col-sm-4">
 				<span  style="color:red;" ng-show="transferReplyForm.postalCode.$error.pattern">
      				請填入郵遞區號三碼或五碼</span>
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.address.$error.required}">
 			<label class="col-sm-3 control-label" for="address">
 				<span ng-if="!mainCtrl.transferReply.shipment || mainCtrl.transferReply.shipment == '郵局掛號' || mainCtrl.transferReply.shipment == '郵局便利箱'">掛號收件地址</span>
 				<span ng-if="mainCtrl.transferReply.shipment && mainCtrl.transferReply.shipment != '郵局掛號' && mainCtrl.transferReply.shipment != '郵局便利箱'">店名</span>
 				<span style="color:red;">*</span>	
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
 					placeholder="{{mainCtrl.addressPlaceHolder}}"
 					data-trigger="focus"
 					placement="auto top"
 					bs-tooltip="{title: '假日郵局不出貨'}"></textarea>
 			</div>
 		</div>
 	</div>
</div> 

<div id="contactInfo">	
 	
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
 	<div class="row text-center">
<div class="btn-group" role="group" aria-label="Submit Button">
	<input type="submit" value="提交" ng-click="mainCtrl.save()" ng-disabled="transferReplyForm.$invalid || mainCtrl.fieldsDisabled" class="btn btn-primary" id="submitResults"/>
</div>
<div class="btn-group" role="group" aria-label="New Transfer Reply Button">
	<input type="submit" value="新增匯款回條" ng-click="mainCtrl.addNew()" ng-disabled="!mainCtrl.fieldsDisabled" class="btn btn-primary"/>
</div>
<div class="btn-group" role="group" aria-label="Close Page Button">
	<input type="button" value="關閉" onclick="document.location.href='${urlPrefix}/list'" class="btn btn-default" ng-if="mainCtrl.login"/>
</div>
 	</div>
</form>

</div>
<script type="text/javascript">
	angular.module('angryCatTransferReplyViewApp', ['erp.date.service', 'erp.ajax.service', 'mgcrea.ngStrap', 'ngCookies', 'ngAnimate'])
		.constant('urlPrefix', '${urlPrefix}')
		.constant('login', "${sessionScope['sessionUser']}" ? true : false)
		.constant('targetData', ${transferReply == null ? "null" : transferReply})
		.constant('user', ${user == null ? "null" : user})
		.constant('moduleName', '${moduleName == null ? "null" : moduleName}')
		.constant('config', ${config == null ? "null" : config})
		.controller('MainCtrl', ['$scope', 'DateService', 'AjaxService', 'urlPrefix', 'login', 'targetData', '$cookies', 'moduleName', '$alert', 'user', 'config', '$modal', function($scope, DateService, AjaxService, urlPrefix, login, targetData, $cookies, moduleName, $alert, user, config, $modal){
			
	     	document.getElementById('container').style.display = 'block';
	     	document.getElementById('warning-compatibility').style.display = 'none';
			
			var self = this,
				saveUrl = urlPrefix + '/batchSaveOrMerge.json';
			
			if(targetData){
				self.transferReply = targetData;
			}else{
				self.transferReply = {};
				if(user){
					angular.copy(user, self.transferReply);
				}
				//assignVal();
			}
			// 目前有幾種確定的管道: Pandora美國團、OHM商店
			var selectOpts = {
				transferTos: ['郵局'],
				brands: ['Pandora', 'OHM Beads', 'Town Talk Polish', '皆有', '其他'],
				salePoints: ['FB社團', 'OHM商店', '粉絲團'],
				shipments: ['郵局掛號', '郵局便利箱', '全家', '7-11']};
			for(var p in selectOpts){
				if(selectOpts.hasOwnProperty(p)){
					var oldList = selectOpts[p];
					var newList = [];
					for(var i = 0; i < oldList.length; i++){
						var oldItem = oldList[i];
						oldList[i] = {label: oldItem, value: oldItem};
					}
				}
			}
			self.selectOpts = selectOpts;
			self.shipMemo = {'郵局掛號': '(0NT)Town Talk商品不適用','郵局便利箱': '(郵資80NT)', '全家': '(郵資60NT)', '7-11': '(郵資60NT)'};
			self.changeItem = function(prop, val){
				if(val != '其他'){
					assignPropVal(prop, val);
				}else{
					self.transferReply[prop] = null;
				}
				if(prop == 'shipment'){
					genAddressMsg();
				}
			};
			
			function genAddressMsg(){
				if(self.transferReply.shipment && (self.transferReply.shipment != '郵局掛號' || self.transferReply.shipment != '郵局便利箱')){
					self.addressPlaceHolder = '店到店請填寫店名並補郵資60元';
				}
				if(!self.transferReply.shipment || self.transferReply.shipment == '郵局掛號' || self.transferReply.shipment == '郵局便利箱'){
					self.addressPlaceHolder = '若有區域 請一定要寫 ex:三重區 內湖區 北屯區...(掛號免運費,其他寄送方式請跟喵娘確認)';
				}
			}
			
			function assignPropVal(prop, val){
				self[prop] = val;
				self.transferReply[prop] = self[prop];
			}
			function assignDefaultWithFirstOpt(prop){
				assignPropVal(prop, self.selectOpts[prop+'s'][0].value);
			}
			function assignNull(prop){
				assignPropVal(prop, null);
			}
			function assignDefaultOption(){
				assignDefaultWithFirstOpt('transferTo');
				assignDefaultWithFirstOpt('brand');
				assignDefaultWithFirstOpt('salePoint');
				assignDefaultWithFirstOpt('shipment');
				genAddressMsg();
			}
			function assignItem(prop, val){
				assignPropVal(prop, val);
				self.changeItem(prop, val);
			}
			assignDefaultOption();
			if(config){
				for(var prop in config){
					if(config.hasOwnProperty(prop)){
						assignItem(prop, config[prop]);
					}
				}
			}
			
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
			
			/*
			function alertSaveSuccess(msg){
				var alertService = 
					$alert({
						title: '儲存成功',
						type: 'success', 
						show: true,
						duration: 3,
						container: '.alerts-container',
						placement: defaultPlacement});
				alertService.init();
				if(msg){
					console.log(msg);	
				}
				submitResults('儲存成功', 'btn btn-success');
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
						placement: defaultPlacement});
				alertService.init();
				if(msg){
					console.log(msg);	
				}
				submitResults('儲存失敗', 'btn btn-danger');
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
						placement: defaultPlacement});
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
					placement: defaultPlacement
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
			*/
			var contactFilled = moduleName + '_contact_filled',
				shipFilled = moduleName + '_ship_filled';
			function assignVal(){
				var contact = $cookies.getObject(contactFilled),
					ship =  $cookies.getObject(shipFilled);
				if(contact){
					for(var prop in contact){
						var val = contact[prop];
						if(val){
							self.transferReply[prop] = val;
						}
					}
					for(var prop in ship){
						var val = ship[prop];
						if(val){
							self.transferReply[prop] = val;
						}
					}
					//document.getElementById("transferDate").focus();
				}else{
					//document.getElementById("fbNickname").focus();
					alertLastFillNotFound();
				}
			}
			function copyProp(props){
				var copy = {};
				for(var i = 0; i < props.length; i++){
					var p = props[i];
					copy[p] = self.transferReply[p];
				}
				return copy;
			}
			function saveFilled(){
				var copyContact = copyProp(['fbNickname', 'mobile', 'tel']);
				$cookies.putObject(contactFilled, copyContact);
				var shipCopy =  copyProp(['shipment', 'name', 'postalCode', 'address']);
				$cookies.putObject(shipFilled, shipCopy);
			}
			var defaultPlacement = 'center'; // this config not working because of css definition '.modal.center .modal-dialog'
			function alertSaveSuccess(msg){
				if(msg){
					console.log(msg);	
				}
				var successModal = $modal({content: '<h2 style="color: green;">成功送出匯款回條</h2>', placement: defaultPlacement, html: true});
			}
			function alertSaveFail(data){
				//var regex = /<body><h1>.*Exception:\s(.*)<\/h1>/;
				var regex = /<b>root cause<\/b><\/p><pre>.*Exception:\s(.*)(\r\n|\n|\r)/g;
				var msg = '';
				var rs;
				var dataArray = [];
				while(rs = regex.exec(data)){
					if(rs && rs.length >= 2){
						var txt = document.createElement("textarea");
					    txt.innerHTML = rs[1];
					    var d = '<br /><span>' + txt.value + '</span>';
					    if(dataArray.indexOf(d) == -1){
					    	dataArray.push(d);
					    }
					}
				}
				if(dataArray.length > 0){
					msg = dataArray.join('');
				}else{
					msg = data;
				}
				
				var failModal = $modal({content: '<h2 style="color: red;">儲存失敗:</h2>' + msg, placement: defaultPlacement, html: true});
			}
			function alertLastFillNotFound(){
				var lastNotFound = $modal({content: '<h4 style="color: red;">沒有最後填寫記錄</h4>', placement: defaultPlacement, html: true});
			}
			self.addNew = function(){
				self.transferReply.id = null;
				
				self.transferReply.productDetails = null;
				self.transferReply.transferAccountCheck = null;
				self.transferReply.transferDate = null;
				self.transferReply.transferAmount = null;
				
				self.fieldsDisabled = false;
			};
			self.fieldsDisabled = false;
			self.save = function(){
				AjaxService.post(saveUrl, [self.transferReply])
					.then(function(response){
						self.transferReply = response.data[0];
						saveFilled();
						alertSaveSuccess();
						self.fieldsDisabled = true;
					},
					function(errResponse){
						alertSaveFail(errResponse.data);
					});
			};
			self.login = login;
			self.tryLastFilled = function(){
				if(self.trying){
					assignVal();
				}
			};
			self.checkSalesNoExisted = function(){
				console.log('checkSalesNoExisted..');
				if(!self.transferReply.salesNo){
					return;
				}
				var validationName = 'salesNoNotExisted';
				AjaxService.get(urlPrefix + '/'+validationName+'/' + self.transferReply.salesNo + '/' + self.transferReply.salePoint)
					.then(function(response){
						$scope.transferReplyForm.salesNo.$setValidity(validationName, response.data.isValid ? true : false);
					},function(responseErr){
						$scope.transferReplyForm.salesNo.$setValidity(validationName, false);
						alert('後端檢核過程發生錯誤，檢核名稱為: ' + validationName);
					});
			};
		}]);
</script>
</body>
</html>