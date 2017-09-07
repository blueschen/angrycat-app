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
	</style> 
</head>
<body style="font-family:微軟正黑體;">
    <h4>出貨明細轉換須知</h4>
    <ul>
    	<li>只支援.xlsx檔</li>
    	<li>只轉換名稱為"內容"的sheet</li>
    	<li>以第一欄為分隔出貨明細基準</li>    	
    </ul>
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

	<script type="text/javascript">
	
	var urlPrefix = "${urlPrefix}";
	
	function uploadToConvert(event){
		var form = document.getElementById("form_convert"),
			formData = new FormData(form),
			xhr = new XMLHttpRequest();
		
		xhr.open("POST", urlPrefix + "/uploadShippingRawData", true);
		xhr.responseType = "blob"; // required
		xhr.onreadystatechange = function(e){
	        if(xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
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
	        	document.getElementById("reset").click(); // clear selected upload file
	        }else if(xhr.status >= 400){
	        	alert("內部處理錯誤，檢查是否為匯入檔案格式有誤:\nxhr.status:" + xhr.status + "\nxhr.readyState:" + xhr.readyState + "\nerr:" + e);
	        }
		};			
		xhr.send(formData);
	}
	</script>
</body>
</html>