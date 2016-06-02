package com.angrycat.erp.test.sql;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.angrycat.erp.model.SalesDetail;
import com.angrycat.erp.sql.SqlRoot;
import com.angrycat.erp.sql.condition.SimpleCondition;
import com.angrycat.erp.sql.condition.StrCondition.MatchMode;

public class SqlRootTest {
	private SqlRoot q = null;
	
	@Before
	public void setUp(){
		q = SqlRoot.getInstance();
		q.select()
			.target("DISTINCT p").getRoot()
		.from()
			.target(SalesDetail.class, "p");
	}
	
	@Test
	public void simpleSql() {
		String expected = "SELECT DISTINCT p\nFROM " + SalesDetail.class.getName() + " p";
		assertEquals(expected, q.genSql());
	}
	
	@Test
	public void conditionSql(){
		q.where()
			.andConds()
				.andStrCondition("p.salePoint = :pSalePoint", MatchMode.ANYWHERE);
		String expected = "SELECT DISTINCT p\nFROM " + SalesDetail.class.getName() + " p\nWHERE p.salePoint = :pSalePoint";
		assertEquals(expected, q.genSql());
	}
	
	@Test
	public void conditionParams(){
		q.where()
		.andConds()
			.andStrCondition("p.salePoint = :pSalePoint", MatchMode.ANYWHERE);
		
		SimpleCondition sc = (SimpleCondition)q.findNodeById("pSalePoint").getFounds().get(0);
		sc.value("敦南誠品");
		Map<String, Object> params = q.getCondIdValuePairs();
		Map<String, Object> expected = new LinkedHashMap<>();
		expected.put("pSalePoint", "敦南誠品");
		
		assertEquals(expected, params);
	}
	
	

}
