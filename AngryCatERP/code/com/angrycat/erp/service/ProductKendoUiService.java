package com.angrycat.erp.service;

import static com.angrycat.erp.common.EmailContact.JERRY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.angrycat.erp.service.magento.MagentoProductService.StockInfo;
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
	List<?> deleteByIds(List<String> ids, Session s){
		List<Product> results = (List<Product>)super.deleteByIds(ids, s);
		// TODO 與Magento庫存非同步連動: 待其他功能完成後，再測試
		// 應該不會發生刪除產品的情況，但為了一致性及方便，還是有設計連動Magento庫存的功能，但在這種情況下，只會把庫存改為0，庫存狀態改為缺貨，不會刪除商品資料
//		results.stream().forEach(p->p.setTotalStockQty(0));
//		asyncUpdateMagentoStock(results);
		return results;
	}
	@Override
	@Transactional
	public List<?> deleteByIds(List<String> ids){
		Session s = sfw.currentSession();
		List<?> results = deleteByIds(ids, s);
		return results;
	}	
	@Override
	public List<Product> batchSaveOrMerge(List<Product> targets, BiFunction<Product, Session, Product> before, Session s){
		List<Product> results = super.batchSaveOrMerge(targets, before, s);
		// TODO 與Magento庫存非同步連動: 待其他功能完成後，再測試
//		asyncUpdateMagentoStock(targets);
		return results;
	}	
	@Override
	@Transactional
	public List<Product> batchSaveOrMerge(List<Product> targets, BiFunction<Product, Session, Product> before){
		Session s = sfw.currentSession();
		List<Product> results = batchSaveOrMerge(targets, before, s);
		return results;
	}
	private void asyncUpdateMagentoStock(List<Product> targets){
		CompletableFuture.supplyAsync(()->magentoProductService.updateStockIfDifferentFromMagento(targets))
			.exceptionally((ex)-> {
				String msg = "Contents:\n"; 
				msg += targets.stream().map(p->p.getModelId()+":"+p.getTotalStockQty()).collect(Collectors.joining("\n"));
				SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
				simpleMailMessage.setTo(JERRY);
				simpleMailMessage.setText(msg + "error:\n" + ex);
				simpleMailMessage.setSubject("asyncUpdateMagentoStock Errors");
				mailSender.send(simpleMailMessage);
				return null;
			});
			
	}
	public void adjustMagentoStockConformed(){
		try{
			List<Product> all = genCondtitionsAfterExecuteQueryList().getResults();
			magentoProductService.updateStockIfMagentoIsMore(all);
		}catch(Throwable e){
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
			simpleMailMessage.setTo(JERRY);
			simpleMailMessage.setText(e.toString()); // TODO stacktrace formatted
			simpleMailMessage.setSubject("asyncUpdateMagentoStock Errors Errors");
		}
	}
	public ProductStockReport generateStockReport(){
		ProductStockReport report = new ProductStockReport();
		try{
			Map<String, Integer> magentoStocks = magentoProductService.listAllInventory()
				.toMap(
					k->k.findValue("sku").textValue(), 
					v->Double.valueOf(v.findValue("qty").textValue()).intValue());
			Set<String> magentoSkus = magentoStocks.keySet();
			
			List<Product> all = genCondtitionsAfterExecuteQueryList().getResults();
			Map<String, Integer> totalStocks = all.stream().collect(Collectors.toMap(Product::getModelId, Product::getTotalStockQty));
			
			Set<String> totalModelIds = totalStocks.keySet();
			Set<String> intersection = new HashSet<>(magentoSkus);
			intersection.retainAll(totalModelIds);
			
			List<StockInfo> intersects = 
			intersection.stream().map(modelId->{
				int magentoStock = magentoStocks.get(modelId);
				int totalStock = totalStocks.get(modelId);
				return new StockInfo(modelId, magentoStock, totalStock);
			}).collect(Collectors.toList());
			
			Set<String> totalDiff = new HashSet<>(totalModelIds);
			totalDiff.removeAll(intersection);
			
			Set<String> magentoDiff = new HashSet<>(magentoSkus);
			magentoDiff.removeAll(intersection);
			
			report.setTotalCount(all.size());
			report.setMagentoCount(magentoSkus.size());
			report.setIntersection(intersects);
			report.setTotalDiff(totalDiff);
			report.setMagentoDiff(magentoDiff);
		}catch(Throwable e){
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage(templateMessage);
			simpleMailMessage.setTo(JERRY);
			simpleMailMessage.setText(e.toString()); // TODO stacktrace formatted
			simpleMailMessage.setSubject("asyncUpdateMagentoStock Errors Errors");
		}
		return report;
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
	MagentoProductService getMagentoProductService(){
		return magentoProductService;
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
	public class ProductStockReport{
		private int totalCount;
		private int magentoCount;
		private List<StockInfo> intersection = Collections.emptyList();
		private List<StockInfo> totalMore = Collections.emptyList();
		private List<StockInfo> magentoMore = Collections.emptyList();
		private List<StockInfo> twoEquals = Collections.emptyList();
		private Set<String> totalDiff = Collections.emptySet();
		private Set<String> magentoDiff = Collections.emptySet();
		public int getTotalCount() {
			return totalCount;
		}
		public void setTotalCount(int totalCount) {
			this.totalCount = totalCount;
		}
		public int getMagentoCount() {
			return magentoCount;
		}
		public void setMagentoCount(int magentoCount) {
			this.magentoCount = magentoCount;
		}
		public List<StockInfo> getIntersection() {
			return intersection;
		}
		public void setIntersection(List<StockInfo> intersection) {
			this.intersection = intersection;
			
			totalMore = intersection.stream().filter(si->si.getTotalStockQty() > si.getMagentoStockQty()).collect(Collectors.toList());
			magentoMore = intersection.stream().filter(si->si.getTotalStockQty() < si.getMagentoStockQty()).collect(Collectors.toList());
			twoEquals = intersection.stream().filter(si->si.getTotalStockQty() == si.getMagentoStockQty()).collect(Collectors.toList());
		}
		public Set<String> getTotalDiff() {
			return totalDiff;
		}
		public void setTotalDiff(Set<String> totalDiff) {
			this.totalDiff = totalDiff;
		}
		public Set<String> getMagentoDiff() {
			return magentoDiff;
		}
		public void setMagentoDiff(Set<String> magentoDiff) {
			this.magentoDiff = magentoDiff;
		}
		public List<StockInfo> getTotalMore() {
			return totalMore;
		}
		public List<StockInfo> getMagentoMore() {
			return magentoMore;
		}
		public List<StockInfo> getTwoEquals() {
			return twoEquals;
		}
		public void printToConsole(){
			StringBuffer sb = new StringBuffer();
			sb.append("庫存表商品數: " + totalCount + "\n");
			sb.append("Magento商品數: " + magentoCount + "\n");
			sb.append("兩者商品皆存在數:" + intersection.size() + "\n");
			
			sb.append("庫存表庫存較多:" + totalMore.size() + "\n");
			if(!totalMore.isEmpty()){
				sb.append("型號----庫存表----Magento----\n");
				totalMore.stream().forEach(si->{
					sb.append(si.getSku() + "----" + si.getTotalStockQty() + "----" + si.getMagentoStockQty() + "----\n");
				});
			}
			
			sb.append("Magento庫存較多:" + magentoMore.size() + "\n");
			if(!magentoMore.isEmpty()){
				sb.append("型號----庫存表----Magento----\n");
				magentoMore.stream().forEach(si->{
					sb.append(si.getSku() + "----" + si.getTotalStockQty() + "----" + si.getMagentoStockQty() + "----\n");
				});
			}
			
			sb.append("兩者庫存相等:" + twoEquals.size() + "\n");
			if(!twoEquals.isEmpty()){
				sb.append("型號----庫存表----Magento----\n");
				twoEquals.stream().forEach(si->{
					sb.append(si.getSku() + "----" + si.getTotalStockQty() + "----" + si.getMagentoStockQty() + "----\n");
				});
			}

			sb.append("庫存表存在，Magento沒有的型號有"+ totalDiff.size() +"筆: " + totalDiff + "\n");
			sb.append("Magento存在，庫存表沒有的型號有"+ magentoDiff.size() +"筆: " + magentoDiff);
			System.out.println(sb.toString());
		}
		// TODO
		public String toHtml(){
			
			return "NOT implemented";
		}
	}
}
