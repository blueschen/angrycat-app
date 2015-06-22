<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<html ng-app="angryCatMemberListApp">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><s:message code="model.name.member"/></title>
<script type="text/javascript" src='<c:url value="/angularjs/1.3.16/angular.js"/>'></script>
<script type="text/javascript" src='<c:url value="/angularjs/ui-bootstrap-tpls-0.13.0.min.js"/>'></script>
<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap.css"/>'/>
<link rel="stylesheet" href='<c:url value="/angularjs/bootstrap/3.1.1/css/bootstrap-theme.css"/>'/>

</head>
<body ng-controller="MainCtrl as mainCtrl">
    
<c:set value="member" var="modelName"/>
<c:set value="${pageContext.request.contextPath}/${modelName}" var="urlPrefix"/>


	<h1>Hello <s:message code="model.name.${modelName}"/>!</h1>
		姓名:
		<input type="text" ng-model="mainCtrl.conditionConfig.conds.condition_pName"><br>
		性別:
		<select 
			ng-model="mainCtrl.conditionConfig.conds.condition_pGender" 
			ng-options="g.value as g.label for g in mainCtrl.genders">
			<option value="">==請選擇==</option>	
		</select>
		<br>
		
		<h4>出生年月日</h4>
    	<div class="row">
		<div class="col-md-6">
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
        </div>
		<br>	
		<input type="button" value="查詢" ng-click="mainCtrl.query()"/>
		<table class="table">
			<tr>
				<td>ID</td>
				<td>身分證字號</td>
				<td>姓名</td>
				<td>英文姓名</td>
				<td>性別</td>
				<td>生日</td>
				<td>VIP</td>
				<td>成為VIP時間</td>
				<td>郵遞區號</td>
			</tr>
			<tr ng-repeat="result in mainCtrl.conditionConfig.results">
				<td>{{result.id}}</td>
				<td>{{result.idNo}}</td>
				<td>{{result.name}}</td>
				<td>{{result.nameEng}}</td>
				<td>{{result.gender | convertGender}}</td>
				<td>{{result.birthday}}</td>
				<td>{{result.important | convertBoolean}}</td>
				<td>{{result.toVipDate}}</td>
				<td>{{result.postalCode}}</td>
			</tr>
		</table>
		
		<br>
		<pagination 
			total-items="mainCtrl.conditionConfig.pageNavigator.totalCount" 
			ng-model="mainCtrl.conditionConfig.conds.currentPage" 
			ng-change="mainCtrl.pageChanged()"
			items-per-page="mainCtrl.conditionConfig.conds.countPerPage"></pagination>
	

<script type="text/javascript">
	angular.module('angryCatMemberListApp', ['ui.bootstrap'])
		.controller('MainCtrl', ['$log', '$http', '$scope', function($log, $http, $scope){
			var self = this,
				getConditionConfigUrl = '${urlPrefix}/getConditionConfig.json';
				
			self.genders = [{label: '男', value: 0}, {label: '女', value: 1}];
			
			$http.get(getConditionConfigUrl)
				.then(function(response){
					self.conditionConfig = response.data;
					$log.log("getting: " + JSON.stringify(response.data));
				},function(errResponse){
					$log.log('Error while fetching notes');
				});

			self.query = function(){
				$http.post(getConditionConfigUrl, self.conditionConfig)
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
			// date related
			self.openCalendar = function($event){
			    $event.preventDefault();
			    $event.stopPropagation();
			    
			    $scope.opened = true;
			}
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
    				console.log('time: ' + time);
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