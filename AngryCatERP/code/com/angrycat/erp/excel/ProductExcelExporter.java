package com.angrycat.erp.excel;

import static com.angrycat.erp.onepos.excel.OnePosInitialExcelAccessor.CAT_ACT;
import static com.angrycat.erp.onepos.excel.OnePosInitialExcelAccessor.CAT_GIFT;
import static com.angrycat.erp.onepos.excel.OnePosInitialExcelAccessor.CAT_PDIS;
import static com.angrycat.erp.onepos.excel.OnePosInitialExcelAccessor.addDiscountItemCells;
import static com.angrycat.erp.onepos.excel.OnePosInitialExcelAccessor.addProductCells;
import static com.angrycat.erp.onepos.excel.OnePosInitialExcelAccessor.addValToCategorySheet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.angrycat.erp.onepos.vo.OnePosDiscountItem;
import com.angrycat.erp.service.QueryBaseService;
import com.angrycat.erp.web.WebUtils;
@Component
@Scope("prototype")
public class ProductExcelExporter extends ExcelExporter<Product> {
	@Autowired
	private QueryBaseService<Product, Product> queryBaseService;
	private String onePosTemplatePath = "E:\\angrycat_workitem\\member\\v36 ONE-POS Data Quick Import  快速匯入 - Empty .xls";
	private boolean ignoredIfWithoutBarcode = false;
	private boolean reductionRequired = false;
	public String getOnePosTemplatePath() {
		return onePosTemplatePath;
	}
	public void setOnePosTemplatePath(String onePosTemplatePath) {
		this.onePosTemplatePath = onePosTemplatePath;
	}
	/**
	 * 如果沒有條碼，是否忽略該筆資料
	 * @return
	 */
	public boolean isIgnoredIfWithoutBarcode() {
		return ignoredIfWithoutBarcode;
	}
	public void setIgnoredIfWithoutBarcode(boolean ignoredIfWithoutBarcode) {
		this.ignoredIfWithoutBarcode = ignoredIfWithoutBarcode;
	}
	/**
	 * 是否需要減項
	 * @return
	 */
	public boolean isReductionRequired() {
		return reductionRequired;
	}
	public void setReductionRequired(boolean reductionRequired) {
		this.reductionRequired = reductionRequired;
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
				
				Sheet productSheet = wb.getSheet("products");
				Sheet catSheet = wb.getSheet("categories");
				
				int batchSize = sfw.getBatchSize();
				Session s = sfw.currentSession();
				int currentCount = 0;
				Set<String> cats = new HashSet<>();
				while(rs.next()){
					Product product = (Product)rs.get(0);
					String barcode = product.getBarcode();
					if(ignoredIfWithoutBarcode && StringUtils.isBlank(barcode)){
						continue;
					}
					Row row = productSheet.createRow(++currentCount);
					
					String sku = product.getModelIdAdjusted();
					String cat = product.getProductCategory().getCode();
					String nameEng = product.getNameEng();
					String msrp = String.valueOf(new Double(product.getSuggestedRetailPrice()).intValue());
					String seriesName = product.getSeriesName();
					
					addProductCells(row, sku, nameEng, cat, msrp, barcode, seriesName);
					String catId = product.getProductCategory().getCode();
					String catName = product.getProductCategory().getName();
					if(StringUtils.isBlank(catName)){
						catName = catId;
					}
					addValToCategorySheet(cats, catSheet, catId, catName);
					if(currentCount % batchSize == 0){
						s.flush();
						s.clear();
					}
				}
				// 在OnePos中，需要把減項比作商品匯入，所以會有商品類別和相關資訊，但商品資料庫沒有這些資料
				if(reductionRequired){
					// 手動新增商品類別和商品
					addValToCategorySheet(cats, catSheet, CAT_GIFT, "禮卷");
					addValToCategorySheet(cats, catSheet, CAT_ACT,	"活動");
					addValToCategorySheet(cats, catSheet, CAT_PDIS, "比例折扣");
					
					List<OnePosDiscountItem> items = OnePosDiscountItem.getDefaultItems();
					for(OnePosDiscountItem item : items){
						Row pRow = productSheet.createRow(++currentCount);
						addDiscountItemCells(pRow, item);
					}
				}
				
				s.flush();
				s.clear();
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
