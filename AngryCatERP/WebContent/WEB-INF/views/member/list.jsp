<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="member" var="modelName"/>
<c:set value="${pageContext.request.contextPath}/${modelName}" var="urlPrefix"/>
<!DOCTYPE html>
<html ng-app="angryCatMemberListApp">
<head>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title><s:message code="model.name.member"/></title>
	
	<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap-theme.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap-responsive.css"/>'/>
	
</head>
<body ng-controller="MainCtrl as mainCtrl">
  
<div class="container">

<div class="jumbotron">
<h4>Hello <s:message code="model.name.${modelName}"/>!</h4>
</div>

<form class="form-horizontal">
 	<div class="form-group">
		<label class="col-sm-2 control-label" for="pName" >
 			姓名
 		</label>
 		<div class="col-sm-6">
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pName" id="pName" class="form-control">
 		</div>
 	</div>
 	<div class="form-group">
		<label class="col-sm-2 control-label" for="pGender">
 			性別
 		</label>
 		<div class="col-sm-6">
 			<select 
				ng-model="mainCtrl.conditionConfig.conds.condition_pGender" 
				ng-options="g.value as g.label for g in mainCtrl.genders"
				id="pGender"
				 class="form-control">
				<option value="">==請選擇==</option>	
			</select>
 		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-2 control-label" for="pBirthdayStart">
 			出生起迄日
		</label>
		<div class="col-sm-3">
			<p class="input-group">
					<input 
					type="text" 
					ng-model="mainCtrl.conditionConfig.conds.condition_pBirthdayStart"
					datepicker-popup="yyyy-MM-dd"
					is-open="pBirthdayStart"
					readonly="readonly"
					id="pBirthdayStart"
					class="form-control">
				<span class="input-group-btn">
                	<button type="button" class="btn btn-default" ng-click="mainCtrl.openCalendar($event, 'pBirthdayStart')"><i class="glyphicon glyphicon-calendar"></i></button>
            	</span>
			</p>
		</div>
		<div class="col-sm-3">
        	<p class="input-group">
            	<input type="text" 
					ng-model="mainCtrl.conditionConfig.conds.condition_pBirthdayEnd"
					datepicker-popup="yyyy-MM-dd"
					is-open="pBirthdayEnd"
					readonly="readonly"
					id="pBirthdayEnd"
					class="form-control">
				<span class="input-group-btn">
					<button type="button" class="btn btn-default" ng-click="mainCtrl.openCalendar($event, 'pBirthdayEnd')"><i class="glyphicon glyphicon-calendar"></i></button>
            	</span>
			</p>
		</div>
 	</div>
 	<div class="form-group">
 	 	<label class="col-sm-2 control-label" for="pIdNo" >
 			身分證字號
 		</label>
 		<div class="col-sm-6">
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pIdNo" id="pIdNo" class="form-control">
 		</div>
 	</div>
 	<div class="form-group">
 		 <label class="col-sm-2 control-label" for="pFbNickname" >
 			FB暱稱
 		</label>
 		<div class="col-sm-6">
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pFbNickname" id="pFbNickname" class="form-control">
 		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-2 control-label" for="pMobile" >
 			電話
 		</label>
 		<div class="col-sm-6">
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pMobile" id="pMobile" class="form-control">
 		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-2 control-label" for="pImportant">
 			VIP
 		</label>
 		<div class="col-sm-6">
 			<select 
				ng-model="mainCtrl.conditionConfig.conds.condition_pImportant" 
				ng-options="v.value as v.label for v in mainCtrl.VIPs"
				id="pImportant"
				class="form-control">
				<option value="">==請選擇==</option>	
			</select>
 		</div>
 	</div>
 </form>
 
 <div class="btn-toolbar" role="toolbar">
 	<div class="btn-group" role="group">
 		<input type="button" value="查詢" ng-click="mainCtrl.query()" class="btn btn-default"/>
 	</div>
 	<div class="btn-group" role="group">
 		<input type="button" value="清除" ng-click="mainCtrl.clear()" class="btn btn-default"/>
 	</div>
 	<div class="btn-group" role="group">
 		<input type="button" value="刪除" ng-click="mainCtrl.deleteItems()" class="btn btn-default"/>
 	</div>
 	<div class="btn-group" role="group">
 		<input type="button" value="新增" onclick="document.location.href = '${urlPrefix}/add';" class="btn btn-default"/>
 	</div>
 	<div class="btn-group" role="group">
		<label class="btn btn-default" for="uploadMember">
 			<input type="file" onchange="angular.element(this).scope().uploadExcel(this.files)" accept=".xlsx" id="uploadMember" style="display:none;"/>
 			上傳會員檔案
 		</label>
 	</div>
 </div>

<div class="table-responsive">		
<table class="table table-bordered table-hover table-condense">
	<tr>
		<td><input type="checkbox" id="allItems" ng-click="mainCtrl.checkOrUncheckAll($event)"></td>
		<td>身分證字號</td>
		<td>姓名</td>
		<td>FB暱稱</td>
		<td>性別</td>
		<td>生日</td>
		<td>VIP</td>
		<td>成為VIP時間</td>
		<td>郵遞區號</td>
		<td>內容</td>
	</tr>
	<tr ng-repeat="result in mainCtrl.conditionConfig.results" >
		<td><input type="checkbox" value="{{result.id}}" name="ids"></td>
		<td>{{result.idNo}}</td>
		<td>{{result.name}}</td>
		<td>{{result.fbNickname}}</td>
		<td>{{result.gender | convertGender}}</td>
		<td>{{result.birthday}}</td>
		<td>{{result.important | convertBoolean}}</td>
		<td>{{result.toVipDate}}</td>
		<td>{{result.postalCode}}</td>
		<td id="{{result.id}}" to-view><span><i class="glyphicon glyphicon-file"></i></span></td>
	</tr>
</table>
</div>		
		<pagination 
			total-items="mainCtrl.conditionConfig.pageNavigator.totalCount" 
			ng-model="mainCtrl.conditionConfig.conds.currentPage" 
			ng-change="mainCtrl.pageChanged()"
			items-per-page="mainCtrl.conditionConfig.conds.countPerPage"
			max-size="10"
			previous-text="&lsaquo;"
			next-text="&rsaquo;"
			first-text="&laquo;"
			last-text="&raquo;"
			boundary-links="true"></pagination>
	
</div>

	
	<script type="text/javascript" src='<c:url value="/jquery/2.1.1/jquery.min.js"/>'></script>
	<script type="text/javascript" src='<c:url value="/angularjs/1.4.3/angular.js"/>'></script>
	<script type="text/javascript" src='<c:url value="/angularjs/ui-bootstrap-tpls-0.13.0.min.js"/>'></script>
<script type="text/javascript">
	angular.module('angryCatMemberListApp', ['ui.bootstrap'])
		.factory('AuthInterceptor', ['$q', function($q){
			return {
				responseError: function(responseRejection){
					if(responseRejection.status == 401){
						document.location.href = '${pageContext.request.contextPath}/login.jsp';
					}
					return $q.reject(responseRejection); // make sure to trigger error handler in the next promise
				}
			};
		}])
		.factory('MemberService', ['$http', function($http){
			var queryAllUrl = '${urlPrefix}/queryAll.json',
				queryByCondsUrl = '${urlPrefix}/queryCondtional.json',
				deleteItemsUrl = '${urlPrefix}/deleteItems.json',
				idCheckName = 'ids',
				getCheckedItems = function(){
					var ids = document.getElementsByName(idCheckName),
						checkedItems = [];
					if(!ids){
						return checkedItems;
					}
					if(ids.length == undefined && ids.checked){// check one
						checkedItems.push(ids);
					}else{// check one more
						for(var i = 0; i < ids.length; i++){
							if(ids[i].checked){
								checkedItems.push(ids[i].value);
							}
						}
					}
					return checkedItems;
				};
				
			return {
				queryAll: function(){
					return $http.get(queryAllUrl);
				},
				queryByConds: function(conds){
					return $http.post(queryByCondsUrl, conds);
				},
				clearConds: function(config){
					if(config && config.conds){
						for(var cond in config.conds){
							if(cond.indexOf('condition_') == 0){
								config.conds[cond] = null;
							}
						}
					}
				},
				getCheckedItems: getCheckedItems,
				validateBeforeDelete: function(checkedItems){
					
					if(checkedItems.length == 0){
						alert('請勾選要刪除的項目');
						return false;
					}
					if(!confirm('確定刪除?')){
						return false;
					}
					return true;
				},
				deleteItems: function(checkedItems){
					return $http.post(deleteItemsUrl, checkedItems);
				},
				checkOrUncheckAll: function($event){
					var isChecked = $event.target.checked,
						ids = document.getElementsByName(idCheckName);
				
					if(ids){
						if(ids.length == undefined){
							ids.checked = isChecked;
						}else{
							for(var i = 0; i < ids.length; i++){
								var item = ids[i];
								item.checked = isChecked;
							}
						}
					}
				},
				openCalendar: function($event, opened, $scope){
				    $event.preventDefault();
				    $event.stopPropagation();
				    
				    $scope[opened] = true;
				}
				
				
			};
		}])
		.service('FileUploadService', ['$http', function($http){
			this.uploadExcel = function(file){
				var uploadUrl = '${urlPrefix}/uploadExcel';
				var fd = new FormData(); // FormData just support IE 10+
				fd.append('uploadExcelFile', file);
				
				return $http.post(uploadUrl, fd, {
					transformRequest: angular.identity,
					headers: {'Content-Type': undefined}
				});
			};
		}])
		.config(['$httpProvider', function($httpProvider){
			$httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest'; // to tell server this is a ajax request
			$httpProvider.interceptors.push('AuthInterceptor');
		}])
		.controller('MainCtrl', ['$log', '$scope', 'MemberService', 'FileUploadService', function($log, $scope, MemberService, FileUploadService){
			
			var self = this;
				
			self.genders = [{label: '男', value: 0}, {label: '女', value: 1}];
			self.VIPs = [{label: '是', value: true}, {label: '否', value: false}];
			
			MemberService.queryAll()
				.then(function(response){
					self.conditionConfig = response.data;
					$log.log("getting: " + JSON.stringify(response.data));
				},function(errResponse){
					$log.log('Error while fetching notes');
				});

			self.query = function(){
				MemberService.queryByConds(self.conditionConfig)
				.then(function(response){
					$log.log('successfully return: ' + JSON.stringify(response.data));
					self.conditionConfig = response.data;
				},function(errResponse){
					$log.log('failed!!!!!' + JSON.stringify(errResponse));
				});
			};
			self.pageChanged = function(){
				self.query();
			}
			self.clear = function(){
				MemberService.clearConds(self.conditionConfig);
			};
			self.deleteItems = function(){
				var checkedItems = MemberService.getCheckedItems();
				if(!MemberService.validateBeforeDelete(checkedItems)){
					return;
				}
				MemberService.deleteItems(checkedItems)
				.then(function(response){
					$log.log('successfully return: ' + JSON.stringify(response.data));
					self.conditionConfig = response.data;
					alert('刪除成功' + checkedItems.length + '筆');
				},function(errResponse){
					$log.log('failed!!!!!' + JSON.stringify(errResponse));
				});
			};
			self.checkOrUncheckAll = function($event){
				MemberService.checkOrUncheckAll($event);
			};
			// date related
			self.openCalendar = function($event, opened){
				MemberService.openCalendar($event, opened, $scope);
			};
			$scope.uploadExcel = function(files){
				FileUploadService.uploadExcel(files[0])
				.then(function(response){
					self.conditionConfig = response.data;
					var msgs = self.conditionConfig.msgs;
					if(msgs){
						var msg = '';
						if(msgs.errorMsg){
							msg = msgs.errorMsg;
						}
						if(msgs.warnMsg){
							msg = msgs.warnMsg;
						}
						if(msgs.infoMsg){
							msg = msgs.infoMsg;
						}
						if(msg){
							if(confirm('是否顯示匯入訊息')){
								alert(msg);
							}
						}
					}
				},function(errResponse){
					$log.log('upload failed: ' + JSON.stringify(errResponse));
				});
			}
		}])
		.filter('convertGender', [function(){
			return function(input){
				return input==0?'男':'女';
			}
		}])
		.filter('convertBoolean', [function(){
			return function(input){
				return input?'是':'否';
			}
		}])
		.directive('datepickerPopup', [function () {
  			function link(scope, element, attrs, ngModel) {
    			// View -> Model
    			ngModel.$parsers.push(function (value) {
    				if(!value){
    					return null;
    				}
    				var d = new Date(Date.parse(value)),
    					year = d.getFullYear(),
    					month = (d.getMonth()+1),
    					date = d.getDate(),
    					time = year + '-' + (month > 9 ? month : ('0' + month)) + '-' + (date > 9 ? date : ('0' + date));
      				return time;
    			});
    		}
  			return {
    			restrict: 'A',
    			require: 'ngModel',
    			link: link
  			};
		}])
		.directive('toView', [function(){
			return function(scope, ele){
				ele.bind('click', function(){
					var id = ele[0].getAttribute('id');
					document.location.href = '${urlPrefix}/view/' + id;
				});
			};
		}]);
</script>

</body>
</html>