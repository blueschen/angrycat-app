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
	
	<title>美國團</title>

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
	/*default position is top-center, using this adjust to center responsively
	.modal-dialog {
    	position: absolute;
    	left: 0;
    	right: 0;
    	top: 0;
    	bottom: 0;
    	margin: auto;
    	height:300px;
	}*/
	.no-gutter > [class*='col-'] {
    	padding-right:0;
    	padding-left:0;
	}
	</style>
</head>
<body ng-controller="MainCtrl as mainCtrl" ng-cloak><!-- preventing angularjs template code displaying with ngCloak  -->
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
<div class="container">
	<div class="row">
		<h2>活動規則說明</h2>
		<p>請以最新公告者為準</p>
		<p>規則規則...</p>
		<div class="checkbox col-sm-3">
    		<label>
      			<input type="checkbox" ng-model="mainCtrl.accepted"> 我已閱讀並完全了解以上注意事項
    		</label>
		</div>
		
		
	</div>
<form class="form-horizontal" name="americanGroupBuyOrderFormForm" ng-if="mainCtrl.accepted && mainCtrl.americanGroupBuy.id" novalidate='novalidate'>

	
 	<div class="form-group">
 		<div  class="col-sm-offset-1 col-sm-8">
 			<label>正取商品<span style="color:red;">*</span></label>
 			<label>(正取總額需滿{{mainCtrl.americanGroupBuy.qualifyTotalAmtThreshold}} USD 
 				<span style="color:red;" ng-if="!mainCtrl.isQualifyTotalAmtAchieveThreshold()">
 					尚差{{mainCtrl.subtract(mainCtrl.americanGroupBuy.qualifyTotalAmtThreshold, mainCtrl.calculateQualifyTotalAmt())}}
 				</span>)
 			</label>
 			<span style="color:red;" ng-show="mainCtrl.qualifyModelIdDuplicated">
 				{{mainCtrl.qualifyModelIdDuplicated}}
 			</span> 			
 		</div>
 	</div>
 	<div id="qualifies" ng-repeat="qualify in mainCtrl.qualifies" class="form-inline">
 		<div class="col-sm-offset-1 col-sm-10">
 			<label class="col-sm-1"><span class="badge">{{$index+1}}</span></label>
			<label ng-class="{'has-error': americanGroupBuyOrderFormForm.qualify_modelId{{$index}}.$error.required || americanGroupBuyOrderFormForm.qualify_modelId{{$index}}.$error.qualifyModelIdDuplicated}">
				<input id="qualify_modelId{{$index}}"
					name="qualify_modelId{{$index}}"
					type="text" 
					class="form-control"
					ng-model="qualify.modelId"
					placeholder="編號"
					ng-disabled="mainCtrl.fieldsDisabled"
					ng-required="true"
					ng-blur="mainCtrl.checkModelIdDuplicated(americanGroupBuyOrderFormForm, 'qualify')">
			</label>
 			<label ng-class="{'has-error': americanGroupBuyOrderFormForm.qualify_productName{{$index}}.$error.required}">
  			 	<input id="qualify_productName{{$index}}"
  			 		name="qualify_productName{{$index}}"
					type="text" 
					class="form-control"
					ng-model="qualify.productName"
					placeholder="英文名字"
					ng-disabled="mainCtrl.fieldsDisabled"
					ng-required="true">
			</label>			
			<label ng-class="{'has-error': americanGroupBuyOrderFormForm.qualify_productAmtUSD{{$index}}.$error.required}">
				<input id="qualify_productAmtUSD{{$index}}"
					name="qualify_productAmtUSD{{$index}}"
					type="number" 
					class="form-control"
					ng-model="qualify.productAmtUSD"
					placeholder="美金定價"
					ng-blur="mainCtrl.calculateResult()"
					ng-disabled="mainCtrl.fieldsDisabled"
					ng-required="true">
			</label>														
 			<div class="btn-group" role="group" aria-label="Qualify Remove Button">
 				<button type="button" class="btn btn-default" ng-click="mainCtrl.removeDetail(qualify)" ng-if="!mainCtrl.fieldsDisabled"><span>移除</span></button>
 			</div>
 			<div class="btn-group" role="group" aria-label="Qualify Add Button">
 				<button type="button" class="btn btn-default" ng-click="mainCtrl.addQualify()" ng-if="!mainCtrl.fieldsDisabled" ng-show="mainCtrl.showQualifyAddBtn(qualify)"><span>新增正取</span></button>
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8">
 			<label>備取商品<span style="color:red;">*</span></label>
 			<label>(備取總額需滿{{mainCtrl.americanGroupBuy.waitTotalAmtThreshold}} USD 
 				<span style="color:red;" ng-if="!mainCtrl.isWaitTotalAmtAchieveThreshold()">
 					尚差{{mainCtrl.subtract(mainCtrl.americanGroupBuy.waitTotalAmtThreshold, mainCtrl.calculateWaitTotalAmt())}}
 				</span>)
 			</label>
 			<span style="color:red;" ng-show="mainCtrl.waitModelIdDuplicated">
 				{{mainCtrl.waitModelIdDuplicated}}
 			</span>
 		</div>
 	</div>
 	<div id="waits" ng-repeat="wait in mainCtrl.waits" class="form-inline">
  		<div class="col-sm-offset-1 col-sm-10">
  			<label class="col-sm-1">
  				<span class="badge">{{$index+1}}</span>
  			</label>
			<label ng-class="{'has-error': americanGroupBuyOrderFormForm.wait_modelId{{$index}}.$error.required || americanGroupBuyOrderFormForm.wait_modelId{{$index}}.$error.waitModelIdDuplicated}">
				<input id="wait_modelId{{$index}}"
					name="wait_modelId{{$index}}"
					type="text" 
					class="form-control"
					ng-model="wait.modelId"
					placeholder="編號"
					ng-disabled="mainCtrl.fieldsDisabled"
					ng-required="true"
					ng-blur="mainCtrl.checkModelIdDuplicated(americanGroupBuyOrderFormForm, 'wait')">
			</label>
  			<label ng-class="{'has-error': americanGroupBuyOrderFormForm.wait_productName{{$index}}.$error.required}">
				<input id="wait_productName{{$index}}"
					name="wait_productName{{$index}}"
					type="text" 
					class="form-control"
					ng-model="wait.productName"
					placeholder="英文名字"
					ng-disabled="mainCtrl.fieldsDisabled"
					ng-required="true">
			</label>			
			<label ng-class="{'has-error': americanGroupBuyOrderFormForm.wait_productAmtUSD{{$index}}.$error.required}">
				<input id="wait_productAmtUSD{{$index}}"
					name="wait_productAmtUSD{{$index}}"
					type="number" 
					class="form-control"
					ng-model="wait.productAmtUSD"
					placeholder="美金定價"
					ng-disabled="mainCtrl.fieldsDisabled"
					ng-required="true">
			</label>									  			
 			<div class="btn-group" role="group" aria-label="Wait Remove Button">
 				<button type="button" class="btn btn-default" ng-click="mainCtrl.removeDetail(wait)" ng-if="!mainCtrl.fieldsDisabled"><span>移除</span></button>
 			</div>
 			<div class="btn-group" role="group" aria-label="Wait Add Button">
 				<button type="button" class="btn btn-default" ng-click="mainCtrl.addWait()" ng-if="!mainCtrl.fieldsDisabled" ng-show="mainCtrl.showWaitAddBtn(wait)"><span>新增備取</span></button>
 			</div>		
 		</div>	
 	</div> 	
	<div class="form-group" ng-if="mainCtrl.isQualifyTotalAmtAchieveThreshold()">
		<div class="col-sm-offset-1 col-sm-8">
			<label for="giftItem">選擇贈品<span ng-if="mainCtrl.selectedGift.modelId">&nbsp;&nbsp;(編號:{{mainCtrl.selectedGift.modelId}})</span></label>
			<select
				ng-model="mainCtrl.selectedGift.productName"
				ng-options="g.value as g.label for g in mainCtrl.gifts"
				class="form-control"
				ng-change="mainCtrl.changeGiftName(mainCtrl.selectedGift.productName)"
				id="giftItem"
				name="giftItem"
				ng-disabled="mainCtrl.fieldsDisabled">
			</select>
		</div>
	</div>
	<div class="form-group" ng-if="mainCtrl.selectedGift.productName && mainCtrl.giftSizes && mainCtrl.giftSizes.length > 0 && mainCtrl.isQualifyTotalAmtAchieveThreshold()">
		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': americanGroupBuyOrderFormForm.giftSize.$error.required}">
			<label for="giftSize">選擇尺寸<span style="color:red;">*</span></label>
			<select
				ng-model="mainCtrl.selectedGift.size"
				ng-options="g.value as g.label for g in mainCtrl.giftSizes"
				class="form-control"
				id="giftSize"
				name="giftSize"
				ng-disabled="mainCtrl.fieldsDisabled"
				ng-required="true"
			>
			</select>
		</div>
	</div>
	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-4">
			<label class="col-sm-6 control-label" for="qualifyTotalAmt">
 				正取總額USD:
 			</label>
 			<label class="control-label pull-right" id="qualifyTotalAmt">{{mainCtrl.calculateQualifyTotalAmt()}}</label>
 		</div>
 	</div>
	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-4">
			<label class="col-sm-6 control-label" for="selectedGiftAmtUSD">
 				選取贈品加價USD:
 			</label>
 			<label class="control-label pull-right" id="selectedGiftAmtUSD">{{mainCtrl.selectedGift.productAmtUSD}}</label>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-4">
			<label class="col-sm-6 control-label" for="subAmtUSD">
 				小計USD:
 			</label>
 			<label class="control-label pull-right" id="subAmtUSD">{{mainCtrl.calculation.subAmtUSD}}</label>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-4">
			<label class="col-sm-6 control-label" for="serviceChargeNTD">
 				代購服務費NTD:
 			</label>
 			<label class="control-label pull-right" id="serviceChargeNTD">{{mainCtrl.americanGroupBuy.serviceChargeNTD}}</label>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-4"
 		data-trigger="hover"
 		placement="auto top"
 		bs-tooltip="{title: '代購總金額NTD = 小計USD * 州稅{{mainCtrl.americanGroupBuy.multiplier}} * 美金匯率{{mainCtrl.americanGroupBuy.rate}} + 代購服務費{{mainCtrl.americanGroupBuy.serviceChargeNTD}}NTD無條件進位'}">
			<label class="col-sm-6 control-label" for="totalAmtNTD">
 				代購總金額NTD:
 			</label>
 			<label class="control-label pull-right" id="totalAmtNTD">{{mainCtrl.calculation.totalAmtNTD}}</label>
 		</div>
 	</div>
 	
 	
 	<div ng-if="!mainCtrl.isOrderFormDisabled()">
 	
 	
	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-4">
			<label class="col-sm-6 control-label" for="salesNo">
 				訂單號碼
 			</label>
 			<label class="control-label pull-right" id="salesNo"style="color:red;">{{mainCtrl.salesNo}}</label>
 		</div>
 	</div>
	<div class="row" style="height: 60px;">
		<div class="checkbox col-sm-3">
    		<label>
      			<input type="checkbox" ng-model="mainCtrl.trying" ng-change="mainCtrl.tryLastFilled();"> 嘗試帶入前次填寫的基本資料
    		</label>
		</div>
		<div class="alerts-container col-sm-6"></div>
	</div> 	
	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': americanGroupBuyOrderFormForm.fbNickname.$error.required}">
			<label class="col-sm-3 control-label" for="fbNickname">
 				FB顯示名稱<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-9">
 				<input type="text"
 					ng-model="mainCtrl.contact.fbNickname" 
 					id="fbNickname"
 					name="fbNickname"
 					class="form-control"
 					ng-required="true"
 					placeholder="請寫全名 Ex. Ifly Wang"
 					ng-disabled="mainCtrl.fieldsDisabled"/>
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
 			<div class="col-sm-9">
 				<input type="text"
					ng-model="mainCtrl.contact.mobile"
					id="mobile"
					name="mobile"
					class="form-control"
					ng-required="true"
					placeholder="Ex. 09xx-xxx-xxx"
					ng-disabled="mainCtrl.fieldsDisabled"/>
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-1 col-sm-8" ng-class="{'has-error': americanGroupBuyOrderFormForm.email.$error.required || americanGroupBuyOrderFormForm.email.$error.email}">
			<label class="col-sm-3 control-label" for="email">
 				Email<span style="color:red;">*</span>
 			</label>
 			<div class="col-sm-9">
 				<input type="email"
					ng-model="mainCtrl.contact.email"
					id="email"
					name="email"
					class="form-control"
					ng-required="true"
					ng-disabled="mainCtrl.fieldsDisabled">
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
 	<div class="row text-center">
 		<div class="btn-group" role="group" aria-label="Submit Button">
 			<input type="submit" 
 				value="提交"
				ng-click="mainCtrl.save()"
				ng-disabled="!mainCtrl.isSubmitPrepared(americanGroupBuyOrderFormForm) || mainCtrl.fieldsDisabled" 
				class="btn btn-primary" 
				id="submitResults"/>
 		</div>
 		<div class="btn-group" role="group" aria-label="New Order Button">
 			<input type="submit" value="建立新訂單" ng-click="mainCtrl.clearAdd()" ng-disabled="!mainCtrl.fieldsDisabled" class="btn btn-primary" id="clearAdd"/>
 		</div>
 		<div class="btn-group" role="group" aria-label="Close Page Button">
 			<input type="button" value="關閉" onclick="document.location.href='${urlPrefix}/list'" class="btn btn-default" ng-if="mainCtrl.login"/>
 		</div>
 	</div>
 	<div class="row" ng-if="mainCtrl.salesNo">
 		<br>
 		<br>
 		<h4><b>訂單建立成功</b></h4>
 		<h5>這樣就完成表單咯~ 接下來請完成訂金匯款並填寫匯款回條</h5>
 		<h5><b>郵局700</b></h5>
 		<h5><b>帳號:0002123-0169388</b></h5>
 		<h5><b>戶名:王逸凡</b></h5>
 		<br>
 		<a href="{{mainCtrl.replyUri}}?fbNickname={{mainCtrl.contact.fbNickname}}&mobile={{mainCtrl.contact.mobile}}&salesNo={{mainCtrl.salesNo}}" target="_blank" class="btn btn-primary">開啟匯款回條</a>
 		<br>
 		<p>所有資料都可以在社團置頂文找到，有任何問題再跟我們聯絡喲</p>
 		<p>然後要強烈建議加小幫手好友才不會漏訊息哦</p>
 		<a href="https://www.facebook.com/amiao.wang.9?pnref=story" target="_blank" class="btn btn-primary">聯絡阿喵小幫手</a>
 		<a href="https://www.facebook.com/wang.miko.71?pnref=story" target="_blank" class="btn btn-primary">聯絡Miko小幫手</a>
 	</div> 	
 	</div>
</form>

</div>
<script type="text/javascript">
// ref. https://read01.com/NoeJa.html
function BigDecimal(init){
	var r = parseToNum(init),
	 	initPoint = decimalPoint(r);
	function parseToNum(input){
		var type = typeof input;
		if(type === 'number'){
			return input;
		}else if(type === 'string'){
			return parseFloat(input);
		}
		throw new Error('input\'s type is ' + type + ', val is ' + input + ', not valid number');
	}
	function decimalPoint(input){
		var s = input.toString();
		if(s.indexOf('.') == -1){
			return 0;
		}
		var point = s.split('.')[1].length;
		return point;
	}
	this.add = function(added){
		var point = decimalPoint(added),
			addMaxPoint = Math.max(initPoint, point),
			m = Math.pow(10, addMaxPoint);
		var result = (r * m + added * m) / m;
		//result = parseFloat(result.toFixed(addMaxPoint));
		return new BigDecimal(result);
	};
	this.subtract = function(minuend){
		var point = decimalPoint(minuend),
			subtractMaxPoint = Math.max(initPoint, point),
			m = Math.pow(10, subtractMaxPoint);
		var result = (r * m - minuend * m) / m;
		result = parseFloat(result.toFixed(subtractMaxPoint));
		return new BigDecimal(result);
	};
	this.multiply = function(multiplier){
		var point = decimalPoint(multiplier),
			multiplyMaxPoint = initPoint + point,
			s1 = r.toString().replace('.', ''),
			s2 = multiplier.toString().replace('.', '');
		var result = parseFloat(s1) * parseFloat(s2) / Math.pow(10, multiplyMaxPoint);
		//result = parseFloat(result.toFixed(multiplyMaxPoint));
		return new BigDecimal(result);
	};
	this.divide = function(divisor){
		var point = decimalPoint(divisor),
			s1 = r.toString().replace('.', ''),
			s2 = divisor.toString().replace('.', '');
		var result = (parseFloat(s1) / parseFloat(s2)) * Math.pow(10, point - initPoint);
		return result;
	};
	this.getNumber = function(){
		return r;
	};
	this.toString = function(){
		return r.toString();
	};
}
</script>
<script type="text/javascript">
	angular.module('angryCatAmericanGroupBuyOrderFormViewApp', ['erp.date.service', 'erp.ajax.service', 'mgcrea.ngStrap', 'ngCookies', 'ngAnimate'])
		.constant('urlPrefix', '${urlPrefix}')
		.constant('login', "${sessionScope['sessionUser']}" ? true : false)
		.constant('americanGroupBuy', ${americanGroupBuy == null ? "null" : americanGroupBuy})
		.constant('isOrderFormDisabled', ${isOrderFormDisabled == null ? "true" : isOrderFormDisabled})
		.constant('replyUri', '${replyUri == null ? "#" : replyUri}')
		.constant('moduleName', '${moduleName == null ? "null" : moduleName}')
		.controller('MainCtrl', ['$scope', 'DateService', 'AjaxService', 'urlPrefix', 'login', 'americanGroupBuy', '$cookies', 'moduleName', '$alert', 'isOrderFormDisabled', 'replyUri', '$modal', function($scope, DateService, AjaxService, urlPrefix, login, americanGroupBuy, $cookies, moduleName, $alert, isOrderFormDisabled, replyUri, $modal){
			var self = this,
				saveUrl = urlPrefix + '/batchSaveOrMerge.json',
				deleteUrl = urlPrefix + '/deleteByIds.json';
			self.replyUri = replyUri;
			self.addQualify = function(){
				if(!self.qualifies){
					self.qualifies = [];
				}
				self.qualifies.push({salesType: '正取'});
			};
			self.showQualifyAddBtn = function(qualify){
				var idx = self.qualifies.indexOf(qualify),
					lastIdx = self.qualifies.length - 1;
				return idx == lastIdx;
			};
			self.addWait = function(){
				if(!self.waits){
					self.waits = [];
				}
				self.waits.push({salesType: '備取'});
			};
			self.showWaitAddBtn = function(wait){
				var idx = self.waits.indexOf(wait),
					lastIdx = self.waits.length - 1;
				return idx == lastIdx;
			};
			self.checkModelIdDuplicated = function(form, srcName){
				var isQualify = srcName == 'qualify',
					destName = isQualify ? 'wait' : 'qualify';
					src = isQualify ? self.qualifies : self.waits,
					dest = isQualify ? self.waits : self.qualifies,
					duplicated = [];
				// 為了簡化後面的程序，先將所有編號檢核設為通過				
				function setValidityAsTrue(container, moduleName){
					var input = moduleName + '_modelId',
						validate = moduleName + 'ModelIdDuplicated';
					for(var i = 0; i < container.length; i++){
						var inputTarget = form[input+i];
						if(inputTarget){
							inputTarget.$setValidity(validate, true);	
						}
					}
					self[validate] = null;
				}
				setValidityAsTrue(src, srcName);
				setValidityAsTrue(dest, destName);
				
				var destModelIds = []; // 收集所有拿來比對的編號
				for(var i = 0; i < dest.length; i++){
					var modelId = dest[i].modelId;
					if(modelId){
						destModelIds.push(modelId);
					}
				}
				
				var inputField = srcName + '_modelId',
					validation = srcName + 'ModelIdDuplicated';
				for(var i = 0; i < src.length; i++){
					var modelId = src[i].modelId,
						item = form[inputField+i];
					if(!modelId || !item){
						continue;
					}
					if(destModelIds.indexOf(modelId) >= 0){
						duplicated.push(modelId);
						item.$setValidity(validation, false);
					}
				}
				if(duplicated.length != 0){
					self[validation] = '備取編號: ' + duplicated.join(', ') + '與正取重複，請另填備取或正取商品';
				}
			};			
			if(americanGroupBuy){
				self.americanGroupBuy = americanGroupBuy;
			}else{
				self.americanGroupBuy = {};
			}
			// subAmtUSD: 正取小計(美金) / totalAmtNTD:代購總金額(台幣)
			var cal = {subAmtUSD: 0, totalAmtNTD: 0};
			self.calculation = cal;
			var DEFAULT_COUNT = 1;
			function repeatCall(func, count){
				for(var i = 0; i < count; i++){
					func();
				}
			}
			function toDefault(){
				self.qualifies = [];
				self.waits = [];
				repeatCall(self.addQualify, DEFAULT_COUNT);
				repeatCall(self.addWait, DEFAULT_COUNT);
				cal.subAmtUSD = 0;
				cal.totalAmtNTD = 0;
				self.selectedGift = {salesType: '贈品', productAmtUSD: 0};
			}
			toDefault();
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
			self.isOrderFormDisabled = function (){
				return isOrderFormDisabled;
			};
			function isNumeric(input){
				return !isNaN(parseInt(input, 10));
			}
			self.subtract = function(subtracted, subtract){
				if((typeof subtracted) === 'number' && (typeof subtract) !== 'number'){
					return subtracted;
				}
				var diff = new BigDecimal(subtracted).subtract(subtract).getNumber();
				return diff;
			};
			self.calculateTotalAmt = function(nums){
				if(!nums){
					return 0;
				}
				var totalAmt = new BigDecimal(0);
				for(var i = 0; i < nums.length; i++){
					var item = nums[i],
						productAmtUSD = item.productAmtUSD; // typeof productAmtUSD == 'number'
					if(!isNumeric(productAmtUSD)){
						productAmtUSD = 0;
					}
					totalAmt = totalAmt.add(productAmtUSD);
				}
				return totalAmt.getNumber();				
			}
			// 計算正取總額
			self.calculateQualifyTotalAmt = function(){
				var totalAmt = self.calculateTotalAmt(self.qualifies);
				return totalAmt;
			};
			self.isQualifyTotalAmtAchieveThreshold = function(){
				var b = self.calculateQualifyTotalAmt() >= americanGroupBuy.qualifyTotalAmtThreshold;
				return b;
			};
			// 計算備取總額
			self.calculateWaitTotalAmt = function(){
				var totalAmt = self.calculateTotalAmt(self.waits);
				return totalAmt;
			};
			self.isWaitTotalAmtAchieveThreshold = function(){
				var b = self.calculateWaitTotalAmt() >= americanGroupBuy.waitTotalAmtThreshold;
				return b;
			};
			
			// 計算小計美金總額(正取+贈品補差額)
			self.calculateSubAmtUSD = function(){
				var subAmtUSD = new BigDecimal(0);
				// 先計算贈品加購價
				if(self.selectedGift.productAmtUSD > 0){
					subAmtUSD = subAmtUSD.add(self.selectedGift.productAmtUSD);
				}
				// 再計算正取總額
				subAmtUSD = subAmtUSD.add(self.calculateQualifyTotalAmt());
				cal.subAmtUSD = subAmtUSD.getNumber();
				return cal.subAmtUSD;
			};
			// 計算代購台幣總金額
			self.calculateTotalAmtNTD = function(subAmtUSD){
				subAmtUSD = subAmtUSD ? subAmtUSD : self.calculateSubAmtUSD();
				var stateTax = americanGroupBuy.multiplier,
					rate = americanGroupBuy.rate,
					serviceChargeNTD = americanGroupBuy.serviceChargeNTD;
				// cal.totalAmtNTD = subAmtUSD * stateTax * rate + serviceChargeNTD;
				cal.totalAmtNTD = new BigDecimal(subAmtUSD).multiply(stateTax).multiply(rate).add(serviceChargeNTD).getNumber();
				cal.totalAmtNTD = Math.ceil(cal.totalAmtNTD);
				return cal.totalAmtNTD;
			};
			// 計算正取美金總額及代購台幣總金額
			self.calculateResult = function(){
				if(!self.isQualifyTotalAmtAchieveThreshold()){
					if(self.selectedGift.id){
						self.deleteOrderForm(self.selectedGift);
					}
					if(self.selectedGift.productName){
						self.selectedGift = {salesType: '贈品', productAmtUSD: 0};	
					}
				}
				var subAmtUSD = self.calculateSubAmtUSD();
				self.calculateTotalAmtNTD(subAmtUSD);
			};
			
			// 選擇贈品規則: 正取選擇總金額滿125美金，可選擇一款65美金的手鍊或手環
			var giftPrice = {
				'經典款純銀蛇鍊':		{modelId: '590702HV',	amtUSD: 65},
				'經典款黑銀蛇鍊': 		{modelId: '590702OX', 	amtUSD: 65},
				'簡約圓釦頭蛇鍊': 		{modelId: '590728', 	amtUSD: 65},
				'經典款純銀手環': 		{modelId: '590713', 	amtUSD: 65},
				'純銀愛心釦頭蛇鍊': 	{modelId: '590719', 	amtUSD: 65},
				'Essence 純銀蛇鍊': 	{modelId: '596000', 	amtUSD: 60},
				'Essence 純銀珠鍊': 	{modelId: '596002', 	amtUSD: 60},
				'Essence 純銀硬環': 	{modelId: '596006', 	amtUSD: 60},
				'心鑽蛇鍊': 			{modelId: '590743CZ', 	amtUSD: 75},
				'2017春季花朵蛇鍊': 	{modelId: '590744CZ', 	amtUSD: 80},
				'方形滿鑽蛇鍊': 		{modelId: '590723CZ', 	amtUSD: 80},
				'心滿鑽蛇鍊': 			{modelId: '590727CZ', 	amtUSD: 80},
				'迪士尼米奇蛇鍊': 		{modelId: '590731CZ', 	amtUSD: 85},
				'Pandora logo金銀雙色蛇鍊': {modelId: '590741CZ', amtUSD: 100},
			};
			var giftNames = [];
			for(var p in giftPrice){
				if(giftPrice.hasOwnProperty(p)){
					giftNames.push(p);
				}
			}
			self.gifts = genOptions(giftNames);
			var sizes = {
				'經典款純銀蛇鍊': 
					['17cm',
					'18cm',
					'19cm',
					'20cm',
					'21cm',
					'23cm']
			};
			sizes['經典款黑銀蛇鍊'] = sizes['經典款純銀蛇鍊'];
			sizes['簡約圓釦頭蛇鍊'] = sizes['經典款純銀蛇鍊'];
			sizes['純銀愛心釦頭蛇鍊'] = sizes['經典款純銀蛇鍊'];
			sizes['心鑽蛇鍊'] = sizes['經典款純銀蛇鍊'];
			sizes['2017春季花朵蛇鍊'] = sizes['經典款純銀蛇鍊'];
			sizes['方形滿鑽蛇鍊'] = sizes['經典款純銀蛇鍊'];
			sizes['心滿鑽蛇鍊'] = sizes['經典款純銀蛇鍊'];
			sizes['迪士尼米奇蛇鍊'] = sizes['經典款純銀蛇鍊'];
			sizes['Pandora logo金銀雙色蛇鍊'] = sizes['經典款純銀蛇鍊'];
			
			sizes['經典款純銀手環'] = [
				'S 17cm',
				'M 19cm',
				'L 21cm',
			];
			
			sizes['Essence 純銀蛇鍊'] = [
				'16cm',
				'17cm',
				'18cm',
				'19cm',
				'20cm',
				'21cm',
			];
			sizes['Essence 純銀珠鍊'] = sizes['Essence 純銀蛇鍊'];
			
			sizes['Essence 純銀硬環'] = [
				'S 16cm',
			    'M 18cm',
			    'L 20cm',
			];
			// 沒有尺寸: Essence珠鍊、愛心滿鑽釦頭蛇鍊、2016新款迪士尼蛇鍊
			self.changeGiftName = function(selectedGiftName){
				var selectedGift = giftPrice[selectedGiftName],
					price = selectedGift.amtUSD;
				price = new BigDecimal(price).subtract(self.americanGroupBuy.giftValAmtUSD).getNumber();
				if(price < 0){
					price = 0;
				}
				self.selectedGift.productAmtUSD = price;
				self.selectedGift.modelId = selectedGift.modelId;
				self.calculateTotalAmtNTD();
				// 產生尺寸清單
				var size = sizes[selectedGiftName];
				if(size && size.length && size.length > 0){
					self.giftSizes = genOptions(size);	
				}else{
					self.giftSizes = [];
				}
				self.selectedGift.size = null;
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
				if(msg){
					console.log(msg);	
				}
				var myModal = $modal({content: '訂單已成功送出!\n(可再填寫新訂單)', placement: 'center'});
				var jqlite = angular.element(document.getElementById('submitResults'));			
				jqlite.attr('value', '提交新訂單');
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
					if(!item.productName || !isNumeric(item.productAmtUSD)){
						continue;
					}
					var newItem = {};
					newItem = copyTo(newItem, item, ['salesType', 'modelId', 'productName', 'productAmtUSD', 'size']);
					newItem = copyTo(newItem, self.contact, ['fbNickname', 'mobile', 'email']);
					newItem = copyTo(newItem, self.calculation, ['totalAmtNTD']);
					r.push(newItem);
				}
				return r;
			}
			self.isSubmitPrepared = function(targetForm){
				var formValid = !targetForm.$invalid,
					qualifyValid = self.qualifies.length > 0,
					qualifyTotalAmtAchieveThreshold = self.isQualifyTotalAmtAchieveThreshold(),
					waitTotalAmtAchieveThreshold = self.isWaitTotalAmtAchieveThreshold();
				return formValid && qualifyValid && qualifyTotalAmtAchieveThreshold && waitTotalAmtAchieveThreshold;
			};
			self.fieldsDisabled = false;
			self.clearAdd = function(){
				self.fieldsDisabled = false;
				self.salesNo = null;
				toDefault();
				$modal({content: '已開啟新訂單'});
			};
			self.copyAdd = function(){
				self.fieldsDisabled = false;
				self.salesNo = null;
			};
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
						// 提交後應disabled整個form
						/*
						if(qualifies.length > 0){
							self.qualifies = d.splice(0, qualifies.length);
						}
						if(waits.length > 0){
							self.waits = d.splice(0, waits.length);
						}
						if(d.length == 1){
							self.selectedGift = d[0];
						}
						*/
						saveFilled();
						alertSaveSuccess();
						document.getElementById('submitResults').value = '提交新訂單';
						self.fieldsDisabled = true;
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