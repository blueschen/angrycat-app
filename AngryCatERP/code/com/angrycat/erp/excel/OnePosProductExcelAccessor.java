package com.angrycat.erp.excel;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.excel.ExcelColumn.Product.OnePos;
import com.angrycat.erp.excel.ExcelColumn.Product.Sheet1;
import com.angrycat.erp.excel.ExcelColumn.Product.Sheet2;
import com.angrycat.erp.excel.ExcelColumn.Product.Sheet3;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.service.http.HttpService;

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
public class OnePosProductExcelAccessor {
	private static final String JPG_POSTFIX = "-o-a.jpg";
	private static final String URL_TEMPLATE = "http://ohmbeads.com/ohm2/media/import/{no}" + JPG_POSTFIX;
	private static final String DEFAULT_IMG_DIR = "C:"+File.separator+"ONE-POS DB"+File.separator+"Project"+File.separator;
	private static final DataFormatter DF = new DataFormatter();
	private static final String INVENTORY = "I";
	@Autowired
	private HttpService httpService;
	private boolean imgProcessEnabled;
	private String templatePath;
	
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
	
	private void storeImage(String src, String jpgDestDir, int sheetIdex, int cellIdx){
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
				Cell cell = row.getCell(cellIdx);
				String no = cell.getStringCellValue();
				storeImage(jpgDestDir, no);
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
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
	
	private static void testProcess(){
		long starttime = System.currentTimeMillis();
		
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		String t1 = "E:\\angrycat_workitem\\member\\2015_09_24\\臺灣OHM商品總庫存清單(類別)_T20150924.xlsx";
		String t2 = "E:\\angrycat_workitem\\member\\臺灣OHM商品總庫存清單(類別)_T20150924 - 複製.xlsx";
		try(FileInputStream fis = new FileInputStream(t2)){
			byte[] data = IOUtils.toByteArray(fis);
			OnePosProductExcelAccessor accessor = acac.getBean(OnePosProductExcelAccessor.class);
			accessor.setImgProcessEnabled(true);
			accessor.setTemplatePath("E:\\angrycat_workitem\\member\\v36 ONE-POS Data Quick Import  快速匯入 - Empty .xls");
			accessor.process(data);
			
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
	 * 將公司內部產品的Excel轉成OnePos需要的Excel匯入格式，<br>
	 * 如果有額外設定的話(imgProcessEnabled = true)，<br>
	 * 可以抓取對應的圖檔之後改名。<br>
	 * 
	 * @param data
	 */
	public void process(byte[] data){
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
			XSSFWorkbook wb = new XSSFWorkbook(bais);
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			HSSFWorkbook outWb = templatePath == null ? new HSSFWorkbook() : new HSSFWorkbook(new FileInputStream(templatePath));){
			
			Sheet categorySheet = outWb.getSheet("categories") == null ? outWb.createSheet("categories") : outWb.getSheet("categories"); // 第一個Sheet是產品類別
			if(outWb.getSheet("brand") == null){ // 第二個Sheet是產品品牌
				outWb.createSheet("brand");
			}
			Sheet productSheet = outWb.getSheet("products") == null ? outWb.createSheet("products") : outWb.getSheet("products"); // 第三個Sheet是產品(品項)
			if(outWb.getSheet("clients") == null){ // 第四個Sheet是客戶
				outWb.createSheet("clients");
			}
			if(outWb.getSheet("vendors") == null){ // 第五個Sheet是供應商
				outWb.createSheet("vendors");
			}
			
			Sheet generalSheet = wb.getSheetAt(0); // 一般商品
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
				
				addValToCategorySheet(categories, categorySheet, catVal);
				
				Row pRow = productSheet.createRow(rowNum);
				addProductCells(pRow, noVal, nameEngVal, catVal, priceVal);
				
				storeImage(tempImgDir, noVal);
			}
			
			Sheet serialSheet = wb.getSheetAt(1);
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
				
				addValToCategorySheet(categories, categorySheet, catVal);
				
				Row pRow = productSheet.createRow(++rowNum);
				addProductCells(pRow, noVal, nameEngVal, catVal, priceVal);
				
				Cell remarkCell = pRow.createCell(OnePos.備註欄); // 把系列名稱先放到備註欄，OnePos沒有直接對應的欄位
				remarkCell.setCellType(CELL_TYPE_STRING);
				remarkCell.setCellValue(serialNameVal);
				
				storeImage(tempImgDir, noVal);
			}
			
			Sheet jewelrySheet = wb.getSheetAt(2);
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
				
				addValToCategorySheet(categories, categorySheet, catVal);
				
				Row pRow = productSheet.createRow(++rowNum);
				addProductCells(pRow, noVal, nameEngVal, catVal, priceVal);
				
				storeImage(tempImgDir, noVal);
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
		try{
			FileUtils.forceDelete(new File(tempPath + customDir));
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	private void addProductCells(Row pRow, String noVal, String nameEngVal, String catVal, String priceVal){
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
	}
	
	private static void addValToCategorySheet(Set<String> categories, Sheet categorySheet, String catVal){
		if(StringUtils.isBlank(catVal)){
			return;
		}
		int beforeCatCount = categories.size();
		categories.add(catVal);
		int afterCatCount = categories.size();
		if(beforeCatCount != afterCatCount){
			int rowNum = afterCatCount;
			Row catRow = categorySheet.createRow(rowNum);
			Cell catNoCell = catRow.createCell(0);
			catNoCell.setCellType(CELL_TYPE_STRING);
			catNoCell.setCellValue(catVal);
			
			Cell catNameCell = catRow.createCell(1);
			catNameCell.setCellType(CELL_TYPE_STRING);
			catNameCell.setCellValue(catVal);
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
	
	public static void main(String[]args){
//		testFileReplaceOldPart();
//		testPack();
		testProcess();
//		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
//		
//		OnePosProductExcelAccessor importer = acac.getBean(OnePosProductExcelAccessor.class);
//		importer.storeImage("E:\\angrycat_workitem\\member\\臺灣OHM商品總庫存清單_T20150923.xlsx", "E:\\angrycat_workitem\\member\\image\\", 2, 0);
//		
//		acac.close();
	}
}
