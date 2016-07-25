package com.angrycat.erp.excel;

import static com.angrycat.erp.common.XSSFUtil.parseCellNumericVal;
import static com.angrycat.erp.common.XSSFUtil.parseCellStrVal;
import static org.apache.poi.ss.util.CellReference.convertColStringToIndex;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.test.BaseTest;

@Component
@Scope("prototype")
public class ProductStockExcelImporter {
	@Autowired
	private SessionFactoryWrapper sfw;
	private String src;
	public void setSrc(String src){
		this.src = src;
	}
	@Transactional
	public void execute(){
		try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(src)));
			Workbook wb = WorkbookFactory.create(bis);){
			Sheet general = wb.getSheet("一般商品");
			
			int G_型號 = convertColStringToIndex("F");
			int G_英文名字 = convertColStringToIndex("G");
			int G_定價 = convertColStringToIndex("H");
			int G_總庫存 = convertColStringToIndex("L");
			int G_未出貨 = convertColStringToIndex("N");
			int G_辦公室庫存 = convertColStringToIndex("O");
			int G_專櫃抽屜 = convertColStringToIndex("P");
			int G_展示櫃 = convertColStringToIndex("Q");
			System.out.println("一般商品");
			iterateUpdateStock(
				general.iterator(),
				G_型號,
				G_英文名字,
				G_定價,
				G_總庫存,
				G_未出貨,
				G_辦公室庫存,
				G_專櫃抽屜,
				G_展示櫃);
			
			Sheet collection = wb.getSheet("Collection 商品");
			int C_型號 = convertColStringToIndex("F");
			int C_英文名字 = convertColStringToIndex("G");
			int C_定價 = convertColStringToIndex("H");
			int C_系列名 = convertColStringToIndex("I");
			int C_總庫存 = convertColStringToIndex("M");
			int C_未出貨 = convertColStringToIndex("P");
			int C_辦公室庫存 = convertColStringToIndex("Q");
			int C_專櫃抽屜 = convertColStringToIndex("R");
			int C_展示櫃 = convertColStringToIndex("S");
			System.out.println("Collection 商品");
			iterateUpdateStock(
				collection.iterator(),
				C_型號,
				C_英文名字,
				C_定價,
				C_總庫存,
				C_未出貨,
				C_辦公室庫存,
				C_專櫃抽屜,
				C_展示櫃);
			iterateUpdateSerialName(collection.iterator(), C_型號, C_系列名);
			Sheet necklace = wb.getSheet("手環手鏈項鏈");
			int N_型號 = convertColStringToIndex("E");
			int N_英文名字 = convertColStringToIndex("F");
			int N_定價 = convertColStringToIndex("G");
			int N_總庫存 = convertColStringToIndex("K");
			int N_未出貨 = convertColStringToIndex("N");
			int N_辦公室庫存 = convertColStringToIndex("O");
			int N_專櫃抽屜 = convertColStringToIndex("P");
			int N_展示櫃 = convertColStringToIndex("Q");

			System.out.println("手環手鏈項鏈");
			iterateUpdateStock(
				necklace.iterator(),
				N_型號,
				N_英文名字,
				N_定價,
				N_總庫存,
				N_未出貨,
				N_辦公室庫存,
				N_專櫃抽屜,
				N_展示櫃);			
			
//			Sheet other = wb.getSheet("其他配件"); // TODO TownTalk庫存是分開管理，並不在同一張庫存表上
//			int O_型號 = convertColStringToIndex("E");
//			int O_商品名稱 = convertColStringToIndex("F");
//			int O_社團價 = convertColStringToIndex("G");
//			int O_專櫃價 = convertColStringToIndex("H");
//			int O_總庫存 = convertColStringToIndex("I");
//			int O_未出貨 = convertColStringToIndex("K");
//			int O_辦公室庫存 = convertColStringToIndex("L");
//			int O_專櫃抽屜 = convertColStringToIndex("M");
//			int O_中和庫存 = convertColStringToIndex("N");
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	private void iterateUpdateStock(
		Iterator<Row> iterator, 
		int 型號, 
		int 英文名字, 
		int 定價,
		int 總庫存,
		int 未出貨,
		int 辦公室庫存,
		int 專櫃抽屜,
		int 展示櫃){
		Session s = sfw.currentSession();
		String queryByModelId = "SELECT p FROM " + Product.class.getName() + " p WHERE p.modelId = :modelId";
		int count = 0;
		while(iterator.hasNext()){
			Row row = iterator.next();
			count++;
			if(count == 1){// 標題略過
				continue;
			}
			String modelId = parseCellStrVal(row, 型號);
			if(StringUtils.isBlank(modelId)){
				continue;
			}
			System.out.println("第"+count+"筆:"+modelId);
			List<Product> products = s.createQuery(queryByModelId).setString("modelId", modelId).list();
			Product p = null;
			if(!products.isEmpty()){
				p = products.get(0);
			}else{
				p = new Product();
				p.setModelId(modelId);
				p.setNameEng(parseCellStrVal(row, 英文名字));
				p.setSuggestedRetailPrice(parseDouble(row, 定價));
				System.out.println("新增一筆:"+modelId);
			}
			int totalStockQty = parseInt(row, 總庫存);
			int officeStockQty = parseInt(row, 辦公室庫存);
			int drawerStockQty = parseInt(row, 專櫃抽屜);
			int showcaseStockQty = parseInt(row, 展示櫃);
			int notShipStockQty = parseInt(row, 未出貨);
			
			p.setTotalStockQty(totalStockQty);
			p.setOfficeStockQty(officeStockQty);
			p.setDrawerStockQty(drawerStockQty);
			p.setShowcaseStockQty(showcaseStockQty);
			p.setNotShipStockQty(notShipStockQty);
			
//			System.out.println(ReflectionToStringBuilder.toString(p, ToStringStyle.MULTI_LINE_STYLE));
			
//			s.saveOrUpdate(p);
//			s.flush();
		}
//		s.clear();
	}
	private void iterateUpdateSerialName(Iterator<Row> iterator, int 型號, int 系列名){
		Session s = sfw.currentSession();
		String queryByModelId = "SELECT p FROM " + Product.class.getName() + " p WHERE p.modelId = :modelId";
		int count = 0;
		while(iterator.hasNext()){
			count++;
			if(count == 1){
				continue;
			}
			Row row = iterator.next();
			String modelId = parseCellStrVal(row, 型號);
			Iterator<Product> itr = s.createQuery(queryByModelId).setString("modelId", modelId).iterate();
			Product p = itr.hasNext() ? itr.next() : new Product();
			if(StringUtils.isBlank(p.getId())){
				p.setModelId(modelId);
			}
			String serialName = parseCellStrVal(row, 系列名);
			if(StringUtils.isNotBlank(serialName)){
				if(StringUtils.isBlank(p.getSeriesName())){
					p.setSeriesName(serialName);
				}
				System.out.println(modelId+"系列名:"+serialName);
			}
			
//			s.saveOrUpdate(p);
//			s.flush();
		}
//		s.clear();
	}
	private static double parseDouble(Row row ,int colIdx){
		Double val = parseCellNumericVal(row, colIdx);
		if(val != null){
			return val;
		}
		return 0;
	}
	private static int parseInt(Row row, int colIdx){
		Double val = parseCellNumericVal(row, colIdx);
		if(val != null){
			return val.intValue();
		}
		return 0; 
	}
	
	private static void testExecute(){
		BaseTest.executeApplicationContext(acac->{
			ProductStockExcelImporter pse = acac.getBean(ProductStockExcelImporter.class);
			String path = "C:\\Users\\JerryLin\\Desktop\\臺灣OHM商品總庫存清單_2016_07_25.xlsx";
			if(new File(path).exists()){
				System.out.println("path file exists");
				// TODO 如果要啟用儲存，要取消iterateUpdateStock和iterateUpdateSerialName兩個地方的註解
				pse.setSrc(path);
				pse.execute();
			}
			
		});
	}
	public static void main(String[]args){
		testExecute();
	}
}
