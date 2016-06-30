package com.angrycat.erp.initialize;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.angrycat.erp.security.User;
import com.angrycat.erp.web.WebUtils;
import com.angrycat.erp.web.controller.LoginController;

public class SecurityFilter implements Filter {
	private Set<String> allowPathStart = new HashSet<>();
	private Set<String> allowPathEqual = new HashSet<>();

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletResponse res = (HttpServletResponse)response;
		HttpServletRequest req = (HttpServletRequest)request;
		
		if(!allow(req)){
			HttpSession session = req.getSession();
			User user = (User)session.getAttribute(WebUtils.SESSION_USER);
			if(user == null){
				if(isAjax(req)){
					res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				}else{
					res.sendRedirect(req.getContextPath() + LoginController.LOGIN_PATH);
				}
				return;
			}
		}
		filterChain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		allowPathStart.add("/angularjs/");
		allowPathStart.add("/jquery/");
		allowPathStart.add("/js/");
		allowPathStart.add("/vendor/");
		allowPathStart.add("/common/");
		allowPathStart.add("/app/");
		allowPathEqual.add("/login.jsp");
		allowPathEqual.add("/login");
		allowPathEqual.add("/login/test");
		
		// 為了讓一般人可以自行填入會員資料，將會員新增頁面的操作獨立出來
		allowPathEqual.add("/member/add");
		allowPathEqual.add("/member/save.json");
		// 會員頁面需要檢核手機和室內電話擇一必填
		allowPathStart.add("/member/mobileDuplicated");
		allowPathStart.add("/member/telDuplicated");
	}
	
	private boolean allow(HttpServletRequest request){
		String uri = request.getRequestURI(); // ex: /AngryCatERP/login.jsp
		String contextPath = request.getContextPath(); // ex: /AngryCatERP
		String contextUri = uri.substring(contextPath.length()); // ex: /login.jsp
		if(allowPathEqual.contains(contextUri)){
			return true;
		}
		boolean allow = allowPathStart
			.stream()
			.anyMatch(s->{return contextUri.startsWith(s);});
		return allow;
	}
	
	private boolean isAjax(HttpServletRequest request){
		String requestedWithHeader = request.getHeader("X-Requested-With");
		return "XMLHttpRequest".equals(requestedWithHeader);
	}
	
}
