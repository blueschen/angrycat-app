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
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	
	<title>阿喵愛生氣Pandora代購 - 匯款回條</title>

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
	<div>
		<h2  class="text-center">阿喵愛生氣Pandora代購 - 匯款回條</h2>
		<p class="text-center">
		[社團公告] 因Pandora業務逐漸轉移至OHM, 另外國際運費及美金匯率不斷升高的關係，為繼續維持實惠的價格，我們不再請美國郵寄簡易包裝紙盒回來，因原本就有用氣泡袋包裝防寄送時踫撞，所以從今天開始購買手鏈/手環會提供絨布袋，但是墜子就不再附簡易包裝紙盒咯，出貨會用氣泡袋包好，謝謝大家~
		</p>
		<p class="text-center">
		Dear各位Pandora同好
		</p>
		<p class="text-center">
		請大家在跟喵娘確認好訂單並匯款完畢之後在這裡填寫你的完整資料, 基本上現貨商品會在一個工作天之內寄出,如我有跟您事先說明是預購商品,則需要等待2-3周才會送達台北, 寄送期間還麻煩耐心等待,有任何問題可以在粉絲團或社團留言,或是直接與我line iflywang聯絡.
		</p>
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
<div class="container">
<form class="form-horizontal" name="transferReplyForm">
	<div class="row" style="height: 60px;">
		<div class="checkbox col-sm-3">
    		<label>
      			<input type="checkbox" ng-model="mainCtrl.trying" ng-change="mainCtrl.tryLastFilled();"> 嘗試帶入最後填寫的基本資料
    		</label>
		</div>
		<div class="alerts-container col-sm-6"></div>
	</div>
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
 					autofocus
 					placeholder="請寫全名 Ex. Ifly Wang"/>
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
 	 	<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.name.$error.required}">
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
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.mobile.$error.required}">
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
					placeholder="Ex. 09xx-xxx-xxx"/>
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
 		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.postalCode.$error.required}">
			<label class="col-sm-3 control-label" for="postalCode">
 				郵遞區號<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-4">
 				<input type="text" ng-model="mainCtrl.transferReply.postalCode" id="postalCode" name="postalCode" class="form-control" ng-required="true"/>
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.address.$error.required}">
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
		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.transferDate.$invalid || transferReplyForm.transferDate.$error.required}">
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
  		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.transferAccountCheck.$error.required}">
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
		 <div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.transferAmount.$error.required}">
			<label class="col-sm-3 control-label" for="transferAmount">
 				匯款金額<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-4">
 				<input type="number" ng-model="mainCtrl.transferReply.transferAmount" id="transferAmount" name="transferAmount" class="form-control" ng-required="true"/>
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
  		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': transferReplyForm.salesNo.$error.required}">
			<label class="col-sm-3 control-label" for="salesNo">
 				訂單編號<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-7">
 				<input
 					type="text"
 					ng-model="mainCtrl.transferReply.salesNo"
 					id="salesNo"
 					name="salesNo"
 					class="form-control"
 					ng-required="true"
 					data-trigger="focus"
 					placement="auto top"
 					bs-tooltip="{title: '如有需要填寫多個訂單編號，請以逗點分隔'}"/>
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
 	<div class="row">
 		<div class="col-sm-offset-3 col-sm-1">
 			<input type="submit" value="提交" ng-click="mainCtrl.save()" ng-disabled="transferReplyForm.$invalid" class="btn btn-primary" id="submitResults"/>
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
		.constant('moduleName', '${moduleName == null ? "null" : moduleName}')
		.controller('MainCtrl', ['$scope', 'DateService', 'AjaxService', 'urlPrefix', 'login', 'targetData', '$cookies', 'moduleName', '$alert', function($scope, DateService, AjaxService, urlPrefix, login, targetData, $cookies, moduleName, $alert){
			var self = this,
				saveUrl = urlPrefix + '/batchSaveOrMerge.json',
				cookieFilled = moduleName + '_filled';
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
			function assignVal(){
				var filled = $cookies.getObject(cookieFilled);
				if(filled){
					for(var prop in filled){
						var val = filled[prop];
						if(val){
							self.transferReply[prop] = val;
						}
					}
					document.getElementById("transferDate").focus();
				}else{
					document.getElementById("fbNickname").focus();
					alertLastFillNotFound();
				}
			}
			function saveFilled(){
				var copy = angular.copy(self.transferReply);
				copy.id = null;
				copy.transferDate = null;
				copy.transferAccountCheck = null;
				copy.transferAmount = null;
				copy.productDetails = null;
				copy.note = null;
				copy.createDate = null;
				$cookies.putObject(cookieFilled, copy);
			}
			if(targetData){
				self.transferReply = targetData;
			}else{
				self.transferReply = {};
				//assignVal();
			}
			self.save = function(){
				var isNew = self.transferReply.id ? false : true;
				if(isNew){
					self.transferReply.brand = "PANDORA";
				}
				AjaxService.post(saveUrl, [self.transferReply])
					.then(function(response){
						self.transferReply = response.data[0];
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