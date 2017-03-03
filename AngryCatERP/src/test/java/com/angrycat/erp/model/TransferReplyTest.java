package com.angrycat.erp.model;

import java.sql.Date;
import java.time.LocalDate;

import org.jboss.logging.Logger;
import org.junit.Test;

import com.angrycat.erp.BaseTestCase;

public class TransferReplyTest extends BaseTestCase{
	private static final Logger LOG = Logger.getLogger(TransferReplyTest.class.getName());
	@Test
	public void save(){
		executor.executeTransaction(s->{
			int i = 0;
			TransferReply tr = new TransferReply();
			tr.setAddress("address_"+i);
			tr.setBrand("PANDORA");
			tr.setFbNickname("FbNickname_"+i);
			tr.setMobile("mobile_"+i);
			tr.setName("name_"+i);
			tr.setNote("note_"+i);
			tr.setPostalCode("postal_"+i);
			tr.setProductDetails("productDetails_"+i);
			tr.setTel("tel_"+i);
			tr.setTransferAccountCheck("transferAccountCheck_"+i);
			tr.setTransferAmount(1000);
			tr.setTransferDate(Date.valueOf(LocalDate.now()));
			LOG.info(tr.getCreateDate()+"");
			s.save(tr);
			s.flush();
		});
	}
}
