package com.angrycat.erp.initialize;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
	private static final List<String> ADMINS = Arrays.asList("iflywang", "admin", "jerry");
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
		
		HttpSession session = req.getSession();
		User user = (User)session.getAttribute(WebUtils.SESSION_USER);
		if(!allow(req)){
			if(user == null){
				if(isAjax(req)){
					res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				}else{
					res.sendRedirect(req.getContextPath() + LoginController.LOGIN_PATH);
				}
				return;
			}
		}
		String uri = req.getRequestURI();
		String contextPath = req.getContextPath();
		String contextUri = uri.substring(contextPath.length());
		if(contextUri.equals("/admin/index")){
			if(user == null){
				res.sendRedirect(req.getContextPath() + LoginController.LOGIN_PATH);
				return;
			}
			if(!ADMINS.contains(user.getUserId())){
				res.sendRedirect(req.getContextPath() + LoginController.MEMBER_LIST_PATH);
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
		allowPathEqual.add("/login/admin");
		
		// 為了讓一般人可以自行填入會員資料，將會員新增頁面的操作獨立出來
		allowPathEqual.add("/member/add");
		allowPathEqual.add("/member/save.json");
		// 為了讓一般人可以自行填入Pandora匯款回條，將Pandora匯款回條新增頁面的操作獨立出來
		allowPathEqual.add("/transferreply/addPandora");
		allowPathEqual.add("/transferreply/addAmericanGroupBuy");
		allowPathEqual.add("/transferreply/addOHMStore");
		allowPathEqual.add("/transferreply/batchSaveOrMerge.json");
		// 美國團訂單
		allowPathEqual.add("/americangroupbuyorderform/add");
		allowPathEqual.add("/americangroupbuyorderform/batchSaveOrMerge.json");
		allowPathEqual.add("/americangroupbuyorderform/deleteByIds.json");
		// 出貨明細
		allowPathEqual.add("/shipping/index");
		allowPathEqual.add("/shipping/uploadShippingRawData");
		// 會員頁面需要檢核手機和室內電話擇一必填
		allowPathStart.add("/member/mobileDuplicated");
		allowPathStart.add("/member/telDuplicated");
		
		allowPathStart.add("/member/idNoDuplicated");
		// 潘朵拉匯款回條要檢核訂單編號是否存在
		allowPathStart.add("/transferreply/salesNoNotExisted");
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
