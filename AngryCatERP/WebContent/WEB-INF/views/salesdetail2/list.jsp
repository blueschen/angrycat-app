<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="${pageContext.request.contextPath}" var="rootPath"/>
<c:set value="${moduleName}KendoData" var="kendoDataKey"/>
<c:set value="${moduleName}SelectedCondition" var="selectedCondition"/>
<c:set value="${moduleName}Parameters" var="parameters"/>
<c:set value="${rootPath}/${moduleName}" var="moduleBaseUrl"/>
<c:set value="${rootPath}/vendor/kendoui/professional.2016.1.226.trial" var="kendouiRoot"/>
<c:set value="${kendouiRoot}/styles" var="kendouiStyle"/>
<c:set value="${kendouiRoot}/js" var="kendouiJs"/>
<c:set value="${rootPath}/common/angrycat" var="angrycatRoot"/>
<c:set value="${angrycatRoot}/js" var="angrycatJs"/>
<c:set value="${angrycatRoot}/styles" var="angrycatStyle"/>
<c:set value="${rootPath}/vendor/bootstrap/3.3.5" var="bootstrapRoot"/>
<c:set value="${bootstrapRoot}/css" var="bootstrapCss"/>
<c:set value="${bootstrapRoot}/js" var="bootstrapJs"/>
   
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8"/>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>Kendo UI Grid實作銷售明細</title>
	
	<link rel="stylesheet" href="${kendouiStyle}/kendo.common.min.css">
	<link rel="stylesheet" href="${kendouiStyle}/kendo.default.min.css">
	
	<link rel="stylesheet" href="${angrycatStyle}/kendo.grid.css">
	
	<link rel="stylesheet" href="${bootstrapCss}/bootstrap.css">
	<link rel="stylesheet" href="${bootstrapCss}/bootstrap-theme.css">
	
</head>
<body>

<jsp:include page="/WEB-INF/views/navigation.jsp"></jsp:include>

<div class="container-fluid">
<div class="well">
</div>
	<span id="updateInfoWindow" style="display:none;"></span>
	<div id="mainGrid"></div>
	<div id="updateNoti"></div>
</div>
	<script type="text/javascript" src="${kendouiJs}/jquery.min.js"></script>
	<script type="text/javascript" src="${kendouiJs}/kendo.web.min.js"></script>
	<script type="text/javascript" src="${kendouiJs}/messages/kendo.messages.zh-TW.min.js"></script>
	<script type="text/javascript" src="${bootstrapJs}/bootstrap.min.js"></script>
	
	<script type="text/javascript" src="${angrycatJs}/angrycat.js"></script>
	<script type="text/javascript" src="${angrycatJs}/angrycat.kendo.grid.js"></script>	
	<script type="text/javascript">
		(function($, kendo, angrycat){"use strict"
			var lastKendoData = ${sessionScope[kendoDataKey] == null ? "null" : sessionScope[kendoDataKey]},
				lastSelectedCondition = ${sessionScope[selectedCondition] == null ? "null" : sessionScope[selectedCondition]},
				parameters = ${requestScope[parameters] == null ? "null" : requestScope[parameters]},
				lockedFlag = true,
				opts = {
					moduleName: "${moduleName}",
					rootPath: "${rootPath}",
					moduleBaseUrl: "${moduleBaseUrl}",
					gridId: "#mainGrid",
					notiId: "#updateNoti",
					updateInfoWindowId: "#updateInfoWindow",
					page: 1,
					pageSize: 15,
					filter: null,
					sort: null,
					group: null,
					editMode: "incell",
					pk: "id",
					lastKendoData: lastKendoData,
					lastSelectedCondition: lastSelectedCondition,
					addRowInit: function(dataItem, editRow){
						dataItem.set("orderDate", new Date());
					},
					lockedFlag: lockedFlag
				};
			
			function fieldsReadyHandler(){
				var context = this,
					discountTypeSelectAction = function(model, dataItem){
						var discount = dataItem.localeNames ? dataItem.localeNames.discount : null;
						if($.isNumeric(discount) && $.isNumeric(model.price)){
							model.set("memberPrice", Math.floor(model.price * parseFloat(discount))); // 無條件捨去
						}
					},
					defaultAutoCompleteFilter = "contains",
					paramEditors = context.getParameterDropDownEditors(parameters, {"折扣別": {selectAction: discountTypeSelectAction}}),
					paramFEditors = context.getParameterFilterEditorColumns(parameters, {filter: defaultAutoCompleteFilter}),
					hidden = {hidden: true},
					locked = {locked: lockedFlag},
					uneditable = {editable: false},
					memberFieldName = "member",
					memberField = {
						type: null,
						validation: {
							isEffectiveMember: context.getDefaultFieldAutoCompleteValidation({
								field: memberFieldName,
								method: "isEffectiveMember",
								validate: function(opts){
									var val = opts.val;
									return val && !val.id;
								},
								msg: "請選擇有效會員資料"
							})
						}
					},
					memberColumn = {
						template: "<span title='#=(member ? member.name : '')#'>#=(member ? member.name : '')#</span>",
						filterable: {
							cell: {
								inputWidth: "100%",
								template: function(args){
									context.getDefaultAutoCompleteFilterEditor({
										ele: args.element,
										dataTextField: "name",
										dataValueField: "name",
										filter: defaultAutoCompleteFilter,
										action: "queryMemberAutocomplete",
										autocompleteFieldsToFilter: ["name"]
									});
								}
							}
						},
						locked: lockedFlag
					},
					memberEditor = context.getAutoCompleteCellEditor({
						textField: "name",
						valueField: "id",
						action: "queryMemberAutocomplete", 
						filter: "contains", 
						autocompleteFieldsToFilter: ["name", "nameEng", "idNo"],
						errorMsgFieldName: memberFieldName,
						selectAction: function(model, dataItem){
							model.set("fbName", dataItem.fbNickname);
							model.set("idNo", dataItem.idNo);
							model.set("mobile", dataItem.mobile);
						}
					}),
					modelIdFieldName = "modelId",
					modelIdEditor = context.getAutoCompleteCellEditor({
						textField: "modelId",
						action: "queryProductAutocomplete", 
						filter: defaultAutoCompleteFilter, 
						autocompleteFieldsToFilter: ["modelId", "nameEng"],
						selectAction: function(model, dataItem){
							model.set("productName", dataItem.nameEng);
							model.set("price", dataItem.suggestedRetailPrice);
							model.set("memberPrice", dataItem.suggestedRetailPrice);
							model.set("discountType", null);
						},
						valuePrimitive: true
					}),
					mobileFieldName = "mobile",
					mobileField = {
						validation: {
							isMobile: function(input){
								if(input.is("[name='"+mobileFieldName+"']") && input.val()){
									input.attr("data-isMobile-msg", "請輸入有效的十碼數字");
									return /09[0-9]{8}/g.test(input.val());
								}
								return true;
							}
						}
					},
					saleStatusColumn = $.extend({}, locked, paramFEditors["銷售狀態"]),
					salePointColumn = $.extend({}, locked, paramFEditors["銷售點"]),
					fields = [
		       			//0fieldName		1column title		2column width	3field type	4column filter operator	5field custom		6column custom			7column editor
						["saleStatus",		"狀態",				100,			"string",	"eq",					null,				saleStatusColumn,		paramEditors["銷售狀態"]],
						["fbName",			"姓名",				150,			"string",	"contains",				null,				locked],
						[memberFieldName,	"會員資料",			150,			"string",	"contains",				memberField,		memberColumn,			memberEditor],
						["salePoint",		"銷售點",				100,			"string",	"eq",					null,				salePointColumn,		paramEditors["銷售點"]],
						[modelIdFieldName,	"型號",				150,			"string",	"startswith",			null,				locked,					modelIdEditor],
						["productName",		"明細",				150,			"string",	"contains",				null,				locked],
						["price",			"定價",				100,			"number",	"gte"],
						["memberPrice",		"實收",				100,			"number",	"gte"],
						["discountType",	"折扣別",				150,			"string",	"contains",				null,				paramFEditors["折扣別"],	paramEditors["折扣別"]],						
						["orderDate",		"銷售日",				150,			"date",		"gte"],
						["payDate",			"付款日",				150,			"date",		"gte"],
						["payType",			"付款別",				150,			"string",	"contains",				null,				paramFEditors["付款別"],	paramEditors["付款別"]],
						["payStatus",		"付款狀態",			150,			"string",	"contains",				null,				paramFEditors["付款狀態"],paramEditors["付款狀態"]],
						["shippingDate",	"出貨日",				150,			"date",		"gte"],
						["sendMethod",		"郵寄方式",			150,			"string",	"eq",					null,				paramFEditors["郵寄方式"],paramEditors["郵寄方式"]],
						["registrant",		"登單者",				150,			"string",	"contains"],
						["note",			"備註",				150,			"string",	"contains"],
						[mobileFieldName,	"手機",				150,			"string",	"contains",				mobileField],
						["idNo",			"身份證",				150,			"string",	"contains"],
						["checkBillStatus",	"對帳狀態",			150,			"string",	"contains"],
						["arrivalStatus",	"已到貨",				150,			"string",	"eq",					null,				hidden],
						["contactInfo",		"郵寄地址電話",		150,			"string",	"contains",				null,				hidden],
						["activity",		"活動",				150,			"string",	"contains",				null,				hidden],
						["priority",		"順序",				150,			"string",	"eq",					null,				hidden],
						["otherNote",		"其他備註",			150,			"string",	"contains",				null,				hidden],
						[opts.pk,			"SalesDetail ID",	150,			"string",	"eq",					null,				hidden],
						["rowId",			"Excel序號",			150,			"string",	"contains",				uneditable,			hidden]
					];
				//console.log("paramFEditors: " + (paramFEditors["銷售狀態"]["filterable"]["cell"]["template"]));
				return fields;
			}
			
			angrycat.kendoGridService
				.init(opts)
				.fieldsReady(fieldsReadyHandler);
		})(jQuery, kendo, angrycat);			
	</script>
</body>
</html>