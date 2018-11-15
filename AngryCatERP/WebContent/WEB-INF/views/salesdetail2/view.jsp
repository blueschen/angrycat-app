<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="${pageContext.request.contextPath}" var="rootPath"/>
<c:set value="${pageContext.request.contextPath}/${moduleName}" var="urlPrefix"/>
<c:set value="${moduleName}Parameters" var="parameters"/>

<!DOCTYPE html>
<html lang="zh-TW">
<head>
	<meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	
	<title>訂單新增</title>

	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap-theme.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/vendor/jquery-ui-1.12.1.custom/jquery-ui.min.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/vendor/jquery-ui-1.12.1.custom/jquery-ui.structure.min.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/vendor/jquery-ui-1.12.1.custom/jquery-ui.theme.min.css"/>'/>
	
	<link rel="stylesheet" href='<c:url value="/vendor/gistfile1.css"/>'/>
    	
	<script type="text/javascript" src="<c:url value="/vendor/jquery/2.1.1/jquery.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/jquery-ui-1.12.1.custom/jquery-ui.min.js"/>"></script>

	<!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->

	<style type="text/css">
		.form-horizontal .control-label.text-left{
    		text-align: left;
		}
		.t-input {
			display: block !important; 
			padding: 0 !important; 
			margin: 0 !important; 
			border: 0 !important; 
			width: 100% !important; 
			border-radius: 0 !important; 
			line-height: 1 !important;
		}
		td {
			margin: 0 !important; 
			padding: 0 !important;
 		}
		.f-group {
			margin-bottom: 3px;
		}
		.alert-center {
			margin: auto; 
			width: 50%; 
			border: 3px solid; 
			padding: 70px 0;	
		}
		
	</style>
</head>
<body>

<div class="container" id="container" style="display: block;">

<div class="row">
	<div class="col-sm-offset-2">
		<h2>新訂單-[<span id="orderNo"></span><span id="tmpOrderNo"></span>]</h2>
	</div>
</div>


<div id="operation-container">

<div class="pull-right">
	<div class="btn-group">
		<button type="button" class="btn btn-default print-not-required" onclick="window.location.href='${urlPrefix}/add'">新訂單</button>
	</div>
	<div class="btn-group">
		<button type="button" class="btn btn-default print-not-required" onclick="window.location.href='${urlPrefix}/list'">關閉</button>
	</div>	
</div>

	
<form class="form-horizontal">
 	<div class="form-group f-group">
 		<div class="form-group col-xs-5 f-group">
			<label class="col-xs-5 control-label" for="name">
 				客戶名字
 			</label>
 			<div class="col-xs-7">
 				<input type="hidden" id="memberId" member-name="id" class="member-field-1"/>
 				<input type="text" id="name" name="name" member-name="name" class="form-control member-field-1" autofocus/>
 			</div>
 		</div>
 		<div class="form-group col-xs-5 f-group">
			<label class="col-xs-5 control-label" for="fbNickname">
 				FB名字
 			</label>
 			<div class="col-xs-7">
 				<input type="text" id="fbNickname" name="fbNickname" member-name="fbNickname" class="form-control member-field-1"/>
 			</div>
 		</div>
 	</div>
 	<div class="form-group f-group">
 		<div class="form-group col-xs-5 f-group">
			<label class="col-xs-5 control-label" for="mobile">
 				手機號碼
 			</label>
 			<div class="col-xs-7">
 				<input type="text" id="mobile" name="mobile" member-name="mobile" class="form-control member-field-1"/>
 			</div>
 		</div>
 		<div class="form-group col-xs-5 f-group">
			<label class="col-xs-5 control-label" for="idNo">
 				身分證字號
 			</label>
 			<div class="col-xs-7">
 				<input type="text" id="idNo" name="idNo" member-name="idNo" class="form-control member-field-1"/>
 			</div>
 		</div>
 	</div>
 	<div class="form-group f-group">
 		<div class="form-group col-xs-5 f-group">
			<label class="col-xs-5 control-label" for="important">
 				客戶分類
 			</label>
 			<div class="col-xs-7">
 				<input type="text" id="important" name="important" member-name="important" class="form-control" disabled="disabled"/>
 			</div>
 		</div>
 		<div class="form-group col-xs-5 f-group">
			<label class="col-xs-5 control-label">
 			</label>			
			<div class="col-xs-7">
				<input type="button" id="addDetail" class="btn btn-default form-control print-not-required" onclick="addSalesDetail()" value="新增商品" title="快速鍵: Alt + R"/>
			</div>		
 		</div>
 	</div>
 	<div class="form-group f-group">
 	</div>
 	 <div class="form-group f-group">
 	</div>
 	<div class="form-group f-group">
 	</div>
 	<div class="form-group f-group">
 	</div>
</form>
<div class="table-responsive">
    <table class="table">
    	<thead>
    		<tr>
    			<th scope="col" class="col-sm-1">序號</th>
    			<th scope="col" class="col-sm-2">型號</th>
    			<th scope="col" class="col-sm-3">名稱</th>
    			<th scope="col" class="col-sm-1">價格</th>
    			<th scope="col" class="col-sm-1">實收</th>
    			<th scope="col" class="col-sm-2">折扣</th>
    			<th scope="col" class="col-sm-1">備註</th>
    			<th scope="col" class="col-sm-1">功能</th>
    		</tr>    		
    	</thead>
      <tbody id="salesDetailsContent">
      </tbody>
    </table>
</div>

<div class="row">
	<div class="col-sm-2 col-sm-offset-4"><b>實付金額</b></div>
	<div class="col-sm-1" style="padding-left: 0px;"><b><span id="totalPrice">0</span></b></div>
	<div class="col-sm-1" style="padding-left: 0px;"><b><span id="totalMemberPrice">0</span></b></div>
</div>

<div class="pull-right">
	<div class="btn-group">
		<button type="button" class="btn btn-default print-not-required" onclick="printSalesDetails()">列印</button>
	</div>
	<div class="btn-group">
		<button type="button" class="btn btn-default print-not-required" onclick="saveSalesDetails()" id="saveBtn">儲存</button>
	</div>	
</div>

</div>

<div id="print-container" style="display: none;">

<form class="form-horizontal">
 	<div class="form-group f-group">
 		<div class="form-group col-xs-5 f-group">
			<label class="col-xs-5 control-label" for="print-member-name">
 				客戶名字
 			</label>
 			<div class="col-xs-7">
 				<input type="text" id="print-member-name" class="form-control"/>
 			</div>
 		</div>
 		<div class="form-group col-xs-5 f-group">
			<label class="col-xs-5 control-label" for="print-member-fbName">
 				FB名字
 			</label>
 			<div class="col-xs-7">
 				<input type="text" id="print-member-fbName" class="form-control"/>
 			</div>
 		</div>
 	</div>
 	<div class="form-group f-group">
 		<div class="form-group col-xs-5 f-group">
			<label class="col-xs-5 control-label" for="print-member-mobile">
 				手機號碼
 			</label>
 			<div class="col-xs-7">
 				<input type="text" id="print-member-mobile" class="form-control"/>
 			</div>
 		</div>
 		<div class="form-group col-xs-5 f-group">
			<label class="col-xs-5 control-label" for="print-member-idNo">
 				身分證字號
 			</label>
 			<div class="col-xs-7">
 				<input type="text" id="print-member-idNo" class="form-control"/>
 			</div>
 		</div>
 	</div>
 	<div class="form-group f-group">
 		<div class="form-group col-xs-5 f-group">
			<label class="col-xs-5 control-label" for="print-member-important">
 				客戶分類
 			</label>
 			<div class="col-xs-7">
 				<input type="text" id="print-member-important" class="form-control"/>
 			</div>
 		</div>
 		<div class="form-group col-xs-5 f-group">
			<label class="col-xs-5 control-label">
 			</label>			
			<div class="col-xs-7"></div>
 		</div>
 	</div>
 	<div class="form-group f-group">
 	</div>
 	 <div class="form-group f-group">
 	</div>
 	<div class="form-group f-group">
 	</div>
 	<div class="form-group f-group">
 	</div>
</form>
<div class="table-responsive">
    <table class="table">
    	<thead>
    		<tr>
    			<th scope="col" class="col-sm-1">序號</th>
    			<th scope="col" class="col-sm-2">型號</th>
    			<th scope="col" class="col-sm-5">名稱</th>
    			<th scope="col" class="col-sm-1">價格</th>
    			<th scope="col" class="col-sm-1">實收</th>
    			<th scope="col" class="col-sm-1">折扣</th>
    			<th scope="col" class="col-sm-1">備註</th>
    		</tr>    		
    	</thead>
      <tbody id="print-salesDetailsContent">
      </tbody>
    </table>
</div>

<div class="row">
	<div class="col-sm-2 col-sm-offset-6"><b>實付金額</b></div>
	<div class="col-sm-1" style="padding-left: 0px;"><b><span id="print-totalPrice">0</span></b></div>
	<div class="col-sm-1" style="padding-left: 0px;"><b><span id="print-totalMemberPrice">0</span></b></div>
</div>

</div>

</div>

<script id="alert-template" type="text/template">
	<div class="alert alert-{type} alert-dismissible" role="alert" style="display: none; text-align: center;">
  		<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
  		{content}
	</div>
</script>

<script id="salesDetailTmpl" type="text/template">
	<tr id="salesDetail-{group-mark}" class="salesDetail" rowId="{group-mark}">
		<th scope="row">
			<input type="hidden" name="id" />
			<span name="row-serial">{group-serial}</span>
		</th>
		<td class="input-td"><input type="text" class="form-control t-input {group} save-field" name="modelId" product-name="modelId" product-group-name="modelId-{group-mark}"/></td>
		<td class="input-td"><input type="text" class="form-control t-input {group} save-field" name="productName" product-name="nameEng" product-group-name="nameEng-{group-mark}"/></td>
		<td class="input-td"><input type="text" class="form-control t-input {group} save-field" name="price" product-name="suggestedRetailPrice" product-group-name="suggestedRetailPrice-{group-mark}" disabled="disabled" id="price-{group-mark}" save-field-type="number"/></td>
		<td class="input-td"><input type="text" class="form-control t-input save-field" name="memberPrice" id="memberPrice-{group-mark}" save-field-type="number"/></td>
		<td class="select-td">
			<select class="form-control save-field discount-select" name="discountType" id="discount-{group-mark}" onchange="updateMemberPriceById('#memberPrice-{group-mark}', '#price-{group-mark}', this.options[this.selectedIndex])">
				{discount-select-options}
			</select>
		</td>
		<td class="input-td"><input type="text" class="form-control t-input save-field" name="note"/></td>
		<td name="removeBtn"><input type="button" class="btn btn-default btn-block" name="remove" value="移除" onclick="removeSalesDetail('#salesDetail-{group-mark}')" style="display: none;"/></td>
	</tr>
</script>

<script id="print-salesDetailTmpl" type="text/template">
	<tr class="salesDetail" >
		<th scope="row">
			<span>{row-serial}</span>
		</th>
		<td style="vertical-align: middle;">{print-modelId}</td>
		<td style="vertical-align: middle;">{print-productName}</td>
		<td style="vertical-align: middle;">{print-price}</td>
		<td style="vertical-align: middle;">{print-memberPrice}</td>
		<td style="vertical-align: middle;">{print-discountType}</td>
		<td style="vertical-align: middle;">{print-note}</td>
	</tr>
</script>

<script type="text/javascript" src="<c:url value="/vendor/bootstrap/3.3.5/js/bootstrap.min.js"/>"></script>
<script type="text/javascript">
// TODO 實付(總)金額是否要存到資料庫 ==> 這樣每一筆銷售明細都要存??
// TODO 日後需要按照訂單編號修改的需求 ??
// TODO bootstrap版型在列印時是使用最小瀏覽的結果，如果有個別欄位很長，畫面上看起來沒問題，列印有時會變得擠在一起
// 在bootstrap 3.x 列印時他會自動使用col-xs，讓很多東西擠在一起
// 處理方法是
// 1. 直接使用col-xs定義欄寬 --> 不過這樣在很多情境下會失去用bootstrap的好處
// 2. 使用第三方css例如gistfile1.css修正這個問題 ==> https://blog.donnierayjones.com/2014/10/fix-bootstrap3-printing/
// 3. 在table內使用input，雖然獲得排版上容易對齊的便利，但同時如果字太多他不會像正常table自動換行
	(function($, moduleBaseUrl, win, parameters, rootPath, user){ // TODO move out some functions??
		function current(m){// ex. 2018-09-11 12:09 -> 201809111209
			function pad(n){ // ex. 9 -> '09'
			    var upper = 10, p = "0", v = "" + n;
			    if (n < 10) {
			        v = p + v;
			    }
			    return v
			}
			// TODO considering host may be in different timezone
			var d = new Date(), YYYY = d.getFullYear(), MM = d.getMonth()+1, DD = d.getDate(), hh = d.getHours(), mm = d.getMinutes();
			var c;
			if(m == 1){
				c = YYYY + '-' + pad(MM) + '-' + pad(DD) + 'T00:00:00.000Z';
			}else{
				c = YYYY + pad(MM) + pad(DD);
			}
			return c;
		}
		
		function randLetter(len){ // if len = 4 -> HGyb or sUdu
		    var alphabets = 'abcdefghijklmnopqrstuvwxyz';
		    alphabets += alphabets.toUpperCase();
		    alphabets = alphabets.split('');
		    var s = "";
		    for (var i = 0; i < len; i++) {
		        var idx = Math.floor(Math.random() * alphabets.length);
		        s += alphabets[idx];
		    }
		    return s;
		}
		// ex. 201811081707-URod
		document.getElementById('tmpOrderNo').innerHTML = current();
		
		// discount select options(dropdown)
		var discounts = parameters["折扣別"];
		var option = "<option value='{value}' discount='{discount}'>{label}</option>";
		var vipIdx = 0;
		var discountOpts = 
			$.map(discounts, function(v, i){
				if (v.nameDefault == 'VIP9折'){
					vipIdx = i;
				}
				return option.replace('{value}', v.nameDefault)
					.replace('{label}', v.nameDefault)
					.replace('{discount}', v.localeNames.discount);
			})
			.join("");
		// add no discount as first option
		discountOpts = "<option value='無' discount='1'>無</option>" + discountOpts;
		vipIdx++;
		var nonVipIdx = 0;
		
		function waitElement(sel, ck) { // TODO considering timeout??
			var poller1 = 
				setInterval(function(){
					var ele = win.document.querySelector(sel);
					if (!ele) {
						return;
					}
					clearInterval(poller1);
					ck(ele);
				}, 100);
		}
		waitElement('input[member-name=name]', function(ele){
			ele.focus();
		});
		var defaultErrorHandler = 
			function(jqxhr){
				var status = jqxhr.status;
				var regex = /<body><h1>.*Exception:\s(.*)<\/h1>/;
				var rs = regex.exec(jqxhr.responseText);
				var msg = jqxhr.responseText;
				if(rs && rs.length >= 2){
					var txt = document.createElement("textarea");
			    	txt.innerHTML = rs[1];
					msg = txt.value;	
				}
				popup('danger', msg, '60000');
			};
		var defaultStatusHandler = 
			{
				401: function() {
					popup('danger', 'timeout: it would be logged out');
					window.location.href = rootPath + "/login.jsp";
				}
			};
		
		/**
		* 利用autocomplete完成「查詢」及「多欄位賦值」兩項工作
		* 查詢結果並非簡單值，而是vo，有利於進一步確認及利用
		* 這個API仰賴html template格式設計，以達到code reuse目的
		* 如果要改變頁面屬性，要特別注意有可能程式需重新測試及debug
		* 
		* @param {string} remote				遠端服務名稱
		* @param {string} sel					選擇器，去找到所有要綁定autocomplete的dom
		* @param {string} keyField				屬性名稱，以此可以找到可用的屬性值，也可用以區分不同組別，譬如有兩個商品，要區別他們的價格屬性，就要識別他們是不同商品			
		* @param {string} voField				針對vo，取得所需的field值
		* @param {requestCallback} selectExtra	選擇autocomplete額外觸發callback
		*/
		function bindAutocomplete(remote, sel, keyField, voField, selectExtra){
			var url = moduleBaseUrl + remote;
			var fields = 
				$(sel).map(function(i, e){
					return $(e).attr(keyField);
				})
				.get();
			$(sel).autocomplete({
				source: function(req, resp){ // ref. getDefaultFieldAutoCompleteDataSource within angrycat.kendo.grid.js
					if(!req.term){
						return;
					}
					var targetField = $(this.element).attr(voField);
					var filter = {
						conds: {
							kendoData: {
								page: 1,
								pageSize: 20,
								filter: { // TODO operator change to 'startswith' ??
									filters: [{value: req.term, operator: "contains", field: targetField, ignoreCase: true}],
									logic: "or"
								},
								sort: [{field: targetField, dir: "asc"}]
							}
						}
					};
					$.ajax(url, {
						data: JSON.stringify(filter),
						dataType: 'json',
						contentType: 'application/json;charset=utf-8',
						traditional: true,
						method: 'POST',
						cache: false,
						success: function(ret, textStatus, jqxhr){
							//console.log('success:\n' + JSON.stringify(ret));
							resp($.map(ret.results, function(v, i){
								return {
									label: v[targetField], // TODO 考量串起更多資訊，讓使用者可以更快速判斷合適的選項，另一個考量點是畫面簡潔
									value: v[targetField],
									info: v // 這邊設定select事件所需物件
								};
							}));
						},
						error: defaultErrorHandler,
						statusCode: defaultStatusHandler
					});
				},
				select: function(event, ui){
					var field = $(this.element).attr(keyField);
					fields.forEach(function(item, idx, array){
						if(item != field) {
							var itemField = $('input['+ keyField +'='+ item + ']').attr(voField);
							$('input['+ keyField +'='+ item + ']').val(ui.item.info[itemField]);
						}
					});
					if(selectExtra){
						selectExtra(sel, keyField, ui.item.info);
					}
				}
			});
		}
		
		var memberKeyField = "member-name";
		
		bindAutocomplete(
			'/queryMemberAutocomplete.json', 
			'.member-field-1', 
			memberKeyField,
			memberKeyField,
			function(groupSel, keyField, vo){
				var vip = '一般會員';
				var selectVip = nonVipIdx;
				if (vo.important == 1) {
					vip = 'VIP';
					selectVip = vipIdx;
				}
				$('input['+ keyField +'=important]').val(vip);
				$('#memberId').val(vo.id);
				
				var selects = win.document.querySelectorAll('select[name="discountType"]');
				for (var i = 0; i < selects.length; i++) {
					var select = selects[i];
					select.selectedIndex = selectVip;
					select.onchange();
				}
			});
		
		var productIds = [];
		function uniqueId(){
			var id;
			do {
			    id = randLetter(5);
			}
			while(productIds.indexOf(id) >= 0);
			productIds.push(id);
			return id;
		}
		
		var groupCount = 0;
		win.addSalesDetail = function(){
			++groupCount;
			var proId = uniqueId();
			var groupMark = "product-field-" + proId;
			var tmpl = 
				$("#salesDetailTmpl")
					.html()
					.replace(/\{group\}/g, groupMark)
					.replace(/\{group\-serial\}/g, "" + groupCount)
					.replace(/\{group\-mark\}/g, "" + proId)
					.replace("{discount-select-options}", discountOpts);
			var row = $(tmpl);
			row.find('td[name=removeBtn]')
				.hover(function(){
					$(this).find('input[name=remove]').show();
				}, function(){
					$(this).find('input[name=remove]').hide();
				});
			$("#salesDetailsContent").append(row);
			
			waitElement('input[product-group-name=modelId-'+ proId +']', function(ele){
				ele.focus();
			});
			
			var discountSelectId = 'discount-' + proId;
			bindAutocomplete(
				'/queryProductAutocomplete.json', 
				'.' + groupMark, 
				'product-group-name',
				'product-name',
				function(groupSel, keyField, vo){
					// 每次選到可能是不同價格產品，這時應該重算「實收金額」
					win.document.getElementById(discountSelectId).onchange();
				});
			
			var vip = $('input['+ memberKeyField +'=important]').val();
			if (vip == 'VIP') {
				waitElement('#' + discountSelectId, function(ele){
					ele.selectedIndex = vipIdx;
				});
			}
		};
		var idsForRemoved = [];
		win.removeSalesDetail = function(rowId){
			--groupCount;
			var row = document.querySelector(rowId);
			var id = row.querySelector('input[name=id]').value;
			if (id) {
				idsForRemoved.push(id);
			}
			
			row.parentElement.removeChild(row);
			var remainings = win.document.querySelectorAll('span[name=row-serial]');
			for (var i = 0; i < remainings.length; i++) {
				remainings[i].innerHTML = (i+1);
			}
		};
		function strDefault(v){
			if(!v){return null;} // input dom value default empty string, we want null
			return v;
		}
		function defaultEmptyStr(v){
			if(!v){return "";}
			return v;
		}
		/*
		* 使用bootstrap alert實作彈跳視窗  // TODO 換成jquery dialog元件可調整選項較多?? ref. http://api.jqueryui.com/dialog/
		* ref. https://getbootstrap.com/docs/3.3/components/#alerts
		* 
		* @param {string} type		彈跳視窗類型，對應bootstrap 3.x的alert元件，ex. success, info, warning, danger
		* @param {string} content	訊息內容，可嵌入html tag
		*/
		function popup(type, content, dur){
			var duration = dur ? dur : '3000';// 預設顯示三秒
			$($('#alert-template').html()
				.replace('{type}', type)
				.replace('{content}', content))
			.appendTo(document.body)
			.fadeIn('500', function(){
				$(this).delay(duration) 
					.fadeOut('3000', function(){ // 三秒內消失
						$(this).remove();
					});
			});
		}
		
		function collectData(){
			var data = {details: []};
			var collects = data.details, today = current(1);
			
			var name		= strDefault($('#name').val());
			var fbName		= strDefault($('#fbNickname').val()); // TODO if there's no fbName, only name ??
			var mobile		= strDefault($('#mobile').val());
			var idNo		= strDefault($('#idNo').val());
			var memberId 	= strDefault($('#memberId').val());
			
			var orderNo 	= $('#orderNo').html();
			
			data.name		= name;
			data.fbName		= fbName;
			data.mobile		= mobile;
			data.idNo		= idNo;
			data.memberId	= memberId;
			data.orderNo	= orderNo;
			
			$('.salesDetail').each(function(idx){
				var row = $(this);
				if(!row.find('input[name=modelId]').val()){
					return; // 'return' represents continue, 'return false' represents break
				}
				
				var sd = {};
				row.find('.save-field').each(function(i){
					var input = $(this);
					sd[input.attr("name")] = input.attr("save-field-type") != "number" ? strDefault(input.val()) : parseFloat(input.val()); // TODO check number value
				});
				collects.push(sd);
				
				var id				= strDefault(row.find('input[name=id]').val());
				var rowId			= row.attr('rowId');
				var salePoint		= '專櫃';
				var saleStatus		= '99. 已出貨';
				var activity		= null;
				var priority		= null;
				var orderDate		= today;
				var checkBillStatus	= '對帳成功';
				var arrivalStatus	= null;
				var shippingDate	= today;
				var sendMethod		= null;
				var payDate			= today;
				var contactInfo		= null;
				var registrant		= user;
				var payType			= null;
				var payStatus		= null;

				sd['id']				= id;
				sd['rowId']				= rowId;
				sd['salePoint'] 		= salePoint;
				sd['saleStatus'] 		= saleStatus;
				sd['activity'] 			= activity;
				sd['priority'] 			= priority;
				sd['orderDate'] 		= orderDate;
				sd['checkBillStatus'] 	= checkBillStatus;
				sd['arrivalStatus'] 	= arrivalStatus;
				sd['shippingDate'] 		= shippingDate;
				sd['sendMethod'] 		= sendMethod;
				sd['payDate'] 			= payDate;
				sd['contactInfo'] 		= contactInfo;
				sd['registrant'] 		= registrant;
				sd['payType'] 			= payType;
				sd['payStatus'] 		= payStatus;
				
				sd['fbName'] 			= fbName;
				sd['mobile'] 			= mobile;
				sd['idNo'] 				= idNo;
				sd['member'] 			= null;
				if(memberId){
					sd['member'] = {id: memberId};
				}
				sd['orderNo'] = orderNo;
			});
			return data;
		}
		
		win.saveSalesDetails = function(){// 包含「新增」、「修改」、「刪除」
			var rows = $('.salesDetail');
			if(rows.length == 0 && idsForRemoved.length == 0){
				console.log('No Item Found');
				return;
			}
			var data = collectData(), collects = data.details, orderNo = data.orderNo;
			console.log('orderNo: ' +orderNo+'==');
			if(idsForRemoved.length > 0){// 刪除
				console.log('idsForRemoved: ' + idsForRemoved);
				$.ajax(moduleBaseUrl + '/deleteByIds.json', {
					data: JSON.stringify(idsForRemoved),
					dataType: 'json',
					contentType: 'application/json;charset=utf-8',
					traditional: true,
					method: 'POST',
					cache: false,
					success: function(ret, textStatus, jqxhr){
						//console.log('success:\n' + JSON.stringify(ret));
						idsForRemoved = [];
						popup('success', '刪除成功', '3000');
					},
					error: defaultErrorHandler,
					statusCode: defaultStatusHandler
				});
			}
			
			if(collects.length > 0){
				if(!orderNo){// 新增訂單
					$.ajax(moduleBaseUrl + '/batchSave.json', {
						data: JSON.stringify(collects),
						dataType: 'json',
						contentType: 'application/json;charset=utf-8',
						traditional: true,
						method: 'POST',
						cache: false,
						success: function(ret, textStatus, jqxhr){
							//console.log('success:\n' + JSON.stringify(ret));
							// 存到後端之後產生id，將id存到頁面就可以修改
							for(var j = 0; j < ret.length; j++) {
								$('tr[rowId='+ret[j].rowId+']').find('input[name=id]').val(ret[j].id);
							}
							popup('success', '儲存成功', '5000');
							
							$('#saveBtn').html('修改');
							$('#orderNo').html(ret[0].orderNo);
							$('#tmpOrderNo').remove();
						},
						error: defaultErrorHandler,
						statusCode: defaultStatusHandler
					});
				}else{//修改訂單
					$.ajax(moduleBaseUrl + '/batchSaveOrMerge.json', {
						data: JSON.stringify(collects),
						dataType: 'json',
						contentType: 'application/json;charset=utf-8',
						traditional: true,
						method: 'POST',
						cache: false,
						success: function(ret, textStatus, jqxhr){
							//console.log('success:\n' + JSON.stringify(ret));
							// 存到後端之後產生id，將id存到頁面就可以修改
							for(var j = 0; j < ret.length; j++) {
								$('tr[rowId='+ret[j].rowId+']').find('input[name=id]').val(ret[j].id);
							}
							popup('success', '修改成功', '5000');
						},
						error: defaultErrorHandler,
						statusCode: defaultStatusHandler
					});
				}
			}		
		};
		// win.onbeforeprint = function(){};
		win.onafterprint = function(){			
			$('#operation-container').show();
			$('#print-container').hide();
		};
		var printItems = 
			{
				member: ['name', 'fbName', 'mobile', 'idNo'],
				salesDetail: ['modelId', 'productName', 'price', 'memberPrice', 'discountType', 'note']
			};
		// Bootstrap版面列印會遇到問題，可參考一些做法
		// ref. https://stackoverflow.com/questions/12302819/how-to-create-a-printable-twitter-bootstrap-page
		win.printSalesDetails = function(){			
			var rows = $('.salesDetail');
			if(rows.length == 0){
				console.log('No Item Found');
				return;
			}
			
			var data = collectData(), memberItems = printItems.member, detailItems = printItems.salesDetail, details = data.details;
			for (var i = 0; i < memberItems.length; i++) {
				var memberItem = memberItems[i];
				document.getElementById('print-member-' + memberItem).value = data[memberItem];	
			}
			
			var rowContent = $('#print-salesDetailsContent');
			rowContent.empty();
			
			for (var j = 0; j < details.length; j++) {
				var detail = details[j];
				var tmpl = 
					$('#print-salesDetailTmpl')
						.html()
						.replace('{row-serial}', j+1);
				for (var k = 0; k < detailItems.length; k++) {
					var prop = detailItems[k];
					tmpl = tmpl.replace('{print-'+ prop +'}', defaultEmptyStr(detail[prop]));
				}
				rowContent.append(tmpl);
			}
			
			document.getElementById('print-totalPrice').innerHTML = document.getElementById('totalPrice').innerHTML;
			document.getElementById('print-totalMemberPrice').innerHTML = document.getElementById('totalMemberPrice').innerHTML;
			
			$('#operation-container').hide();
			$('#print-container').show();

			win.print();
		};
		win.updateMemberPriceById = function(memberPrice, price, option){
			option.selected = true;
			var discount = option.getAttribute('discount');
			var originalPrice = $(price).val();
			if ($.isNumeric(originalPrice) && $.isNumeric(discount)){
				var r = parseFloat(originalPrice) * parseFloat(discount);
				$(memberPrice).val(Math.floor(r));
			}
			
			var totalPrice = 0;
			$('input[name=price]').each(function(i){
				var p = this.value;
				if($.isNumeric(p)){
					totalPrice += parseFloat(p);
				}
			});
			$('#totalPrice').html(totalPrice);
			
			var totalMemberPrice = 0;
			$('input[name=memberPrice]').each(function(i){
				var p = this.value;
				if($.isNumeric(p)){
					totalMemberPrice += parseFloat(p);
				}				
			});
			$('#totalMemberPrice').html(totalMemberPrice);
		};
		// 設定快速鍵
		$(win.document.body).keydown(function(e){
			var altKey = e.altKey, keyCode = e.keyCode;
			if(altKey && keyCode == 82){// Alt + R 直接觸發 Add new record	
				win.document.querySelector('#addDetail').click();
			}
		});
	})(jQuery, 
	'${urlPrefix}', 
	window, 
	${requestScope[parameters] == null ? "null" : requestScope[parameters]}, 
	'${rootPath}',
	'${user}');


</script>
</body>
</html>