package com.angrycat.erp.initialize.config;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.genserial.DefaultSerialGenerator;
import com.angrycat.erp.genserial.GenSerialUtil;
import com.angrycat.erp.model.AmericanGroupBuyOrderForm;
import com.angrycat.erp.model.SalesDetail;
import com.angrycat.erp.sql.SqlRoot;
import com.mchange.v2.c3p0.ComboPooledDataSource;

@Configuration
@PropertySource("classpath:/application.properties")
@ComponentScan(basePackages={"com.angrycat.erp.service", "com.angrycat.erp.excel", "com.angrycat.erp.ds", "com.angrycat.erp.component", "com.angrycat.erp.businessrule", "com.angrycat.erp.log", "com.angrycat.erp.shortnews", "com.angrycat.erp.onepos", "com.angrycat.erp.scheduletask", "com.angrycat.erp.member.vip"})
@EnableTransactionManagement(proxyTargetClass=true)
@EnableScheduling
@EnableAsync
public class RootConfig {
	public static final String DEFAULT_BATCH_SIZE = "100";
	
	public static final String VERSION = LocalDateTime.now().toString();
	
	private static final String SEP = File.separator;
	private static final String LOCAL_TOMCAT_PATH = "C:/dts/apache-tomcat-8.0.23".replace("/", SEP);
	private static final String NAS_TOMCAT_PRODUCTION_PATH = "/usr/local/apache-tomcat-8.0.23".replace("/", SEP);
	private static final String NAS_TOMCAT_TEST1_PATH = "/usr/local/apache-tomcat-8.0.23_test1".replace("/", SEP);
	private static final String NAS_TOMCAT_TEST2_PATH = "/usr/local/apache-tomcat-8.0.23_test2".replace("/", SEP);
	private static final String NAS_TOMCAT_TEST3_PATH = "/usr/local/apache-tomcat-8.0.23_test3".replace("/", SEP);
	private static final String NAS_TOMCAT_TEST4_PATH = "/usr/local/apache-tomcat-8.0.23_test4".replace("/", SEP);
	
	private static final String CATALINA_HOME = "catalina.home";
	@Autowired
	private Environment env;
	
	@PostConstruct
	public void init(){
		//  catalina.home目前是提供log4j2設定用。如果是不啟動Tomcat的本地端測試，也可以透過這個參數，設定要操作的是本地還是NAS資料庫，dataSource會根據這個值調整拿資料庫設定，讓改變設定的地方集中在此。
		String serverRoot = System.getProperty(CATALINA_HOME);
		if(StringUtils.isBlank(serverRoot)){
//			System.setProperty("catalina.home", NAS_TOMCAT_PRODUCTION_PATH);
			System.setProperty(CATALINA_HOME, LOCAL_TOMCAT_PATH);
//			System.setProperty("catalina.home", NAS_TOMCAT_TEST1_PATH);
		}
		// 新增流水號產生器: 美國團訂單
		GenSerialUtil.addGenerator(new DefaultSerialGenerator(AmericanGroupBuyOrderForm.SALESNO_GENERATOR_ID, sessionFactory(dataSource()).getObject()));
		GenSerialUtil.addGenerator(new DefaultSerialGenerator(SalesDetail.ORDERNO_GENERATOR_ID, sessionFactory(dataSource()).getObject()));
	}
	
	@Bean(destroyMethod="close")
	public DataSource dataSource(){
		// works!!
//		DriverManagerDataSource ds = new DriverManagerDataSource();
//		ds.setUrl("jdbc:mariadb://localhost:3306/attorney_t1");
//		ds.setDriverClassName("org.mariadb.jdbc.Driver");
//		ds.setUsername("root");
		
//		MySQLDataSource ds = new MySQLDataSource();
//		ds.setUrl("jdbc:mariadb://localhost:3306/angrycat");
//		ds.setUser("root");
//		ds.setPassword("root");
		
		// c3p0 connection pool datasource
		// c3p0 config ref. http://www.mchange.com/projects/c3p0/index.html#configuration
		// c3p0 config sample http://stackoverflow.com/questions/26583120/connections-checking-in-c3p0-pool
		// c3p0 config sample http://stackoverflow.com/questions/11125962/correct-way-to-keep-pooled-connections-alive-or-time-them-out-and-get-fresh-one
		ComboPooledDataSource ds = new ComboPooledDataSource();
		try{
			ds.setDriverClass(env.getProperty("jdbc.driverClassName"));
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		String serverRoot = System.getProperty(CATALINA_HOME);
		String remoteUrl = env.getProperty("jdbc.remote.url"); // 透過內網，從本地端連到主機資料庫位置
		String jdbcUrl = env.getProperty("jdbc.url"); // 在本地端，連到本地資料庫位置
		String nasJdbcUrl = env.getProperty("jdbc.intranet.url"); // 在主機上，連到主機資料庫位置
//		if(LOCAL_TOMCAT_PATH.equals(serverRoot)){
//			jdbcUrl = env.getProperty("jdbc.url");
//		}else if(NAS_TOMCAT_PRODUCTION_PATH.equals(serverRoot)){
//			jdbcUrl = nasJdbcUrl;
//		}else if(NAS_TOMCAT_TEST1_PATH.equals(serverRoot)){
//			jdbcUrl = nasJdbcUrl + "_test1";
//		}else if(NAS_TOMCAT_TEST2_PATH.equals(serverRoot)){
//			jdbcUrl = nasJdbcUrl + "_test2";
//		}else if(NAS_TOMCAT_TEST3_PATH.equals(serverRoot)){
//			jdbcUrl = nasJdbcUrl + "_test3";
//		}else if(NAS_TOMCAT_TEST4_PATH.equals(serverRoot)){
//			jdbcUrl = nasJdbcUrl + "_test4";
//		}
//		jdbcUrl = nasJdbcUrl;
//		System.out.println("jdbcUrl: " + jdbcUrl);
		ds.setJdbcUrl(jdbcUrl);//設定資料庫連線位置
		ds.setUser(env.getProperty("jdbc.username"));
		ds.setPassword(env.getProperty("jdbc.password"));
		ds.setInitialPoolSize(5);
		ds.setMinPoolSize(5);
		ds.setMaxPoolSize(20);
		ds.setAcquireIncrement(3);
		ds.setMaxStatements(50);
		ds.setCheckoutTimeout(1800);
		
		// 測試連線的設定
		ds.setTestConnectionOnCheckin(true);
		ds.setTestConnectionOnCheckout(false); // for performance disabled
		ds.setIdleConnectionTestPeriod(300); // 五分鐘沒有連線，就測試連線
		
		return ds;
	}
	
	@Bean
	public LocalSessionFactoryBean sessionFactory(DataSource dataSource){
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();
		sfb.setDataSource(dataSource);
		sfb.setPackagesToScan("com.angrycat.erp.model", "com.angrycat.erp.security");
		Properties props = new Properties();
		props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
		props.setProperty("hibernate.show_sql", "false");
		props.setProperty("hibernate.jdbc.batch_size", DEFAULT_BATCH_SIZE);
		sfb.setHibernateProperties(props);
		return sfb;
		
	}
	
	@Bean
	public PlatformTransactionManager transactionManager(LocalSessionFactoryBean sessionFactory){
		HibernateTransactionManager htm = new HibernateTransactionManager();
		htm.setSessionFactory(sessionFactory.getObject());
		return htm;
	}

//	@Bean(destroyMethod="close")
//	public DataSource nasDataSource(){
//		
//		// c3p0 connection pool datasource
//		ComboPooledDataSource ds = new ComboPooledDataSource();
//		try{
//			ds.setDriverClass(env.getProperty("jdbc.driverClassName"));
//		}catch(Throwable e){
//			throw new RuntimeException(e);
//		}
//		ds.setJdbcUrl(env.getProperty("jdbc.intranet.url"));
//		ds.setUser(env.getProperty("jdbc.username"));
//		ds.setPassword(env.getProperty("jdbc.password"));
//		ds.setInitialPoolSize(5);
//		ds.setMinPoolSize(5);
//		ds.setMaxPoolSize(20);
//		ds.setMaxStatements(50);
//		ds.setCheckoutTimeout(1800);
//		return ds;
//	}
//	
//	@Bean
//	public LocalSessionFactoryBean nasSessionFactory(DataSource nasDataSource){
//		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();
//		sfb.setDataSource(nasDataSource);
//		sfb.setPackagesToScan("com.angrycat.erp.model", "com.angrycat.erp.security");
//		Properties props = new Properties();
//		props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
//		props.setProperty("hibernate.show_sql", "false");
//		props.setProperty("hibernate.jdbc.batch_size", "100");
//		sfb.setHibernateProperties(props);
//		return sfb;
//		
//	}
//	
//	@Bean
//	public PlatformTransactionManager nasTransactionManager(LocalSessionFactoryBean nasSessionFactory){
//		HibernateTransactionManager htm = new HibernateTransactionManager();
//		htm.setSessionFactory(nasSessionFactory.getObject());
//		return htm;
//	}
	
	@Bean
	public Map<String, String> displayCountries(){
		return CommonUtil.getDisplayCountry();
	}
	
	@Bean
	/**
	 * 製作前端AngularJS下拉選單所需要的物件型態
	 * @return
	 */
	public String displayJsonCountries(){
		List<Map<String, String>> results = new ArrayList<>();
		displayCountries().forEach((country, language)->{
			Map<String, String> item = new HashMap<>();
			item.put("label", language);
			item.put("value", country);
			results.add(item);
		});
		return CommonUtil.parseToJson(results);
	}
	
	@Bean
	public JavaMailSender mailSender(){
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("msa.hinet.net");
		mailSender.setPort(25);
		return mailSender;
	}
	
	@Bean
	public SimpleMailMessage templateMessage(){
		SimpleMailMessage templateMessage = new SimpleMailMessage();
		templateMessage.setFrom("jerrylin@ohmbeads.com.tw");
		return templateMessage;
	}
	
	@Bean
	@Scope("prototype")
	public SqlRoot getSqlRoot(){
		return SqlRoot.getInstance();
	}
	
	@Bean
	@Scope("prototype")
	public String version(){
		return VERSION;
	}
}
