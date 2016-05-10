package com.angrycat.erp.excel;

import java.io.File;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.format.FormatListFactory;
import com.angrycat.erp.format.ObjectFormat;
import com.angrycat.erp.model.SalesDetail;
import com.angrycat.erp.service.QueryBaseService;
import com.angrycat.erp.test.BaseTest;
@Service
@Scope("prototype")
public class SalesDetailExcelExporter extends ExcelExporter<SalesDetail> {
	
	@Override
	public List<ObjectFormat> getFormats() {
		return FormatListFactory.ofSalesDetailForExcelExport();
	}

	private static void testExport(){
		BaseTest.executeApplicationContext(acac->{
			QueryBaseService<SalesDetail, SalesDetail> service = acac.getBean("queryBaseService", QueryBaseService.class);
			service.setRootAndInitDefault(SalesDetail.class);
			SalesDetailExcelExporter exporter = acac.getBean(SalesDetailExcelExporter.class);
			File file = exporter.normal(service);
		});
	}
	
	public static void main(String[]args){
		testExport();
	}
}
