package com.angrycat.erp.excel;

import java.util.List;

import org.springframework.stereotype.Component;

import com.angrycat.erp.format.ObjectFormat;
@Component
public class NotImplementedExcelExporter extends ExcelExporter<Object> {

	@Override
	public List<ObjectFormat> getFormats() {
		throw new UnsupportedOperationException("這個模組沒有實作Excel匯出");
	}

}
