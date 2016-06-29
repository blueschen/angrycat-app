<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="test" var="moduleName"/>
<c:set value="${pageContext.request.contextPath}/${moduleName}" var="urlPrefix"/>
<!DOCTYPE html>
<html lang="zh-TW" ng-app="angryCatTestViewApp">
<head>
	<meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	
	<title>OHM測試</title>

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

</head>
<body ng-controller="MainCtrl as mainCtrl">

<div class="container">
	<div class="col-sm-offset-5">
		<h2>OHM測試</h2>
	</div>
	
	<div name="testForm">
		<div ng-repeat="exam in mainCtrl.showExams">
			<div class="panel panel-primary">
  				<div class="panel-heading">第{{$index+1}}題 <strong>:</strong> {{exam.description}}</div>
  				
  				<div class="panel-body">
  					<div class="row">
  						<div class="col-sm-3" ng-repeat="item in exam.items">
  							<div class="form-group">
  								<div class="btn-group" data-toggle="buttons">
  									<label class="btn btn-default">
  										<input type="checkbox" autocomplete="off" ng-model="item.selected">
  										<span class="glyphicon glyphicon-unchecked" ng-if="!item.selected"></span>
  										<span class="glyphicon glyphicon-check" ng-if="item.selected"></span>
  									</label>
  									<label class="btn btn-default">
  										<span class="label label-default">{{item.sequence}}</span>
  										{{item.description}}
  									</label>
  									<label class="btn btn-success" ng-if="item.correct">
  										R
  									</label>
  								</div>
  							</div>
  						</div>
  					</div>
  				</div>
  				<!-- 
  				<ul class="list-group" ng-repeat="item in exam.items">
    				<li class="list-group-item" ng-class="{'list-group-item-success': mainCtrl.corrected && item.selected && item.correct, 'list-group-item-danger': mainCtrl.corrected && item.selected && !item.correct}"><input type="checkbox" ng-model="item.selected"/>
    					<span>
    						<span class="label label-pill label-default">{{item.sequence}}</span>
    						{{item.description}}
    						<span class="badge alert-success" ng-if="mainCtrl.corrected && item.correct">R</span>
    					</span>
    					<span class="label label-info" ng-if="mainCtrl.corrected && item.correct && exam.hint">{{exam.hint}}</span>
    				</li>
  				</ul> -->
			</div>
		</div>
		<div ng-if="mainCtrl.score >= 0" class="alert alert-info" role="alert">
			<div class="col-sm-offset-5">
				<strong style="font-size: 20px;">{{mainCtrl.score}}</strong>
			</div>
		</div>
 		<div class="form-group">
 			<div class="col-sm-offset-5">
 				<input type="submit" value="計分" ng-click="mainCtrl.scoring()" ng-disabled="mainCtrl.corrected" class="btn btn-default"/>
 				<input type="button" value="重測" ng-click="mainCtrl.retest()" class="btn btn-default"/>
 			</div>
 		</div>		
	</div>
	
	<div ng-if="mainCtrl.scores" class="panel-group" ng-model="mainCtrl.activePanel" role="tablist" aria-multiselectable="true" bs-collapse>
  		<div class="panel panel-default">
    		<div class="panel-heading" role="tab">
      			<h4 class="panel-title">
        			<a bs-collapse-toggle>
          			今日統計
        			</a>
      			</h4>
    		</div>
    		<div class="panel-collapse" role="tabpanel" bs-collapse-target>
      			<table class="table">
      				<thead>
      					<tr>
      						<th>考試次數</th>
      						<th>最高分</th>
      						<th>平均分</th>
      					</tr>
      				</thead>
      				<tbody>
      					<tr>
      						<td>{{mainCtrl.statistics.examCount}}</td>
      						<td>{{mainCtrl.statistics.maxScore}}</td>
      						<td>{{mainCtrl.statistics.avgScore}}</td>
      					</tr>
      				</tbody>
      			</table>
    		</div>    		
  		</div>
  		<div class="panel panel-default">
    		<div class="panel-heading" role="tab">
      			<h4 class="panel-title">
        			<a bs-collapse-toggle>
          			今日計分
        			</a>
      			</h4>
    		</div>
    		<div class="panel-collapse" role="tabpanel" bs-collapse-target>
      			<ul class="list-group" ng-repeat="sc in mainCtrl.scores track by $index">
    				<li class="list-group-item">{{sc}}</li>
  				</ul>
    		</div>  		
  		</div> 		
	</div>	
</div>
<script type="text/javascript">
	angular.module('angryCatTestViewApp', ['erp.date.service', 'erp.ajax.service', 'mgcrea.ngStrap'])
		.constant('urlPrefix', '${urlPrefix}')
		.constant('login', "${sessionScope['sessionUser']}" ? true : false)
		.constant('targetData', ${showExams == null ? "null" : showExams})
		.controller('MainCtrl', ['$scope', 'DateService', 'AjaxService', 'urlPrefix', 'login', 'targetData', function($scope, DateService, AjaxService, urlPrefix, login, targetData){
			var self = this;
			self.showExams = targetData;
			self.login = login;
			self.activePanel = -1;
			self.scoring = function(){
				AjaxService.post(urlPrefix + "/score.json", self.showExams)
				.then(function(response){
					var info = response.data;
					self.showExams = info.corrected;
					self.score = info.score;
					self.corrected = true;
					self.scores = info.scores;
					self.statistics = info.statistics;
					self.activePanel = -1;
				},
				function(errResponse){
					alert('計分失敗，錯誤訊息: ' + JSON.stringify(errResponse));
				});
			};
			self.retest = function(){
				AjaxService.post(urlPrefix + "/retest.json")
				.then(function(response){
					var newExam = response.data;
					self.showExams = newExam;
					self.score = undefined;
					self.corrected = false;
					self.activePanel = -1;
				},
				function(errResponse){
					alert('重測失敗，錯誤訊息: ' + JSON.stringify(errResponse));
				});
				
			};
			
		}])			
		;
</script>
</body>
</html>