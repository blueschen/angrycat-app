package com.angrycat.erp.excel;

import static org.apache.poi.ss.examples.AddDimensionedImage.ConvertImageUnits.POINTS_PER_MILLIMETRE;
import static org.apache.poi.ss.examples.AddDimensionedImage.ConvertImageUnits.pixel2WidthUnits;
import static org.apache.poi.ss.examples.AddDimensionedImage.ConvertImageUnits.pointsToMillimeters;
import static org.apache.poi.ss.examples.AddDimensionedImage.ConvertImageUnits.pointsToPixels;
import static org.apache.poi.ss.examples.AddDimensionedImage.ConvertImageUnits.widthUnits2Millimetres;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
/**
 * 利用Apache POI函式庫處理Excel中的圖片
 * ref. AddDimensionedImage.java
 * @author JerryLin
 *
 */
public class ExcelImgProcessor {
	public static final int COL_WIDTH_UNIT = 256;// 欄位寬度單位為字元(character)寬度的1/256
	public static final int ROW_HEIGHT_UNIT = 21;// 欄位高度單位為點距(point)的1/20
	private Workbook wb;
	private Drawing drawing;
	private CreationHelper helper;
	private int pictureType = Workbook.PICTURE_TYPE_JPEG;
	private Sheet sheet;
	
	public ExcelImgProcessor(Workbook wb, Sheet sheet){
		this.wb = wb;
		this.sheet = sheet;
		helper = wb.getCreationHelper();
		drawing = sheet.createDrawingPatriarch();
	}
	public void setPictureType(int pictureType) {
		this.pictureType = pictureType;
	}
	private byte[] getBytes(File file){
		byte[] bytes = new byte[0];
		try(FileInputStream fis = new FileInputStream(file);){
//			BufferedImage image = ImageIO.read(fis);
//			System.out.println("width: " + image.getWidth() + " pxs, height: " + image.getHeight() + " pxs");
			bytes = IOUtils.toByteArray(fis);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		return bytes;
	}
	/**
	 * 有可能出現空的檔案，如果是空的就砍掉
	 * @param file
	 * @return
	 */
	private boolean deleteEmptyFile(File file){
		boolean deleted = false;
		if(file.exists() && file.length() == 0){
			deleted = file.delete();
		}
		return deleted;
	}
	/**
	 * see the method {@link #addImgFitToCell(byte[] bytes, int rowIdx, int colIdx)}.
	 */
	public void addImgFitToCell(String imgPath, int rowIdx, int colIdx){
		addImgFitToCell(imgPath, rowIdx, colIdx, null);
	}
	/**
	 * see the method {@link #addImgFitToCell(byte[] bytes, int rowIdx, int colIdx)}.
	 */
	public int addImgFitToCell(String imgPath, int rowIdx, int colIdx, Function<String, File> fileNotFoundProcess){
		File file = new File(imgPath);
		int picIdx = 0;
		if(!file.exists()){
			if(fileNotFoundProcess != null){
				file = fileNotFoundProcess.apply(imgPath);
			}
			if(!file.exists()){
				System.out.println("圖檔不存在: "+imgPath);
				return picIdx;
			}else{
				deleteEmptyFile(file);
			}
		}else{
			deleteEmptyFile(file);
		}
		if(file.exists()){
			picIdx = addImgFitToCell(getBytes(file), rowIdx, colIdx);
		}
		return picIdx;
	}
	/**
	 * 把圖片加到Cell當中，圖片的高度和寬度與Cell一致。
	 * @param bytes
	 * @param rowIdx
	 * @param colIdx
	 */
	public int addImgFitToCell(byte[] bytes, int rowIdx, int colIdx){
		if(bytes == null || bytes.length == 0){
			System.out.println("ExcelImgProcessor.addImgFitToCell 沒有圖片資料，不會往下執行");
			return 0;
		}
		int picIdx = wb.addPicture(bytes, pictureType);
		
		ClientAnchor anchor = helper.createClientAnchor();
		anchor.setDx1(0);// 這裡空出一點距離，這樣為圖片儲存格加上格線的時候才顯示的出來
		anchor.setDy1(0);
		anchor.setDx2(0);
		anchor.setDy2(0);
		anchor.setCol1(colIdx);
		anchor.setRow1(rowIdx);
		anchor.setCol2(colIdx+1);
		anchor.setRow2(rowIdx+1);
		
		Picture pic = drawing.createPicture(anchor, picIdx);
		return picIdx;
	}
	public int addImgAdjustingCell(String imgPath, int rowIdx, int colIdx){
		return addImgAdjustingCell(imgPath, rowIdx, colIdx, null);
	}
	public int addImgAdjustingCell(String imgPath, int rowIdx, int colIdx, Function<String, File> fileNotFoundProcess){
		File file = new File(imgPath);
		int picIdx = 0;
		if(!file.exists()){
			if(fileNotFoundProcess != null){
				file = fileNotFoundProcess.apply(imgPath);
			}
			if(!file.exists()){
				System.out.println("圖檔不存在: "+imgPath);
				return picIdx;
			}else{
				deleteEmptyFile(file);
			}
		}else{
			deleteEmptyFile(file);
		}
		if(file.exists()){
			picIdx = addImgAdjustingCell(getBytes(file), rowIdx, colIdx);
		}
		return picIdx;
	}
	public int addImgAdjustingCell(byte[] bytes, int rowIdx, int colIdx){
		if(bytes == null || bytes.length == 0){
			System.out.println("ExcelImgProcessor.addImgAdjustingCell 沒有圖片資料，不會往下執行");
			return 0;
		}
		int picIdx = 0;
		try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes)){
			
			BufferedImage bi = ImageIO.read(bais);
			int widthPxs = bi.getWidth();
			int heightPxs = bi.getHeight();
			short widthUnits = pixel2WidthUnits(widthPxs);
			double millimeters = pixelToMillimeters(heightPxs);
			
			int width = sheet.getColumnWidth(colIdx);
			if(widthUnits > width){
				sheet.setColumnWidth(colIdx, widthUnits);
			}
			Row row = sheet.getRow(rowIdx);
			row.setHeightInPoints((float)(millimeters * POINTS_PER_MILLIMETRE)); // 在xls圖片高度像素會變得比原來大，所以圖片會顯得比儲存格列高略高，但在xlsx中則是剛剛好
			picIdx = wb.addPicture(bytes, pictureType);
			
			ClientAnchor anchor = helper.createClientAnchor();
			anchor.setDx1(0);
			anchor.setDy1(0);
			anchor.setDx2(0);
			anchor.setDy2(0);
			anchor.setCol1(colIdx);
			anchor.setRow1(rowIdx);
			anchor.setCol2(colIdx);
			anchor.setRow2(rowIdx);
			
			Picture pic = sheet.createDrawingPatriarch().createPicture(anchor, picIdx);
			pic.resize();
			
			
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		return picIdx;
	}
	/**
	 * see the method {@link #addCommentImg(byte[] bytes, int rowIdx, int colIdx)}.
	 */
	public void addCommentImg(String imgPath, int rowIdx, int colIdx){
		addCommentImg(imgPath, rowIdx, colIdx, null);
	}
	/**
	 * see the method {@link #addCommentImg(byte[] bytes, int rowIdx, int colIdx)}.
	 */
	public void addCommentImg(String imgPath, int rowIdx, int colIdx, Function<String, File> fileNotFoundProcess){
		File file = new File(imgPath);
		if(!file.exists()){
			if(fileNotFoundProcess != null){
				file = fileNotFoundProcess.apply(imgPath);
			}
			if(!file.exists()){
				System.out.println("圖檔不存在: "+imgPath);
				return;
			}else{
				deleteEmptyFile(file);
			}
		}else{
			deleteEmptyFile(file);
		}
		if(file.exists()){
			addCommentImg(getBytes(file), rowIdx, colIdx);
		}
		
	}
	/**
	 * 將圖片加到註解背景圖片，這個功能在Apache POI中只有HSSFWorkbook-xls檔才有支援
	 * @param bytes
	 * @param rowIdx
	 * @param colIdx
	 */
	public void addCommentImg(byte[]bytes, int rowIdx, int colIdx){
		if(!(wb instanceof HSSFWorkbook)){
			throw new RuntimeException("ExcelImgProcessor.addCommentImg 僅支援xls檔");
		}
		if(bytes == null || bytes.length == 0){
			System.out.println("ExcelImgProcessor.addCommentImg 沒有圖片資料，不會往下執行");
			return;
		}
		int picIdx = wb.addPicture(bytes, pictureType);
		addCommentImg(picIdx, rowIdx, colIdx);
	}
	public void addCommentImg(int picIdx, int rowIdx, int colIdx){
		
		ClientAnchor anchor = helper.createClientAnchor();
		anchor.setDx1(0);
		anchor.setDy1(0);
		anchor.setDx2(0);
		anchor.setDy2(0);
		anchor.setCol1(colIdx);
		anchor.setRow1(rowIdx);
		anchor.setCol2(colIdx+4);
		anchor.setRow2(rowIdx+5);
		
		HSSFComment comment = (HSSFComment)drawing.createCellComment(anchor); // 只有HSSF有支援備註背景圖檔的功能
		comment.setBackgroundImage(picIdx);
		
		Row row = sheet.getRow(rowIdx);
		Cell cell = row.getCell(colIdx) != null ? row.getCell(colIdx) : row.createCell(colIdx);
		cell.setCellComment(comment);
	}
	private static void testAddImgFitToCell(){
		final int IMG_COL_IDX = 2;
		try(FileOutputStream fos = new FileOutputStream("C:\\Users\\JerryLin\\Desktop\\pic\\picture.xlsx");
			Workbook wb = new XSSFWorkbook();){
			Sheet sheet = wb.createSheet("img test");
			sheet.setColumnWidth(IMG_COL_IDX, 20*COL_WIDTH_UNIT);
			Row firstRow = sheet.createRow(0);
			Cell c1 = firstRow.createCell(0);
			c1.setCellValue("姓名");
			Cell c2 = firstRow.createCell(1);
			c2.setCellValue("電話");
			Cell c3 = firstRow.createCell(IMG_COL_IDX);
			c3.setCellValue("圖片");
			
			ExcelImgProcessor eip = new ExcelImgProcessor(wb, sheet);
			List<String> imgPaths = Arrays.asList("C:\\Users\\JerryLin\\Desktop\\pic\\AAA001.jpg", "C:\\Users\\JerryLin\\Desktop\\pic\\AAA002.jpg", "C:\\Users\\JerryLin\\Desktop\\pic\\AAA003.jpg");
			
			for(int i = 1; i < 20; i++){
				Row row = sheet.createRow(i);
				row.setHeight((short)(50*ROW_HEIGHT_UNIT));
				
				Cell cell1 = row.createCell(0);
				cell1.setCellValue("姓名"+i);
				
				Cell cell2 = row.createCell(1);
				cell2.setCellValue("電話"+i);
				
				Cell cell3 = row.createCell(IMG_COL_IDX);
				eip.addImgFitToCell(imgPaths.get(i%3), row.getRowNum(), IMG_COL_IDX);
			}
			wb.write(fos);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	private static void testAddCommentImg(){
		final int IMG_COL_IDX = 2;
		try(FileOutputStream fos = new FileOutputStream("C:\\Users\\JerryLin\\Desktop\\pic\\picture.xls");
			Workbook wb = new HSSFWorkbook();){
			
			Sheet sheet = wb.createSheet("img test");
			sheet.setColumnWidth(IMG_COL_IDX, 20*COL_WIDTH_UNIT);
			Row firstRow = sheet.createRow(0);
			Cell c1 = firstRow.createCell(0);
			c1.setCellValue("姓名");
			Cell c2 = firstRow.createCell(1);
			c2.setCellValue("電話");
			Cell c3 = firstRow.createCell(IMG_COL_IDX);
			c3.setCellValue("圖片");
			
			ExcelImgProcessor eip = new ExcelImgProcessor(wb, sheet);
			List<String> imgPaths = Arrays.asList("C:\\Users\\JerryLin\\Desktop\\pic\\images.jpg", "C:\\Users\\JerryLin\\Desktop\\pic\\images2.jpg", "C:\\Users\\JerryLin\\Desktop\\pic\\images3.jpg");
			
			for(int i = 1; i < 20; i++){
				Row row = sheet.createRow(i);
				row.setHeight((short)(50*ROW_HEIGHT_UNIT));
				
				Cell cell1 = row.createCell(0);
				cell1.setCellValue("姓名"+i);
				
				Cell cell2 = row.createCell(1);
				cell2.setCellValue("電話"+i);
				
				Cell cell3 = row.createCell(IMG_COL_IDX);
				String imgPath = imgPaths.get(i%3);
				eip.addImgFitToCell(imgPath, row.getRowNum(), IMG_COL_IDX);
				eip.addCommentImg(imgPath, row.getRowNum(), IMG_COL_IDX);
			}
			wb.write(fos);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	/**
	 * 專門用來測試HSSF版本的功能
	 * also see the method {@link #testXSSFAddDimensionedImage()}.
	 */
	private static void testHSSFAddDimensionedImage(){
		final int IMG_COL_IDX = 2;
		try(FileOutputStream fos = new FileOutputStream("C:\\Users\\JerryLin\\Desktop\\pic\\picture.xls");
			Workbook wb = new HSSFWorkbook();){
			
			Sheet sheet = wb.createSheet("img test");
			Row firstRow = sheet.createRow(0);
			Cell c1 = firstRow.createCell(0);
			c1.setCellValue("姓名");
			Cell c2 = firstRow.createCell(1);
			c2.setCellValue("電話");
			Cell c3 = firstRow.createCell(IMG_COL_IDX);
			c3.setCellValue("圖片");
			
			ExcelImgProcessor eip = new ExcelImgProcessor(wb, sheet);
			List<String> imgPaths = Arrays.asList("C:\\Users\\JerryLin\\Desktop\\pic\\images.jpg", "C:\\Users\\JerryLin\\Desktop\\pic\\images2.jpg", "C:\\Users\\JerryLin\\Desktop\\pic\\images3.jpg");
			
			for(int i = 1; i < 20; i++){
				Row row = sheet.createRow(i);
				
				Cell cell1 = row.createCell(0);
				cell1.setCellValue("姓名"+i);
				
				Cell cell2 = row.createCell(1);
				cell2.setCellValue("電話"+i);
				
				Cell cell3 = row.createCell(IMG_COL_IDX);
				String imgPath = imgPaths.get(i%3);
				try(FileInputStream fis = new FileInputStream(imgPath)){
					BufferedImage bi = ImageIO.read(fis);
					int widthPixel = bi.getWidth();
					int heightPixel = bi.getHeight();
					
					double widthMM = widthUnits2Millimetres(pixel2WidthUnits(widthPixel));
					double heightMM = widthUnits2Millimetres(pixel2WidthUnits(heightPixel));
					
					new org.apache.poi.hssf.usermodel.examples.AddDimensionedImage().addImageToSheet(getHSSFCellNumber(row.getRowNum(), IMG_COL_IDX), (HSSFSheet)sheet, imgPath, widthMM, heightMM, org.apache.poi.hssf.usermodel.examples.AddDimensionedImage.EXPAND_ROW_AND_COLUMN);
				}catch(Throwable e){
					throw new RuntimeException(e);
				}
			}
			wb.write(fos);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	/**
	 * 在poi-examples中有提供XSSF和HSSF版本的AddDimensionedImage，這個類別的用處在於讓Excel可以插入圖片，並且(可自訂)調整欄寬列高以符合圖片的大小。
	 * 兩版本有相近的API，但還是有參數和用法上的差異。
	 * testXSSFAddDimensionedImage是專門用來測試XSSF版本的功能
	 */
	private static void testXSSFAddDimensionedImage(){
		final int IMG_COL_IDX = 2;
		try(FileOutputStream fos = new FileOutputStream("C:\\Users\\JerryLin\\Desktop\\pic\\picture.xlsx");
			Workbook wb = new XSSFWorkbook();){
			
			Sheet sheet = wb.createSheet("img test");
			Drawing drawing = sheet.createDrawingPatriarch();
			Row firstRow = sheet.createRow(0);
			Cell c1 = firstRow.createCell(0);
			c1.setCellValue("姓名");
			Cell c2 = firstRow.createCell(1);
			c2.setCellValue("電話");
			Cell c3 = firstRow.createCell(IMG_COL_IDX);
			c3.setCellValue("圖片");
			
			List<String> imgPaths = Arrays.asList("C:\\Users\\JerryLin\\Desktop\\pic\\images.jpg", "C:\\Users\\JerryLin\\Desktop\\pic\\images2.jpg", "C:\\Users\\JerryLin\\Desktop\\pic\\images3.jpg");
			
			for(int i = 1; i < 20; i++){
				Row row = sheet.createRow(i);
				int rowIdx = row.getRowNum();
				
				Cell cell1 = row.createCell(0);
				cell1.setCellValue("姓名"+i);
				
				Cell cell2 = row.createCell(1);
				cell2.setCellValue("電話"+i);
				
				Cell cell3 = row.createCell(IMG_COL_IDX);
				String imgPath = imgPaths.get(i%3);
				try(FileInputStream fis = new FileInputStream(imgPath)){
					// 找出原圖的長寬，單位為像素
					BufferedImage bi = ImageIO.read(fis);
					int widthPixels = bi.getWidth();
					int heightPixels = bi.getHeight();
					// AddDimensionedImage中調整欄寬列高的單位是毫米(millimetre)，所以利用內部提供的API將像素轉為毫米
					double widthMM = widthUnits2Millimetres(pixel2WidthUnits(widthPixels));
					double heightMM = widthUnits2Millimetres(pixel2WidthUnits(heightPixels));
					
					new org.apache.poi.ss.examples.AddDimensionedImage().addImageToSheet(IMG_COL_IDX, rowIdx, sheet, drawing, new File(imgPath).toURI().toURL(), widthMM, heightMM, org.apache.poi.ss.examples.AddDimensionedImage.EXPAND_ROW_AND_COLUMN);
				}catch(Throwable e){
					throw new RuntimeException(e);
				}
			}
			wb.write(fos);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	/**
	 * 取得HSSF儲存格代碼，譬如A5
	 * @param rowIdx
	 * @param colIdx
	 * @return
	 */
	private static String getHSSFCellNumber(int rowIdx, int colIdx){
		org.apache.poi.hssf.util.CellReference cr = new org.apache.poi.hssf.util.CellReference(rowIdx, colIdx);
		String cellNum = cr.formatAsString().replace("$", "");
		return cellNum;
	}
	/**
	 * 取得XSSF儲存格代碼，譬如A3
	 * @param rowIdx
	 * @param colIdx
	 * @return
	 */
	private static String getXSSFCellNumber(int rowIdx, int colIdx){
		org.apache.poi.ss.util.CellReference cr = new org.apache.poi.ss.util.CellReference(rowIdx, colIdx);
		String cellNum = cr.formatAsString().replace("$", "");
		return cellNum;
	}
	/**
	 * 像素轉點距，Excel列高單位為點距
	 * @param pxs
	 * @return
	 */
	private static double pixelToPoints(int pxs){
		System.out.println("pxs: " + pxs);
		double millimeters = pixelToMillimeters(pxs);
		System.out.println("millimeters: " + millimeters);
		double points = millimetersToPoints(millimeters);
		System.out.println("points: " + points);
		return points;
	}
	/**
	 * 像素轉毫米
	 * @param pxs
	 * @return
	 */
	private static double pixelToMillimeters(int pxs){
		short widthUnits = pixel2WidthUnits(pxs);
		double millimeters = widthUnits2Millimetres(widthUnits);
		return millimeters;
	}
	/**
	 * 毫米轉點距
	 * @param millimeters
	 * @return
	 */
	private static double millimetersToPoints(double millimeters){
		return (millimeters * 72D)/25.4; 
	}
	private static void testMillimetersToPoints(){
		double points = 122.9846269216348;
		double millimeters = pointsToMillimeters(points);
		System.out.println("毫米: " + millimeters);
		points = millimetersToPoints(millimeters);
		System.out.println("點距: " + points);
		int pxs = pointsToPixels(points);
		System.out.println("像素: " + pxs);
	}
	private static void testAddImgAdjustingCell(){
		final int IMG_COL_IDX = 2;
		try(FileOutputStream fos = new FileOutputStream("C:\\Users\\JerryLin\\Desktop\\pic\\picture.xlsx");
			Workbook wb = new XSSFWorkbook();){
			Sheet sheet = wb.createSheet("img test");
			Row firstRow = sheet.createRow(0);
			Cell c1 = firstRow.createCell(0);
			c1.setCellValue("姓名");
			Cell c2 = firstRow.createCell(1);
			c2.setCellValue("電話");
			Cell c3 = firstRow.createCell(IMG_COL_IDX);
			c3.setCellValue("圖片");
			
			ExcelImgProcessor eip = new ExcelImgProcessor(wb, sheet);
			List<String> imgPaths = Arrays.asList("C:\\Users\\JerryLin\\Desktop\\pic\\images.jpg", "C:\\Users\\JerryLin\\Desktop\\pic\\images2.jpg", "C:\\Users\\JerryLin\\Desktop\\pic\\images3.jpg");
			
			for(int i = 1; i < 20; i++){
				Row row = sheet.createRow(i);
				
				Cell cell1 = row.createCell(0);
				cell1.setCellValue("姓名"+i);
				
				Cell cell2 = row.createCell(1);
				cell2.setCellValue("電話"+i);
				
				Cell cell3 = row.createCell(IMG_COL_IDX);
				eip.addImgAdjustingCell(imgPaths.get(i%3), row.getRowNum(), IMG_COL_IDX);
			}
			wb.write(fos);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	
	}
	public static void main(String[]args){
		testAddImgFitToCell();
//		testAddCommentImg();
//		testHSSFAddDimensionedImage();
//		testXSSFAddDimensionedImage();
//		testMillimetersToPoints();
//		testAddImgAdjustingCell();
	}
}
