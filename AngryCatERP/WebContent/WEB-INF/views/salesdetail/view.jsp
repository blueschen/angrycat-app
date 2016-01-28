<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="${pageContext.request.contextPath}/${moduleName}" var="urlPrefix"/>
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
<input type="hidden" value="{{mainCtrl.salesDetail.memberId}}"/>


<div class="container">
	<div class="col-sm-offset-2">
		<h2>銷售明細資料</h2>
	</div>
<form class="form-horizontal" name="salesDetailForm">
 	<div class="form-group">
 		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="salePoint">
 				銷售點
 			</label>
 			<div class="col-sm-7">
 				<select
 					ng-model="mainCtrl.salesDetail.salePoint"
 					ng-options="g.value as g.label for g in mainCtrl.salePoints"
 					id="salePoint"
 					class="form-control">
 					<option value="">==請選擇==</option>
 				</select>
 			</div> 		
 		</div>
 		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="saleStatus">
 				狀態
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.salesDetail.saleStatus" id="saleStatus" class="form-control"/>
 			</div> 		
 		</div>
 	</div>
	<div class="form-group">
 		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="fbName">
 				FB名稱
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.salesDetail.fbName" id="fbName" class="form-control"/>
 			</div> 		
 		</div>
 		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="activity">
 				社團/fanpage
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.salesDetail.activity" id="activity" class="form-control"/>
 			</div> 		
 		</div> 		
	</div>
	<div class="form-group">
 		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="modelId">
 				型號
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.salesDetail.modelId" id="modelId" class="form-control"/>
 			</div> 		
 		</div>
 		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="productName">
 				明細
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.salesDetail.productName" id="productName" class="form-control"/>
 			</div> 		
 		</div> 		
	</div>
	<div class="form-group">
 		<div class="form-group col-sm-5" ng-class="{'has-error': salesDetailForm.price.$invalid}">
 			<label class="col-sm-5 control-label" for="price">
 				含運金額
 			</label>
 			<div class="col-sm-7">
 				<input type="number" ng-model="mainCtrl.salesDetail.price" id="price" name="price" class="form-control" integer/>
 			</div> 		
 		</div>
 		<div class="form-group col-sm-5" ng-class="{'has-error': salesDetailForm.memberPrice.$invalid}">
 			<label class="col-sm-5 control-label" for="memberPrice">
 				會員價(實收價格)
 			</label>
 			<div class="col-sm-7">
 				<input type="number" ng-model="mainCtrl.salesDetail.memberPrice" id="memberPrice" name="memberPrice" class="form-control" integer/>
 			</div> 		
 		</div> 		
	</div>
	<div class="form-group">
 		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="priority">
 				順序
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.salesDetail.priority" id="priority" class="form-control"/>
 			</div> 		
 		</div>
 		<div class="form-group col-sm-5" ng-class="{'has-error': salesDetailForm.orderDate.$invalid}">
 			<label class="col-sm-5 control-label" for="orderDate">
 				接單日
 			</label>
 			<div class="col-sm-7">
 				<input id="orderDate"
 					class="form-control" 
 					ng-model="mainCtrl.salesDetail.orderDate" 
 					name="orderDate" 
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
 		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="checkBillStatus">
 				對帳狀態
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.salesDetail.checkBillStatus" id="checkBillStatus" class="form-control"/>
 			</div> 		
 		</div>
 		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="idNo">
 				身份證字號
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.salesDetail.idNo" id="idNo" class="form-control"/>
 			</div> 		
 		</div> 		
	</div>
 	<div class="form-group">
 		<div class="form-group col-sm-5">
			<label class="col-sm-5 control-label" for="discountType">
 				折扣類型
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.salesDetail.discountType" id="discountType" class="form-control"/>
 			</div>
 		</div>
 		<div class="form-group col-sm-5">
			<label class="col-sm-5 control-label" for="arrivalStatus">
 				已到貨
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.salesDetail.arrivalStatus" id="arrivalStatus" class="form-control"/>
 			</div>
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="form-group col-sm-5" ng-class="{'has-error': salesDetailForm.shippingDate.$invalid}">
 			<label class="col-sm-5 control-label" for="shippingDate">
 				出貨日
 			</label>
 			<div class="col-sm-7">
 				<input id="shippingDate"
 					class="form-control" 
 					ng-model="mainCtrl.salesDetail.shippingDate" 
 					name="shippingDate" 
 					bs-datepicker 
 					type="text" 
 					autoclose="1"
 					date-format="yyyy-MM-dd"
 					placeholder="yyyy-MM-dd"
 					date-type="string">			      
 			</div>	 		
 		</div>
 		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="sendMethod">
 				郵寄方式
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.salesDetail.sendMethod" id="sendMethod" name="sendMethod" class="form-control"/>
 			</div> 		
 		</div>
 	</div>
 	<div class="form-group">
 		<div class="form-group col-sm-5" ng-class="{'has-error': salesDetailForm.payDate.$invalid}">
 			<label class="col-sm-5 control-label" for="payDate">
 				付款日期
 			</label>
 			<div class="col-sm-7">
 				<input id="payDate"
 					class="form-control" 
 					ng-model="mainCtrl.salesDetail.payDate" 
 					name="payDate" 
 					bs-datepicker 
 					type="text" 
 					autoclose="1"
 					date-format="yyyy-MM-dd"
 					placeholder="yyyy-MM-dd"
 					date-type="string">			      
 			</div>	 		
 		</div>
 		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="contactInfo">
 				郵寄地址電話
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.salesDetail.contactInfo" id="contactInfo" name="contactInfo" class="form-control"/>
 			</div> 		
 		</div> 		
 	</div>
 	<div class="form-group">
 		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="registrant">
 				登單者
 			</label>
 			<div class="col-sm-7">
 				<input type="text" ng-model="mainCtrl.salesDetail.registrant" id="registrant" name="registrant" class="form-control"/>
 			</div> 		
 		</div> 
 	</div> 	 	
 	<div class="form-group">
		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="note">
 				備註	
 			</label>
 			<div class="col-sm-7">
 				<textarea ng-model="mainCtrl.salesDetail.note" id="note" rows="3" cols="30"  class="form-control"></textarea>
 			</div>		
		</div>
 		<div class="form-group col-sm-5">
 			<label class="col-sm-5 control-label" for="otherNote">
 				其他備註	
 			</label>
 			<div class="col-sm-7">
 				<textarea ng-model="mainCtrl.salesDetail.otherNote" id="otherNote" rows="3" cols="30"  class="form-control"></textarea>
 			</div>
 		</div>		
 	</div>
 	<div class="form-group">
 		<div class="col-sm-offset-3">
 			<input type="submit" value="儲存" ng-click="mainCtrl.save()" ng-disabled="salesDetailForm.$invalid" class="btn btn-default"/>
 			<input type="button" value="關閉" onclick="document.location.href='${urlPrefix}/list'" class="btn btn-default"/>
 		</div>
 	</div>
</form>

</div>
<script type="text/javascript">
	angular.module('angryCatSalesDetailViewApp', ['erp.date.service', 'erp.ajax.service', 'mgcrea.ngStrap'])
		.constant('urlPrefix', '${urlPrefix}')
		.constant('login', "${sessionScope['sessionUser']}" ? true : false)
		.constant('targetData', ${salesDetail == null ? "null" : salesDetail})
		.controller('MainCtrl', ['$scope', 'DateService', 'AjaxService', 'urlPrefix', 'login', 'targetData', function($scope, DateService, AjaxService, urlPrefix, login, targetData){
			var self = this,
				saveUrl = urlPrefix + '/save.json';
			self.salesDetail = {};
			
			if(targetData){
				self.salesDetail = targetData;
			}
			self.save = function(){
				var isNew = self.salesDetail.id ? false : true;
				AjaxService.post(saveUrl, self.salesDetail)
					.then(function(response){
						self.salesDetail = response.data;
						alert('儲存成功!!');
					},
					function(errResponse){
						alert('儲存失敗，錯誤訊息: ' + JSON.stringify(errResponse));
					});
			};
			self.login = login;
			self.salePoints = [{label: 'FB社團', value: 'FB社團'}, {label: '敦南誠品', value: '敦南誠品'}];
		}]);
</script>
</body>
</html>