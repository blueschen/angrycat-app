package com.angrycat.erp.excel;

import static com.angrycat.erp.common.XSSFUtil.parseCellNumericVal;
import static com.angrycat.erp.common.XSSFUtil.parseCellStrVal;
import static org.apache.poi.ss.util.CellReference.convertColStringToIndex;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hibernate.Session;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.component.XSSFProcessor;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.test.BaseTest;

@Component
@Scope("prototype")
public class ProductStockExcelImporter {
	private static final String COLUMN_NAME_型號			= "型號";
	private static final String COLUMN_NAME_英文名字 		= "英文名字";
	private static final String COLUMN_NAME_品名 		= "品名";
	private static final String COLUMN_NAME_定價 		= "定價";
	private static final String COLUMN_NAME_總庫存 		= "庫存";
	private static final String COLUMN_NAME_Taobao庫存 	= "Taobao庫存";
	private static final String COLUMN_NAME_人民幣 		= "人民幣";
	private static final String COLUMN_NAME_系列名 		= "系列名";
	
	private static final String COLUMN_NAME_專櫃售價 		= "專櫃售價";
	private static final String COLUMN_NAME_售價 		= "售價";
		
	@Autowired
	private SessionFactoryWrapper sfw;
	@Autowired
	private BeanFactory beanFactory;
	private String src;
	private boolean mergeDisabled;
		
	public void setSrc(String src){
		this.src = src;
	}
	public boolean isMergeDisabled() {
		return mergeDisabled;
	}
	public void setMergeDisabled(boolean mergeDisabled) {
		this.mergeDisabled = mergeDisabled;
	}
	private XSSFProcessor initProcessor(Workbook wb, String sheetName){
		XSSFProcessor processor = beanFactory.getBean(XSSFProcessor.class, wb, sheetName, "Z");
		return processor;
	}
	private void persistAsProduct(Session s, Workbook wb, String...sheetNames){
		String queryByModelId = "SELECT p FROM " + Product.class.getName() + " p WHERE p.modelId = :modelId";
		List<String> priceChangeds = new ArrayList<>();
		
		for(String sheetName : sheetNames){
			XSSFProcessor processor = initProcessor(wb, sheetName);
			List<String> taobaoStockIsMore = new ArrayList<>();
			
			processor.iteratorBypassHeader();
			while(processor.hasNext()){
				processor.next();
				String modelId = processor.getStrVal(COLUMN_NAME_型號);
				if(StringUtils.isBlank(modelId)){
					continue;
				}
				modelId = modelId.trim();
//				int totalStockQty = processor.getIntValu(COLUMN_NAME_總庫存);
//				int taobaoStockQty = processor.getIntValu(COLUMN_NAME_Taobao庫存);
//				
//				if(taobaoStockQty > totalStockQty){
//					taobaoStockIsMore.add(modelId + "淘寶庫存("+ taobaoStockQty +")大於總庫存("+ totalStockQty +")");
//					//throw new RuntimeException(modelId + "淘寶庫存("+ taobaoStockQty +")大於總庫存("+ totalStockQty +")");
//				}
//				
//				String nameEng = processor.getStrVal(COLUMN_NAME_英文名字);
//				double price = processor.getDoubleVal(COLUMN_NAME_定價);
//				price = price != 0 ? price : processor.getDoubleVal(COLUMN_NAME_專櫃售價);
//				price = price != 0 ? price : processor.getDoubleVal(COLUMN_NAME_售價); 
//				Double priceAsRMB = processor.getDouble(COLUMN_NAME_人民幣);
//				String name = processor.getStrVal(COLUMN_NAME_品名);
//				String seriesName = processor.getStrVal(COLUMN_NAME_系列名);
//				
				// 以下針對小安提供Excel客製欄位
				int totalStockQty = processor.getIntValu("庫存");
				int taobaoStockQty = 0;
				
				if(taobaoStockQty > totalStockQty){
					taobaoStockIsMore.add(modelId + "淘寶庫存("+ taobaoStockQty +")大於總庫存("+ totalStockQty +")");
					//throw new RuntimeException(modelId + "淘寶庫存("+ taobaoStockQty +")大於總庫存("+ totalStockQty +")");
				}
				
				String nameEng = processor.getStrVal(COLUMN_NAME_英文名字);
				double price = processor.getDoubleVal(COLUMN_NAME_定價);
				Double priceAsRMB = 0.0;
				String name = processor.getStrVal(COLUMN_NAME_品名);
				String seriesName = processor.getStrVal(COLUMN_NAME_系列名);
				
				Product p = (Product)s.createQuery(queryByModelId).setString("modelId", modelId).uniqueResult();
				if(p == null){
					p = new Product();
					p.setModelId(modelId);
					p.setNameEng(nameEng);
					p.setSuggestedRetailPrice(price);
					p.setPriceAsRMB(priceAsRMB);
					p.setMainCategory(sheetName);
					
					p.setSeriesName(seriesName); // collection商品
					p.setName(name); // 保養品
				}else{
//					int originalStockQty = p.getTotalStockQty();
//					String updateStock = modelId + "["+ originalStockQty + "=>" + totalStockQty + "]";
					double originalPrice = p.getSuggestedRetailPrice();
					if(originalPrice != price){
						String priceChanged = modelId + "["+ originalPrice + "=>" + price + "]";
						priceChangeds.add(priceChanged);
					}
				}
				
				if(StringUtils.isNotBlank(nameEng) && StringUtils.isBlank(name)){
					p.setNameEng(nameEng);
					p.setName(nameEng);
				}else if(StringUtils.isNotBlank(name) && StringUtils.isBlank(nameEng)){
					p.setNameEng(name);
					p.setName(name);
				}else if(StringUtils.isNotBlank(name) && StringUtils.isNotBlank(nameEng)){
					p.setNameEng(nameEng);
					p.setName(name);
				}else{
					throw new RuntimeException(modelId+":name ["+name+"] and nameEng ["+nameEng+"] both not found value");
				}
				
				p.setTotalStockQty(totalStockQty);
				p.setTaobaoStockQty(taobaoStockQty);
				
				if(!mergeDisabled){
					s.saveOrUpdate(p);
					s.flush();
				}
			}
			if(!mergeDisabled){
				s.clear();
			}
			System.out.println(sheetName + " toalCount: " + processor.getCurrentRowIdx());
			if(priceChangeds.size() > 0){
				System.out.println("priceChangeds: " + priceChangeds.stream().collect(Collectors.joining(",")));
			}
			
			if(taobaoStockIsMore.size() > 0){
				System.out.println("淘寶庫存大於總庫存：");
				for(String msg : taobaoStockIsMore){
					System.out.println(msg);
				}
			}
			System.out.println("========================");
		}
	}
	/**
	 * TODO 要重新考量會遇到的多種情境，譬如: 更新庫存、比對資料、觀察資料、通報沒有庫存或定價等...
	 * TODO 重新思考方法名稱
	 */
	@Transactional
	public void resolveOHMToDB(){
		
		try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(src)));
			Workbook wb = WorkbookFactory.create(bis);){
			
			Session s = sfw.currentSession();
			persistAsProduct(s, wb, "手環手鏈項鏈戒指", "Collection商品", "一般商品");
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	@Transactional
	public void resolveTowntalkToDB(){
		
		try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(src)));
			Workbook wb = WorkbookFactory.create(bis);){
			
			Session s = sfw.currentSession();
			persistAsProduct(s, wb, "工作表1");
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	@Transactional
	public void resolveOHMToDBAt20181213(){
		
		try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(src)));
			Workbook wb = WorkbookFactory.create(bis);){
			
			Session s = sfw.currentSession();
			persistAsProduct(s, wb, "所有商品");
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	@Deprecated
	@Transactional
	public void execute(){
		try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(src)));
			Workbook wb = WorkbookFactory.create(bis);){
			Sheet general = wb.getSheet("一般商品");
			
			int G_型號 = convertColStringToIndex("F");
			int G_英文名字 = convertColStringToIndex("G");
			int G_定價 = convertColStringToIndex("H");
			int G_總庫存 = convertColStringToIndex("L");
			System.out.println("一般商品");
			iterateUpdateStock(
				general.iterator(),
				G_型號,
				G_英文名字,
				G_定價,
				G_總庫存);
			
			Sheet collection = wb.getSheet("Collection 商品");
			int C_型號 = convertColStringToIndex("F");
			int C_英文名字 = convertColStringToIndex("G");
			int C_定價 = convertColStringToIndex("H");
			int C_系列名 = convertColStringToIndex("I");
			int C_總庫存 = convertColStringToIndex("M");
			System.out.println("Collection 商品");
			iterateUpdateStock(
				collection.iterator(),
				C_型號,
				C_英文名字,
				C_定價,
				C_總庫存);
			iterateUpdateSerialName(collection.iterator(), C_型號, C_系列名);
			Sheet necklace = wb.getSheet("手環手鏈項鏈");
			int N_型號 = convertColStringToIndex("E");
			int N_英文名字 = convertColStringToIndex("F");
			int N_定價 = convertColStringToIndex("G");
			int N_總庫存 = convertColStringToIndex("K");
			System.out.println("手環手鏈項鏈");
			iterateUpdateStock(
				necklace.iterator(),
				N_型號,
				N_英文名字,
				N_定價,
				N_總庫存);			
			
			Sheet other = wb.getSheet("保養品");
			int CARE_型號 = convertColStringToIndex("G");
			int CARE_品名 = convertColStringToIndex("H");
			int CARE_專櫃售價 = convertColStringToIndex("J");
			int CARE_總庫存 = convertColStringToIndex("K");
			System.out.println("保養品");
			iterateUpdateCareStock(
				other.iterator(),
				CARE_型號,
				CARE_品名,
				CARE_專櫃售價,
				CARE_總庫存);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	@Deprecated
	private void iterateUpdateStock(
		Iterator<Row> iterator, 
		int 型號, 
		int 英文名字, 
		int 定價,
		int 總庫存){
		Session s = sfw.currentSession();
		String queryByModelId = "SELECT p FROM " + Product.class.getName() + " p WHERE p.modelId = :modelId";
		int count = 0;
		List<String> added = new ArrayList<>();
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
//			System.out.println("第"+count+"筆:"+modelId);
			List<Product> products = s.createQuery(queryByModelId).setString("modelId", modelId).list();
			Product p = null;
			if(!products.isEmpty()){
				p = products.get(0);
			}else{
				p = new Product();
				p.setModelId(modelId);
				p.setNameEng(parseCellStrVal(row, 英文名字));
				p.setSuggestedRetailPrice(parseDouble(row, 定價));
				added.add(modelId);
			}
			int totalStockQty = parseInt(row, 總庫存);			
			p.setTotalStockQty(totalStockQty);
			
//			System.out.println(ReflectionToStringBuilder.toString(p, ToStringStyle.MULTI_LINE_STYLE));
			if(!mergeDisabled){
				s.saveOrUpdate(p);
				s.flush();
			}
		}
		if(!mergeDisabled){
			s.clear();
		}
		System.out.println("共處理" + (count-1) + "筆");
		System.out.println("共新增" + added.size() + "筆:" + added.stream().collect(Collectors.joining(",")));
	}
	@Deprecated
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
//				System.out.println(modelId+"系列名:"+serialName);
			}
			if(!mergeDisabled){
				s.saveOrUpdate(p);
				s.flush();
			}
		}
		if(!mergeDisabled){
			s.clear();
		}
//		System.out.println("共處理" + (count-1) + "筆");
	}
	@Deprecated
	private void iterateUpdateCareStock(
			Iterator<Row> iterator, 
			int 型號, 
			int 品名, 
			int 專櫃售價,
			int 總庫存){
			Session s = sfw.currentSession();
			String queryByModelId = "SELECT p FROM " + Product.class.getName() + " p WHERE p.modelId = :modelId";
			int count = 0;
			List<String> added = new ArrayList<>();
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
//				System.out.println("第"+count+"筆:"+modelId);
				List<Product> products = s.createQuery(queryByModelId).setString("modelId", modelId).list();
				Product p = null;
				if(!products.isEmpty()){
					p = products.get(0);
				}else{
					p = new Product();
					p.setModelId(modelId);
					p.setName(parseCellStrVal(row, 品名));
					p.setSuggestedRetailPrice(parseDouble(row, 專櫃售價));
					added.add(modelId);
				}
				int totalStockQty = parseInt(row, 總庫存);			
				p.setTotalStockQty(totalStockQty);
				
//				System.out.println(ReflectionToStringBuilder.toString(p, ToStringStyle.MULTI_LINE_STYLE));
				if(!mergeDisabled){
					s.saveOrUpdate(p);
					s.flush();
				}
			}
			if(!mergeDisabled){
				s.clear();
			}
			System.out.println("共處理" + (count-1) + "筆");
			System.out.println("共新增" + added.size() + "筆:" + added.stream().collect(Collectors.joining(",")));
		}
	@Deprecated
	private static double parseDouble(Row row ,int colIdx){
		Double val = parseCellNumericVal(row, colIdx);
		if(val != null){
			return val;
		}
		return 0;
	}
	@Deprecated
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
			String path = "C:\\Users\\JerryLin\\Desktop\\臺灣OHM商品總庫存清單_2016_12_07.xlsx";
			if(new File(path).exists()){
				System.out.println("path file exists");
				// TODO 如果要啟用儲存，要取消iterateUpdateStock，iterateUpdateSerialName和iterateUpdateCareStock三個地方的註解
				pse.setMergeDisabled(true);
				pse.setSrc(path);
				pse.execute();
			}
			
		});
	}
	public static void main(String[]args){
		testExecute();
	}
}
