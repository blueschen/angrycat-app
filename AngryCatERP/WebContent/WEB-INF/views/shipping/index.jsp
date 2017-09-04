<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set value="${pageContext.request.contextPath}/shipping" var="urlPrefix"/>  
<!DOCTYPE html>
<html>
<head>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>出貨明細</title>
</head>
<body>
    <h4>出貨明細轉換須知</h4>
    <ul>
    	<li>只支援.xlsx檔</li>
    	<li>只轉換名稱為"內容"的sheet</li>
    	<li>忽略第一行且從第二行開始轉換</li>
    	<li>只轉換最前面兩欄對其他欄位沒有影響</li>
    	<li>以第一欄為分隔出貨明細基準</li>
    </ul>
	<form action="${urlPrefix}/uploadShippingRawData" method="post" enctype="multipart/form-data" id="form_convert">
		<label>轉換成出貨明細Excel</label><br>
		<input type="file" name="uploadTarget" onchange="uploadToConvert();" accept=".xlsx"><br>
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