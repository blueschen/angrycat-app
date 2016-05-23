<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>    
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
					<li ${moduleName == "member" ? "class='active'" : ""}>
						<a href="${pageContext.request.contextPath}/member/list">會員查詢</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/member/add">會員新增</a>
					</li>
					<li ${moduleName == "datachangelog" ? "class='active'" : ""}>
						<a href="${pageContext.request.contextPath}/datachangelog/list">異動紀錄查詢</a>
					</li>
					<li ${moduleName == "datadeletelog" ? "class='active'" : ""}>
						<a href="${pageContext.request.contextPath}/datadeletelog/list">已刪除資料異動紀錄查詢</a>
					</li>
					<li ${moduleName == "salesdetail" ? "class='active'" : ""}>
						<a href="${pageContext.request.contextPath}/salesdetail/list">銷售明細查詢</a>
					</li>
					<li ${moduleName == "salesdetail2" ? "class='active'" : ""}>
						<a href="${pageContext.request.contextPath}/salesdetail2/list">銷售明細新介面</a>
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