package com.angrycat.erp.scheduletask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.angrycat.erp.service.LoadImgService;

@Component
public class ProductScheduleTask {
	@Autowired
	private LoadImgService loadImgService;
	
	/**
	 * 每天上午三點執行
	 */
	@Scheduled(cron="0 0 3 * * ?")
	public void loadImg(){
		loadImgService.execute();
	}
}
