package com.angrycat.erp.test;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.VipDiscountDetail;

public class PropertyTest extends BaseTest {
	public static void main(String[]args){
		testFindIdAnnotated();
	}
	
	private static void testFindCollectionProperty(){
		Member m = new Member();
		
		for(int i = 0; i < 10; i++){
			
			VipDiscountDetail d = new VipDiscountDetail();
			m.getVipDiscountDetails().add(d);
		}
		
		BeanWrapperImpl bean = new BeanWrapperImpl(m);
		Object obj = bean.getPropertyValue("vipDiscountDetails[0]");
		
	}
	
	private static void testFindIdAnnotated(){
		executeApplicationContext(acac->{
			Member m = new Member();
			m.setId("tttteeesss");
			
			SessionFactory sf = acac.getBean(LocalSessionFactoryBean.class).getObject();
			String id = sf.getClassMetadata(m.getClass()).getIdentifierPropertyName();
			try {
				System.out.println("id property name: " + PropertyUtils.getProperty(m, id));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
}
