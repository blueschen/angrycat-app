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
					fields = [
		       			//0fieldName			1column title		2column width	3field type	4column filter operator	5field custom		6column custom			7column editor
		       			["brand",				"購買商品品牌",		150,			"string",	"contains"],
		       			["salePoint",			"訂購管道",			150,			"string",	"contains"],
		       			["productDetails",		"購買明細",			150,			"string",	"contains"],
		       			["shipment",			"寄送方式",			150,			"string",	"contains"],
		       					       			
		       			//["transferTo",			"匯款至",				150,			"string",	"contains"],
		       			["transferAccountCheck","匯款帳號後5碼",		150,			"string",	"contains"],
		       			["transferDate",		"匯款日期",			150,			"date",		"gte"],
						["transferAmount",		"匯款金額",			150,			"number",	"gte"],
						
						["billChecked",			"對帳是否成功",		180,			"boolean",	"eq",					null,			changeBooleanFilter,			null,			kendo.template('<strong>#= billChecked ? "<span style=\'color: green;\'>'+billCheckIsTrue+'</span>" : "<span style=\'color: red;\'>'+billCheckIsFalse+'</span>" #</strong>')],
						["workNote",			"Note",				150,			"string",	"contains",				null],
						["computerBillCheckNote","電腦對帳",			180,			"string",	"contains"],
						
						["fbNickname",			"FB顯示名稱",			150,			"string",	"contains",				null],
						["name",				"收件人真實姓名",		150,			"string",	"contains",				null],
						["mobile",				"手機號碼",			150,			"string",	"contains"],
						["tel",					"備用聯絡電話",		150,			"string",	"contains"],
		       			
		       			["postalCode",			"郵遞區號",			150,			"string",	"contains"],
						["address",				"掛號收件地址",		150,			"string",	"contains"],						
						
		       			["note",				"其他備註",			150,			"string",	"contains",				null],
						["createDate",			"填單時間",			150,			"date",		"gte"],
						[opts.pk,				"TransferReply ID",	150,			"string",	"gte",					uneditable,			hidden]
					];
				return fields;
			}
			
			var kendoGridService = angrycat.kendoGridService.init(opts);
			
			function afterGridInitHandler(mainGrid){
				mainGrid.element.on('dblclick', 'td', function(e){
					var cell = $(this),
						row = cell.closest('tr'),
						field = kendoGridService.getFieldViaCell(cell),
						dataItem = mainGrid.dataItem(row);
					
					if('billChecked' === field && dataItem){
						var val = dataItem[field];
						dataItem.set(field, !dataItem[field]); // 用set才會觸發dirty flag，後續的修改才能成功
						
						// ref. http://stackoverflow.com/questions/275931/how-do-you-make-an-element-flash-in-jquery
						cell.fadeOut().fadeIn('slow', function(){
							//var column = kendoGridService.getColumnViaCell(this, mainGrid),
							//	template = column.template;
							//cell.html(kendo.template(template)(dataItem));
							mainGrid.clearSelection(); // 為了讓紅色文字更為明顯，清除select帶來的背景紅色
							mainGrid.closeCell();
							cell.closest("table").focus(); // ref. http://stackoverflow.com/questions/28828228/kendo-grid-how-to-set-focus-back-to-a-grid-cell-after-canceling-current-editing
						});
					}
				});
				
				function appendUploadMsg(content){
					var $uploadMsgs = $('.upload-msg');
					if($uploadMsgs.length == 0){
						$('#uploadWindow').append(content);
					}else{
						$(content).insertBefore($uploadMsgs[0]);	
					}
				}
				function appendFailMsg(msg){
					var content = '<table class="table upload-msg upload-err"><tbody><tr><td><span><b style="color: red">'+ msg +'</b></span></td></tr></tbody></table>';
					appendUploadMsg(content);
				}
				
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
									select: function(e){
										$.each(e.files, function(index, f){
											if(f.extension != '.csv'){
												e.preventDefault();
												appendFailMsg('目前僅支援上傳csv檔，不支援' + f.extension + '檔');
												return false;
											}
										});
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
												var table = '<table class="table upload-msg upload-success"><tbody>' + content + '</tbody></table>'
												appendUploadMsg(table);
											}
										}else{
											appendFailMsg('上傳資料失敗，請稍後重新上傳，或尋求協助');
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
					var target = $("th[data-field=" + "computerBillCheckNote" + "]"),
						isLocked = target.closest("div.k-grid-header-locked").length > 0,
						grid = e.sender.wrapper,
						rows = isLocked ? grid.find("div.k-grid-content-locked tr") : grid.find("div.k-grid-content tr"),
						columnIndex = target.index(),
						range = /^(匯款金額:\(|轉帳日期:\(|帳後五碼:\()/,
						warningClz = 'alert alert-danger';
					
					for (var j = 0; j < rows.length; j++) {
						var row = $(rows[j]),
                        	dataItem = e.sender.dataItem(row),
                        	note = dataItem.get('computerBillCheckNote'),
                        	cell = row.children().eq(columnIndex);
                        if(range.test(note)){
                        	cell.addClass(warningClz);
                        }else{
                        	cell.removeClass(warningClz);
                        }
                        /*
                        dataItem.bind('change', function(e){
                        	if(e.field === 'billChecked'){
                        		var pos = 8,
                        			tmpt = mainGrid.options.columns[pos+1].template,
                        			uid = this.get('uid'),
                        			target = mainGrid.wrapper.find("[data-uid="+ uid +"]").find("td:eq("+pos+")");
                        		console.log('row uid' + uid);
                        		target.html(kendo.template(tmpt)(dataItem));
                        	}
                        });*/
					}
				});
			}
			kendoGridService.fieldsReady(fieldsReadyHandler, afterGridInitHandler);
		})(jQuery, kendo, angrycat);
		setTimeout(function(){
			console.log(JSON.stringify($("#mainGrid").data("kendoGrid").getOptions()));
		}, 3000);
	</script>
</body>
</html>