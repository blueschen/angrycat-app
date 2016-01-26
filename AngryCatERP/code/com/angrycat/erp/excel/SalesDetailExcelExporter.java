package com.angrycat.erp.excel;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.format.FormatListFactory;
import com.angrycat.erp.format.ObjectFormat;
import com.angrycat.erp.model.SalesDetail;
@Service
@Scope("prototype")
public class SalesDetailExcelExporter extends ExcelExporter<SalesDetail> {

	@Override
	public List<ObjectFormat> getFormats() {
		return FormatListFactory.ofSalesDetailForExcelExport();
	}

}
