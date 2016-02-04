package com.angrycat.erp.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.lucene.LucenePDFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.excel.ProductExcelExporter;
import com.angrycat.erp.excel.ProductExcelImporter;
import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.onepos.excel.OnePosInitialExcelAccessor;
import com.angrycat.erp.service.http.HttpService;

@Service
@Scope("prototype")
public class ProductAccessService {
	private static final String JPG_POSTFIX = "-o-a.jpg";
	private static final String URL_TEMPLATE = "http://ohmbeads.com/ohm2/media/import/{no}" + JPG_POSTFIX;
	
	@Autowired
	private ProductExcelImporter productExcelImporter;
	@Autowired
	private SessionFactoryWrapper sfw;
	@Autowired
	private ProductExcelExporter productExcelExporter;
	@Autowired
	private QueryBaseService<Product, Product> queryBaseService;
	@Autowired
	private HttpService httpService;
	private String storeImagePath = "E:\\angrycat_workitem\\產品\\barCode\\2015_10_16\\image\\";
	
	@PostConstruct
	public void init(){
		queryBaseService.setRootAndInitDefault(Product.class);
	}
	public void importProductFromExcelToDB(byte[]data){
		productExcelImporter.persist(data);
	}
	
	/**
	 * 匯入由Kit提供的產品PDF，如果已經存在但沒有條碼，則新增條碼；如果沒有產品，就新增一筆產品。
	 * required. pdfbox-lucene
	 * ref. http://pdfbox.apache.org/1.8/cookbook/textextraction.html
	 * @param data
	 */
	public void importProductFromPDFToDB(byte[]data){
		try(BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(data));){
			Document doc = LucenePDFDocument.getDocument(bis);
			StringBuffer msg = new StringBuffer();
			
			List<?> list = doc.getFields();
			for(int i = 0; i < list.size(); i++){
				Object element = list.get(i);
				if(element.getClass() == Field.class){
					Field field = (Field)element;
					if(field.isIndexed()){
						Reader reader = field.readerValue();
						if(reader!=null && reader.getClass() == StringReader.class){
							StringReader r = (StringReader)reader;
							List<String> lines = org.apache.commons.io.IOUtils.readLines(r);
							LinkedList<List<String>> rows = new LinkedList<>();
							for(int lineNum = 0; lineNum < lines.size(); lineNum++){
								String line = lines.get(lineNum);
								String[] splits = line.split(" ");
								int wordSize = splits.length;
								String firstWord = splits[0].trim();
								if(wordSize >= 4 && ("Barcode".equals(firstWord) || isEAN13(firstWord))){
									String lastWord = splits[wordSize-1].trim();
									if("Barcode".equals(firstWord)
									&& "EAN13".equals(splits[1].trim())
									&& "SKU".equals(splits[2].trim())
									&& "Name".equals(splits[3].trim())
									&& "MSRP".equals(splits[4].trim())){
										continue;// 標題略過
									}else if(isEAN13(firstWord)){
										List<String> row = new ArrayList<>();
										int nameWordCount = wordSize - 3;
										String[] collectName = new String[nameWordCount];
										for(int nameIdx = 2; nameIdx < (wordSize-1); nameIdx++){
											collectName[nameIdx-2] = splits[nameIdx];
										}
										
										String barcode = firstWord;
										String sku = splits[1].trim();
										String name = StringUtils.join(collectName, " ");
										String msrp = lastWord;
										row.add(barcode);
										row.add(sku);
										row.add(name);
										row.add(msrp);
										rows.add(row);
									}else{
										throw new RuntimeException("不會吧?到這裡還錯!!");
									}
								}else{// 因為pdf原始資料有斷行的情況，所以要額外處理。目前觀察到的斷行模式是EAN13一行、SKU一行、Name和MSRP同一行。其中Name本身又有空白的可能，所以只能先去掉最末端的MSRP，再拼回Name
									List<String> row = null;
									List<String> lastRow = rows.getLast();
									int lastRowSize = lastRow.size();
									if(lastRowSize == 4){
										row = new ArrayList<>();
										rows.add(row);
									}else{
										row = lastRow;
									}
									int rowSize = row.size();
									if(rowSize < 2){
										if(wordSize == 1){
											row.add(firstWord);
										}else{
											throw new RuntimeException("還沒有發現的例外!! ori row: " + StringUtils.join(row.toArray(), "、") + ", new val: " + StringUtils.join(splits, "、"));
										}
									}else{
										int nameCount = wordSize-1;
										String[]collectName = new String[nameCount];
										for(int nameIdx = 0; nameIdx < nameCount; nameIdx++){
											collectName[nameIdx] = splits[nameIdx];
										}
										
										String name = StringUtils.join(collectName, " ");
										String msrp = splits[wordSize-1];
										row.add(name);
										row.add(msrp);
									}
								}
							}							
							
							sfw.executeTransaction(s->{
								int total = 0;
								List<String> added = new ArrayList<>();
								List<String> updated = new ArrayList<>();
								List<String> ignored = new ArrayList<>();
								List<String> duplicated = new ArrayList<>();
								String lastSku = null;
								for(int j = 0 ; j < rows.size(); j++){
									List<String> row = rows.get(j);
									String barcode = row.get(0);
									String sku = row.get(1);
									String name = row.get(2);
									String msrp = row.get(3);
									
									List<Product> products = s.createQuery("FROM " + Product.class.getName() + " p WHERE upper(p.modelId) = :pModelId").setString("pModelId", sku.toUpperCase()).list();
									Product product = null;
									if(!products.isEmpty()){
										product = products.get(0);
										if(StringUtils.isBlank(product.getBarcode())){
											product.setBarcode(barcode);
											s.update(product);
											updated.add(sku);
											s.flush();
										}else if(!product.getBarcode().equals(barcode)){
											throw new RuntimeException("型號"+sku+"相同的產品條碼不一樣??");
										}else{
											duplicated.add(sku);
										}
									}else{
										// 目前產品最主要的資料源有二: 一是腦袋給的庫存Excel、一是喵娘向Kit要的產品條碼PDF。
										// 庫存與公司現行有在販售的有直接關係，所以應當以庫存為主新增
										// Kit的資料應該用來更新條碼即可，不涉及新增產品，以避免混亂
//										product = new Product();
//										product.setBarcode(barcode);
//										product.setModelId(sku);
//										product.setNameEng(name);
//										if(NumberUtils.isNumber(msrp)){
//											product.setSuggestedRetailPrice(Double.parseDouble(msrp));
//										}
//										s.save(product);
//										s.flush();
//										added.add(sku);
										ignored.add(sku);
									}
									lastSku = sku;
									total++;
								}
								s.clear();
								msg.append("總共成功處理: " + total + "筆\n最後一筆型號為: " + lastSku);
								if(!added.isEmpty()){
									int addCount = added.size();
									msg.append("\n新增產品型號"+addCount+"筆:" + StringUtils.join(added.toArray(), "、"));
								}
								if(!updated.isEmpty()){
									int updateCount = updated.size();
									msg.append("\n新增條碼型號"+updateCount+"筆:" + StringUtils.join(updated.toArray(), "、"));
								}
								if(!ignored.isEmpty()){
									int ignoreCount = ignored.size();
									msg.append("\n沒有在資料庫找到產品型號"+ignoreCount+"筆:" + StringUtils.join(ignored.toArray(), "、"));
								}
								if(!duplicated.isEmpty()){
									int duplicateCount = duplicated.size();
									msg.append("\n產品已經有一樣條碼"+duplicateCount+"筆:" + StringUtils.join(duplicated.toArray(), "、"));
								}
							});
						}
					}
				}

			}
			System.out.println("msg: " + msg.toString());
		}catch(Throwable e){
			throw new RuntimeException(e);
		}		
	}
	
	public void writeProductExcelWithBarcodeScannableAndImg(String dest){
		
	}
	
	/**
	 * 就算遇到沒有條碼的資料，還是一併匯出
	 * @return
	 */
	public File toOnePosImportExcelIncludingBarcodeNotExisted(){
		productExcelExporter.setReductionRequired(true);
		File file = productExcelExporter.onePos();
		productExcelExporter.setReductionRequired(false);
		return file;
	}
	/**
	 * 略過沒有條碼的資料
	 * @return
	 */
	public File toOnePosImportExcelExcludingBarcodeNotExisted(){
		productExcelExporter.setReductionRequired(true);
		productExcelExporter.setIgnoredIfWithoutBarcode(true);
		File file = productExcelExporter.onePos();
		productExcelExporter.setReductionRequired(false);
		productExcelExporter.setIgnoredIfWithoutBarcode(false);
		return file;
	}
	
	/**
	 * 如果在指定位置找不到OHM產品圖片，就從預設網址去下載回來
	 * 為了方便逐次存取，會把該次新增的圖片檔案獨立打包成一個壓縮檔，但不會影響到在指定位置新增的檔案。這樣同一個檔案等於新增兩次。
	 */
	public void downloadOHMImageIfNotExisted(){
		AtomicInteger i = new AtomicInteger(0);
		List<String> downloadSkus = new ArrayList<>();
		queryBaseService.executeScrollableQuery((rs, sfw)->{
			while(rs.next()){
				Product product = (Product)rs.get(0);
				String modelId = product.getModelId();
				String storePath = storeImagePath + modelId + ".jpg";
				if(new File(storePath).exists()){
					continue;
				}
				String url = URL_TEMPLATE.replace("{no}", modelId);
				try(FileOutputStream fos = new FileOutputStream(storePath)){
					httpService.sendPost(url, bis->{
						IOUtils.copy(bis, fos);
						downloadSkus.add(modelId);
						i.addAndGet(1);
					});
				}catch(Throwable e){
					throw new RuntimeException(e);
				}
			}
			File storeDir = new File(storeImagePath);
			String targetZip = storeDir.getParent() + File.separator + "ohm_images_add_" + LocalDate.now().toString() + ".zip";
			String skus = StringUtils.join(downloadSkus, "、");
			System.out.println("成功下載: " + i.get() + "張圖片: " + skus);
			copyAndPackImage(skus, targetZip);
			return true;
		});		
	}
	
	/**
	 * 複製OHM圖片，並壓縮成一個zip檔
	 * @param skus: 以頓號分隔開的型號 TODO
	 * @return
	 */
	public String copyAndPackImage(String skus, String targetZip){
		String tempDirPath = FileUtils.getTempDirectoryPath() + RandomStringUtils.randomAlphanumeric(8) + File.separator;
		String packTarget = StringUtils.isNotBlank(targetZip) ? targetZip : FileUtils.getTempDirectoryPath() + RandomStringUtils.randomAlphanumeric(8) + ".zip";
		System.out.println("tempDirPath: " + tempDirPath + ", packTarget: " + packTarget);
		File tempDir = new File(tempDirPath);
		try{
			if(!tempDir.exists()){
				FileUtils.forceMkdir(tempDir);
			}
			String[] skuSplits = skus.split("、");
			for(String sku : skuSplits){
				String storePath = storeImagePath + sku + ".jpg";
				String copyPath = tempDirPath + sku + ".jpg";
				try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(storePath));
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(copyPath))){
					IOUtils.copy(bis, bos);
				}
			}
			OnePosInitialExcelAccessor.pack(tempDirPath, packTarget);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			try{
				FileUtils.forceDelete(tempDir);
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
		}
		return packTarget;
	}
	
	private static boolean isEAN13(String val){
		val = val.trim();
		if(StringUtils.isNumeric(val) && val.length() == 13){
			return true;
		}
		return false;
	}
	
	private static void testCopyAndPackImage(){
		try(AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);){
			ProductAccessService ser = acac.getBean(ProductAccessService.class);
			ser.copyAndPackImage("AAX005、AAX004、AAX003、AAX002、AAX001、AAX105、AAX104、AAX103、AAX102、AAX101、AAX100、AAN002、AAL024、AAA050B、AAA050、AAY025、AAY024、WHH010、WHH009、WHH008、WHH007、WHH006、WHH005、WHH004、WHH003、WHH002、WHH001、WHG026、WHG025、WHG024、WHG023、WHG022、WHG021、WHG020、WHG019、WHG018、WHG017、WHG016、WHG015、WHG014、WHG013、WHG012、WHG011、WHG010、WHG009、WHG008、WHG007、WHG006、WHG005、WHG004、WHG003、WHG002、WHG001", null);
				
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	private static void testDownloadOHMImageIfNotExisted(){
		try(AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);){
			ProductAccessService ser = acac.getBean(ProductAccessService.class);
			ser.downloadOHMImageIfNotExisted();
				
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	private static void testToOnePosImportExcelExcludingBarcodeNotExisted(){
		try(AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);){
			ProductAccessService ser = acac.getBean(ProductAccessService.class);
			ser.toOnePosImportExcelExcludingBarcodeNotExisted();
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	private static void testToOnePosImportExcelIncludingBarcodeNotExisted(){
		try(AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);){
			ProductAccessService ser = acac.getBean(ProductAccessService.class);
			ser.toOnePosImportExcelIncludingBarcodeNotExisted();
				
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	private static void testImportProductFromPDFToDB(){
		String file1 = "E:\\angrycat_workitem\\產品\\barCode\\2015_10_16\\ohm-tw-catalog-barcodes-151015.pdf";
		String file2 = "E:\\angrycat_workitem\\產品\\barCode\\2016_02_02\\ohm-tw-catalog-barcodes-2016-Q1-160127.pdf";
		try(AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
			FileInputStream fis = new FileInputStream(file2);){
			byte[]data = IOUtils.toByteArray(fis);
			ProductAccessService ser = acac.getBean(ProductAccessService.class);
			ser.importProductFromPDFToDB(data);
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	private static void testImportProductFromExcelToDB(){
		String file = "E:\\angrycat_workitem\\產品\\2016_01_25\\臺灣OHM商品總庫存清單(類別)_T20150924.xlsx";
		try(AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
			FileInputStream fis = new FileInputStream(file);){
			byte[]data = IOUtils.toByteArray(fis);
			ProductAccessService ser = acac.getBean(ProductAccessService.class);
			ser.importProductFromExcelToDB(data);
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	private static void testProductModelIdAdjusted(){
		String t1 = "TT175(TT175)";
		String t2 = "TT017 (A)";
		String t3 = "TT121(BT)";
		
		Product p = new Product();
		p.setModelId(t1);
		System.out.println(p.getModelId() + ":" + p.getModelIdAdjusted());
		p.setModelId(t2);
		System.out.println(p.getModelId() + ":" + p.getModelIdAdjusted());
		p.setModelId(t3);
		System.out.println(p.getModelId() + ":" + p.getModelIdAdjusted());
	}
	
	private static void testDoubleToString(){
		Double t1 = 8858845925826d;
		System.out.println(t1.toString());
		System.out.println(new BigDecimal(t1).toString());
	}
	
	private static void testSplits(){
		String t1 = "Same Same, But Different 2000";
		System.out.println(StringUtils.join(t1.split(" "), "、"));
	}
	
	public static void main(String[]args){
//		testImportProductFromExcelToDB();
//		testImportProductFromPDFToDB();
		testToOnePosImportExcelExcludingBarcodeNotExisted();
//		testProductModelIdAdjusted();
//		testDoubleToString();
//		testSplits();
//		testToOnePosImportExcel();
//		testDownloadOHMImageIfNotExisted();
//		testCopyAndPackImage();
		
	}
}
