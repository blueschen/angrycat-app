package com.angrycat.erp.initialize.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.mariadb.jdbc.MySQLDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import com.angrycat.erp.query.ConditionalQuery;
import com.angrycat.erp.query.HibernateQueryExecutable;

@Configuration
@ComponentScan(basePackages={"com.angrycat.erp.service"})
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
	@Scope("prototype")
	public HibernateQueryExecutable<?> conditionalQuery(LocalSessionFactoryBean lsfb){
		HibernateQueryExecutable<?> query = new ConditionalQuery<Object>(lsfb.getObject());
		return query;
	}
}
