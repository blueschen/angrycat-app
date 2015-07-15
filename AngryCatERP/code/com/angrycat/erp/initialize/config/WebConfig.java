package com.angrycat.erp.initialize.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.accept.ContentNegotiationManagerFactoryBean;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
@EnableWebMvc
@ComponentScan("com.angrycat.erp.web.controller")
public class WebConfig extends WebMvcConfigurerAdapter {
	@Bean
	public ViewResolver viewResolver(ContentNegotiationManagerFactoryBean contentManager){
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix(".jsp");
		resolver.setExposeContextBeansAsAttributes(true);
		resolver.setViewClass(JstlView.class);
		
//		TilesViewResolver resolver = new TilesViewResolver();
		
		// priority: file extension->Accept header
//		ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
//		resolver.setContentNegotiationManager(contentManager.getObject());
		return resolver;
	}
	
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer){
		configurer.enable();
	}
	
	@Bean
	public MessageSource messageSource(){
		ReloadableResourceBundleMessageSource messageSource = 
			new ReloadableResourceBundleMessageSource();
		messageSource.setBasenames("classpath:messages", "classpath:ValidationMessages");
//		messageSource.setCacheSeconds(10);
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	} 
	
	/**
	 * let validator message encoding same with messageSource
	 * @return
	 */
	@Bean(name = "validator")
	public LocalValidatorFactoryBean validator(){
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.setValidationMessageSource(messageSource());
		return validator;
	}
	
	/**
	 * ref. http://www.silverbaytech.com/2013/04/16/custom-messages-in-spring-validation/
	 */
	@Override
	public Validator getValidator(){
		return validator();
	}
	
	/**
	 * for upload settings
	 * @return
	 * @throws IOException
	 */
	@Bean
	public MultipartResolver multipartResolver() throws IOException{
//		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
//				
//		String serverRoot = System.getProperty("catalina.home");
//		String uploadTemp = serverRoot + "/uploads/tmp";
//		multipartResolver.setUploadTempDir(new FileSystemResource(uploadTemp));
//		multipartResolver.setMaxUploadSize(2097152); // 2M
//		multipartResolver.setMaxInMemorySize(0);
		
		StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();
		return multipartResolver;
	}
	
	@Bean
	public ContentNegotiationManagerFactoryBean contentManager(){
		ContentNegotiationManagerFactoryBean cnmfb = new ContentNegotiationManagerFactoryBean();
		cnmfb.setFavorPathExtension(true);
		cnmfb.setIgnoreAcceptHeader(false);
		cnmfb.setDefaultContentType(MediaType.TEXT_HTML);
		cnmfb.setUseJaf(false);
		
		Properties props = new Properties();
		props.setProperty("html", "text/html");
		props.setProperty("json", "application/json");
		props.setProperty("xml", "application/xml");
		cnmfb.setMediaTypes(props);
		
		return cnmfb;
	}
}
