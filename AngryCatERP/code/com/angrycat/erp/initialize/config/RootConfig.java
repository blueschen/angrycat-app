package com.angrycat.erp.initialize.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.mariadb.jdbc.MySQLDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.angrycat.erp.query.ConditionalQuery;
import com.angrycat.erp.query.HibernateQueryExecutable;
import com.angrycat.erp.query.QueryConfig;
import com.angrycat.erp.query.QueryConfigurable;

@Configuration
@ComponentScan(basePackages={"com.angrycat.erp.service", "com.angrycat.erp.excel", "com.angrycat.erp.ds"})
@EnableTransactionManagement
public class RootConfig {
	@Bean
	public DataSource dataSource(){
		// works!!
//		DriverManagerDataSource ds = new DriverManagerDataSource();
//		ds.setUrl("jdbc:mariadb://localhost:3306/attorney_t1");
//		ds.setDriverClassName("org.mariadb.jdbc.Driver");
//		ds.setUsername("root");
		
		MySQLDataSource ds = new MySQLDataSource();
		ds.setUrl("jdbc:mariadb://localhost:3306/angrycat");
		ds.setUser("root");
		ds.setPassword("root");
		return ds;
	}
	
	@Bean
	public LocalSessionFactoryBean sessionFactory(DataSource dataSource){
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();
		sfb.setDataSource(dataSource);
		sfb.setPackagesToScan("com.angrycat.erp.model", "com.angrycat.erp.security");
		Properties props = new Properties();
		props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
		props.setProperty("hibernate.show_sql", "true");
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
	
	@Bean
	@Scope("prototype")
	public HibernateQueryExecutable<?> conditionalQuery(LocalSessionFactoryBean lsfb){
		HibernateQueryExecutable<?> query = new ConditionalQuery<Object>(lsfb.getObject());
		return query;
	}
	
	// for testing
	@Bean
	@Scope("prototype")
	public QueryConfigurable queryConfigurable(){
		return new QueryConfig();
	}
}
