package com.angrycat.erp.service;

import static com.angrycat.erp.common.EmailContact.JERRY;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.initialize.StartupWebAppInitializer;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.service.http.HttpService;

@Service
@Scope("singleton")
public class LoadImgService {
	@Autowired
	private SessionFactoryWrapper sfw;
	@Autowired
	private HttpService httpService;
	@Autowired
	private MailSender mailSender;
	@Autowired
	private SimpleMailMessage templateMessage;
	
	@PostConstruct
	void init(){
//		new Thread(()->{
//			execute();
//		}).start();
	}
	public void execute(){
		AtomicInteger localCount = new AtomicInteger(0);
		AtomicInteger remoteCount = new AtomicInteger(0);
		sfw.executeTransaction(s->{
			String queryProducts = "SELECT p FROM " + Product.class.getName() + " p WHERE p.modelId IS NOT NULL AND p.imgDir IS NULL";
			List<Product> products = s.createQuery(queryProducts).list();
			if(products.isEmpty()){
				return;
			}
			Map<String, Product> results = products.stream().collect(Collectors.toMap(Product::getModelId, Function.identity()));
			Set<String> modelIds = results.keySet();
			
			String SEP = File.separator;
			String root = StartupWebAppInitializer.getUploadRoot();
			String imgFolder = getImgFolderPath();
			modelIds.forEach(modelId->{
				String filePath = imgFolder + SEP + modelId + ".jpg";
				File file = new File(filePath);
				if(file.exists()){
					String path = filePath.replace(root, "");
					Product product = results.get(modelId);
					product.setImgDir(path);
					s.update(product);
					s.flush();
					localCount.addAndGet(1);
				}
			});
			s.clear();
			
			List<Product>remainings = s.createQuery(queryProducts).list();
			if(remainings.isEmpty()){
				return;
			}
			Map<String, Product> productsFound = remainings.stream().collect(Collectors.toMap(Product::getModelId, Function.identity()));
			Set<String>modelIdsFound = productsFound.keySet();
			
			modelIdsFound.forEach(modelId->{
				String storePath = imgFolder + SEP + modelId + ".jpg";
				String url = ProductAccessService.URL_TEMPLATE.replace("{no}", modelId);
				try(FileOutputStream fos = new FileOutputStream(storePath)){
					httpService.sendPost(url, bis->{
						if(bis.available() == 0){
							return;
						}
						IOUtils.copy(bis, fos);
						
						Product product = productsFound.get(modelId);
						String path = storePath.replace(root, "");
						product.setImgDir(path);
						s.update(product);
						s.flush();
						remoteCount.addAndGet(1);
					});
				}catch(Throwable e){
					throw new RuntimeException(e);
				}
			});
			s.clear();
		});
		String sendMsg = "透過主機" + StartupWebAppInitializer.getUploadRoot() +"圖檔更新成功" + localCount.get() + "筆，透過遠端圖檔更新成功" + remoteCount.get() + "筆";
		String subject = "產品圖檔更新訊息";
		
		SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
		simpleMailMessage.setTo(JERRY);
		simpleMailMessage.setText(sendMsg);
		simpleMailMessage.setSubject(subject);
		mailSender.send(simpleMailMessage);
	}
	public static String modeId(String imgName){
		if(StringUtils.isBlank(imgName) || !imgName.contains(".jpg")){
			return null;
		}
		return imgName.substring(0, imgName.indexOf(".jpg"));
	}
	public static String getImgFolderPath(){
		String SEP = File.separator;
		String root = StartupWebAppInitializer.getUploadRoot();
		String imgFolder = StartupWebAppInitializer.getUploadsTempPath() + SEP + "img";
		return imgFolder;
	}
}
