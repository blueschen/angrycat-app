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
	<title>安格卡特Angrycat匯款回條</title>
	
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
					lockedFlag: lockedFlag,
					docType: "${docType}"
				};
			
			function fieldsReadyHandler(){
				var context = this,
					defaultAutoCompleteFilter = "contains",
					hidden = {hidden: true},
					locked = {locked: lockedFlag},
					uneditable = {editable: false},
					billCheckIsTrue = "對帳成功",
					billCheckIsFalse = "未對帳",
					changeBooleanFilter = {
						filterable: {
							messages: {
								isTrue: billCheckIsTrue,
								isFalse: billCheckIsFalse
							}
						}
					},
					hiddenEditor = function(container, options){ // 由設定的觸發事件更改值，如果有提供UI輸入，反而容易弄錯或混淆。如果設為uneditable，又無法正常啟用後續更新的程序，所以提供一個使用者無法直接操作的空UI
						var input = $('<input type="hidden">');
						input.attr("name", options.field);
						input.appendTo(container);
					},
					fields = [
		       			//0fieldName			1column title		2column width	3field type	4column filter operator	5field custom		6column custom			7column editor
		       			["brand",				"購買商品品牌",		150,			"string",	"contains"],
		       			["salePoint",			"訂購管道",			150,			"string",	"contains"],
		       			["productDetails",		"購買明細",			150,			"string",	"contains"],
		       			["shipment",			"寄送方式",			150,			"string",	"contains"],
		       					       			
		       			["transferTo",			"匯款至",				150,			"string",	"contains"],
		       			["transferAccountCheck","匯款帳號後5碼",		150,			"string",	"contains"],
		       			["transferDate",		"匯款日期",			150,			"date",		"gte"],
						["transferAmount",		"匯款金額",			150,			"number",	"gte"],
						
						["billChecked",			"對帳是否成功",		180,			"boolean",	"eq",					null,			changeBooleanFilter,			hiddenEditor,			kendo.template('<strong>#= billChecked ? "<span style=\'color: green;\'>'+billCheckIsTrue+'</span>" : "<span style=\'color: red;\'>'+billCheckIsFalse+'</span>" #</strong>')],
						["computerBillCheckNote","電腦對帳",			200,			"string",	"contains"],
						
						["fbNickname",			"FB顯示名稱",			150,			"string",	"contains",				null],
						["mobile",				"手機號碼",			150,			"string",	"contains"],
						["tel",					"備用聯絡電話",		150,			"string",	"contains"],
		       			["name",				"收件人真實姓名",		150,			"string",	"contains",				null],
		       			["postalCode",			"郵遞區號",			150,			"string",	"contains"],
						["address",				"掛號收件地址",		150,			"string",	"contains"],						
						
		       			["note",				"其他備註",			150,			"string",	"contains",				null,				hidden],
						["createDate",			"填單時間",			150,			"date",		"gte"],
						[opts.pk,				"TransferReply ID",	150,			"string",	"gte",					uneditable,			hidden]
					];
				return fields;
			}
			
			var kendoGridService = angrycat.kendoGridService.init(opts);
			
			function afterGridInitHandler(mainGrid){				
				mainGrid.tbody.on('dblclick', 'td', function(e){
					var cell = $(this),
						row = cell.closest('tr'),
						lockCount = kendoGridService.getLockCount(),
						cellIdx = cell.index()+lockCount, // column索引計算會受到lock欄位影響
						column = mainGrid.options.columns[cellIdx],
						field = column.field,
						dataItem = mainGrid.dataItem(row);
					
					if('billChecked' === field && dataItem){
						var val = dataItem[field];
						dataItem.set(field, !dataItem[field]); // 用set才會觸發dirty flag，後續的修改才能成功
						var template = column.template;
						// ref. http://stackoverflow.com/questions/275931/how-do-you-make-an-element-flash-in-jquery
						cell.fadeOut().fadeIn('slow', function(){
							cell.html(kendo.template(template)(dataItem));	
						});
						e.preventDefault();
					}
				});
				mainGrid.element.find('.k-grid-downloadExcel')
					.after('<span class="v-divider"></span><a href="#" class="k-button" id="startUpload">上傳csv</a>')
					.closest('.k-grid-toolbar')
					.find('#startUpload')
					.on('click', function(){
						var previousUpload = $('#uploadCsv').data('kendoUpload');
						if(previousUpload){
							previousUpload.destroy();
							$('#uploadCsv').remove();
						}
						var previousWindow = $('#uploadWindow').data('kendoWindow')
						if(previousWindow){
							previousWindow.destroy();
							$('#uploadWindow').remove();
						}
						
						$('<div id="uploadWindow"><input type="file" id="uploadCsv" name="csv"></div>')
							.appendTo(document.body)
							.promise()
							.done(function(ele){
								$('#uploadCsv').kendoUpload({
									async: {
										autoUpload: true,
										saveUrl: opts.moduleBaseUrl + '/uploadCsv.json'
									},
									success: function(e){
										var resp = e.response;
										if(resp && resp.data === 'success'){
											var lastKendoData = resp.lastKendoData,
												importMsg = resp.importMsg;
											if(lastKendoData){
												mainGrid.dataSource.query(JSON.parse(lastKendoData));	
											}
											var tr = '<tr><td>{title}</td><td>{info}</td></tr>';
											if(importMsg){
												var content = '';
												for(var p in importMsg){
													if(importMsg.hasOwnProperty(p)){
														content += tr.replace('{title}', p).replace('{info}', importMsg[p]);
													}
												}
												var table = '<table class="table"><tbody>' + content + '</tbody></table>'
												$("#uploadWindow").append(table);
											}
										}
									},
									multiple: false
								}).click();
								
								$("#uploadWindow").kendoWindow({
									width: "30%",
									modal: true,
									position: {
										top: "25%",
										left: "30%"
									}
								})
								.data("kendoWindow")
								//.center()
								.open();
							});
					});
				
				mainGrid.bind("dataBound", function(e){
					var rows = e.sender.tbody.children(),
						columnIndex = this.wrapper.find(".k-grid-header [data-field=" + "computerBillCheckNote" + "]").index(),
						range = /^(僅匯款金額不符|僅轉帳日期不符|僅帳號後五碼不符)/,
						warningClz = 'alert alert-danger';
					for (var j = 0; j < rows.length; j++) {
						var row = $(rows[j]);
                        var dataItem = e.sender.dataItem(row);
                        var note = dataItem.get('computerBillCheckNote');
                        var cell = row.children().eq(columnIndex);
                        if(range.test(note)){
                        	cell.addClass(warningClz);
                        }else{
                        	cell.removeClass(warningClz);
                        }
					}
				});
			}
			kendoGridService.fieldsReady(fieldsReadyHandler, afterGridInitHandler);
		})(jQuery, kendo, angrycat);
		
	</script>
</body>
</html>