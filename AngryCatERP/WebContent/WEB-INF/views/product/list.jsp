<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="${pageContext.request.contextPath}" var="rootPath"/>
<c:set value="${moduleName}KendoData" var="kendoDataKey"/>
<c:set value="${moduleName}SelectedCondition" var="selectedCondition"/>
<c:set value="${rootPath}/${moduleName}" var="moduleBaseUrl"/>
<c:set value="${moduleBaseUrl}/downloadImage" var="loadImgUrl"/>
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
	<title>Kendo UI Grid實作產品暨庫存表</title>
	
	<link rel="stylesheet" href="${kendouiStyle}/kendo.common.min.css">
	<link rel="stylesheet" href="${kendouiStyle}/kendo.default.min.css">
	
	<link rel="stylesheet" href="${angrycatStyle}/kendo.grid.css">
	
	<link rel="stylesheet" href="${bootstrapCss}/bootstrap.css">
	<link rel="stylesheet" href="${bootstrapCss}/bootstrap-theme.css">
	
</head>
<body>

<jsp:include page="/WEB-INF/views/navigation.jsp"></jsp:include>

<div class="container-fluid">
<hr>
</div>
	<span id="updateInfoWindow" style="display:none;"></span>
	<div id="mainGrid"></div>
	<div id="updateNoti"></div>
	<div id="productImgWindow"></div>
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
				loadImgUrl = '${loadImgUrl}' + '/',
				lockedFlag = true,
				calcTotalStockQty = function(e){
					var columnIndex = this.cellIndex(e.container),
		        		fieldName = this.thead.find("th").eq(columnIndex-1).attr("data-field");
		        	console.log("fieldName:"+ fieldName);
					if(!fieldName || !fieldName.endsWith("StockQty")){
						return;
					}
					var input = e.container.find(".k-input");
					input.blur(function(){
						var model = e.model,
							officeStockQty = model.get("officeStockQty"),
							drawerStockQty = model.get("drawerStockQty"),
							showcaseStockQty = model.get("showcaseStockQty"),
							notShipStockQty = model.get("notShipStockQty"),
							drawerInZhongheStockQty = model.get("drawerInZhongheStockQty"),
							showcaseInZhongheStockQty = model.get("showcaseInZhongheStockQty")
						;
						var totalStockQty = officeStockQty + drawerStockQty + showcaseStockQty;
						totalStockQty -= notShipStockQty;
						totalStockQty -= drawerInZhongheStockQty;
						totalStockQty -= showcaseInZhongheStockQty;
						model.set("totalStockQty", totalStockQty);
					});
				},
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
					lockedFlag: lockedFlag,
					editAction: calcTotalStockQty
				};
			
			function fieldsReadyHandler(){
				var context = this,
					defaultAutoCompleteFilter = "contains",
					hidden = {hidden: true},
					locked = {locked: lockedFlag},
					uneditable = {editable: false},
					imgColumn = {
						template: "<img alt='#=(modelId ? modelId : '沒有型號')#' class='productImg' style='height:50px; width:50px;' src='#=(modelId ? (\'"+loadImgUrl+"\' + modelId) : '')#'></span>"
					},
					productCategoryFieldName = "productCategory",
					productCategoryField = {
						type: null,
						validation: {
							isEffectiveProductCategory: context.getDefaultFieldAutoCompleteValidation({
								field: productCategoryFieldName,
								method: "isEffectiveProductCategory",
								validate: function(opts){
									var val = opts.val;
									return val && !val.id;
								},
								msg: "請選擇有效商品類別資料"
							})
						}
					},
					productCategoryColumn = {
						template: "<span title='#=(productCategory ? productCategory.code : '')#'>#=(productCategory ? productCategory.code : '')#</span>",
						filterable: {
							cell: {
								inputWidth: "100%",
								template: function(args){
									context.getDefaultAutoCompleteFilterEditor({
										ele: args.element,
										dataTextField: "code",
										dataValueField: "code",
										filter: defaultAutoCompleteFilter,
										action: "queryProductCategoryAutocomplete",
										autocompleteFieldsToFilter: ["code"]
									});
								}
							}
						}
					},
					productCategoryEditor = context.getAutoCompleteCellEditor({
						textField: "code",
						valueField: "id",
						action: "queryProductCategoryAutocomplete", 
						filter: "contains", 
						autocompleteFieldsToFilter: ["code"],
						errorMsgFieldName: productCategoryFieldName
					}),
					fields = [
		       			//0fieldName					1column title	2column width	3field type	4column filter operator	5field custom			6column custom			7column editor
		       			["modelId",						"型號",			150,			"string",	"eq"],
		       			["nameEng",						"英文名字",		200,			"string",	"eq"],
						["name",						"中文名字",		100,			"string",	"contains"],
						["suggestedRetailPrice",		"定價",			100,			"number",	"eq"],
						["imgDir",						"圖片",			100,			"string",	"contains",				uneditable,				imgColumn],
						["totalStockQty",				"總庫存",			100,			"number",	"eq"],
						["notShipStockQty",				"未出貨",			100,			"number",	"eq"],
						["officeStockQty",				"辦公室庫存",		150,			"number",	"eq"],
						["drawerStockQty",				"專櫃抽屜",		150,			"number",	"eq"],
						["showcaseStockQty",			"展示櫃",			100,			"number",	"eq"],
						["drawerInZhongheStockQty",		"中和庫存",		150,			"number",	"eq"],
						["showcaseInZhongheStockQty",	"中和展示",		150,			"number",	"eq"],
						["barcode",						"條碼號",			150,			"string",	"contains",				null,					hidden],
						["seriesName",					"系列名",			150,			"string",	"contains",				uneditable,				hidden],
						[productCategoryFieldName,		"商品類別代號",	150,			"string",	"contains",				productCategoryField,	productCategoryColumn,	productCategoryEditor],
						[opts.pk,						"Product ID",	150,			"string",	"eq",					uneditable,				hidden]
					];
				return fields;
			}
			
			function afterGridInitHandler(mainGrid){
				var displays = [],
					foundImg = false;
				var timer = setInterval(function(){
					// 因為template初始化很慢，所以設定timer
					var img = mainGrid.tbody.find("img.productImg");
					if(img.length > 0){
						clearInterval(timer);
						img.click(function(){
							var src = $(this).attr("src");
							if(!src){
								return;
							}
							var win = 
								$("#productImgWindow").kendoWindow({
									width: "650px",
									content: {
										template: "<img src='"+src+"'/>"
									}
								}).data("kendoWindow");
							win.center().open();
						});
					}
				},500);
			}
			
			angrycat.kendoGridService
				.init(opts)
				.fieldsReady(fieldsReadyHandler, afterGridInitHandler);
		})(jQuery, kendo, angrycat);			
	</script>
</body>
</html>