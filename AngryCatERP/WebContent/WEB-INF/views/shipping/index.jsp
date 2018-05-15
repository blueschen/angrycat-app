<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set value="${pageContext.request.contextPath}/shipping" var="urlPrefix"/>  
<!DOCTYPE html>
<html>
<head>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>出貨明細</title>
	<style type="text/css">
     	.btn {
     		border-color: #ccc;
     		border: 1px solid transparent; 
     		border-radius: 4px;
     		padding: 6px 12px;
     		cursor: pointer;
     		display: inline-block;
     		background-color: #ccc;
     	}
     	/* The alert message box */
		.alert {
    		padding: 20px;
    		background-color: #f44336; /* Red */
    		color: white;
    		margin-bottom: 15px;
		}

		/* The close button */
		.closebtn {
    		margin-left: 15px;
    		color: white;
    		font-weight: bold;
    		float: right;
    		font-size: 22px;
    		line-height: 20px;
    		cursor: pointer;
    		transition: 0.3s;
		}

		/* When moving the mouse over the close button */
		.closebtn:hover {
    		color: black;
		}
		
		.ajax_loader{
    		position:absolute;
    		width:100%;
    		height:100%;
    		left:0;
    		top:0;
    		background:rgba(0,0,0,.5);
		}
		.ajax_loader img{
    		position:absolute;    
    		left:50%;
    		top:50%;
		}
	</style> 
</head>
<body style="font-family:微軟正黑體;">
    <h4>出貨明細轉換須知</h4>
    <ul>
    	<li>只支援.xlsx檔</li>
    	<li>只轉換名稱為"內容"的sheet</li>
    	<li>以第一欄為分隔出貨明細基準</li>    	
    </ul>
    <div id="ajax_loader" class="ajax_loader" style="display: none;"><img src="<c:url value="/common/spinner/ajax-loader.gif"/>"></div>
	<form action="${urlPrefix}/uploadShippingRawData" method="post" enctype="multipart/form-data" id="form_convert">
		<fieldset>
			<legend>轉換成出貨明細Excel</legend>
			<label>出貨日期</label>&nbsp;&nbsp;&nbsp;
			<input type="text" name="shippingDate">
			<br>
			<br>
			<label for="upload" class="btn">
				上傳檔案轉換成固定格式
				<input type="file" name="uploadTargetFixed" onchange="uploadToConvert();" accept=".xlsx" style="display: none;" id="upload">
			</label>
			<br>
			<br>
			<label for="upload2" class="btn">
				上傳檔案轉換成指定格式
				<input type="file" name="uploadTargetSpecified" onchange="uploadToConvert();" accept=".xlsx" style="display: none;" id="upload2">
			</label>
		</fieldset>
		<input type="reset" id="reset" style="display: none;">
	</form>
	<!-- ref. https://www.w3schools.com/howto/howto_js_alert.asp -->	
	<div class="alert" id="alert" style="display:none;">
  		<span class="closebtn" onclick="this.parentElement.style.display='none';">&times;</span>
  		<div id="alertContent"></div>
	</div> 
	<script type="text/javascript">
	
	var urlPrefix = "${urlPrefix}";
	
	function uploadToConvert(event){
		// input file在選擇檔案的時候，會花費不少時間將檔案傳到瀏覽器，之後才會觸發change事件，所以會顯得處理得較慢
		document.getElementById('ajax_loader').style.display = 'block';
		document.getElementById('alert').style.display = 'none';
		var form = document.getElementById("form_convert"),
			formData = new FormData(form),
			xhr = new XMLHttpRequest();
		
		xhr.open("POST", urlPrefix + "/uploadShippingRawData", true);
		xhr.responseType = "blob"; // required
		xhr.onreadystatechange = function(e){
	        if(xhr.readyState === XMLHttpRequest.DONE) {
	        	if(xhr.status === 200){
		        	var xlsxData = new Blob([xhr.response], {type: 'application/vnd.ms-excel'}),
	        			fileName = "details.xlsx";
	        		if(window.navigator && window.navigator.msSaveOrOpenBlob){ // for IE (IE 11 available)
	        			window.navigator.msSaveOrOpenBlob(xlsxData, fileName);
	        		}else{
		            	var a = document.createElement("a");
		            	window.URL = window.URL || window.webkitURL;
		            	a.href = window.URL.createObjectURL(xlsxData);
		            	a.download = fileName;
		            	a.style.display = "none";
		            	document.body.appendChild(a);
		            	a.click();
		            	document.body.removeChild(a);
		            	window.URL.revokeObjectURL(xlsxData);
	        		}
	        	}else{
					var regex = /<body><h1>.*Exception:\s(.*)<\/h1>/;
					var rs = regex.exec(xhr.responseText);
					var msg = xhr.responseText;
					if(rs && rs.length >= 2){
						var txt = document.createElement("textarea");
					    txt.innerHTML = rs[1];
						msg = txt.value;	
					}
					document.getElementById('alertContent').innerHTML = msg;
					document.getElementById('alert').style.display = 'block';
	        	}
	        	document.getElementById('ajax_loader').style.display = 'none';
	        	document.getElementById("reset").click(); // clear selected upload file
	        }else if(xhr.readyState === 2){
	        	if(xhr.status !== 200){
	        		xhr.responseType = "text" // 在此處更改類型，才能取得responseText
	        	}
	        }
		};			
		xhr.send(formData);
	}
	</script>
</body>
</html>