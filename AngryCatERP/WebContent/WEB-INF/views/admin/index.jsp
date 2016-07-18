<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="admin" var="moduleName"/>
<c:set value="${pageContext.request.contextPath}" var="rootPath"/>
<c:set value="${rootPath}/${moduleName}" var="urlPrefix"/>
<!DOCTYPE html>
<html lang="zh-TW" ng-app="angryCatAdminApp">
<head>
	<meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	
	<title>管理員</title>

	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap.css"/>'/>
	<link rel="stylesheet" href='<c:url value="/vendor/bootstrap/3.3.5/css/bootstrap-theme.css"/>'/>
    
	<script type="text/javascript">		
		<%@ include file="/vendor/angularjs/1.4.3/angular.min.js" %>
		<%@ include file="/vendor/angularjs/1.4.3/i18n/angular-locale_zh-tw.js" %>
		<%@ include file="/common/ajax/ajax-service.js" %>
		<%@ include file="/common/date/date-service.js" %>
	</script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/vendor/angular-strap/2.3.1/angular-strap.tpl.min.js"/>"></script>

	<!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
    
    <style type="text/css">
    	.dropdown-submenu {
    		position: relative;
		}
		.dropdown-submenu>.dropdown-menu {
    		top: 0;
    		left: 100%;
   			margin-top: -6px;
    		margin-left: -1px;
    		-webkit-border-radius: 0 6px 6px 6px;
    		-moz-border-radius: 0 6px 6px;
    		border-radius: 0 6px 6px 6px;
		}
		.dropdown-submenu:hover>.dropdown-menu {
    		display: block;
		}
		.dropdown-submenu>a:after {
    		display: block;
    		content: " ";
    		float: right;
    		width: 0;
    		height: 0;
    		border-color: transparent;
    		border-style: solid;
    		border-width: 5px 0 5px 5px;
    		border-left-color: #ccc;
    		margin-top: 5px;
    		margin-right: -10px;
		}
		.dropdown-submenu:hover>a:after {
    		border-left-color: #fff;
		}
		.dropdown-submenu.pull-left {
    		float: none;
		}
		.dropdown-submenu.pull-left>.dropdown-menu {
    		left: -100%;
    		margin-left: 10px;
    		-webkit-border-radius: 6px 0 6px 6px;
    		-moz-border-radius: 6px 0 6px 6px;
    		border-radius: 6px 0 6px 6px;
		}
		.form-horizontal
		.control-label{
			text-align: left;
		}
    </style>
</head>
<body ng-controller="MainCtrl as mainCtrl">
<div class="container">

	<div class="row">
		<div class="col-sm-3">
			<div class="btn-group-vertical btn-block" aria-label="Vertical button group">
				<div id="parameter"
					class="btn-group"
        			ng-click="mainCtrl.changePos('parameter');"
        			ng-class="{'open':mainCtrl.open==='parameter'}">
        			<button id="btnGroupParameter"
        				class="btn btn-default dropdown-toggle" 
        				aria-expanded="false" 
        				aria-haspopup="true" 
        				data-toggle="dropdown" 
        				type="button">
        				參數
        				<span class="caret"></span>
        			</button>
        			<ul class="dropdown-menu btn-block" aria-labelledby="btnGroupParameter" isolate-click>
        				<li><a href="#" ng-click="mainCtrl.changePos('testCount');">出題</a></li>
        			</ul>	
        		</div>
			
				<!-- 多層選單範例 ref.http://bootsnipp.com/snippets/featured/multi-level-dropdown-menu-bs3 -->
				<!-- 
				<div id="parameter" 
					class="btn-group" 
					ng-click="mainCtrl.parameterOpen ? (mainCtrl.parameterOpen = false) : (mainCtrl.parameterOpen = true)" 
					ng-class="{'open': mainCtrl.parameterOpen}">
            		<button id="btnGroupParameter"
        				class="btn btn-default dropdown-toggle" 
        				aria-expanded="false" 
        				aria-haspopup="true" 
        				data-toggle="dropdown" 
        				type="button">
        				參數
        				<span class="caret"></span>
        			</button>
    				<ul class="dropdown-menu multi-level" role="menu" aria-labelledby="btnGroupParameter">
              			<li><a href="#">Some action</a></li>
              			<li><a href="#">Some other action</a></li>
              			<li class="divider"></li>
              			<li class="dropdown-submenu">
                			<a tabindex="-1" href="#">Hover me for more options</a>
                			<ul class="dropdown-menu">
                  				<li><a tabindex="-1" href="#">Second level</a></li>
                  				<li class="dropdown-submenu">
                    				<a href="#">Even More..</a>
                    				<ul class="dropdown-menu">
                        				<li><a href="#">3rd level</a></li>
                    					<li><a href="#">3rd level</a></li>
                    				</ul>
                  				</li>
                  				<li><a href="#">Second level</a></li>
                  				<li><a href="#">Second level</a></li>
                			</ul>
              			</li>
            		</ul>
        		</div>
        		 -->
        		<!-- ref.http://getbootstrap.com/components/#dropdowns -->
        		<!-- 
        		<div id="other"
					class="btn-group"
        			ng-click="mainCtrl.open='1'"
        			ng-class="{'open':mainCtrl.open==='1'}">
        			<button id="btnGroupOther"
        				class="btn btn-default dropdown-toggle" 
        				aria-expanded="false" 
        				aria-haspopup="true" 
        				data-toggle="dropdown" 
        				type="button">
        				Other
        				<span class="caret"></span>
        			</button>
        			<ul class="dropdown-menu" aria-labelledby="btnGroupOther">
        				<li><a href="#">Other1</a></li>
        				<li><a href="#">Other2</a></li>
        			</ul>	
        		</div> -->
        		<div id="logout"
        			class="btn-group">
        			<label id="btnGroupLogout"
        				class="btn btn-default dropdown-toggle" 
        				aria-expanded="false" 
        				aria-haspopup="true" 
        				data-toggle="dropdown"
        				ng-click="mainCtrl.logout()">
        				<span class="glyphicon glyphicon-user"></span>
        				登出
        			</label>
        		</div>			
			</div>
		

		</div>
		<div class="col-sm-9">
			<div ng-if="mainCtrl.open === 'testCount'">
				<div>
					<form class="form-horizontal" role="form" name="testCountForm">
						<fieldset>
							<legend>出題配題數</legend>
							<div class="form-group">
								<label class="col-sm-2 control-label" for="testTotalCount">總題數</label>
								<div class="col-sm-10">
									<input id="testTotalCount" 
										type="number" 
										class="form-control" 
										placeholder="總題數" 
										ng-model="mainCtrl.testCount.localeNames.total"
										disabled="disabled">
								</div>
							</div>
							<div class="form-group">
								<label class="col-sm-2 control-label" for="testProductCount">產品題數</label>
								<div class="col-sm-10">
									<input id="testProductCount" 
										type="number" 
										class="form-control" 
										placeholder="產品題數" 
										ng-model="mainCtrl.testCount.localeNames.product"
										ng-change="mainCtrl.changeTestCount(testCountForm.product.$error.pattern)"
										ng-pattern="/^([0-9]{1,2}|100)$/"
										name="product">
									<span style="color:red;" ng-show="testCountForm.product.$error.pattern">
										請輸入1到100
									</span>
								</div>
							</div>
							<div class="form-group">
								<label class="col-sm-2 control-label" for="testExamCount">題庫題數</label>
								<div class="col-sm-10">
									<input id="testExamCount" 
										type="number" 
										class="form-control" 
										placeholder="題庫題數" 
										ng-model="mainCtrl.testCount.localeNames.exam"
										ng-change="mainCtrl.changeTestCount();"
										ng-pattern="/^([0-9]{1,2}|100)$/"
										name="exam">
									<span style="color:red;" ng-show="testCountForm.exam.$error.pattern">
										請輸入1到100
									</span>
								</div>
							</div>							
						</fieldset>
					</form>
				</div>
			</div>
		</div>
	</div>
	
	
	<div class="alert-row"></div>
</div>
<script type="text/javascript">
	angular.module('angryCatAdminApp', ['erp.date.service', 'erp.ajax.service', 'mgcrea.ngStrap'])
		.constant('rootPath', '${rootPath}')
		.constant('urlPrefix', '${urlPrefix}')
		.constant('login', "${sessionScope['sessionUser']}" ? true : false)
		.constant('targetData', ${settings == null ? "null" : settings})
		.controller('MainCtrl', ['$scope', 'DateService', 'AjaxService', 'urlPrefix', 'login', 'targetData', 'rootPath', '$alert', '$window', function($scope, DateService, AjaxService, urlPrefix, login, targetData, rootPath, $alert, $window){
			var self = this;
			self.changePos = function(idx){
				self.open = idx;
			};
			self.login = login;
			self.open = "testCount";
			self.logout = function(){
				$window.location.href = rootPath + "/logout";
			};
			function getNumOrZero(parent, props){
				var splits = props.split(".");
				while(splits.length>0){
					var p=splits[0];
					if(!parent[p]){
						break;
					}else{
						splits.splice(0,1);
						parent = parent[p];
					}
				}
				if(splits.length>0 || !parent){
					return 0;
				}
				return parent;
			}
			function setValue(parent, props, val){
				var splits = props.split(".");
				while(splits.length>1){
					var p=splits[0];
					if(!parent[p]){
						parent[p] = {};
					}
					parent = parent[p];
					splits.splice(0,1);
				}
				parent[splits[0]] = val;
			}
			self.changeTestCount = function(err){
				if(err){return;}
				var product = getNumOrZero(self, "testCount.localeNames.product"),
					exam = getNumOrZero(self, "testCount.localeNames.exam");
				setValue(self, "testCount.localeNames.total", product+exam);
				AjaxService
					.post(rootPath + "/parameter2/batchSaveOrMerge.json", [self.testCount])
					.then(function(response){
						$alert({
							title:"出題數參數", 
							content:"更新成功", 
							placement:"right",
							type:"info",
							show:true,
							duration:2,
							container:".alert-row"});
					},function(errResponse){
						alert('儲存失敗，錯誤訊息: ' + JSON.stringify(errResponse));
					});
			};
			self.testCount = targetData["testCount"];
			var localeNames = self.testCount.localeNames;
			localeNames.total = parseInt(localeNames.total, 10);
			localeNames.product = parseInt(localeNames.product, 10);
			localeNames.exam = parseInt(localeNames.exam, 10);
			
		}])
		.directive('isolateClick', function(){// 讓點擊事件無法往上傳播
			return {
				link: function(scope, ele){
					ele.on('click', function(e){
						e.stopPropagation();
					});
				}
			};
		})
		;
</script>
</body>
</html>