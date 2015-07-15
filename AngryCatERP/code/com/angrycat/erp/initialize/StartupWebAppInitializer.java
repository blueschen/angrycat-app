package com.angrycat.erp.initialize;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration.Dynamic;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.initialize.config.WebConfig;

public class StartupWebAppInitializer extends
		AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[]{RootConfig.class};
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class[]{WebConfig.class};
	}

	@Override
	protected String[] getServletMappings() {
		return new String[]{"/"};
	}

	@Override
	protected void customizeRegistration(Dynamic registration){
		String serverRoot = System.getProperty("catalina.home");
		String uploadTemp = serverRoot + "/uploads/tmp";
		registration.setMultipartConfig(new MultipartConfigElement(uploadTemp, 2097152, 4194304, 0));
	}
}
