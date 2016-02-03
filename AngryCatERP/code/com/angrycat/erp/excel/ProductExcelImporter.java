package com.angrycat.erp.excel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.angrycat.erp.model.Product;
import com.angrycat.erp.model.ProductCategory;
@Component
@Scope("prototype")
public class ProductExcelImporter extends ExcelImporter {
		
		private static final String COL_NAME_CAT = "類別";
		private static final String COL_NAME_MODEL_ID = "型號";
		private static final String COL_NAME_NAME_ENG = "英文名字";
		private static final String COL_NAME_PRICE = "定價";
		private static final String COL_NAME_SERIES_NAME = "系列名";
		private static final String COL_NAME_BARCODE = "條碼號碼";
		
		private List<Integer> sheetRange = new ArrayList<>();
		private List<Map<Integer, String>> sheetColumns = new ArrayList<>();
		DataFormatter df = new DataFormatter();
		
	@Override
	boolean processRow(Row row, Session s, int sheetIdx, int readableRowNum,
			Map<String, Integer> msg) {
		Map<Integer, String> columns = sheetColumns.get(sheetIdx);
		Product product = new Product();
		boolean isIgnored = false;
		for(int i = 0; i < row.getLastCellNum(); i++){
			Cell cell = row.getCell(i);
			if(cell == null){
				continue;
			}
			Object val = getXSSFValueByCellType(cell);
			if(val == null){
				continue;
			}
			String cellVal = val.toString();
			if(StringUtils.isBlank(cellVal)){
				continue;
			}
			cellVal = cellVal.trim();
			String sourceVal = cellVal;
			cellVal = cellVal.replace(" ", "");
			
			String columnName = columns.get(i);
			if(COL_NAME_MODEL_ID.equals(columnName)){// 略過重複
				long count = (long)s.createQuery("SELECT COUNT(p.id) FROM " + Product.class.getName() + " p WHERE upper(p.modelId) = :pModelId").setString("pModelId", sourceVal.toUpperCase()).uniqueResult();
				if(count > 0){
					break;
				}
				product.setModelId(sourceVal); // 特定產品譬如Towntalk的型號是自行編碼，所以會出現奇異的格式，暫且先保留這部分，待輸出格式時再行轉換
			}else if(COL_NAME_CAT.equals(columnName)){
				List<ProductCategory> cats = s.createQuery("FROM " + ProductCategory.class.getName() + " p WHERE p.code = :pCode").setString("pCode", cellVal).list();
				ProductCategory cat = null;
				if(!cats.isEmpty()){
					cat = cats.get(0);
				}else{
					cat = new ProductCategory();
					cat.setCode(cellVal);
					s.save(cat);
				}
				product.setProductCategory(cat);
			}else if(COL_NAME_NAME_ENG.equals(columnName)){
				product.setNameEng(cellVal);
			}else if(COL_NAME_PRICE.equals(columnName)){
				if(!NumberUtils.isNumber(cellVal)){
					throw new RuntimeException("sheet" + sheetIdx + "第"+ (i+1)+"欄第"+(row.getRowNum()+1)+"列: " + COL_NAME_PRICE + " 不是數字: " + cellVal);
				}
			}else if(COL_NAME_SERIES_NAME.equals(columnName)){
				product.setSeriesName(cellVal);
			}else if(COL_NAME_BARCODE.equals(columnName)){
				if(val.getClass() == Double.class){// 過大的double值直接轉成字串，都會變成科學表達式，所以最好利用BigDecimal再轉一次
					BigDecimal d = new BigDecimal((Double)val);
					product.setBarcode(d.toString());
				}else{
					product.setBarcode(cellVal);
				}
			}else{
//				throw new RuntimeException("sheet" + sheetIdx + "第"+(i+1)+"欄第"+(row.getRowNum()+1)+"列: 沒有找到事先定義的欄位: " + cellVal);
			}
		}
		if(StringUtils.isBlank(product.getModelId())){
			isIgnored = true;
		}
		if(!isIgnored){
			s.save(product);
		}
		return !isIgnored;
	}

	@Override
	protected List<Integer> sheetRange(){
		return sheetRange;
	}
	
	/**
	 * 要處理所有sheet，所以要動態計算每一次要處理的sheet索引範圍
	 */
	@Override
	protected void beforeProcessRow(){
		Workbook wb = getWorkbook();
		
		int sheetCount = wb.getNumberOfSheets();
		sheetRange.clear();
		sheetColumns.clear();
		for(int i = 0; i < sheetCount; i++){
			sheetRange.add(i);
			Map<Integer, String> columns = new HashMap<Integer, String>();
			sheetColumns.add(columns);
			Sheet sheet = wb.getSheetAt(i);
			Row firstRow = sheet.getRow(0);
			if(firstRow != null){
				for(int j = 0; j < firstRow.getLastCellNum(); j++){
					Cell cell = firstRow.getCell(j);
					if(cell != null){
						String val = df.formatCellValue(cell);
						if(StringUtils.isNotBlank(val)){
							columns.put(j, val.trim());
						}
					}
				}
			}
		}
		
		
	}
}
