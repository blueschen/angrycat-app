package com.angrycat.erp.initialize.config;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.accept.ContentNegotiationManagerFactoryBean;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.CssLinkResourceTransformer;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

@Configuration
@EnableWebMvc
@ComponentScan("com.angrycat.erp.web.controller")
public class WebConfig extends WebMvcConfigurerAdapter {
	private static final Charset UTF8 = Charset.forName("UTF-8");
	
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
	
	@Bean(name="messageSource")
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
	/**
	 * 在此產生jackson converter一來是要擴充支援的媒體類型(如純文字)<br>
	 * 二來註冊Hibernate4Module，避免在透過jackson轉換字串時，遇到lazy initialize的錯誤<br>
	 * 雖然在處理少量、簡單資料結構，可以透過annotation或者mixin的方式避開lazy initialize的問題，<br>
	 * 但隨著系統愈趨複雜，這些工作變得很瑣細而沒必要。<br>
	 * 透過Hibernate4Module，Spring MVC的responseBody可以正常回傳不在Hibernate Session範圍內的data model。
	 * @return
	 */
	public MappingJackson2HttpMessageConverter jacksonMessageConverter(){
		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
		List<MediaType> jsonTypes = new ArrayList<>(jsonConverter.getSupportedMediaTypes());
		jsonTypes.add(MediaType.TEXT_PLAIN);
		jsonConverter.setSupportedMediaTypes(jsonTypes);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		jsonConverter.setObjectMapper(mapper);
		
		return jsonConverter;
	}
	
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters){
		
		converters.add(new StringHttpMessageConverter(UTF8));
		converters.add(new ByteArrayHttpMessageConverter());
		converters.add(jacksonMessageConverter());
		super.configureMessageConverters(converters);
	}
	
	// ref. http://www.baeldung.com/cachable-static-assets-with-spring-mvc
	// ref. https://blog.csdn.net/xiejx618/article/details/40478275
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		int oneYear = 60 * 60 * 24 * 365;
		// origin: /js/test.js
		// ContentVersionStrategy: /js/test-69ea0cf3b5941340f06ea65583193168.js
		// FixedVersionStrategy: /v1.2.3/js/test.js
	    registry.addResourceHandler("/vendor/**", "/common/**")
	            .addResourceLocations("/vendor/", "/common/")
	            .setCachePeriod(oneYear)
	            .resourceChain(false)
	            .addResolver(
	            		new VersionResourceResolver()
	            			.addContentVersionStrategy("/**")
	            			//.addFixedVersionStrategy("0.0.1", "/**") // 目前使用FixedVersionStrategry某些css似乎會遇到問題
	            )
	            .addTransformer(new CssLinkResourceTransformer()); // css有時會@import其他的css，透過這個轉換器，可以重寫他 

	    // 除了在這邊設定之外，還要加上org.springframework.web.servlet.resource.ResourceUrlEncodingFilter
	    // 這個Filter目前是加在SecurityInitializer中
	    
	    // 在前端使用c:url標籤就可寫出被轉換的uri
	}
}
