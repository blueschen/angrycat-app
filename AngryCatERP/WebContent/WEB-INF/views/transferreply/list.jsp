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
	<title>Pandora匯款回條</title>
	
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
					fields = [
		       			//0fieldName			1column title		2column width	3field type	4column filter operator	5field custom		6column custom			7column editor
						["fbNickname",			"FB顯示名稱",			150,			"string",	"contains",				null,				locked],
						["name",				"真實姓名",			150,			"string",	"contains",				null,				locked],
						["mobile",				"手機號碼",			150,			"string",	"contains"],
						["tel",					"備用聯絡電話",		150,			"string",	"contains"],
						["postalCode",			"郵遞區號",			150,			"string",	"contains"],
						["address",				"掛號收件地址",		150,			"string",	"contains"],
						["createDate",			"填單時間",			150,			"date",		"gte"],
						["salesNo",				"訂單編號",			150,			"string",	"contains"],
						["transferDate",		"匯款日期",			150,			"date",		"gte"],
						["transferAccountCheck","匯款帳號後5碼",		150,			"string",	"contains"],
						["transferAmount",		"匯款金額",			150,			"number",	"gte"],
						["productDetails",		"購買明細",			150,			"string",	"contains"],
						["note",				"其他備註",			150,			"string",	"contains",				null,				hidden],
						["brand",				"品牌",				150,			"string",	"contains",				uneditable,			hidden],
						[opts.pk,				"TransferReply ID",	150,			"string",	"gte",					uneditable,			hidden]
					];
				return fields;
			}
			angrycat.kendoGridService
				.init(opts)
				.fieldsReady(fieldsReadyHandler);
		})(jQuery, kendo, angrycat);			
	</script>
</body>
</html>