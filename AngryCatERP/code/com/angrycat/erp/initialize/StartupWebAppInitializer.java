package com.angrycat.erp.initialize;

import java.io.File;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration.Dynamic;

import org.apache.commons.io.FileUtils;
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
		File dir = new File(uploadTemp);
		if(!dir.exists()){
			try{
				FileUtils.forceMkdir(dir);
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
		}
		registration.setMultipartConfig(new MultipartConfigElement(uploadTemp, 2097152, 4194304, 0));
	}
}
