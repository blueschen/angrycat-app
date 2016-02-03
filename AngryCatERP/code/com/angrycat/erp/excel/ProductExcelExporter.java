package com.angrycat.erp.excel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.angrycat.erp.format.FormatListFactory;
import com.angrycat.erp.format.ObjectFormat;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.onepos.excel.OnePosInitialExcelAccessor;
import com.angrycat.erp.service.QueryBaseService;
import com.angrycat.erp.web.WebUtils;
@Component
@Scope("prototype")
public class ProductExcelExporter extends ExcelExporter<Product> {
	@Autowired
	private QueryBaseService<Product, Product> queryBaseService;
	private String onePosTemplatePath = "E:\\angrycat_workitem\\member\\v36 ONE-POS Data Quick Import  快速匯入 - Empty .xls";
	private boolean ignoredIfWithoutBarcode = false;
	public String getOnePosTemplatePath() {
		return onePosTemplatePath;
	}
	public void setOnePosTemplatePath(String onePosTemplatePath) {
		this.onePosTemplatePath = onePosTemplatePath;
	}
	public boolean isIgnoredIfWithoutBarcode() {
		return ignoredIfWithoutBarcode;
	}
	public void setIgnoredIfWithoutBarcode(boolean ignoredIfWithoutBarcode) {
		this.ignoredIfWithoutBarcode = ignoredIfWithoutBarcode;
	}
	@PostConstruct
	public void init(){
		queryBaseService.setRootAndInitDefault(Product.class);
	}
	
	public File onePos(){
		return onePos(queryBaseService);
	}
	
	public File onePos(QueryBaseService<Product, Product> queryBaseService){		
		File tempFile = queryBaseService.executeScrollableQuery((rs, sfw)->{
			File defaultTemplateFile = new File(onePosTemplatePath);
			String template = null;
			if(defaultTemplateFile.exists()){
				template = onePosTemplatePath;
			}else{
				template = WebUtils.getWebRootFile("v36 ONE-POS Data Quick Import  快速匯入 - Empty .xls");
			}
			String tempPath = FileUtils.getTempDirectoryPath() + RandomStringUtils.randomAlphanumeric(8) + ".xls";
			System.out.println("tempPath: " + tempPath);
			File file = new File(tempPath);
			try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
				HSSFWorkbook wb = new HSSFWorkbook(new BufferedInputStream(new FileInputStream(template)))){
					
				CellStyle dateStyle = wb.createCellStyle();
				dateStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd"));
				
				Sheet sheet = wb.getSheet("products");
				
				int batchSize = sfw.getBatchSize();
				Session s = sfw.currentSession();
				int currentCount = 0;
				while(rs.next()){
					Product product = (Product)rs.get(0);
					String barcode = product.getBarcode();
					if(ignoredIfWithoutBarcode && StringUtils.isBlank(barcode)){
						continue;
					}
					Row row = sheet.createRow(++currentCount);
					
					String sku = product.getModelIdAdjusted();
					String cat = product.getProductCategory().getCode();
					String nameEng = product.getNameEng();
					String msrp = String.valueOf(new Double(product.getSuggestedRetailPrice()).intValue());
					
					OnePosInitialExcelAccessor.addProductCells(row, sku, nameEng, cat, msrp, barcode);
					if(currentCount % batchSize == 0){
						s.flush();
						s.clear();
					}
				}
				wb.write(bos);	
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
			return file;
		});
		return tempFile;
	}
	
	
	
	
	@Override
	public List<ObjectFormat> getFormats() {
		return FormatListFactory.ofProducForExcelExport();
	}

}
