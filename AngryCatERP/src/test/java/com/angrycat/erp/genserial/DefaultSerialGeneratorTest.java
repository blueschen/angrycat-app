package com.angrycat.erp.genserial;

import java.util.logging.Logger;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;

import com.angrycat.erp.BaseTestCase;
import com.angrycat.erp.model.DefaultSerial;

public class DefaultSerialGeneratorTest extends BaseTestCase{
	private static final String ID = "test_id";
	private static final Logger LOG = Logger.getLogger(DefaultSerialGeneratorTest.class.getName());
	
	private DefaultSerialGenerator generator;
	
	@Before
	public void init() throws Throwable{
		super.init();
		generator = new DefaultSerialGenerator(ID, sessionFactory.getObject());
	}
	
	public DefaultSerial findOrNewOne(Session s){
		DefaultSerial t = (DefaultSerial)s.get(DefaultSerial.class, ID);
		if(t == null){
			t = new DefaultSerial();
			t.setId(ID);
			t.setSep0("AP-");
			t.setNo("00001");
			t.setResetNoTo(1);
			s.save(t);
			s.flush();
		}
		return t;
	}
	@Test
	public void increment(){
		executor.executeTransaction(s->{
			DefaultSerial serial = findOrNewOne(s);
			String nextNo = generator.getNext(serial);
			LOG.info("nextNo is " + nextNo);
		});
	}
}
