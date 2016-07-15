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
    
    <style type="text/css">
    	/*.check 支援圖片選取之後，改變顯示狀態 */
    	.check{
    		color: #996;
    		opacity: 0.5;
    	}
    	@media only screen and (max-width: 400px) {
        	body { font-size:100%;}
     	}
    </style>

</head>
<body ng-controller="MainCtrl as mainCtrl">

<div class="container">
	
		<div class="btn-group">
			<label class="btn btn-sm btn-default" ng-click="mainCtrl.startTest()">
				<span ng-if="mainCtrl.exam" class="glyphicon glyphicon-repeat"></span>
				<span ng-if="!mainCtrl.exam" class="glyphicon glyphicon-play"></span>
				測試
      			<span ng-if="mainCtrl.exam">重來</span>
				<span ng-if="!mainCtrl.exam">開始</span>
			</label>
			<label ng-click="mainCtrl.nextExam()" class="btn btn-sm btn-default" ng-if="mainCtrl.exam && mainCtrl.examCount != mainCtrl.examNum">
				<span class="glyphicon glyphicon-menu-right"></span>
 				下一題
 			</label>
 			<label ng-click="mainCtrl.scoring()" class="btn btn-sm btn-default" ng-if="mainCtrl.exam && mainCtrl.examCount == mainCtrl.examNum">
 				<span class="glyphicon glyphicon-fire"></span>
 				計分
 			</label>
		</div>
		



	<div class="panel-group">
	<div name="testForm" ng-if="mainCtrl.exam">
		<div class="panel panel-primary">
  			<div class="panel-heading">
  				<!-- 如果題目是圖片 -->
  				<div ng-if="mainCtrl.exam.topicImaged">
  					<div class="row">
  						<div class="thumbnail col-sm-3">
  							<img class="media-object" alt="題目" ng-src="{{mainCtrl.examTopicImgPath}}">
  						</div>
  						<div class="col-sm-9">
  							第{{mainCtrl.examCount}}題 <strong>:</strong>{{mainCtrl.examDescription}}
  						</div>
  					</div>
  				</div>
  				<!-- 如果題目不是圖片 -->
  				<div ng-if="!mainCtrl.exam.topicImaged">
  					第{{mainCtrl.examCount}}題 <strong>:</strong> {{mainCtrl.exam.description}}
  				</div>
  			</div>
 
   				<!-- 如果題項不是圖片 -->
  				<ul class="list-group" ng-if="!mainCtrl.exam.questionImaged">
  					<li class="list-group-item" ng-repeat="item in mainCtrl.exam.items">
  						<span class="button-checkbox">
 							<label class="btn btn-sm btn-block" ng-class="{'btn-default':!item.correct, 'btn-success':item.correct}">
 								<span class="state-icon glyphicon glyphicon-check pull-left" ng-if="item.selected"></span>
 								<span class="state-icon glyphicon glyphicon-unchecked pull-left" ng-if="!item.selected"></span>
 								<span class="badge pull-left">
 									{{item.sequence}}
 								</span>
 								<span class="pull-left">
 									{{item.description}}
 								</span>
 								&nbsp;<!-- 這裡需要至少一個字，否則其他字左靠之後，按鈕高度會不正常縮小 -->
 								<input type="checkbox" class="hidden" autocomplete="off" ng-model="item.selected" ng-disabled="mainCtrl.stopReply" ng-change="mainCtrl.correctAfterReply()">
 							</label>
 						</span>
  					</li>
  				</ul>
  			<!-- 如果題項是圖片 -->	
  			<div class="panel-body" ng-if="mainCtrl.exam.questionImaged">
  				<div class="row">
  					<div class="col-sm-3" ng-repeat="item in mainCtrl.exam.items">
 						<div class="thumbnail">
 							<label class="btn" ng-class="{'btn-primary':!item.correct, 'btn-success':item.correct}">
 								<img class="img-thumbnail img-check" ng-class="{'check':item.selected}" ng-src="{{item.description ? '${urlPrefix}/downloadImage/' + item.description : ''}}" alt="題項">
 								<input type="checkbox" class="hidden" autocomplete="off" ng-model="item.selected" ng-disabled="mainCtrl.stopReply" ng-change="mainCtrl.correctAfterReply()">
 							</label>
 						</div>
  						<!--  
  						<div class="form-group">
  							<div class="btn-group" data-toggle="buttons">
  								<label class="btn" ng-class="{'btn-default':!item.correct, 'btn-success':item.correct}">
  									<input type="checkbox" autocomplete="off" ng-model="item.selected" ng-disabled="mainCtrl.stopReply" ng-change="mainCtrl.correctAfterReply()">
  									<span class="glyphicon glyphicon-unchecked" ng-if="!item.selected"></span>
  									<span class="glyphicon glyphicon-check" ng-if="item.selected"></span>
  								</label>
  								<label class="btn btn-default">
  									<span class="label label-default">{{item.sequence}}</span>
  									{{item.description}}
  								</label>
  							</div>
  						</div> -->
  					</div>
  				</div>
  			</div>
  			
  			<div class="panel-footer" ng-if="mainCtrl.corrected && mainCtrl.exam.hint">
  				<i class="glyphicon glyphicon-info-sign"></i>
  				<span class="label label-default">
  					{{mainCtrl.exam.hint}}
  				</span>
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

	<div ng-if="mainCtrl.scores" ng-model="mainCtrl.activePanel" role="tablist" aria-multiselectable="true" bs-collapse>	
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
    				<li class="list-group-item">
    					<span class="label" ng-class="{'label-default':$index!=0, 'label-success':$index==0}">
    						{{sc}}
    					</span>
    				</li>
  				</ul>
    		</div>  		
  		</div> 		
	</div>
	
	</div>
	<div class="row">
		 <div class="col-sm-offset-5 col-sm-2">
		 	<label class="btn btn-default">
 				<a href="${pageContext.request.contextPath}/logout"><span class="glyphicon glyphicon-user"></span>登出</a>
 			</label>
		</div>
	</div>	
</div>
<script type="text/javascript">
	angular.module('angryCatTestViewApp', ['erp.date.service', 'erp.ajax.service', 'mgcrea.ngStrap'])
		.constant('urlPrefix', '${urlPrefix}')
		.constant('login', "${sessionScope['sessionUser']}" ? true : false)
		.constant('targetData', ${info == null ? "null" : info})
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
		.controller('MainCtrl', ['$scope', 'DateService', 'AjaxService', 'urlPrefix', 'login', 'targetData', function($scope, DateService, AjaxService, urlPrefix, login, targetData){
			var self = this;
			self.statistics = targetData.statistics;
			self.scores = targetData.scores;
			self.login = login;
			self.activePanel = 0,
			self.examCount = 1; // 第幾題
			
			var findTopic = /圖片(\S+)的(\S+)為何/g;
			function setExam(exam){
				if(exam.topicImaged){
					var found = findTopic.exec(exam.description);
					if(found){
						var topic = found[1];
						self.examTopicImgPath = urlPrefix + '/downloadImage/' + topic;
						self.examDescription = exam.description.replace(topic, '');	
					}
				}
				self.exam = exam;
			}
			
			self.startTest = function(){
				AjaxService.post(urlPrefix + "/startTest.json")
				.then(function(response){
					var info = response.data;
					setExam(info.firstExam);
					self.examNum = info.examNum;
					self.scores = null;
					self.statistics = null;
					self.examCount = 1;
					self.stopReply = false;
					self.corrected = false;
				},
				function(errResponse){
					alert('出題失敗，錯誤訊息: ' + JSON.stringify(errResponse));
				});
			}
			self.correctAfterReply = function(){
				self.stopReply = true;
				AjaxService.post(urlPrefix + "/correctAfterReply.json", self.exam)
				.then(function(response){
					var answer = response.data;
					setExam(answer);
					self.corrected = true;
				},
				function(errResponse){
					alert('訂正失敗，錯誤訊息: ' + JSON.stringify(errResponse));
				});
			}			
			self.nextExam = function(){
				if(self.examCount + 1 > self.examNum){
					return;
				}
				AjaxService.post(urlPrefix + "/nextExam.json")
				.then(function(response){
					var nextExam = response.data;
					setExam(nextExam);
					self.examCount++;
					self.stopReply = false;
					self.corrected = false;
				},
				function(errResponse){
					alert('下一題出題失敗，錯誤訊息: ' + JSON.stringify(errResponse));
				});
			}
			self.scoring = function(){
				AjaxService.post(urlPrefix + "/score.json")
				.then(function(response){
					var info = response.data;
					self.scores = info.scores;
					self.statistics = info.statistics;
					self.activePanel = 1;
					self.exam = null;
					self.examNum = null;
					self.examCount = 1;
					self.corrected = false;
				},
				function(errResponse){
					alert('計分失敗，錯誤訊息: ' + JSON.stringify(errResponse));
				});
			};
		}])			
		;
</script>
</body>
</html>