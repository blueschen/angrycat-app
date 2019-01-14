package com.angrycat.erp.scheduletask;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.angrycat.erp.service.MailService;
import com.angrycat.erp.service.ProductKendoUiService;
import com.angrycat.erp.service.ProductKendoUiService.ProductStockReport;

import static com.angrycat.erp.common.EmailContact.*;

@Component
public class ProductStockScheduleTask {
	@Autowired
	private ProductKendoUiService productKendoUiService;
	@Autowired
	private MailService mailService;
	
	@PostConstruct
	public void init(){
		mailService
			.to(MIKO) 
			.cc(IFLY, JERRY, BLUES)
			.subject("庫存狀態通知");
//		mailService
//		.to(JERRY)
//		.subject("庫存狀態通知");
	}
//	@Scheduled(cron="0 10 10 * * ?")
	@Scheduled(cron="0 55 23 * * ?")
	public void updateStockIfMagentoIsMore(){		
		productKendoUiService.updateStockIfMagentoIsMore();
	}
//	@Scheduled(cron="0 20 13 * * ?")
	@Scheduled(cron="0 50 23 * * 1")
	public void generateStockReport(){		
		ProductStockReport report = productKendoUiService.generateStockReport();
		mailService.content(report.toHtml())
			.sendHTML();
	}
}
