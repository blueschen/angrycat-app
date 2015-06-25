package com.angrycat.erp.initialize;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.WebApplicationInitializer;

public class SecurityInitializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		FilterRegistration.Dynamic securityFilter = 
			servletContext.addFilter("securityFilter", SecurityFilter.class);
		securityFilter.addMappingForUrlPatterns(null, false, "/*");
		System.out.println("SecurityInitializer entering");
	}

}
