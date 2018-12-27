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
	
	<link rel="stylesheet" href="<c:url value="/vendor/kendoui/professional.2016.1.226.trial/styles/kendo.common.min.css"/>">
	<link rel="stylesheet" href="<c:url value="/vendor/kendoui/professional.2016.1.226.trial/styles/kendo.default.min.css"/>">
	
	<link rel="stylesheet" href="<c:url value="/common/angrycat/styles/kendo.grid.css"/>">
	
	<link rel="stylesheet" href="<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap.css"/>">
	<link rel="stylesheet" href="<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap-theme.css"/>">
</head>
<body>

<jsp:include page="/WEB-INF/views/navigation.jsp"></jsp:include>

<div class="container-fluid">
	<span id="updateInfoWindow" style="display:none;"></span>
	<div id="mainGrid"></div>
	<div id="updateNoti"></div>
</div>
	<script type="text/javascript" src="<c:url value="/vendor/kendoui/professional.2016.1.226.trial/js/jquery.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/kendoui/professional.2016.1.226.trial/js/kendo.web.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/kendoui/professional.2016.1.226.trial/js/messages/kendo.messages.zh-TW.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/bootstrap/3.3.5/js/bootstrap.min.js"/>"></script>
	
	<script type="text/javascript" src="<c:url value="/common/angrycat/js/angrycat.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/common/angrycat/js/angrycat.kendo.grid.js"/>"></script>	
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
					pageSize: 100,
					filter: null,
					sort: null,
					group: null,
					editMode: "incell",
					pk: "id",
					lastKendoData: lastKendoData,
					lastSelectedCondition: lastSelectedCondition,
					addRowInit: function(dataItem, editRow){
						dataItem.set("orderDate", new Date());
						dataItem.set("saleStatus", parameters["銷售狀態"][1].nameDefault);
						dataItem.set("salePoint", parameters["銷售點"][1].nameDefault);
					},
					lockedFlag: lockedFlag,
					docType: "${docType}"
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
		       			//0fieldName		1column title		2column width	3field type	4column filter operator	5field custom		6column custom			7column editor          8column template
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
						["checkBillStatus",	"對帳狀態",			150,			"string",	"contains"],
						//["payDate",			"付款日",				150,			"date",		"gte"],
						//["payType",			"付款別",				150,			"string",	"contains",				null,				paramFEditors["付款別"],	paramEditors["付款別"]],
						//["payStatus",		"付款狀態",			150,			"string",	"contains",				null,				paramFEditors["付款狀態"],paramEditors["付款狀態"]],
						["shippingDate",	"出貨日",				150,			"date",		"gte"],
						["sendMethod",		"郵寄方式",			150,			"string",	"eq",					null,				paramFEditors["郵寄方式"],paramEditors["郵寄方式"]],
						//["registrant",		"登單者",				150,			"string",	"contains"],
						["note",			"備註",				150,			"string",	"contains"],
						["orderNo",			"訂單編號",			150,			"string",	"startswith"],
						[mobileFieldName,	"手機",				150,			"string",	"contains",				mobileField],
						["idNo",			"身份證",				150,			"string",	"contains"],						
						["arrivalStatus",	"已到貨",				150,			"string",	"eq",					null,				hidden],
						["contactInfo",		"郵寄地址電話",		150,			"string",	"contains",				null,				hidden],
						["activity",		"活動",				150,			"string",	"contains",				null,				hidden],
						["priority",		"順序",				150,			"string",	"eq",					null,				hidden],
						["otherNote",		"其他備註",			150,			"string",	"contains",				null,				hidden],
						[opts.pk,			"SalesDetail ID",	150,			"string",	"eq",					uneditable,			hidden],
						["rowId",			"Excel序號",			150,			"string",	"contains",				uneditable,			hidden]
					];
				//console.log("paramFEditors: " + (paramFEditors["銷售狀態"]["filterable"]["cell"]["template"]));
				return fields;
			}
			function afterGridInitHandler(mainGrid){
				var changeStatusColor = 
					function(cell, status){
						if(!cell || !status){
							return;
						}
						var bgc = "white";
						var color = "white";
						switch (status) {
						case "10. 待出貨":
							bgc = "#ff99e6";
							break;
						case "20. 集貨中":
							bgc = "#ffa64d";
							break;
						case "30. 調貨中":
							bgc = "#66ff66";
							break;
						case "40. 待補貨":
							bgc = "#33ccff";
							break;
						case "99. 已出貨":
							bgc = "black";
							break;
						case "98. 作廢":
							bgc = "#999999";
							color = "black";
							break;
						}
						cell.css("background-color", bgc);
						cell.css("color", color);
					};
				
				var changeStatusColorIfRowCellContainsStatus = 
					function(row, status){
						var cells = row.children();
						for(var q = 0; q < cells.length; q++){
							var c = cells.eq(q);
							var f = context.getFieldViaCell(c);
							if(f == "saleStatus"){
								changeStatusColor(c, status);
								break;
							}
						}
					};
					
				var context = this;
				mainGrid.bind("dataBound", function(e){					
					var rows = mainGrid.items();
					if(rows.length == 0){
						return;
					}
					for(var i = 0; i < rows.length; i++){
						var row = $(rows[i]);
						var dataItem = mainGrid.dataItem(row);
						if(!dataItem){
							continue;
						}
						changeStatusColorIfRowCellContainsStatus(row, dataItem.get("saleStatus"));
					}
				});
				mainGrid.dataSource.bind("change", function(e){
					if (e.action != 'itemchange' && e.field != 'saleStatus'){
						return;
					}
					var rows = mainGrid.items();
					if(rows.length == 0){
						return;
					}
					for(var j = 0; j < rows.length; j++){
						var row = $(rows[j]);
						var dataItem = mainGrid.dataItem(row);
						if(!dataItem || dataItem !== e.items[0]){
							continue;
						}
						changeStatusColorIfRowCellContainsStatus(row, dataItem.get("saleStatus"));;
					}
				});
			}
			angrycat.kendoGridService
				.init(opts)
				.fieldsReady(fieldsReadyHandler, afterGridInitHandler);
		})(jQuery, kendo, angrycat);			
	</script>
</body>
</html>