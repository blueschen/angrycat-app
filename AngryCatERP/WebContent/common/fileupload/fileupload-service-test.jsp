<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>    
<!DOCTYPE html>
<html ng-app="fileUploadTest">
<head>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>Insert title here</title>

	<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap-theme.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap-responsive.css"/>'/>
	
	<link rel="stylesheet" href='<c:url value="/common/spinner/spinner.css"/>'/>

</head>
<body ng-controller="MainCtrl as mainCtrl">
	<div class="container">
		<div class="row">
			<div class="col-sm-12">
				<div class="page-header">
				File Upload Testing...
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-3">
				<label for="file1" class="btn btn-default">
					<input type="file" id="file1" style="display:none;" onchange="angular.element(this).scope().uploadFiles('myFiles1', this.files)" accept=""/>
					上傳單檔
				</label>
			</div>
			<div class="col-sm-3">
				<label for="file2" class="btn btn-default">
					<input type="file" id="file2" style="display:none;" onchange="angular.element(this).scope().uploadFiles('myFiles1', this.files)" multiple/>
					上傳多檔
				</label>
			</div>
			<div class="col-sm-3">
				<erp-file-btn file-id="fileUpload1" file-model-assign="results.myFiles2" btn="檔案上傳指令測試" accept-type=".xlsx"></erp-file-btn>
				<button type="button" ng-click="mainCtrl.showFileModel('results.myFiles2')">顯示file-model</button>
			</div>
			<div class="col-sm-3">
				<erp-file-ajax-btn file-id="fileUpload2" btn="立即上傳檔案" accept-type=".xlsx" input-name="myfiles1" request-url="${pageContext.request.contextPath}/uploadMultiFiles">
					<erp-file-ajax-callback callback="fileChangeCtrl.callback"></erp-file-ajax-callback>
				</erp-file-ajax-btn>
			</div>
		</div>
		<form class="form-horizontal">
					<div class="form-group">
						<label class="control-label col-sm-4" for="name">姓名</label>
						<div class="col-sm-4">
							<input type="text" ng-model="results.testBO.name" class="form-control" id="name"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-sm-4" for="code">代碼</label>
						<div class="col-sm-4">
							<input type="text" ng-model="results.testBO.code" class="form-control col-sm-4" id="code"/>
						</div>
					</div>
					<div class="form-group">
						<div class="col-sm-offset-4 col-sm-4">
							<erp-file-btn file-id="fileupload3" file-model-assign="results.myfiles1" btn="上傳資料" is-multiple="true"></erp-file-btn>
						</div>
					</div>
					<div class="btn-group col-sm-offset-4">
						<button type="button" ng-click="mainCtrl.showData()" class="btn btn-default">顯示資料結構</button>
						<button type="button" ng-click="mainCtrl.sendData()" class="btn btn-default">開始傳送測試資料</button>
					</div>
				</form>
	</div>


	<script type="text/javascript" src='<c:url value="/angularjs/1.4.3/angular.js"/>'></script>
	<script type="text/javascript" src='<c:url value="/common/spinner/spinner-service.js"/>'></script>
	<script type="text/javascript" src='<c:url value="/common/fileupload/fileupload-service.js"/>'></script>
	<script type="text/javascript" src='<c:url value="/common/fileupload/fileupload-model-directive.js"/>'></script>
	<script type="text/javascript" src='<c:url value="/common/fileupload/fileupload-ajax-directive.js"/>'></script>
	<script type="text/javascript">
		angular.module('fileUploadTest', ['erp.spinner', 'erp.fileupload', 'erp.fileupload.model.directive', 'erp.fileupload.ajax.directive'])
			.controller('MainCtrl', ['GlobalSpin', 'FileUploader', '$scope', '$parse', function(GlobalSpin, FileUploader, $scope, $parse){
				var self = this;
				
				$scope.uploadFiles = function(inputName, files){
					var config = {
							data:{},
							url: '${pageContext.request.contextPath}/uploadMultiFiles'};
					config.data[inputName] = files;
					
					GlobalSpin.startMask();
					
					FileUploader.uploadForm(config)
						.then(function(res){
							GlobalSpin.stopMask();
							alert('success: ' + JSON.stringify(res));
						},function(resErr){
							GlobalSpin.stopMask();
							alert('failed: ' + JSON.stringify(resErr));
						});
				};
				self.showFileModel = function(attr){
					var getter = $parse(attr);
					if($scope.results && $scope.results.myFiles1){
						alert('$scope.results.myFiles1 instanceof FileList: ' + (getter($scope) instanceof FileList));	
					}
				};
				self.showData = function(){
					alert(JSON.stringify($scope.results));
				};
				self.sendData = function(){
					var config = {data: $scope.results, url: '${pageContext.request.contextPath}/uploadAll'};
					GlobalSpin.startMask();
					FileUploader.uploadForm(config)
						.then(function(res){
							GlobalSpin.stopMask();
							alert('success: ' + JSON.stringify(res));
						},function(resErr){
							GlobalSpin.stopMask();
							alert('failed: ' + JSON.stringify(resErr));
						});
				};
			}])
			.directive('erpFileAjaxCallback', [function(){
				return {
					restrict: 'E',
					scope:{
						ck:'=callback' //isolating scope, exposing attribute name as outer interface, '='、'@'、'&' represent injecting type: '=' is JSON object, '@' is string, '&' is function 
					},
					controller:function(){this.tt='rrr';}, // local controller
					controllerAs: 'CkCtrl',
					require: '^erpFileChange', // finding controller at parent node 'erpFileChange'
					link: function(scope, element, attrs, fileChangeCtrl){//the forth argument is injected controller from errFileChange
						if(scope.ck){
							scope.ck.success = function(){alert('pp upload success');};
							scope.ck.fail = function(){alert('pp upload fail');};
						}
					}
				};
			}]);
	</script>
</body>
</html>