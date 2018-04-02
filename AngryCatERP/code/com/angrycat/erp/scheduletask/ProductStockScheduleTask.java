package com.angrycat.erp.scheduletask;

import static com.angrycat.erp.common.EmailContact.BLUES;
import static com.angrycat.erp.common.EmailContact.IFLY;
import static com.angrycat.erp.common.EmailContact.JERRY;
import static com.angrycat.erp.common.EmailContact.MIKO;
import static com.angrycat.erp.common.EmailContact.SLOW;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.angrycat.erp.service.MailService;
import com.angrycat.erp.service.ProductKendoUiService;
import com.angrycat.erp.service.ProductKendoUiService.ProductStockReport;

@Component
public class ProductStockScheduleTask {
//	@Autowired
//	private ProductKendoUiService productKendoUiService;
//	@Autowired
//	private MailService mailService;
//	
//	@PostConstruct
//	public void init(){
//		mailService
////			.to(MIKO, SLOW) 
////			.cc(IFLY, JERRY, BLUES) // TODO 待上線後再啟用
//			.subject("庫存狀態通知");
//	}
	// TODO 待上線後再啟用
	/*
	@Scheduled(cron="0 55 23 * * ?")
	public void updateStockIfMagentoIsMore(){
		
//		productKendoUiService.updateStockIfMagentoIsMore();
	}
	@Scheduled(cron="0 50 23 * * 1")
	public void generateStockReport(){		
		ProductStockReport report = productKendoUiService.generateStockReport();
		mailService.content(report.toHtml())
			.sendHTML();
	}*/
}
