package com.angrycat.erp.initialize.config;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@Configuration
@PropertySource("classpath:/application.properties")
@ComponentScan(basePackages={"com.angrycat.erp.service", "com.angrycat.erp.excel", "com.angrycat.erp.ds", "com.angrycat.erp.component"})
@EnableTransactionManagement(proxyTargetClass=true)
public class RootConfig {
	@Autowired
	private Environment env;
	
	@PostConstruct
	public void init(){
		String serverRoot = System.getProperty("catalina.home");
		if(StringUtils.isNotBlank(serverRoot)){
			System.setProperty("catalina.home", "C:/dts/apache-tomcat-8.0.23");
		}
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
		ComboPooledDataSource ds = new ComboPooledDataSource();
		try{
			ds.setDriverClass(env.getProperty("jdbc.driverClassName"));
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		ds.setJdbcUrl(env.getProperty("jdbc.url"));
		ds.setUser(env.getProperty("jdbc.username"));
		ds.setPassword(env.getProperty("jdbc.password"));
		ds.setMinPoolSize(5);
		ds.setMaxPoolSize(20);
		ds.setMaxStatements(50);
		ds.setCheckoutTimeout(1800);
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
		props.setProperty("hibernate.jdbc.batch_size", "100");
		sfb.setHibernateProperties(props);
		return sfb;
		
	}
	
	@Bean
	public PlatformTransactionManager transactionManager(LocalSessionFactoryBean sessionFactory){
		HibernateTransactionManager htm = new HibernateTransactionManager();
		htm.setSessionFactory(sessionFactory.getObject());
		return htm;
	}

}
