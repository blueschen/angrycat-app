package com.angrycat.erp;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.DefaultSerial;

public class BaseTestCase {
	protected LocalSessionFactoryBean sessionFactory;
	protected SessionFactoryWrapper executor;
	
	@Before
	public void init() throws Throwable{
		RootConfig config = new RootConfig();
		sessionFactory = config.sessionFactory(null);
		
		// 因為RootConifg當中要透過Spring container注入機制才能取得變數初始化DataSource，所以這邊不用DataSource，而是設定SessionFactory
		// TODO 測試完畢後要拿掉資料庫設定
		Properties props = sessionFactory.getHibernateProperties();
		props.setProperty("hibernate.connection.driver_class", "org.mariadb.jdbc.Driver");
		props.setProperty("hibernate.connection.url", "");
		props.setProperty("hibernate.connection.username", "");
		props.setProperty("hibernate.connection.password", "");
		
		sessionFactory.setAnnotatedClasses(DefaultSerial.class);
		sessionFactory.afterPropertiesSet(); // 如果脫離Spring container要單獨執行，要加上這段才能成功
		
		executor = new SessionFactoryWrapper();
		executor.setLocalSessionFactoryBean(sessionFactory);
	}
	@After
	public void deinit(){
		sessionFactory.destroy();
	}
}
