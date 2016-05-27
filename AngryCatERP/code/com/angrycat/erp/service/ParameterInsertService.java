package com.angrycat.erp.service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ParameterInsertService {
	@Autowired
	private SessionFactoryWrapper sfw;
	
	/**
	 * 清除舊資料後，初始化參數類別及參數
	 */
	@Transactional
	public void initAfterClearOld(){
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
		
		Arrays.asList(		
		new Parameters("銷售狀態", s)
			.addParameter("10. 待出貨")
			.addParameter("20. 集貨中")
			.addParameter("30. 調貨中")
			.addParameter("40. 待補貨")
			.addParameter("99. 已出貨")
		
		,new Parameters("銷售點", s)
			.addParameter("誠品") // TODO 敦南誠品??
			.addParameter("社團") // TODO FB社團??
			.addParameter("粉絲團")
			.addParameter("商店")
			.addParameter("私訊")
		
		,new Parameters("折扣別", s,		"discount")
			.addParameter("VIP9折",		"0.9")
			.addParameter("VIPBD8折",	"0.8")
			.addParameter("EVENT85", 	"0.85")
			.addParameter("EVENT9", 	"0.9")
			.addParameter("EVENT8", 	"0.8")
			.addParameter("FREEBLT", 	"0")
			.addParameter("FREEBED", 	"0")
		
		,new Parameters("付款別", s)
			.addParameter("誠品")
			.addParameter("PayPal")
			.addParameter("匯款")
			.addParameter("現金")
			.addParameter("刷卡")
		
		,new Parameters("付款狀態", s)
			.addParameter("已付款")
			.addParameter("未付款")
			.addParameter("已付訂金")
		
		,new Parameters("郵寄方式", s)
			.addParameter("郵局掛號")
			.addParameter("EMS(國際)")
			.addParameter("黑貓")
			.addParameter("全家店取")
			.addParameter("7-11店取")
			.addParameter("面交")
			.addParameter("Fedex(國際)")
			.addParameter("國際掛號")
			
		).forEach(p->{
			p.save();
		});
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
		public Parameters addParameter(String name){
			Parameter p = new Parameter();
			p.setNameDefault(name);
			p.setParameterCategory(cat);
			params.add(p);
			return this;
		}
		public Parameters addParameter(String name, String...propertyValues){
			addParameter(name);
			Parameter p = params.getLast();
			IntStream.range(0, propertyValues.length).forEach(idx->{
				p.getLocaleNames().put(propertyNames[idx], propertyValues[idx]);
			});
			
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
	
	private static void testInitAfterClearOld(){
		BaseTest.executeApplicationContext(acac->{
			ParameterInsertService serv = acac.getBean(ParameterInsertService.class);
			serv.initAfterClearOld();
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
	
	public static void main(String[]args){
//		testInitAfterClearOld();
		testToJsonStr();
	}
}
