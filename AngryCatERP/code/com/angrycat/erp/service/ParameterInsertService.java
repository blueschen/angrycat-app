package com.angrycat.erp.service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.model.Parameter;
import com.angrycat.erp.model.ParameterCategory;
import com.angrycat.erp.test.BaseTest;
/**
 * 新增參數
 * 將所有參數統一在程式管理
 * @author JerryLin
 *
 */
@Service
@Scope("prototype")
public class ParameterInsertService {
	@Autowired
	private SessionFactoryWrapper sfw;
	@Transactional
	public void execute(){
//		clearAllParams();
		initSalesDetailParams();
		initTestParams();
	}
	@Transactional
	public void clearAllParams(){
		Session s = sfw.currentSession();
		
		String delParam = "SELECT DISTINCT p FROM " + Parameter.class.getName() + " p";
		String delParamCat = "SELECT DISTINCT p FROM " + ParameterCategory.class.getName() + " p";
		List<Parameter> params = s.createQuery(delParam).list();
		params.forEach(p->{
			s.delete(p);
		});
		List<ParameterCategory> cats = s.createQuery(delParamCat).list();
		cats.forEach(c->{
			s.delete(c);
		});
		s.flush();
		s.clear();
	}
	/**
	 * 初始化銷售明細參數類別及參數
	 */
	@Transactional
	public void initSalesDetailParams(){
		Session s = sfw.currentSession();
		
		Arrays.asList(		
		new Parameters("銷售狀態", s)
			.add("10. 待出貨")
			.add("20. 集貨中")
			.add("30. 調貨中")
			.add("40. 待補貨")
			.add("99. 已出貨")
			.add("98. 作廢")
		
		,new Parameters("銷售點", s)
			.add("敦南誠品")
			.add("FB社團")
			.add("粉絲團")
			.add("商店")
			.add("私訊")
		
		,new Parameters("折扣別", s,		"discount")
			.add("VIP9折",		"0.9")
			.add("VIPBD8折",	"0.8")
			.add("EVENT85", 	"0.85")
			.add("EVENT9", 	"0.9")
			.add("EVENT8", 	"0.8")
			.add("FREEBLT", 	"0")
			.add("FREEBED", 	"0")
		
		,new Parameters("付款別", s)
			.add("誠品")
			.add("PayPal")
			.add("匯款")
			.add("現金")
			.add("刷卡")
		
		,new Parameters("付款狀態", s)
			.add("已付款")
			.add("未付款")
			.add("已付訂金")
		
		,new Parameters("郵寄方式", s)
			.add("郵局掛號")
			.add("EMS(國際)")
			.add("黑貓")
			.add("全家店取")
			.add("7-11店取")
			.add("面交")
			.add("Fedex(國際)")
			.add("國際掛號")
			
		).forEach(p->{
			p.save();
		});
		s.flush();
	}
	@Transactional
	public void initTestParams(){
		Session s = sfw.currentSession();
		
		Arrays.asList(
			new Parameters("出題", s)
				.add("配題數")
				.addProperty("total", "5")
				.addProperty("product", "5")
				.addProperty("exam", "0")
		).forEach(p->{
			p.save();
		});
		s.flush();
	}
	@Transactional
	public void initMockParams(){
		Session s = sfw.currentSession();
		
		Arrays.asList(
			new Parameters("測試用", s)
				.add("測試一")
				.add("測試二")
				.add("測試三")
				.add("測試四")
				.add("測試五")
		).forEach(p->{
			p.save();
		});
		s.flush();
	}
	private static class Parameters{
		private ParameterCategory cat;
		private Session s;
		private LinkedList<Parameter> params = new LinkedList<>();
		private String[] propertyNames;
		
		public Parameters(String catName){
			this.cat = new ParameterCategory();
			this.cat.setName(catName);
		};
		public Parameters(String catName, Session s){
			this(catName);
			this.s = s;
		};
		public Parameters(String catName, Session s, String... propertyNames){
			this(catName, s);
			this.propertyNames = propertyNames;
		};		
		public Parameters add(String name){
			Parameter p = new Parameter();
			p.setNameDefault(name);
			p.setParameterCategory(cat);
			params.add(p);
			return this;
		}
		public Parameters add(String name, String...propertyValues){
			add(name);
			Parameter p = params.getLast();
			IntStream.range(0, propertyValues.length).forEach(idx->{
				p.getLocaleNames().put(propertyNames[idx], propertyValues[idx]);
			});
			return this;
		}
		public Parameters addProperty(String propertyName, String propertyValue){
			Parameter p = params.getLast();
			p.getLocaleNames().put(propertyName, propertyValue);
			return this;
		}
		public void save(){
			s.save(cat);
			s.flush();
			params.forEach(p->{
				s.save(p);
			});
			s.flush();
			s.clear();
		}
	}
	
	private static void testInitSalesDetailParams(){
		BaseTest.executeApplicationContext(acac->{
			ParameterInsertService serv = acac.getBean(ParameterInsertService.class);
			serv.initSalesDetailParams();
		});
	}
	private static void testInitTestParams(){
		BaseTest.executeApplicationContext(acac->{
			ParameterInsertService serv = acac.getBean(ParameterInsertService.class);
			serv.initTestParams();
		});
	}
	private static void testInitMockParams(){
		BaseTest.executeApplicationContext(acac->{
			ParameterInsertService serv = acac.getBean(ParameterInsertService.class);
			serv.initMockParams();
		});
	} 
	private static void testToJsonStr(){
		BaseTest.executeApplicationContext(acac->{
			SessionFactoryWrapper sfw = acac.getBean(SessionFactoryWrapper.class);
			List<Parameter> ps = sfw.executeFindResults(s->{
				List<Parameter> ret = s.createQuery("SELECT DISTINCT p FROM " + Parameter.class.getName() + " p WHERE p.nameDefault = :name").setString("name", "VIP9折").list();
				return ret;
			});
			if(!ps.isEmpty()){
				System.out.println(CommonUtil.parseToJson(ps.get(0)));
			}
		});
	}
	private static void testExecute(){
		BaseTest.executeApplicationContext(acac->{
			ParameterInsertService serv = acac.getBean(ParameterInsertService.class);
			serv.execute();
		});
	} 
	public static void main(String[]args){
//		testInitSalesDetailParams();
//		testInitTestParams();
//		testToJsonStr();
//		testInitMockParams();
		testExecute();
	}
}
