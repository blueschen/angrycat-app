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
					lockedFlag: lockedFlag,
					docType: "${docType}"
				};
			
			function fieldsReadyHandler(){
				var context = this,
					defaultAutoCompleteFilter = "contains",
					hidden = {hidden: true},
					locked = {locked: lockedFlag},
					uneditable = {editable: false},
					imgColumn = {
						template: "<img alt='#=(imgDir ? modelId : '暫無圖片')#' class='productImg' style='height:50px; width:50px;' src='#=(imgDir ? (\'"+loadImgUrl+"\' + modelId) : '')#'>"
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
		       			["modelId",						"型號",			150,			"string",	"contains"], // TODO 型號於新增時應檢查是否重覆
		       			["nameEng",						"英文名字",		200,			"string",	"contains"],
						["name",						"中文名字",		100,			"string",	"contains"],
						["suggestedRetailPrice",		"定價",			100,			"number",	"eq"],
						["imgDir",						"圖片",			100,			"string",	"contains",				uneditable,				imgColumn],
						["totalStockQty",				"總庫存",			100,			"number",	"eq"],
						["barcode",						"條碼號",			150,			"string",	"contains",				null,					hidden],
						["seriesName",					"系列名",			150,			"string",	"contains",				uneditable,				hidden],
						["totalStockChangeNote",		"總庫存修改備註",	150,			"string",	"contains",				null,					hidden],
						[productCategoryFieldName,		"商品類別代號",	150,			"string",	"contains",				productCategoryField,	productCategoryColumn,	productCategoryEditor],
						[opts.pk,						"Product ID",	150,			"string",	"eq",					uneditable,				hidden]
					];
				return fields;
			}
			
			function afterGridInitHandler(mainGrid){
				var clkDisplayImg = function(){
					var img = mainGrid.tbody.find("img.productImg").click(function(){
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
				};
				mainGrid.bind("dataBound", clkDisplayImg);
				mainGrid.bind("saveChanges", function(e){
					var ds = mainGrid.dataSource,
						undirty = ds._pristineData,
						dirty = ds._data,
						undirtyQty = {},
						dirtyQty = {},
						ids=[];
					for(var i=0; i<undirty.length; i++){
						var und = undirty[i];
						if(und.id){
							undirtyQty[und.id]=und.totalStockQty;
						}
					}
					for(var i=0; i<dirty.length; i++){
						var d = dirty[i];
						if(d.id){
							dirtyQty[d.id]=d.totalStockQty;
						}
					}
					for(var id in undirtyQty){
						if(undirtyQty.hasOwnProperty(id) && undirtyQty[id]!=dirtyQty[id]){
							ids.push(id);
							var dataItem = ds.get(id);
							//console.log("dataItem:"+JSON.stringify(dataItem));
						}
					}
					//console.log("undirtyQty:"+JSON.stringify(undirtyQty)+", dirtyQty:"+JSON.stringify(dirtyQty) + ", ids:"+JSON.stringify(ids));
					if(ids.length>0){
						var response = prompt("請輸入異動總庫存的原因");
						if(!response){
							alert("無法儲存資料!!");
							e.preventDefault();
							return;
						}
						for(var i=0; i<ids.length; i++){
							var id = ids[i],
								dataItem = ds.get(id);
							console.log('dataItem id: ' + id);
							if(dataItem){ // 如果刪除物件，會發生有id卻沒有dataItem的狀況
								dataItem.set("totalStockChangeNote", response);
							}
						}
					}
					
				});
				/*
				function totalStockQtyChangeHandler(e){
					var field = e.field,
						dataItem = this;
					if(field == "totalStockQty"){
						console.log("dataItem:"+dataItem.get("uid")+"totalStockQty changed!!");
					}
					// ref. http://stackoverflow.com/questions/26892228/does-kendo-data-datasource-store-the-old-value-somewhere
					var ds = mainGrid.dataSource,
						undirty = ds._pristineData,
						dirty = ds._data; // 修改後的資料，亦可透過ds.data()取得
					//console.log("dirty:"+JSON.stringify(dirty));
					//console.log("_pristineData:" + JSON.stringify(mainGrid.dataSource._pristineData));
					// TODO 如果不填寫原因，就觸發恢復的按鈕
					//$(".k-grid-cancel-changes").click();
					console.log("ds data:" + JSON.stringify(ds.data()));
				}
				function dataItemBoundHandler(e){
					var trs = e.sender.items();
					var dataItems = $(trs).map(function(idx, ele){
						var d = mainGrid.dataItem($(ele));
						d.unbind("change", totalStockQtyChangeHandler)
						.bind("change", totalStockQtyChangeHandler);
						return d;
					});
				}
				mainGrid.bind("dataBound", dataItemBoundHandler);
				*/
			}
			
			angrycat.kendoGridService
				.init(opts)
				.fieldsReady(fieldsReadyHandler, afterGridInitHandler);
		})(jQuery, kendo, angrycat);			
	</script>
</body>
</html>