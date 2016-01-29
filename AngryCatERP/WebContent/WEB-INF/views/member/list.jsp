<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="member" var="modelName"/>
<c:set value="${pageContext.request.contextPath}/${moduleName}" var="urlPrefix"/>
<!DOCTYPE html>
<html ng-app="angryCatMemberListApp">
<head>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title><s:message code="model.name.member"/></title>
	
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap-theme.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/common/spinner/spinner.css"/>'/>
	
	<script type="text/javascript">
		<%@ include file="/vendor/angularjs/1.4.3/angular.min.js" %>
		<%@ include file="/vendor/angularjs/1.4.3/i18n/angular-locale_zh-tw.js" %>
		<%@ include file="/vendor/angular-bootstrap/ui-bootstrap-tpls-0.13.0.min.js" %>
		<%@ include file="/common/spinner/spinner-service.js" %>
		<%@ include file="/common/fileupload/fileupload-service.js" %>
		<%@ include file="/common/fileupload/fileupload-ajax-directive.js" %>
		<%@ include file="/common/ajax/ajax-service.js" %>
	</script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.tpl.min.js"/>"></script>
	
</head>
<body ng-controller="MainCtrl as mainCtrl">
<div>
	<nav role="navigation" class="navbar navbar-default navbar-fixed-top">
		<div class="container">
			<div class="navbar-header">
				<button type="button" data-target="#navbarCollapse" data-toggle="collapse" class="navbar-toggle">
					<span class="sr-only">Toggle navigation</span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
				</button>
				<a href="#" class="navbar-brand">Angrycat</a>
			</div>
			<div id="navbarCollapse" class="collapse navbar-collapse">
				<ul class="nav navbar-nav">
					<li ng-class="{'active': mainCtrl.moduleName == 'member'}">
						<a href="${pageContext.request.contextPath}/member/list">會員查詢</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/member/add">會員新增</a>
					</li>
					<li ng-class="{'active': mainCtrl.moduleName == 'datachangelog'}">
						<a href="${pageContext.request.contextPath}/datachangelog/list">異動紀錄查詢</a>
					</li>
					<li ng-class="{'active': mainCtrl.moduleName == 'datadeletelog'}">
						<a href="${pageContext.request.contextPath}/datadeletelog/list">已刪除資料異動紀錄查詢</a>
					</li>
					<li ng-class="{'active': mainCtrl.moduleName == 'salesdetail'}">
						<a href="${pageContext.request.contextPath}/salesdetail/list">銷售明細查詢</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/salesdetail/add">銷售明細新增</a>
					</li>										
				</ul>
				<ul class="nav navbar-nav navbar-right">
					<li>
						<a href="${pageContext.request.contextPath}/logout"><span class="glyphicon glyphicon-user"></span>登出</a>
					</li>
				</ul>
			</div>		
		</div>
	</nav>
</div>



<div class="container">

<div class="jumbotron">
<h4>Hello <s:message code="model.name.${modelName}"/>!</h4>
</div>

<form class="form-horizontal" name="memberListForm">
 	<div class="form-group">
		<label class="col-sm-2 control-label" for="pName" >
 			姓名
 		</label>
 		<div class="col-sm-6">
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pName" id="pName" class="form-control" autofocus>
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
 	<div class="form-group" ng-class="{'has-error': (memberListForm.pBirthdayStart.$dirty && memberListForm.pBirthdayStart.$invalid) || (memberListForm.pBirthdayEnd.$dirty && memberListForm.pBirthdayEnd.$invalid)}">
 		<label class="col-sm-2 control-label" id="birthDur">
 			出生日期起迄日
		</label>
		<div class="col-sm-3">
 			<input id="pBirthdayStart" 
 				class="form-control" 
 				ng-model="mainCtrl.conditionConfig.conds.condition_pBirthdayStart" 
 				name="pBirthdayStart" 
 				bs-datepicker 
 				type="text" 
 				autoclose="1"
 				date-format="yyyy-MM-dd"
 				placeholder="yyyy-MM-dd"
 				date-type="string"
 				aria-labelledby="birthDur">			
		</div>
		<div class="col-sm-3">
 			<input id="pBirthdayEnd" 
 				class="form-control" 
 				ng-model="mainCtrl.conditionConfig.conds.condition_pBirthdayEnd" 
 				name="pBirthdayEnd" 
 				bs-datepicker 
 				type="text" 
 				autoclose="1"
 				date-format="yyyy-MM-dd"
 				placeholder="yyyy-MM-dd"
 				date-type="string"
 				aria-labelledby="birthDur">			
		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-2 control-label" for="pBirthdayMonth">
 			生日月份
 		</label>
 		<div class="col-sm-3">
 			<select 
				ng-model="mainCtrl.conditionConfig.conds.condition_pBirthdayMonthStart" 
				ng-options="v.value as v.label for v in mainCtrl.months"
				id="pBirthdayMonthStart"
				class="form-control"
				id="pBirthdayMonth">
				<option value="">==請選擇==</option>	
			</select>
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
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pFbNickname" id="pFbNickname" name="pFbNickname" class="form-control">
 		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-2 control-label" for="pMobile" >
 			手機電話
 		</label>
 		<div class="col-sm-6">
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pMobile" id="pMobile" class="form-control">
 		</div>
 	</div>
 	<div class="form-group">
 		<label class="col-sm-2 control-label" for="pTel" >
 			室內電話
 		</label>
 		<div class="col-sm-6">
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pTel" id="pTel" class="form-control">
 		</div>
 	</div>
 	<div class="form-group" ng-class="{'has-error': (memberListForm.pToVipEndDateStart.$dirty && memberListForm.pToVipEndDateStart.$invalid) || (memberListForm.pToVipEndDateEnd.$dirty && memberListForm.pToVipEndDateEnd.$invalid)}">
 		<label class="col-sm-2 control-label" id="toVipEndDateDur">
 			VIP到期日
		</label>
		<div class="col-sm-3">
 			<input id="pToVipEndDateStart" 
 				class="form-control" 
 				ng-model="mainCtrl.conditionConfig.conds.condition_pToVipEndDateStart" 
 				name="pToVipEndDateStart" 
 				bs-datepicker 
 				type="text" 
 				autoclose="1"
 				date-format="yyyy-MM-dd"
 				placeholder="yyyy-MM-dd"
 				date-type="string"
 				aria-labelledby="toVipEndDateDur">			
		</div>
		<div class="col-sm-3">
 			<input id="pToVipEndDateEnd" 
 				class="form-control" 
 				ng-model="mainCtrl.conditionConfig.conds.condition_pToVipEndDateEnd" 
 				name="pToVipEndDateEnd" 
 				bs-datepicker 
 				type="text" 
 				autoclose="1"
 				date-format="yyyy-MM-dd"
 				placeholder="yyyy-MM-dd"
 				date-type="string"
 				aria-labelledby="toVipEndDateDur">			
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
 	<div class="form-group">
 		<label class="col-sm-2 control-label" for="pClientId" >
 			客戶編號
 		</label>
 		<div class="col-sm-6">
 			<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pClientId" id="pClientId" class="form-control">
 		</div>
 	</div>
 	<div class="btn-toolbar" role="toolbar">
 	<div class="btn-group" role="group">
 		<button type="submit" ng-click="mainCtrl.query()" class="btn btn-default" ng-disabled="memberListForm.$dirty && memberListForm.$invalid">查詢</button>
 	</div>
 	<div class="btn-group" role="group">
 		<input type="button" value="清除" ng-click="mainCtrl.clear()" class="btn btn-default"/>
 	</div>
 	<div class="btn-group" role="group" ng-if="mainCtrl.isAdmin()">
 		<erp-file-ajax-btn file-id="uploadMember" btn="上傳會員檔案" accept-type=".xlsx" input-name="uploadExcelFile" request-url="${urlPrefix}/uploadExcel">
			<erp-file-ajax-callback></erp-file-ajax-callback>
		</erp-file-ajax-btn>
 	</div>
 	<div class="btn-group" role="group" ng-if="mainCtrl.isAdmin()">
 		<input type="button" ng-click="mainCtrl.downloadExcel()" class="btn btn-default"  ng-disabled="memberListForm.$dirty && memberListForm.$invalid" value="下載會員檔案"/>
 	</div>
 	<div class="btn-group" role="group" ng-if="mainCtrl.isAdmin()">
 		<input type="button" ng-click="mainCtrl.downloadOnePos()" class="btn btn-default"  ng-disabled="memberListForm.$dirty && memberListForm.$invalid" value="下載OnePos匯入檔案"/>
 	</div> 	
 	<div class="btn-group" role="group" ng-if="mainCtrl.isAdmin()">
 		<input type="button" ng-click="mainCtrl.downloadTemplate()" class="btn btn-default" value="下載範本"/>
 	</div>
 </div>
 </form>

<div class="table-responsive">		
<table class="table table-bordered table-hover table-condense">
	<tr>
		<td><input type="checkbox" id="allItems" ng-click="mainCtrl.checkOrUncheckAll($event)"></td>
		<td>身分證字號</td>
		<td>姓名</td>
		<td>FB暱稱</td>
		<td>性別</td>
		<td query-order-label="生日" query-order-by="p.birthday"></td>					   
		<td>VIP</td>
		<td>成為VIP時間</td>
		<td>郵遞區號</td>
		<td>內容</td>
		<td>異動紀錄</td>
	</tr>
	<tr ng-repeat="result in mainCtrl.conditionConfig.results">
		<td><input type="checkbox" value="{{result.id}}" name="ids"></td>
		<td><span ng-bind="result.idNo"></span></td>
		<td><span ng-bind="result.name"></span></td>
		<td><span ng-bind="result.fbNickname"></span></td>
		<td><span ng-bind="result.gender | convertGender"></span></td>
		<td><span ng-bind="result.birthday"></span></td>
		<td><span ng-bind="result.important | convertBoolean"></span></td>
		<td><span ng-bind="result.toVipDate"></span></td>
		<td><span ng-bind="result.postalCode"></span></td>
		<td>
			<span to-view="{{result.id}}">
				<i class="glyphicon glyphicon-file"></i>	
			</span>
		</td>
		<td>
			<span to-change-log="{{result.id}}">
				<i class="glyphicon glyphicon-pencil"></i>	
			</span>
		</td>
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
<div class="btn-toolbar" role="toolbar">
 	<div class="btn-group" role="group">
 		<input type="button" value="刪除" ng-click="mainCtrl.deleteItems()" class="btn btn-default"/>
 	</div>
</div>	
</div>

<script type="text/javascript">
	angular.module('angryCatMemberListApp', ['ui.bootstrap', 'erp.fileupload.ajax.directive', 'mgcrea.ngStrap'])
		.constant('urlPrefix', '${urlPrefix}')
		.constant('moduleName', '${moduleName}')
		.constant('currentUserId', '${sessionUser.userId}')
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
		.config(['$httpProvider', '$compileProvider', function($httpProvider, $compileProvider){
			$httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest'; // to tell server this is a ajax request
			$httpProvider.interceptors.push('AuthInterceptor');
			$compileProvider.debugInfoEnabled(false); // after set false, angular.element(htmlEle).scope() will return undefined to improve performance
		}])
		.factory('MemberService', ['$http', 'urlPrefix', function($http, urlPrefix){
			var queryAllUrl = urlPrefix + '/queryAll.json',
				queryByCondsUrl = urlPrefix + '/queryConditional.json',
				deleteItemsUrl = urlPrefix + '/deleteItems.json',
				copyConditionUrl = urlPrefix + '/copyCondition.json',
				resetConditionUrl = urlPrefix + '/resetConditions.json',
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
					return $http.get(resetConditionUrl)
						.then(function(response){
							config.conds = response.data.conds;
						},function(responseErr){
							alert('重置查詢條件錯誤');
						});
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
				copyCondition: function(conds){
					var promise = $http.post(copyConditionUrl, conds);
					return promise;
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
				}				
			};
		}])
		.controller('MainCtrl', ['$log', '$scope', 'MemberService', '$window', 'urlPrefix', 'moduleName', 'currentUserId', function($log, $scope, MemberService, $window, urlPrefix, moduleName, currentUserId){			
			var self = this;
				
			self.genders = [{label: '男', value: 0}, {label: '女', value: 1}];
			self.VIPs = [{label: '是', value: true}, {label: '否', value: false}];
			self.isAdmin = function(){
				return currentUserId == 'admin' || currentUserId == 'root';
			};
			MemberService.queryAll()
				.then(function(response){
					self.conditionConfig = response.data;
				},function(errResponse){
					$log.log('Error while fetching notes');
				});

			self.query = function(){
				return MemberService.queryByConds(self.conditionConfig)
				.then(function(response){
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
				$scope.memberListForm.$setPristine();
				delete $scope.memberListForm.$error.parse;
				delete $scope.memberListForm.$error.date;
			};
			self.deleteItems = function(){
				var checkedItems = MemberService.getCheckedItems();
				if(!MemberService.validateBeforeDelete(checkedItems)){
					return;
				}
				MemberService.deleteItems(checkedItems)
				.then(function(response){
					self.conditionConfig = response.data;
					alert('刪除成功' + checkedItems.length + '筆');
				},function(errResponse){
					$log.log('failed!!!!!' + JSON.stringify(errResponse));
				});
			};
			self.downloadExcel = function(){
				MemberService.copyCondition(self.conditionConfig)
					.then(function(){
						$window.location.href = urlPrefix + '/downloadExcel';
					});
			};
			self.downloadOnePos = function(){
				MemberService.copyCondition(self.conditionConfig)
				.then(function(){
					$window.location.href = urlPrefix + '/downloadOnePos';
				});
			};
			self.downloadTemplate = function(){
				$window.location.href = urlPrefix + '/downloadTemplate';
			};
			self.checkOrUncheckAll = function($event){
				MemberService.checkOrUncheckAll($event);
			};
			self.moduleName = moduleName;
			self.months=[{label:'一', value:1},{label:'二', value:2},{label:'三', value:3},{label:'四', value:4},{label:'五', value:5},{label:'六', value:6},{label:'七', value:7},{label:'八', value:8},{label:'九', value:9},{label:'十', value:10},{label:'十一', value:11},{label:'十二', value:12}];
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
		.directive('toView', [function(){
			return function(scope, ele, attrs){
				ele.bind('click', function(){
					document.location.href = '${urlPrefix}/view/' + attrs.toView;
				});
			};
		}])
		.directive('toChangeLog', [function(){
			return function(scope, ele, attrs){
				ele.bind('click', function(){
					document.location.href = '${pageContext.request.contextPath}/datachangelog/list?docId=' + attrs.toChangeLog + '&docType=com.angrycat.erp.model.Member';
				});
			};
		}])
		.directive('erpFileAjaxCallback', ['$log', function($log){
			return {
				restrict: 'E',
				link: function(scope, element, attrs){
					var ck = scope.fileChangeCtrl.callback;
					if(ck){
						ck.success = function(response){
							if(scope.mainCtrl){
								scope.mainCtrl.conditionConfig = response.data;
								var msgs = scope.mainCtrl.conditionConfig.msgs;
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
							}
						};
						ck.fail = function(errResponse){$log.log('upload failed: ' + JSON.stringify(errResponse));};
					}
				}
			};
		}])
		.directive('queryOrderBy', ['urlPrefix', 'MemberService', function(urlPrefix, MemberService){
			return {
				restrict: 'A',
				template: function(element, attrs){
					var label = attrs.queryOrderLabel;
					return '<span>'+label+'<i></i></span>';
				},
				link: function($scope, element, attrs){
					var asc = null,
						orderBy = attrs.queryOrderBy,
						mainCtrl = $scope.mainCtrl;
					function queryOrderBy(event){
						if(asc){
							asc = false;
						}else{
							asc = true;
						}
						var orderType = orderBy + (asc ? ' ASC' : ' DESC');
						mainCtrl.conditionConfig.conds.orderType = orderType;
						MemberService.queryByConds(mainCtrl.conditionConfig)
							.then(function(response){
								var target = angular.element(angular.element(element.children()[0]).children()[0]);
								if(asc){
									target.attr('class', 'glyphicon glyphicon-triangle-top');
								}else{
									target.attr('class', 'glyphicon glyphicon-triangle-bottom');
								}
								mainCtrl.conditionConfig = response.data;
							},function(responseErr){
								alert('排序查詢發生錯誤: ' + JSON.stringify(responseErr));
							});
					}
					element.on('click', queryOrderBy);
					$scope.$on('$destroy', function(){
						element.off('click', queryOrderBy);
					});
				}
			};
		}])
		;
</script>

</body>
</html>