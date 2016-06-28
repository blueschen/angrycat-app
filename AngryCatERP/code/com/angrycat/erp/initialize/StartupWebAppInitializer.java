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
		String uploadTemp = getUploadsTempPath();
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
	public static String getUploadRoot(){
		String serverRoot = System.getProperty("catalina.home");
		return serverRoot;
	}
	/**
	 * 不管根路徑，取得子目錄位置
	 * @return
	 */
	public static String getUploadsTempSubPath(){
		String SEP = File.separator;
		return "/uploads/tmp".replace("/", SEP);
	}
	/**
	 * 取得上傳的暫存路徑
	 * @return
	 */
	public static String getUploadsTempPath(){
		String serverRoot = getUploadRoot();
		String uploadTemp = serverRoot + getUploadsTempSubPath();
		return uploadTemp;
	}
}
