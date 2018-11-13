package com.angrycat.erp.genserial;

import java.util.logging.Logger;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;

import com.angrycat.erp.BaseTestCase;
import com.angrycat.erp.model.DefaultSerial;
import com.angrycat.erp.model.SalesDetail;

public class SalesDetailSerialGeneratorTest extends BaseTestCase{
	private static final String ID = SalesDetail.ORDERNO_GENERATOR_ID;
	private static final Logger LOG = Logger.getLogger(SalesDetailSerialGeneratorTest.class.getName());
	
	private DefaultSerialGenerator generator;
	
	@Before
	public void init() throws Throwable{
		super.init();
		generator = new DefaultSerialGenerator(ID, sessionFactory.getObject());
	}
	// INSERT INTO shr_defaultserial (id, no, dateSep0, sep1, resetNoTo, resetNoField) VALUES('ORDERNO_GENERATOR', '01', 'yyyyMMdd', '-', 1, 'dateSep0');
	public DefaultSerial findOrNewOne(Session s){
		DefaultSerial t = (DefaultSerial)s.get(DefaultSerial.class, ID);
		if(t == null){
			System.out.println("generator not found");
			t = new DefaultSerial();
			t.setId(ID);
			t.setNo("01");
			t.setDateSep0("yyyyMMdd");
			t.setSep1("-");
			t.setResetNoTo(1);
			t.setResetNoField("dateSep0");
			s.save(t);
			s.flush();
		}
		return t;
	}
	@Test
	public void increment(){
		executor.executeTransaction(s->{
			DefaultSerial serial = findOrNewOne(s);
			for(int i = 0; i < 5; i++){
				String nextNo = generator.getNext(serial);
				LOG.info("nextNo is " + nextNo);
			}
		});
	}
}
