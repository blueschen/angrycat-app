<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<html ng-app="angryCatMemberListApp">
<head>
<meta content="width=device-width, initial-scale=1.0" name="viewport">
<title><s:message code="model.name.member"/></title>
<script type="text/javascript" src='<c:url value="/jquery/2.1.1/jquery.min.js"/>'></script>
<script type="text/javascript" src='<c:url value="/angularjs/1.3.16/angular.js"/>'></script>
<script type="text/javascript" src='<c:url value="/angularjs/ui-bootstrap-tpls-0.13.0.min.js"/>'></script>
<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap.css"/>'/>
<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap-theme.css"/>'/>
<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap-responsive.css"/>'/>

<c:set value="member" var="modelName"/>
<c:set value="${pageContext.request.contextPath}/${modelName}" var="urlPrefix"/>
</head>
<body ng-controller="MainCtrl as mainCtrl">
 
 <h1>Hello <s:message code="model.name.${modelName}"/>!</h1>
 
 <div class="container-fluid">
 <div class="bs-docs-grid">
 <div class="row show-grid">
 	<div class="span2">
 		<h4>姓名:</h4>
 	</div>
  	<div class="span4">
 		<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pName">
 	</div>
   	<div class="span2">
 		<h4>性別:</h4>
 	</div>
   	<div class="span4">
 		<select 
			ng-model="mainCtrl.conditionConfig.conds.condition_pGender" 
			ng-options="g.value as g.label for g in mainCtrl.genders">
			<option value="">==請選擇==</option>	
		</select>
 	</div>
 </div>
 
 <div class="row show-grid">
 	<div class="span2">
 		<h4>出生年月日</h4>
 	</div>
 	<div class="span4">
 		<p class="input-group">
		<input 
			type="text" 
			ng-model="mainCtrl.conditionConfig.conds.condition_pBirthday"
			datepicker-popup="yyyy-MM-dd"
			is-open="opened"
			readonly="readonly"
			class="form-control"
			>
			<span class="input-group-btn">
                <button type="button" class="btn btn-default" ng-click="mainCtrl.openCalendar($event)"><i class="glyphicon glyphicon-calendar"></i></button>
              </span>
        </p>
 	</div>
 	 <div class="span2">
 	
 	</div>
 	<div class="span4">
 	
 	</div>
</div>   
</div>	
		<input type="button" value="查詢" ng-click="mainCtrl.query()"/>
		<input type="button" value="刪除" ng-click="mainCtrl.deleteItems()"/>
		<input type="button" value="新增" onclick="document.location.href = '${urlPrefix}/add';"/>
<div class="bs-docs-grid">
<div class="row show-grid">
	<div class="span1">
 		<input type="checkbox" id="allItems" ng-click="mainCtrl.isCheckAll($event)">
 	</div>
 	<div class="span2">
 		身分證字號
 	</div>
 	<div class="span2">
 		姓名
 	</div>
 	<div class="span1">
 		FB暱稱
 	</div>
 	<div class="span1">
 		性別
 	</div>
 	<div class="span1">
 		生日	
 	</div>
 	<div class="span1">
 		VIP
 	</div>
 	<div class="span2">
 		成為VIP時間
 	</div>
 	<div class="span1">
 		郵遞區號
 	</div>	
</div>

<div class="row show-grid" ng-repeat="result in mainCtrl.conditionConfig.results">
	<div class="span1">
 		<input type="checkbox" value="{{result.id}}" name="ids">
 	</div>
 	<div class="span2">
 		{{result.idNo}}
 	</div>
 	<div class="span2">
 		{{result.name}}
 	</div>
 	<div class="span1">
 		{{result.fbNickname}}
 	</div>
 	<div class="span1">
 		{{result.gender | convertGender}}
 	</div>
 	<div class="span1">
 		{{result.birthday}}	
 	</div>
 	<div class="span1">
 		{{result.important | convertBoolean}}
 	</div>
 	<div class="span2">
 		{{result.toVipDate}}
 	</div>
 	<div class="span1">
 		{{result.postalCode}}
 	</div>	
</div>
</div>
		<pagination 
			total-items="mainCtrl.conditionConfig.pageNavigator.totalCount" 
			ng-model="mainCtrl.conditionConfig.conds.currentPage" 
			ng-change="mainCtrl.pageChanged()"
			items-per-page="mainCtrl.conditionConfig.conds.countPerPage"></pagination>
	
</div>
<script type="text/javascript">
	angular.module('angryCatMemberListApp', ['ui.bootstrap'])
		.controller('MainCtrl', ['$log', '$http', '$scope', function($log, $http, $scope){
			var self = this,
				queryAll = '${urlPrefix}/queryAll.json',
				queryCondtional = '${urlPrefix}/queryCondtional.json',
				deleteItems = '${urlPrefix}/deleteItems.json';
				
			self.genders = [{label: '男', value: 0}, {label: '女', value: 1}];
			
			$http.get(queryAll)
				.then(function(response){
					self.conditionConfig = response.data;
					$log.log("getting: " + JSON.stringify(response.data));
				},function(errResponse){
					$log.log('Error while fetching notes');
				});

			self.query = function(){
				$http.post(queryCondtional, self.conditionConfig)
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
			self.deleteItems = function(){
				var ids = document.getElementsByName('ids');
				if(!ids){
					alert('沒有可刪除的項目');
					return;
				}
				var isChecked = false,
					checkedItems = [];
				
				if(ids.length == undefined){
					isChecked = ids.checked;
				}else{
					for(var i = 0; i < ids.length; i++){
						if(ids[i].checked){
							isChecked = true;
							checkedItems.push(ids[i].value);
						}
					}
				}
				if(!isChecked){
					alert('請勾選要刪除的項目');
					return;
				}
				
				$http.post(deleteItems, checkedItems)
				.then(function(response){
					$log.log('successfully return: ' + JSON.stringify(response.data));
					self.conditionConfig = response.data;
					alert('成功刪除: ' + checkedItems.length + '筆');
				},function(errResponse){
					$log.log('failed!!!!!' + JSON.stringify(errResponse));
				});
			};
			self.add = function(){}
			self.isCheckAll = function($event){
				var isChecked = $event.target.checked,
					ids = document.getElementsByName('ids');
				
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
			};
			// date related
			self.openCalendar = function($event){
			    $event.preventDefault();
			    $event.stopPropagation();
			    
			    $scope.opened = true;
			};
		}])
		.filter('convertGender', function(){
			return function(input){
				return input==0?'男':'女';
			}
		})
		.filter('convertBoolean', function(){
			return function(input){
				return input?'是':'否';
			}
		})
		.directive('datepickerPopup', function () {
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
    				console.log('ori time value: ' + value);
      				return time;
    			});
    		}
  			return {
    			restrict: 'A',
    			require: 'ngModel',
    			link: link
  			};
		});
</script>

</body>
</html>