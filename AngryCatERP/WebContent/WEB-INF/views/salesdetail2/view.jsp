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
    
    <script type="text/javascript" src="<c:url value="/vendor/angularjs/1.4.3/angular.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/angularjs/1.4.3/i18n/angular-locale_zh-tw.js"/>"></script>
	<script type="text/javascript">
		<%@ include file="/common/ajax/ajax-service.js" %>
		<%@ include file="/common/date/date-service.js" %>
	</script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.tpl.min.js"/>"></script>
	
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
		td {margin: 0 !important; padding: 0 !important;}
		.f-group {
			margin-bottom: 3px;
		}
	</style>
</head>
<body>

<div class="container" id="container" style="display: block;">
	<div class="col-sm-offset-2">
		<h2>新訂單-[<span id="orderNo"></span>]</h2>
	</div>
<form class="form-horizontal" name="memberForm">
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
				<input type="button" id="addDetail" class="btn btn-default form-control" onclick="addSalesDetail()" value="新增商品" title="Some tooltip text!"/>
			</div>		
 		</div>
 	</div>
</form>
<div class="table-responsive">
    <table class="table table-bordered table-dark">
    	<thead>
    		<tr>
    			<th scope="col" class="col-xs-1">序號</th>
    			<th scope="col" class="col-xs-2">商品型號</th>
    			<th scope="col" class="col-xs-3">商品名稱</th>
    			<th scope="col" class="col-xs-1">價格</th>
    			<th scope="col" class="col-xs-1">實收金額</th>
    			<th scope="col" class="col-xs-2">折扣說明</th>
    			<th scope="col" class="col-xs-1">備註</th>
    			<th scope="col" class="col-xs-1">功能</th>
    		</tr>    		
    	</thead>
      <tbody id="salesDetailsContent">
      </tbody>
    </table>
</div>
<div>
	<div class="row">
		<div class="col-xs-2 col-xs-offset-4"><b>實付金額</b></div>
		<div class="col-xs-1"><b><span id="totalPrice">0</span></b></div>
		<div class="col-xs-1"><b><span id="totalMemberPrice">0</span></b></div>
	</div>
</div>
<div class="pull-right">
	<div class="btn-group">
		<button type="button" class="btn btn-lg btn-primary" onclick="printSalesDetails()">列印</button>
	</div>
	<div class="btn-group">
		<button type="button" class="btn btn-lg btn-primary" onclick="saveSalesDetails()">儲存</button>
	</div>	
</div>

</div>
<script id="salesDetailTmpl" type="text/template">
	<tr id="salesDetail-{group-mark}" class="salesDetail">
		<th scope="row">
			<input type="hidden" name="id" />
			<span name="row-serial">{group-serial}</span>
		</th>
		<td><input type="text" class="form-control t-input {group} save-field" name="modelId" product-name="modelId" product-group-name="modelId-{group-mark}"/></td>
		<td><input type="text" class="form-control t-input {group} save-field" name="productName" product-name="nameEng" product-group-name="nameEng-{group-mark}"/></td>
		<td><input type="text" class="form-control t-input {group} save-field" name="price" product-name="suggestedRetailPrice" product-group-name="suggestedRetailPrice-{group-mark}" disabled="disabled" id="price-{group-mark}" save-field-type="number"/></td>
		<td><input type="text" class="form-control t-input save-field" name="memberPrice" id="memberPrice-{group-mark}" save-field-type="number"/></td>
		<td>
			<select class="form-control save-field" name="discountType" id="discount-{group-mark}" onchange="updateMemberPriceById('#memberPrice-{group-mark}', '#price-{group-mark}', this.options[this.selectedIndex].getAttribute('discount'))">
				{discount-select-options}
			</select>
		</td>
		<td><input type="text" class="form-control t-input save-field" name="note"/></td>
		<td><input type="button" class="btn btn-default btn-block" name="remove" value="移除" onclick="removeSalesDetail('#salesDetail-{group-mark}')"/></td>
	</tr>
</script>
<script type="text/javascript">
// TODO 清除頁面沒用到程式碼，譬如angularjs
// TODO 彈跳視窗是否要選用jquery widget，譬如noty??
// TODO 考量日後可能有修改的需求
	(function($, moduleBaseUrl, win, parameters, rootPath){ // TODO move out some functions??
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
				c = YYYY + pad(MM) + pad(DD) + pad(hh) + pad(mm);
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
		document.getElementById('orderNo').innerHTML = current() + '-' + randLetter(4);
		
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
		discountOpts = "<option value='' discount='1'>無</option>" + discountOpts;
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
				alert(msg);
			};
		var defaultStatusHandler = 
			{
				404: function() {
					alert( "page not found" );
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
			var groupMark = "product-field-" + groupCount;
			var proId = uniqueId();
			var tmpl = 
				$("#salesDetailTmpl")
					.html()
					.replace(/\{group\}/g, groupMark)
					.replace(/\{group\-serial\}/g, "" + groupCount)
					.replace(/\{group\-mark\}/g, "" + proId)
					.replace("{discount-select-options}", discountOpts);
			$("#salesDetailsContent").append(tmpl);
			
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
		win.removeSalesDetail = function(rowId){
			--groupCount;
			var row = document.querySelector(rowId);
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
		win.saveSalesDetails = function(){
			var rows = $('.salesDetail');
			if(rows.length == 0){
				console.log('No Item Found');
				return;
			}
			var collects = [], today = current(1);
			rows.each(function(idx){
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
				
				var id				= null;
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
				var registrant		= null;
				var payType			= null;
				var payStatus		= null;

				var fbName			= strDefault($('#fbNickname').val()); // TODO if there's no fbName, only name ??
				var mobile			= strDefault($('#mobile').val());
				var idNo			= strDefault($('#idNo').val());
				var memberId 		= strDefault($('#memberId').val());
				
				var orderNo 		= $('#orderNo').html();
				
				sd['id']				= id;
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
			$.ajax(moduleBaseUrl + '/batchSaveOrMerge.json', {
				data: JSON.stringify(collects),
				dataType: 'json',
				contentType: 'application/json;charset=utf-8',
				traditional: true,
				method: 'POST',
				cache: false,
				success: function(ret, textStatus, jqxhr){
					console.log('success:\n' + JSON.stringify(ret));
					// TODO 後續處理??
				},
				error: defaultErrorHandler,
				statusCode: defaultStatusHandler
			});
		};
		// Bootstrap版面列印會遇到問題，可參考一些做法
		// ref. https://stackoverflow.com/questions/12302819/how-to-create-a-printable-twitter-bootstrap-page
		win.printSalesDetails = function(){
			var rows = $('.salesDetail');
			if(rows.length == 0){
				console.log('No Item Found');
				return;
			}
			win.print();
		};
		win.updateMemberPriceById = function(memberPrice, price, discount){
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
	"${rootPath}");


</script>
</body>
</html>