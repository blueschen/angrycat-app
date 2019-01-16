<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>   
<div style="margin-bottom: 15px;">
	<nav role="navigation" class="navbar navbar-default navbar-fixed-top" id="navbarDiv">
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
					<li class="dropdown">
						<!-- 直接使用angular-strap的指令bs-dropdown -->
          				<a href="#" 
          					class="dropdown-toggle" 
          					data-toggle="dropdown" 
          					role="button" 
          					aria-haspopup="true" 
          					aria-expanded="false"
          					>異動記錄 <span class="caret"></span>
          				</a>
          				<ul class="dropdown-menu">
            				<li ${moduleName == "datachangelog" ? "class='active'" : ""}>
            					<a href="${pageContext.request.contextPath}/datachangelog/list">異動紀錄查詢</a>
            				</li>
            				<li role="separator" class="divider"></li>
            				<li ${moduleName == "datadeletelog" ? "class='active'" : ""}>
            					<a href="${pageContext.request.contextPath}/datadeletelog/list">已刪除資料異動紀錄查詢</a>
            				</li>
          				</ul>
        			</li>
					<!-- 
					<li ${moduleName == "salesdetail" ? "class='active'" : ""}>
						<a href="${pageContext.request.contextPath}/salesdetail/list">銷售明細查詢</a>
					</li> -->
					<li ${moduleName == "salesdetail2" ? "class='active'" : ""}>
						<a href="${pageContext.request.contextPath}/salesdetail2/list">銷售明細</a>
					</li>
					<li>
						<a href="#" onclick="window.open('${pageContext.request.contextPath}/salesdetail2/add','salesdetailAdd','height='+screen.height+',width='+screen.width).focus();">建立新訂單</a>
					</li>
					<li class="dropdown">
						<!-- 直接使用angular-strap的指令bs-dropdown -->
          				<a href="#" 
          					class="dropdown-toggle" 
          					data-toggle="dropdown" 
          					role="button" 
          					aria-haspopup="true"
          					aria-expanded="false">考試 <span class="caret"></span>
          				</a>
          				<ul class="dropdown-menu">
            				<li ${moduleName == "exam" ? "class='active'" : ""}>
            					<a href="${pageContext.request.contextPath}/exam/list">題庫查詢</a>
            				</li>
            				<li role="separator" class="divider"></li>
            				<li><a href="${pageContext.request.contextPath}/exam/add">題庫新增</a></li>
            				<li role="separator" class="divider"></li>
            				<li ${moduleName == "examstatistics" ? "class='active'" : ""}>
            					<a href="${pageContext.request.contextPath}/examstatistics/list">考試成績查詢</a>
            				</li>
          				</ul>
        			</li>
        			<li ${moduleName == "product" ? "class='active'" : ""}>
						<a href="${pageContext.request.contextPath}/product/list">產品及庫存</a>
					</li>
					<li ${moduleName == "purchasebill" ? "class='active'" : ""}>
						<a href="${pageContext.request.contextPath}/purchasebill/list">進貨</a>
					</li>
					<li class="dropdown">
						<!-- 直接使用angular-strap的指令bs-dropdown -->
          				<a href="#" 
          					class="dropdown-toggle" 
          					data-toggle="dropdown" 
          					role="button" 
          					aria-haspopup="true" 
          					aria-expanded="false">潘朵拉 <span class="caret"></span>
          				</a>
          				<ul class="dropdown-menu">
            				<li ${moduleName == "transferreply" ? "class='active'" : ""}>
            					<a href="${pageContext.request.contextPath}/transferreply/list">匯款回條</a>
            				</li>
            				<li role="separator" class="divider"></li>
            				<li ${moduleName == "americangroupbuyorderform" ? "class='active'" : ""}>
            					<a href="${pageContext.request.contextPath}/americangroupbuyorderform/list">美國團訂單</a>
            				</li>
          				</ul>
        			</li>
				</ul>
				<ul class="nav navbar-nav navbar-right">
					<li>
						<a href="${pageContext.request.contextPath}/logout"><span class="glyphicon glyphicon-user"></span>&nbsp;&nbsp;&nbsp;&nbsp;${sessionScope.sessionUserId }&nbsp;&nbsp;登出</a>
					</li>
				</ul>
			</div>		
		</div>
	</nav>
</div>