package com.angrycat.erp.service;

import static com.angrycat.erp.common.EmailContact.JERRY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.angrycat.erp.model.Product;
import com.angrycat.erp.service.magento.MagentoProductService;
@Service
@Scope("prototype")
@Qualifier("productKendoUiService")
public class ProductKendoUiService extends KendoUiService<Product, Product> {
	private static final long serialVersionUID = 9088514750485237337L;
	@Autowired
	private MagentoProductService magentoProductService;
	@Autowired
	private MailSender mailSender;
	@Autowired
	private SimpleMailMessage templateMessage;
	
	@Override
	@Transactional
	public List<?> deleteByIds(List<String> ids){
		List<Product> results = (List<Product>)super.deleteByIds(ids);
		// TODO 與Magento庫存非同步連動: 待其他功能完成後，再測試
//		Map<String, Object> params = 
//			results
//				.stream()
//				.map(r->{r.setTotalStockQty(0); return r;}) // 把所有庫存歸0
//				.collect(Collectors.toMap(p->p.getModelId(), p->p.getTotalStockQty()));
//		asyncUpdateMagentoStock(params);
		return results;
	}
	
	@Override
	@Transactional
	public List<Product> batchSaveOrMerge(List<Product> targets, BiFunction<Product, Session, Product> before){
		Session s = sfw.currentSession();
		List<Product> results = super.batchSaveOrMerge(targets, before, s);
		// TODO 與Magento庫存非同步連動: 待其他功能完成後，再測試
//		Map<String, Object> params = 
//			results
//				.stream()
//				.collect(Collectors.toMap(p->p.getModelId(), p->p.getTotalStockQty()));
//		asyncUpdateMagentoStock(params);
		return results;
	}
	private void asyncUpdateMagentoStock(Map<String, Object> params){
		CompletableFuture.supplyAsync(()->magentoProductService.updateInventoryByProductId(params))
			.exceptionally((ex)-> {
				String msg = "Contents:\n"; 
				msg += params.keySet().stream().map(k->k+":"+params.get(k)).collect(Collectors.joining("\n"));
				SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
				simpleMailMessage.setTo(JERRY);
				simpleMailMessage.setText(msg);
				simpleMailMessage.setSubject("Update Magento Errors");
				mailSender.send(simpleMailMessage);
				return null;
			});
			
	}
	/**
	 * 測試非同步
	 */
	private void mockAsyncRequest(){
		CompletableFuture.supplyAsync(()->{
			try{
				Thread.sleep(10000);
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
			return "mock async request successful";
		}).exceptionally((e)->{
			return "executing mockAsyncRequest errors:\n" + e;
		})
		.thenAccept(retMsg->{
			System.out.println("mockAsyncRequest:\n" + retMsg);
		});
		System.out.println("main thread keeps executing");
	}
	public static String genTotalStockChangeNote(String action, String title, int stockChanged){
		if(stockChanged == 0){
			return null;
		}
		List<String> template = new ArrayList<>();
		template.add(action);
		template.add(title);
		if(stockChanged > 0){
			template.add("總庫存+"+stockChanged);
		}
		if(stockChanged < 0){
			template.add("總庫存"+stockChanged);
		}
		String note = "產生自:"+StringUtils.join(template, "_");
		return note;
	}
}
