package com.angrycat.erp.excel;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.format.FormatListFactory;
import com.angrycat.erp.format.ObjectFormat;
import com.angrycat.erp.model.TransferReply;
@Service
@Scope("prototype")
public class TransferReplyExcelExporter extends ExcelExporter<TransferReply> {

	@Override
	public List<ObjectFormat> getFormats() {
		return FormatListFactory.ofTransferReplyForExcelExport();
	}

}
