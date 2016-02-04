package com.angrycat.erp.onepos.excel;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.common.DatetimeUtil;
import com.angrycat.erp.excel.ExcelColumn.Member;
import com.angrycat.erp.excel.ExcelColumn.OnePosClient;
import com.angrycat.erp.excel.ExcelColumn.Product.OnePos;
import com.angrycat.erp.excel.ExcelColumn.Product.Sheet1;
import com.angrycat.erp.excel.ExcelColumn.Product.Sheet2;
import com.angrycat.erp.excel.ExcelColumn.Product.Sheet3;
import com.angrycat.erp.excel.ExcelImgProcessor;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.onepos.vo.OnePosDiscountItem;
import com.angrycat.erp.service.http.HttpService;
import com.angrycat.erp.web.controller.MemberController;

/**
 * @author JerryLin<br>
 * 
 * 將公司內部產品的Excel轉成OnePos需要的Excel匯入格式。
 * 使用者自己手動編輯Excel效率不見得會比跑程式差。
 * 跑程式唯一顯著的優勢是抓圖。
 * <p>
 * 如果有額外設定的話(imgProcessEnabled = true)，
 * 可以抓取對應的圖檔之後改名以符合OnePos的命名要求，
 * 但要注意效能非常慢，
 * 1082筆資料需要執行7分鐘，
 * 這還只是本機端的測試結果。
 * 效能上的特性讓這隻程式不適合部署在網路主機上。
 * <p>
 * 可以透過範本或新增的方式匯出檔案，
 * 但強烈建議透過OnePos的範本(設定templatePath)匯出。
 */
@Service
@Scope("prototype")
public class OnePosInitialExcelAccessor {
	private static final String JPG_POSTFIX = "-o-a.jpg";
	private static final String URL_TEMPLATE = "http://ohmbeads.com/ohm2/media/import/{no}" + JPG_POSTFIX;
	private static final String DEFAULT_IMG_DIR = "C:"+File.separator+"ONE-POS DB"+File.separator+"Project"+File.separator;
	private static final DataFormatter DF = new DataFormatter();
	private static final String INVENTORY = "I";
	public static final String NON_INVENTORY = "N";
	public static final String CAT_GIFT = "GIFT"; // 代表禮券
	public static final String CAT_ACT = "ACT"; // 代表活動
	public static final String CAT_PDIS = "PDIS"; // 代表比例折扣(非固定金額折扣)
	public static final String BRAND_ID = "OHM";
	@Autowired
	private HttpService httpService;
	private boolean imgProcessEnabled;
	private boolean clientProcessEnabled;
	private String templatePath;
	private Map<String, String> barCodes = getBarCodes();
	private int importBarCodeCount = 0;
	private String outFormat = ".xlsx";
	private String memberPath = "E:\\angrycat_workitem\\member\\2015_10_05\\OHM Beads TW (AngryCat) 一般會員資料_update.xlsx"; // 會員資料的位置
	private String wipesPath = ""; // 拭金拭銀布資料的位置
	
	public boolean isImgProcessEnabled() {
		return imgProcessEnabled;
	}
	public void setImgProcessEnabled(boolean imgProcessEnabled) {
		this.imgProcessEnabled = imgProcessEnabled;
	}
	public String getTemplatePath() {
		return templatePath;
	}
	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}
	public boolean isClientProcessEnabled() {
		return clientProcessEnabled;
	}
	public void setClientProcessEnabled(boolean clientProcessEnabled) {
		this.clientProcessEnabled = clientProcessEnabled;
	}
	public String getOutFormat() {
		return outFormat;
	}
	public void setOutFormat(String outFormat) {
		this.outFormat = outFormat;
	}
	/**
	 * 匯入Excel，根據指定的Sheet和Column，找到產品型號。
	 * 依照OHM官方提供的規則，只要有型號，就可以在網路上找到對應的圖片。
	 * 圖片找到後，把他存放在指定資料夾位置。
	 * @param src: 來源Excel
	 * @param jpgDestDir: 圖片存放目錄
	 * @param sheetIdex: Sheet位置
	 * @param colIdx: Column位置
	 */
	private void storeImage(String src, String jpgDestDir, int sheetIdex, int colIdx){
		try(InputStream is = new FileInputStream(src);
		XSSFWorkbook wb = new XSSFWorkbook(is);){
			Sheet sheet = wb.getSheetAt(sheetIdex);
			Iterator<Row> rows = sheet.iterator();
			
			while(rows.hasNext()){
				Row row = rows.next();
				int rowNum = row.getRowNum();
				if(rowNum == 0){
					continue;
				}
				Cell cell = row.getCell(colIdx);
				String no = cell.getStringCellValue();
				storeImage(jpgDestDir, no);
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 用產品型號兜出圖片URL，找到之後放在指定目錄
	 * @param jpgDestDir
	 * @param no
	 */
	private void storeImage(String jpgDestDir, String no){
		if(!imgProcessEnabled){
			return;
		}
		if(StringUtils.isBlank(jpgDestDir)){
			jpgDestDir = DEFAULT_IMG_DIR;
		}
		String url = URL_TEMPLATE.replace("{no}", no);
		String dest = jpgDestDir + no + JPG_POSTFIX;
		try(FileOutputStream fos = new FileOutputStream(dest)){
			httpService.sendPost(url, bis->{
				try{
					IOUtils.copy(bis, fos);
				}catch(Throwable e){
					throw new RuntimeException(e);
				}
			});
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * ohm-tw-catalog-barcodes-151015.xlsx這張Excel表是整理ohm-tw-catalog-barcodes-151015.csv轉來
	 * @return
	 */
	private Map<String, String> getBarCodes(){
//		String path = "E:\\angrycat_workitem\\產品\\型號和條碼.xlsx";
		String path = "E:\\angrycat_workitem\\產品\\barCode\\2015_10_16\\ohm-tw-catalog-barcodes-151015.xlsx";
		Map<String, String> barCodes = Collections.emptyMap();
		File file = new File(path);
		if(!file.exists()){
			return barCodes;
		}
		int tempBarCodeCount=0;
		try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			XSSFWorkbook wb = new XSSFWorkbook(bis);){
			Sheet sheet = wb.getSheetAt(0);
			Iterator<Row> rows = sheet.iterator();
			barCodes = new LinkedHashMap<>();
			while(rows.hasNext()){
				Row row = rows.next();
				Cell cell = row.getCell(0);
				if(cell!=null){
					String barCode = DF.formatCellValue(cell);
					if(StringUtils.isBlank(barCode)){
						continue;
					}
					barCode = barCode.trim();
					if(!StringUtils.isNumeric(barCode) || barCode.length() != 13){
						System.out.println("ignored barCode: " + barCode);
						continue;
					}
					Cell skuCell = row.getCell(1);
					if(skuCell == null){
						continue;
					}
					String sku = DF.formatCellValue(skuCell);
					if(StringUtils.isBlank(sku)){
						continue;
					}
					sku = sku.trim();
					barCodes.put(sku, barCode);
					tempBarCodeCount++;
				}
			}
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		System.out.println("total tempBarCodeCount: " + tempBarCodeCount);
		return barCodes;
	}
	/**
	 * 轉出型號、可掃描條碼、條碼數字對照表(可掃描條碼需配合額外條碼字型)
	 * 資料源為ohm-catalog-barcode-150601.pdf
	 * @param dest
	 */
	public void writeBriefBarCodeScannable(String dest){
		int rowNum = 0;
		try(XSSFWorkbook wb = new XSSFWorkbook();
		FileOutputStream fos = new FileOutputStream(dest)){
			
			Sheet sheet = wb.createSheet("商品條碼表");
			Row firstRow = sheet.createRow(rowNum++);
			int COL_IDX_MODEL_ID = 0;
			int COL_IDX_BARCODE = 1;
			int COL_IDX_BARCODE_NO = 2;
			Cell c1 = firstRow.createCell(COL_IDX_MODEL_ID);
			c1.setCellType(XSSFCell.CELL_TYPE_STRING);
			c1.setCellValue("型號");
			Cell c2 = firstRow.createCell(COL_IDX_BARCODE);
			c2.setCellType(XSSFCell.CELL_TYPE_STRING);
			c2.setCellValue("條碼");
			Cell c3 = firstRow.createCell(COL_IDX_BARCODE_NO);
			c3.setCellType(XSSFCell.CELL_TYPE_STRING);
			c3.setCellValue("條碼號");
			
			barCodes.entrySet();
			for(Map.Entry<String, String> b : barCodes.entrySet()){
				Row row = sheet.createRow(rowNum++);
				Cell cModelId = row.createCell(COL_IDX_MODEL_ID);
				cModelId.setCellType(XSSFCell.CELL_TYPE_STRING);
				cModelId.setCellValue(b.getKey());
				
				Cell cBarCode = row.createCell(COL_IDX_BARCODE);
				cBarCode.setCellType(XSSFCell.CELL_TYPE_STRING);
				cBarCode.setCellValue("*" + b.getValue() + "*"); // 如果要透過免費的條碼字型製作條碼，條碼數字在輸出前，前後要加上星號。
				
				Cell cBarCodeNo = row.createCell(COL_IDX_BARCODE_NO);
				cBarCodeNo.setCellType(XSSFCell.CELL_TYPE_STRING);
				cBarCodeNo.setCellValue(b.getValue());
			}
			
			wb.write(fos);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	/**
	 * 根據Kit給的"最新"xlsx檔(可能是csv轉)，匯整可掃描條碼
	 * 可以根據設定的路徑抓圖，並把圖嵌入Excel
	 * 如果沒有在本地取得圖，就會發HTTP去抓，抓成功後會放在指定路徑，再把圖嵌入Excel
	 * 也可以只用來抓圖而已
	 * @param src: 來源xlsx，此格式是以Kit提供ohm-tw-catalog-barcodes-151015.csv為基礎
	 * @param dest: 目的xlsx，此格式是模仿Kit提供ohm-tw-catalog-barcodes-151015.pdf而來
	 * @param imgPathTemplate: 圖片存取路徑模板，將產品型號帶入，就可以得到圖片路徑。
	 */
	public void writeCompleteBarCodeScannable(String src, String dest, String imgPathTemplate){
		// ohm-tw-catalog-barcodes-151015.csv、ohm-tw-catalog-barcodes-151015.pdf
		try(FileInputStream fis = new FileInputStream(src);
			XSSFWorkbook inWb = new XSSFWorkbook(fis);
			FileOutputStream fos = new FileOutputStream(dest);
			Workbook outWb = ".xlsx".equals(outFormat) ? new XSSFWorkbook(): new HSSFWorkbook()){
			
			DataFormat df = outWb.createDataFormat();
			CellStyle cs = outWb.createCellStyle();
			cs.setDataFormat(df.getFormat("@")); // 文字格式
			
			Sheet productSheet = outWb.createSheet("product");
			
			productSheet.setColumnWidth(3, 10*ExcelImgProcessor.COL_WIDTH_UNIT);
			
			Row firstRow = productSheet.createRow(0);
			Cell c1 = firstRow.createCell(0);
			c1.setCellValue("Barcode");
			Cell c2 = firstRow.createCell(1);
			c2.setCellValue("EAN13");
			Cell c3 = firstRow.createCell(2);
			c3.setCellValue("SKU");
			Cell c4 = firstRow.createCell(3);
			c4.setCellValue("Image");
			Cell c5 = firstRow.createCell(4);
			c5.setCellValue("Name");
			Cell c6 = firstRow.createCell(5);
			c6.setCellValue("MSRP");
			
			ExcelImgProcessor eip = new ExcelImgProcessor(outWb, productSheet);
			
			Sheet srcSheet = inWb.getSheetAt(0);
			Iterator<Row> srcRows = srcSheet.iterator();
			while(srcRows.hasNext()){
				Row srcRow = srcRows.next();
				int rowNum = srcRow.getRowNum();
				if(rowNum == 0 || rowNum != 1){
					continue;
				}
				String barCodeVal = getCellStrVal(srcRow, 0);
				String skuVal = getCellStrVal(srcRow, 1);
				String nameVal = getCellStrVal(srcRow, 2);
				String msrpVal = getCellStrVal(srcRow, 3);
				
				Row destRow = productSheet.createRow(rowNum);
				destRow.setHeight((short)(40*ExcelImgProcessor.ROW_HEIGHT_UNIT));
				Cell barCodeScannable = destRow.createCell(0);
				barCodeScannable.setCellValue("*"+barCodeVal+"*");
//				barCodeScannable.setCellStyle(cs);
				
				Cell barCodeNum = destRow.createCell(1);
				barCodeNum.setCellValue(barCodeVal);
//				barCodeNum.setCellStyle(cs);
				
				Cell sku = destRow.createCell(2);
				sku.setCellValue(skuVal);
//				sku.setCellStyle(cs);
				
				Cell image = destRow.createCell(3);
				int picIdx = eip.addImgFitToCell(imgPathTemplate.replace("{sku}", skuVal), rowNum, 3, imgPath->{
					String no = imgPath.substring(imgPath.lastIndexOf("\\")+1, imgPath.lastIndexOf("."));
					String url = URL_TEMPLATE.replace("{no}", no);
					System.out.println("url: " + url);
					File file = new File(imgPath);
					try(FileOutputStream img = new FileOutputStream(file)){
						httpService.sendPost(url, bis->{
							try{
								IOUtils.copy(bis, img);
							}catch(Throwable e){
								throw new RuntimeException(e);
							}
						});
					}catch(Throwable e){
						throw new RuntimeException(e);
					}
					return file;
				});
				
//				if(".xls".equals(outFormat) && picIdx > 0){// Apache POI的HSSF才有支援註解背景圖片
//					eip.addCommentImg(picIdx, rowNum, 3);
//				}
				
				Cell name = destRow.createCell(4);
				name.setCellValue(nameVal);
//				name.setCellStyle(cs);
				
				Cell msrp = destRow.createCell(5);
				msrp.setCellValue(msrpVal);
//				msrp.setCellStyle(cs);
			}
			productSheet.autoSizeColumn(0);
			productSheet.autoSizeColumn(1);
			productSheet.autoSizeColumn(2);
			productSheet.autoSizeColumn(4);
			productSheet.autoSizeColumn(5);
					
			outWb.write(fos);
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 產出折扣的條碼資料
	 * @param dest
	 */
	public void writeDiscountBarCode(String dest){
		try(FileOutputStream fos = new FileOutputStream(dest);
			Workbook outWb = ".xlsx".equals(outFormat) ? new XSSFWorkbook(): new HSSFWorkbook()){
			
			Sheet discountSheet = outWb.createSheet("discount");
			
			discountSheet.setColumnWidth(3, 10*ExcelImgProcessor.COL_WIDTH_UNIT);
			
			Row _1stRow = discountSheet.createRow(0);
			Cell h0 = _1stRow.createCell(0);
			h0.setCellValue("可掃描條碼");
			Cell h1 = _1stRow.createCell(1);
			h1.setCellValue("條碼");
			Cell h2 = _1stRow.createCell(2);
			h2.setCellValue("類別編號");
			Cell h3 = _1stRow.createCell(3);
			h3.setCellValue("折扣編號");
			Cell h4 = _1stRow.createCell(4);
			h4.setCellValue("名稱");
			Cell h5 = _1stRow.createCell(5);
			h5.setCellValue("售價");

			DataFormat df = outWb.createDataFormat();
			CellStyle cs = outWb.createCellStyle();
			cs.setDataFormat(df.getFormat("@")); // 文字格式
			List<OnePosDiscountItem> items = OnePosDiscountItem.getDefaultItems();
			for(int i = 0; i < items.size(); i++){
				OnePosDiscountItem item = items.get(i);
				Row row = discountSheet.createRow(i+1);
				
				Cell i0 = row.createCell(0);
				i0.setCellValue(item.getScannableBarCode());
				i0.setCellStyle(cs);
				
				Cell i1 = row.createCell(1);
				i1.setCellValue(item.getBarCode());
				i1.setCellStyle(cs);
				
				Cell i2 = row.createCell(2);
				i2.setCellValue(item.getCategoryId());
				i2.setCellStyle(cs);
				
				Cell i3 = row.createCell(3);
				i3.setCellValue(item.getId());
				i3.setCellStyle(cs);
				
				Cell i4 = row.createCell(4);
				i4.setCellValue(item.getName());
				i4.setCellStyle(cs);
				
				Cell i5 = row.createCell(5);
				i5.setCellValue(Integer.parseInt(item.getPrice()));
			}		
			outWb.write(fos);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	private static void testProcess(){
		long starttime = System.currentTimeMillis();
		
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		String t1 = "E:\\angrycat_workitem\\產品\\臺灣OHM商品總庫存清單(類別)_T20150924.xlsx";
		String t2 = "E:\\angrycat_workitem\\member\\臺灣OHM商品總庫存清單(類別)_T20150924 - 複製.xlsx";
		String t3 = "E:\\angrycat_workitem\\產品\\臺灣OHM商品總庫存清單(類別)_T20151028.xlsx";
		String t4 = "E:\\angrycat_workitem\\產品\\2015_12_01\\臺灣OHM商品總庫存清單(類別)_T20150924.xlsx";
		String t5 = "E:\\angrycat_workitem\\產品\\2015_12_16\\臺灣OHM商品總庫存清單(類別)_T20150924.xlsx";
		try(
			FileInputStream fis = new FileInputStream(t5)){
			byte[] data = IOUtils.toByteArray(fis);
			OnePosInitialExcelAccessor accessor = acac.getBean(OnePosInitialExcelAccessor.class);
//			accessor.setImgProcessEnabled(true);
			accessor.setClientProcessEnabled(true);
			accessor.setTemplatePath("E:\\angrycat_workitem\\member\\v36 ONE-POS Data Quick Import  快速匯入 - Empty .xls");
			accessor.process(data, "1214保養品項");
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		acac.close();
		long endtime = System.currentTimeMillis();
		System.out.println("start time: " + (endtime - starttime)/(1000*60) + "分鐘");
	}
	
	private String retrieveStrVal(Cell cell){
		String val = DF.formatCellValue(cell);
		return StringUtils.trim(val);
	}
	
	/**
	 * 將公司內部產品的Excel轉成OnePos需要的Excel匯入格式。
	 * 如果有額外設定的話(imgProcessEnabled = true)，可以抓取對應的圖檔之後改名。
	 * 客戶的資料源是另一張Excel。
	 * 要與getBarCodes()互相配合，要請銷售管理人員另外提供商品條碼清單
	 * @param data: 原始資料源是"臺灣OHM商品總庫存清單(類別)_T20150924.xlsx"
	 */
	public void process(byte[] data, String sheetName){
		String tempPath = FileUtils.getTempDirectoryPath();
		String customDir = RandomStringUtils.randomAlphanumeric(8); 
		String tempDir = tempPath + customDir + File.separator;
		System.out.println("process tempDir: " + tempDir);
		String tempImgDir = tempDir + "image" + File.separator;
		try{
			FileUtils.forceMkdir(new File(tempImgDir));
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		String customFile = RandomStringUtils.randomAlphanumeric(8) + ".xls";
		String customFilePath = tempDir + customFile;
		File file = new File(customFilePath);
		
		try(ByteArrayInputStream bais = new ByteArrayInputStream(data);
			XSSFWorkbook productWb = new XSSFWorkbook(bais);
			XSSFWorkbook memberWb = clientProcessEnabled ? new XSSFWorkbook(new FileInputStream(memberPath)) : new XSSFWorkbook();
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			HSSFWorkbook outWb = templatePath == null ? new HSSFWorkbook() : new HSSFWorkbook(new FileInputStream(templatePath));){// 要產品OnePos匯入格式，最好使用OnePos提供範本
			
			CellStyle dateStyle = outWb.createCellStyle();
			dateStyle.setDataFormat(outWb.getCreationHelper().createDataFormat().getFormat("yyyy/MM/dd"));
			
			Sheet categorySheet = outWb.getSheet("categories") == null ? outWb.createSheet("categories") : outWb.getSheet("categories"); // 第一個Sheet是產品類別
			Sheet brandSheet = outWb.getSheet("brands") == null ? outWb.createSheet("brands") : outWb.getSheet("brands"); // 第二個Sheet是產品品牌
			Sheet productSheet = outWb.getSheet("products") == null ? outWb.createSheet("products") : outWb.getSheet("products"); // 第三個Sheet是產品(品項)
			Sheet clientSheet = outWb.getSheet("clients") == null ? outWb.createSheet("clients") : outWb.getSheet("clients"); // 第四個Sheet是客戶
			if(outWb.getSheet("vendors") == null){ // 第五個Sheet是供應商
				outWb.createSheet("vendors");
			}
			
			Row brandRow = brandSheet.createRow(1);
			Cell brandId = brandRow.createCell(0);
			brandId.setCellValue("OHM");
			Cell brandName = brandRow.createCell(1);
			brandName.setCellValue("OHM Beads TW");
			// 第一次初始匯入
			if(StringUtils.isBlank(sheetName)){
				Sheet generalSheet = productWb.getSheetAt(0); // 一般商品
				Iterator<Row> generalSheetRows = generalSheet.iterator();
				int rowNum = 0;
				Set<String> categories = new LinkedHashSet<>();
				while(generalSheetRows.hasNext()){
					Row row = generalSheetRows.next();
					rowNum = row.getRowNum();
					if(rowNum == 0){// 略過標題列
						continue;
					}
					
					Cell cat = row.getCell(Sheet1.類別);
					Cell no = row.getCell(Sheet1.型號);
					Cell nameEng = row.getCell(Sheet1.英文名字);
					Cell price = row.getCell(Sheet1.定價);
					
					String catVal = retrieveStrVal(cat);
					String noVal = retrieveStrVal(no);
					String nameEngVal = retrieveStrVal(nameEng);
					String priceVal = retrieveStrVal(price);
					
					if(StringUtils.isBlank(noVal) || StringUtils.isBlank(catVal)){
						continue;
					}
					
					addValToCategorySheet(categories, categorySheet, catVal, catVal);
					
					Row pRow = productSheet.createRow(rowNum);
					String barCode = barCodes.get(noVal);
					addProductCells(pRow, noVal, nameEngVal, catVal, priceVal, barCode);
					
					storeImage(tempImgDir, noVal);
				}
				
				Sheet serialSheet = productWb.getSheetAt(1);
				Iterator<Row> serialRows = serialSheet.iterator();
				while(serialRows.hasNext()){
					Row row = serialRows.next();
					int rowCount = row.getRowNum();
					if(rowCount == 0){
						continue;
					}
					
					Cell cat = row.getCell(Sheet2.類別);
					Cell no = row.getCell(Sheet2.型號);
					Cell nameEng = row.getCell(Sheet2.英文名字);
					Cell price = row.getCell(Sheet2.定價);
					Cell serialName = row.getCell(Sheet2.系列名);
					
					String catVal = retrieveStrVal(cat);
					String noVal = retrieveStrVal(no);
					String nameEngVal = retrieveStrVal(nameEng);
					String priceVal = retrieveStrVal(price);
					String serialNameVal = retrieveStrVal(serialName);
					
					if(StringUtils.isBlank(noVal) || StringUtils.isBlank(catVal)){
						continue;
					}
					
					addValToCategorySheet(categories, categorySheet, catVal, catVal);
					
					Row pRow = productSheet.createRow(++rowNum);
					String barCode = barCodes.get(noVal);
					addProductCells(pRow, noVal, nameEngVal, catVal, priceVal, barCode);
					
					Cell remarkCell = pRow.createCell(OnePos.備註欄); // 把系列名稱先放到備註欄，OnePos沒有直接對應的欄位
					remarkCell.setCellType(CELL_TYPE_STRING);
					remarkCell.setCellValue(serialNameVal);
					
					storeImage(tempImgDir, noVal);
				}
				
				Sheet jewelrySheet = productWb.getSheetAt(2);
				Iterator<Row> jewelryRows = jewelrySheet.iterator();
				while(jewelryRows.hasNext()){
					Row row = jewelryRows.next();
					int rowCount = row.getRowNum();
					if(rowCount == 0){
						continue;
					}
					
					Cell cat = row.getCell(Sheet3.類別);
					Cell no = row.getCell(Sheet3.型號);
					Cell nameEng = row.getCell(Sheet3.英文名字);
					Cell price = row.getCell(Sheet3.定價);
					
					String catVal = retrieveStrVal(cat);
					String noVal = retrieveStrVal(no);
					String nameEngVal = retrieveStrVal(nameEng);
					String priceVal = retrieveStrVal(price);
					
					if(StringUtils.isBlank(noVal) || StringUtils.isBlank(catVal)){
						continue;
					}
					
					addValToCategorySheet(categories, categorySheet, catVal, catVal);
					
					Row pRow = productSheet.createRow(++rowNum);
					String barCode = barCodes.get(noVal);
					addProductCells(pRow, noVal, nameEngVal, catVal, priceVal, barCode);
					
					storeImage(tempImgDir, noVal);
				}

				// 手動新增商品類別和商品
				addValToCategorySheet(categories, categorySheet, CAT_GIFT, "禮卷");
				addValToCategorySheet(categories, categorySheet, CAT_ACT, "活動");
				addValToCategorySheet(categories, categorySheet, CAT_PDIS, "比例折扣");
				
				List<OnePosDiscountItem> items = OnePosDiscountItem.getDefaultItems();
				for(OnePosDiscountItem item : items){
					Row pRow = productSheet.createRow(++rowNum);
					addDiscountItemCells(pRow, item);
				}
				// 處理會員，未來應改為從資料庫存取 TODO
				if(clientProcessEnabled){
					Sheet memberSheet = memberWb.getSheetAt(0);// 將會員轉為OnePos所需客戶格式
					Iterator<Row> memberRows = memberSheet.iterator();
					while(memberRows.hasNext()){
						Row row = memberRows.next();
						int rowCount = row.getRowNum();
						if(rowCount == 0 || isRowEmpty(row, Member.COLUMN_COUNT)){
							continue;
						}
						String clientId = MemberController.genClientId("TW", rowCount);
						String clientName = getCellStrVal(row, Member.真實姓名);
						String address = getCellStrVal(row, Member.地址);
						String mobile = getCellStrVal(row, Member.手機電話);
						String tel = getDefualtTelNumIfBothEmpty(mobile, getCellStrVal(row, Member.室內電話));
						String email = getCellStrVal(row, Member.電子信箱);
						Date birthday = getCellDateVal(row, Member.出生年月日);
						
						com.angrycat.erp.model.Member member = new com.angrycat.erp.model.Member();
						member.setClientId(clientId);
						member.setName(clientName);
						member.setAddress(address);
						member.setMobile(mobile);
						member.setTel(tel);
						member.setEmail(email);
						if(birthday != null){
							member.setBirthday(new java.sql.Date(birthday.getTime()));
						}
						
						Row cRow = clientSheet.createRow(rowCount);
						addClient(cRow, dateStyle, member);
					}
				}
			}else{// 後來的零散匯入
				Sheet generalSheet = productWb.getSheet(sheetName); // 指定Sheet
				Iterator<Row> generalSheetRows = generalSheet.iterator();
				int rowNum = 0;
				Set<String> categories = new LinkedHashSet<>();
				while(generalSheetRows.hasNext()){
					Row row = generalSheetRows.next();
					rowNum = row.getRowNum();
					if(rowNum == 0){// 略過標題列
						continue;
					}
					System.out.println("Sheet2.類別: " + Sheet2.類別 + ", Sheet2.型號: " + Sheet2.型號 + ", Sheet2.英文名字: " + Sheet2.英文名字);
					Cell cat = row.getCell(Sheet2.類別);
					Cell no = row.getCell(Sheet2.型號);
					Cell nameEng = row.getCell(Sheet2.英文名字);
					Cell price = row.getCell(Sheet2.定價);
					Cell serialName = row.getCell(Sheet2.系列名);
					Cell barcode = row.getCell(Sheet2.系列名+1);
					
					String catVal = retrieveStrVal(cat);
					String noVal = retrieveStrVal(no);
					String nameEngVal = retrieveStrVal(nameEng);
					String priceVal = retrieveStrVal(price);
					String serialNameVal = serialName == null ? "" : retrieveStrVal(serialName);
					String barcodeVal = barcode == null ? "" : retrieveStrVal(barcode);
					barcodeVal = barcodeVal.replace(" ", "");
					if(StringUtils.isBlank(noVal) || StringUtils.isBlank(catVal)){
						continue;
					}					
					addValToCategorySheet(categories, categorySheet, catVal, catVal);
					
					Row pRow = productSheet.createRow(rowNum);
					System.out.println("noVal: " + noVal + ", nameEngVal: " + nameEngVal + ", catVal: " + catVal + ", priceVal: " + priceVal);
					String barCode = barCodes.get(noVal);
					
					addProductCells(pRow, noVal, nameEngVal, catVal, priceVal, barCode);
					
					if(StringUtils.isNotBlank(serialNameVal)){
						Cell remarkCell = pRow.createCell(OnePos.備註欄); // 把系列名稱先放到備註欄，OnePos沒有直接對應的欄位
						remarkCell.setCellType(CELL_TYPE_STRING);
						remarkCell.setCellValue(serialNameVal);
					}					
					storeImage(tempImgDir, noVal);
				}
				
			}

			outWb.write(bos);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		
		if(imgProcessEnabled){
			fileReplaceOldPart(new File(tempDir + "image"), "-o-a", "");	
		}
		
		String zipPath = tempPath + customDir + ".zip";
		zipFolder(tempPath + customDir, zipPath);
		System.out.println("zip path: " + zipPath);
		System.out.println("import BarCode Count:" + importBarCodeCount);
		try{
			FileUtils.forceDelete(new File(tempPath + customDir));
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}	
	
	/**
	 * 將會員資料轉為OnePos客戶匯入格式，抽出這段邏輯是便於讓不同資料源可以共用
	 * @param cRow
	 * @param dateStyle
	 * @param member
	 * @return
	 */
	public static Row addClient(Row cRow, CellStyle dateStyle, com.angrycat.erp.model.Member member){
		String clientId = member.getClientId();
		String clientName = member.getName();
		String address = member.getAddress();
		String mobile = member.getMobile();
		String tel = member.getTel();
		String email = member.getEmail();
		String level = "1";// 價格級別，1是一般價，2是VIP價
		Date birthday = member.getBirthday();
		Date builtDate = new Date(System.currentTimeMillis());
		
		addCellStrValIfNotBlank(cRow, OnePosClient.客戶編號, clientId);
		addCellStrValIfNotBlank(cRow, OnePosClient.客戶名稱, clientName);
		addCellStrValIfNotBlank(cRow, OnePosClient.電話1, mobile);
		addCellStrValIfNotBlank(cRow, OnePosClient.電話2, tel);
		addCellStrValIfNotBlank(cRow, OnePosClient.電郵, email);
		addCellStrValIfNotBlank(cRow, OnePosClient.級別, level);
		
		if(birthday!=null){
			Cell birthdayCell = cRow.createCell(OnePosClient.生日日期);
			birthdayCell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			birthdayCell.setCellValue(birthday);	
			birthdayCell.setCellStyle(dateStyle);
		}
		
		Cell builtDateCell = cRow.createCell(OnePosClient.新增日期);
		builtDateCell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		builtDateCell.setCellValue(builtDate);
		builtDateCell.setCellStyle(dateStyle);
		
		if(StringUtils.isNotBlank(address)){
			int addressLen = address.length();
			if(addressLen >= 150){
				String address3 = address.substring(100, 150);
				String address2 = address.substring(50, 100);
				String address1 = address.substring(0, 50);
				addAddressCells(cRow, address1, address2, address3);
			}else if(addressLen >= 100 && addressLen < 150){
				String address3 = address.substring(100, addressLen);
				String address2 = address.substring(50, 100);
				String address1 = address.substring(0, 50);
				addAddressCells(cRow, address1, address2, address3);
			}else if(addressLen >= 50 && addressLen < 100){
				String address2 = address.substring(50, addressLen);
				String address1 = address.substring(0, 50);
				addAddressCells(cRow, address1, address2);
			}else if(addressLen >= 0 && addressLen < 50){
				String address1 = address.substring(0, addressLen);
				addAddressCells(cRow, address1);
			}	
		}
		
		return cRow;
	}
	
	
	/**
	 * 如果沒留手機，也沒留室話，就給定室話預設值
	 * @param mobile
	 * @param tel
	 * @return
	 */
	private String getDefualtTelNumIfBothEmpty(String mobile, String tel){
		if(StringUtils.isBlank(mobile) && StringUtils.isBlank(tel)){
			return "00000";
		}
		return tel;
	}
	
	/**
	 * 判斷該列是否為空
	 * @param row
	 * @param colCount
	 * @return
	 */
	private boolean isRowEmpty(Row row, int colCount){
		boolean empty = true;
		for(int i = 0; i < colCount; i++){
			Cell cell = row.getCell(i);
			if(cell != null){
				String val = DF.formatCellValue(cell);
				if(StringUtils.isNotBlank(val)){
					empty = false;
					break;
				}	
			}
		}
		return empty;
	}
	private static void addCellStrValIfNotBlank(Row cRow, int colIdx, String val){
		if(StringUtils.isBlank(val)){
			return;
		}
		Cell cell = cRow.createCell(colIdx);
		cell.setCellType(CELL_TYPE_STRING);
		cell.setCellValue(val);
	}
	
	private void addCellDateValIfMatched(Row cRow, int colIdx, String val){
		if(StringUtils.isBlank(val)){
			return;
		}
		Date d = parseStrValtoDate(val);
		Cell cell = cRow.createCell(colIdx);
		cell.setCellValue(d);
	}
	
	private static void addAddressCells(Row row, String... addresses){
		List<Integer> idx = Arrays.asList(OnePosClient.地址第1行, OnePosClient.地址第2行, OnePosClient.地址第3行);
		for(int i = 0; i < addresses.length; i++){
			Cell cell = row.createCell(idx.get(i));
			cell.setCellType(CELL_TYPE_STRING);
			cell.setCellValue(addresses[i]);
		}
	}
	
	private Date getCellDateVal(Row cRow, int colIdx){
		Cell cell = cRow.getCell(colIdx);
		Date d = null;
		if(cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC){
			d = cell.getDateCellValue();
		}else if(cell.getCellType() == XSSFCell.CELL_TYPE_STRING){
			String val = cell.getStringCellValue();
			if(StringUtils.isNotBlank(val)){
				val = processStr(val);
				d = parseStrValtoDate(val);
			}
		}
		return d;
	}
	
	private Date parseStrValtoDate(String input){
		Date d = null;
		if(StringUtils.isNotBlank(input)){
			input = processStr(input);
			String pattern = DatetimeUtil.getDatePattern(input);
			try{
				DateFormat df = new SimpleDateFormat(pattern);
				d = df.parse(input);
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
		}
		return d;
	}
	
	private static String getCellStrVal(Row cRow, int colIdx){
		Cell cell = cRow.getCell(colIdx);
		if(cell == null){
			System.out.println("cell == null colIdx: " + colIdx);
			return null;
		}
		if(cell.getCellType() == XSSFCell.CELL_TYPE_STRING){
//			System.out.println("STRING: " + cell.getStringCellValue());
		}else if(cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC){
//			System.out.println("NUMERIC: " + cell.getNumericCellValue());
		}else if(cell.getCellType() == XSSFCell.CELL_TYPE_BLANK){
//			System.out.println("BLANK: " + cell.getStringCellValue());
		}else if(cell.getCellType() == XSSFCell.CELL_TYPE_FORMULA){
//			System.out.println("FORMULA: " + cell.getCellFormula());
		}else if(cell.getCellType() == XSSFCell.CELL_TYPE_ERROR){
//			System.out.println("ERROR: " + cell.getErrorCellValue());
		}
		String val = DF.formatCellValue(cell);
		if(val == null){
			return null;
		}
		val = processStr(val);
		return val;
	}
	
	private static String processStr(String input){
		input = input.trim();
		input = input.replace("\n", "");
		input = input.replace("\r", "");
		return input;
	}
	
	
	public static void addProductCells(Row pRow, String noVal, String nameEngVal, String catVal, String priceVal, String barCode, String seriesName){
		addProductCells(pRow, noVal, nameEngVal, catVal, priceVal, barCode);
		if(StringUtils.isNotBlank(seriesName)){
			Cell remarkCell = pRow.createCell(OnePos.備註欄); // 把系列名稱先放到備註欄，OnePos沒有直接對應的欄位
			remarkCell.setCellType(CELL_TYPE_STRING);
			remarkCell.setCellValue(seriesName);
		}
	}
	
	public static void addProductCells(Row pRow, String noVal, String nameEngVal, String catVal, String priceVal, String barCode){
		Cell productIdCell = pRow.createCell(OnePos.產品編號);
		productIdCell.setCellType(CELL_TYPE_STRING);
		productIdCell.setCellValue(noVal);
		
		Cell productNameCell = pRow.createCell(OnePos.產品名稱);
		productNameCell.setCellType(CELL_TYPE_STRING);
		productNameCell.setCellValue(nameEngVal);
		
		Cell catNoCell = pRow.createCell(OnePos.類別編號);
		catNoCell.setCellType(CELL_TYPE_STRING);
		catNoCell.setCellValue(catVal);
		
		Cell modelCell = pRow.createCell(OnePos.型號);
		modelCell.setCellType(CELL_TYPE_STRING);
		modelCell.setCellValue(noVal);
		
		Cell priceCell = pRow.createCell(OnePos.售價);
		priceCell.setCellType(CELL_TYPE_NUMERIC);
		priceCell.setCellValue(StringUtils.isNotBlank(priceVal) ? Integer.parseInt(priceVal) : 0);
		
		Cell inventoryCell = pRow.createCell(OnePos.性質);
		inventoryCell.setCellType(CELL_TYPE_STRING);
		inventoryCell.setCellValue(INVENTORY);
		
		if(StringUtils.isNotBlank(barCode)){
			Cell barCodeCell = pRow.createCell(OnePos.條碼編號);
			barCodeCell.setCellType(CELL_TYPE_STRING);
			barCodeCell.setCellValue(barCode);
		}
		
		Cell brandIdCell = pRow.createCell(OnePos.品牌編號);
		brandIdCell.setCellType(CELL_TYPE_STRING);
		brandIdCell.setCellValue(BRAND_ID);
		
//		Cell invCell = pRow.createCell(OnePos.庫存);
//		invCell.setCellType(CELL_TYPE_NUMERIC);
//		invCell.setCellValue(0);
	}
	
	/**
	 * 新增固定金額折抵項目，在OnePos下面，把固定金額折抵項目當作商品項匯入，所以新增折抵就是新增商品項目，但金額為負數
	 * @param pRow
	 * @param noVal
	 * @param nameEngVal
	 * @param catVal
	 * @param priceVal
	 */
	public static void addDiscountItemCells(Row pRow, OnePosDiscountItem item){
		Cell productIdCell = pRow.createCell(OnePos.產品編號);
		productIdCell.setCellType(CELL_TYPE_STRING);
		productIdCell.setCellValue(item.getId());
		
		Cell productNameCell = pRow.createCell(OnePos.產品名稱);
		productNameCell.setCellType(CELL_TYPE_STRING);
		productNameCell.setCellValue(item.getName());
		
		Cell catNoCell = pRow.createCell(OnePos.類別編號);
		catNoCell.setCellType(CELL_TYPE_STRING);
		catNoCell.setCellValue(item.getCategoryId());
		
//		Cell modelCell = pRow.createCell(OnePos.型號);
//		modelCell.setCellType(CELL_TYPE_STRING);
//		modelCell.setCellValue(noVal);
		
		Cell priceCell = pRow.createCell(OnePos.售價);
		priceCell.setCellType(CELL_TYPE_NUMERIC);
		priceCell.setCellValue(StringUtils.isNotBlank(item.getPrice()) ? Integer.parseInt(item.getPrice()) : 0);
		
		Cell inventoryCell = pRow.createCell(OnePos.性質);
		inventoryCell.setCellType(CELL_TYPE_STRING);
		inventoryCell.setCellValue(NON_INVENTORY);
		
		Cell barCodeCell = pRow.createCell(OnePos.條碼編號);
		barCodeCell.setCellType(CELL_TYPE_STRING);
		barCodeCell.setCellValue(item.getBarCode());
		
		Cell brandIdCell = pRow.createCell(OnePos.品牌編號);
		brandIdCell.setCellType(CELL_TYPE_STRING);
		brandIdCell.setCellValue(item.getBrandId());
	}
	/**
	 * 在同一批匯入的檔案當中，如果沒有該商品類別，就新增。
	 * 因為條件不同，所以這個method也不完全適用後來的情況。
	 * 後來是零散匯入，所以某次新增的商品，也可能沿用舊的商品類別。
	 * 如果新商品延用舊的類別，除非要把舊的資料全部跑一次，否則這裡的程式無法判別。
	 * 所幸OnePos的匯入方式有覆蓋及跳過兩種，就商品類別而言，不管選哪種都不會有影響。
	 * 唯一要記得的是，每次匯入OnePos之前都要先匯入商品類別，再匯入商品，方不會出現匯入失敗的情形。
	 * @param categories
	 * @param categorySheet
	 * @param catId
	 * @param catName
	 */
	public static void addValToCategorySheet(Set<String> categories, Sheet categorySheet, String catId, String catName){
		if(StringUtils.isBlank(catId)){
			return;
		}
		int beforeCatCount = categories.size();
		categories.add(catId);
		int afterCatCount = categories.size();
		if(beforeCatCount != afterCatCount){
			int rowNum = afterCatCount;
			Row catRow = categorySheet.createRow(rowNum);
			Cell catNoCell = catRow.createCell(0);
			catNoCell.setCellType(CELL_TYPE_STRING);
			catNoCell.setCellValue(catId);
			
			Cell catNameCell = catRow.createCell(1);
			catNameCell.setCellType(CELL_TYPE_STRING);
			catNameCell.setCellValue(catName);
		}
	}
	
	/**
	 * ref. http://stackoverflow.com/questions/15968883/how-to-zip-a-folder-itself-using-java
	 * @param srcDir
	 * @param zipPath
	 */
	public static void pack(String srcDir, String zipDest){
		try{
			Path zipPath = Files.createFile(Paths.get(zipDest));
			try(ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(zipPath)))){
				Path srcPath = Paths.get(srcDir);
				String srcPathAsPrefix = srcPath.toAbsolutePath().toString() + File.separator;
				System.out.println("srcPath: " + srcPathAsPrefix);
				Files.walk(srcPath)
					.filter(path -> !Files.isDirectory(path))
					.forEach(path->{
						String absolutePath = path.toAbsolutePath().toString();
						String name = absolutePath.replace(srcPathAsPrefix, "");
						System.out.println("name: " + name);
						ZipEntry ze = new ZipEntry(name);
						try{
							zos.putNextEntry(ze);
							zos.write(Files.readAllBytes(path));
							zos.closeEntry();
						}catch(Throwable e){
							throw new RuntimeException(e);
						}
					});
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	
	private static void testZipFolder(){
		zipFolder("C:\\fMNFMul7", "C:\\tmp\\fMNFMul7.zip");
	}
	
	/**
	 * ref. http://stackoverflow.com/questions/15968883/how-to-zip-a-folder-itself-using-java
	 * @param folderPath
	 * @param zipFilePath
	 */
	public static void zipFolder(String folderPath, String zipFilePath){
		File folder = new File(folderPath);
		File zipFile = new File(zipFilePath);
		try(FileOutputStream fos = new FileOutputStream(zipFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ZipOutputStream zos = new ZipOutputStream(bos);){
			int folderPrefixLen = folder.getPath().length()+1;
			
			processFolder(folder, zos, folderPrefixLen);
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	private static void processFolder(File folder, ZipOutputStream zos, int folderPrefixLen)throws Throwable{
		for(File file : folder.listFiles()){
			if(file.isFile()){
				ZipEntry ze = new ZipEntry(file.getPath().substring(folderPrefixLen));
				zos.putNextEntry(ze);
				try(FileInputStream fis = new FileInputStream(file)){
					IOUtils.copy(fis, zos);
				}
				zos.closeEntry();
			}else if(file.isDirectory()){
				processFolder(file, zos, folderPrefixLen);
			}
		}
	}
	
	private static void testPack(){
		pack("C:\\fMNFMul7", "C:\\tmp\\fMNFMul7.zip");
	}
	
	private static void testFileReplaceOldPart(){
		fileReplaceOldPart(new File("E:\\angrycat_workitem\\pos\\onepos\\OnePosImportInitial\\image"), "-o-a", "");
	}
	
	/**
	 * 用指定字元取代部份名稱
	 * @param oldFolder
	 * @param oldPart
	 * @param newPart
	 */
	private static void fileReplaceOldPart(File oldFolder, String oldPart, String newPart){
		for(File oldFile : oldFolder.listFiles()){
			if(oldFile.isFile()){
				if(oldFile.getName().contains(oldPart)){
					String newFilePath = oldFile.getAbsolutePath().replace(oldPart, newPart);
					File newFile = new File(newFilePath);
					if(!oldFile.renameTo(newFile)){
						System.out.println("rename failed: ");
					}
				}
			}else if(oldFile.isDirectory()){
				fileReplaceOldPart(oldFile, oldPart, newPart);
			}	
		}
	}
	
	private static void testWriteBriefBarCodeScannable(){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		OnePosInitialExcelAccessor accessor = acac.getBean(OnePosInitialExcelAccessor.class);
		accessor.writeBriefBarCodeScannable("E:\\angrycat_workitem\\產品\\型號和可掃描條碼對照.xlsx");
		acac.close();
	}
	
	private static void testWriteCompleteBarCodeScannable(){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		OnePosInitialExcelAccessor accessor = acac.getBean(OnePosInitialExcelAccessor.class);
		accessor.setOutFormat(".xlsx");
		accessor.writeCompleteBarCodeScannable("E:\\angrycat_workitem\\產品\\barCode\\2015_10_16\\ohm-tw-catalog-barcodes-151015.xlsx", "E:\\angrycat_workitem\\產品\\barCode\\2015_10_16\\型號和可掃描條碼對照test.xlsx", "E:\\angrycat_workitem\\產品\\barCode\\2015_10_16\\image\\{sku}.jpg");
		acac.close();
	}
	
	private static void testStringPad(){
		String t1 = "1";
		String t2 = "99";
		System.out.println(StringUtils.leftPad(t1, 4, "0"));
		System.out.println(StringUtils.leftPad(t2, 4, "0"));
	}
	
	
	private static void testAddImgToExcel(){
		addImgToExcel("C:\\Users\\JerryLin\\Desktop\\pic\\images.jpg", "C:\\Users\\JerryLin\\Desktop\\pic\\pic.xlsx", 1, 1, 1, 1);
	}
	
	private static void testAddImgsToExcel(){
		addImgsToExcel(Arrays.asList("C:\\Users\\JerryLin\\Desktop\\pic\\images.jpg", "C:\\Users\\JerryLin\\Desktop\\pic\\images2.jpg", "C:\\Users\\JerryLin\\Desktop\\pic\\images3.jpg"), "C:\\Users\\JerryLin\\Desktop\\pic\\pic.xlsx", 2);
	}
	/**
	 * 將圖片放在指定的欄位位置，並輸出Excel
	 * @param imgPaths
	 * @param outPath
	 * @param colIdx
	 */
	private static void addImgsToExcel(List<String> imgPaths, String outPath, int colIdx){
		final int COL_WIDTH_UNIT = 256;// 欄位寬度單位為字元(character)寬度的1/256
		final int ROW_HEIGHT_UNIT = 20;// 欄位高度單位為點距(point)的1/20
		try(Workbook wb = new XSSFWorkbook();
			FileOutputStream fos = new FileOutputStream(outPath)){
			
			Sheet sheet = wb.createSheet("My Excel With Pic");
			sheet.setColumnWidth(colIdx, 15*COL_WIDTH_UNIT);
			Drawing drawing = sheet.createDrawingPatriarch();
			CreationHelper helper = wb.getCreationHelper();
			
			for(int i = 0; i < imgPaths.size(); i++){
				String imgPath = imgPaths.get(i);
				try(FileInputStream fis = new FileInputStream(imgPath);){
					byte[] bytes = IOUtils.toByteArray(fis);
					int picIdx = wb.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
					fis.close();
					
					Row row = sheet.createRow(i);
					row.setHeight((short)(50*ROW_HEIGHT_UNIT));
					
					ClientAnchor anchor = helper.createClientAnchor();
					anchor.setDx1(0);
					anchor.setDy1(0);
					anchor.setDx2(0);
					anchor.setDy2(0);
					anchor.setCol1(colIdx);
					anchor.setRow1(i);
					anchor.setCol2(colIdx+1);
					anchor.setRow2(i+1);
					
					drawing.createPicture(anchor, picIdx);
				}catch(Throwable e){
					throw new RuntimeException(e);
				}
			}
			wb.write(fos);
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	private static void addImgToExcel(String imgPath, String outPath, int leftTopColIdx, int leftTopRowIdx, int rightBottomColIdx, int rightBottomRowIdx){
		final int COL_WIDTH_UNIT = 256;
		final int ROW_HEIGHT_UNIT = 20;
		try(Workbook wb = new XSSFWorkbook();
			FileInputStream fis = new FileInputStream(imgPath);
			FileOutputStream fos = new FileOutputStream(outPath)){
			byte[] bytes = IOUtils.toByteArray(fis);
			int picIdx1 = wb.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
			int picIdx2 = wb.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
			fis.close();
			
			Sheet sheet = wb.createSheet("My Excel With Pic");
			sheet.setColumnWidth(0, 10*COL_WIDTH_UNIT);
			Row row = sheet.createRow(0);
			row.setHeight((short)(200*ROW_HEIGHT_UNIT));
			Drawing drawing = sheet.createDrawingPatriarch();
			
			CreationHelper helper = wb.getCreationHelper();
			ClientAnchor anchor1 = helper.createClientAnchor();
			anchor1.setDx1(0);
			anchor1.setDy1(0);
			anchor1.setDx2(0);
			anchor1.setDy2(0);
			anchor1.setCol1(0);
			anchor1.setRow1(0);
			anchor1.setCol2(1);
			anchor1.setRow2(1);
			
			Picture pic1 = drawing.createPicture(anchor1, picIdx1);
//			pic1.resize();
			
//			ClientAnchor anchor2 = helper.createClientAnchor();
//			anchor2.setCol1(leftTopColIdx);
//			anchor2.setRow1(++leftTopRowIdx);
//			anchor2.setCol2(rightBottomColIdx);
//			anchor2.setRow2(++rightBottomRowIdx);
//			
//			Picture pic2 = drawing.createPicture(anchor2, picIdx1);
//			pic2.resize();
			
			wb.write(fos);
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		
		
	}
	
	
	private static void testWriteDiscountBarCode(){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		OnePosInitialExcelAccessor accessor = acac.getBean(OnePosInitialExcelAccessor.class);
		accessor.setOutFormat(".xlsx");
		accessor.writeDiscountBarCode("E:\\angrycat_workitem\\產品\\barCode\\2015_10_27\\折扣條碼.xlsx");
		acac.close();
	}
	
	public static void main(String[]args){
//		testFileReplaceOldPart();
//		testPack();
		testProcess();
//		testWriteBriefBarCodeScannable();
//		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
//		
//		OnePosProductExcelAccessor importer = acac.getBean(OnePosProductExcelAccessor.class);
//		importer.storeImage("E:\\angrycat_workitem\\member\\臺灣OHM商品總庫存清單_T20150923.xlsx", "E:\\angrycat_workitem\\member\\image\\", 2, 0);
//		
//		acac.close();
//		testStringPad();
//		testAddImgToExcel();
//		testAddImgsToExcel();
//		testWriteCompleteBarCodeScannable();
//		testWriteDiscountBarCode();
	}
}
