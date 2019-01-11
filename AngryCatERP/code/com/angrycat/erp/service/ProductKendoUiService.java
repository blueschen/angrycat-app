package com.angrycat.erp.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.angrycat.erp.model.Product;
import com.angrycat.erp.service.magento.MagentoProductService;
import com.angrycat.erp.service.magento.MagentoProductService.StockInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
@Scope("prototype")
@Qualifier("productKendoUiService")
public class ProductKendoUiService extends KendoUiService<Product, Product> {
	private static final long serialVersionUID = 9088514750485237337L;
	@Autowired
	private MagentoProductService magentoProductService;
	@Autowired
	private MailService mailService;
	
	@Override
	List<?> deleteByIds(List<String> ids, Session s){
		List<Product> results = (List<Product>)super.deleteByIds(ids, s);
		// 應該不會發生刪除產品的情況，但為了一致性及方便，還是有設計連動Magento庫存的功能，但在這種情況下，只會把庫存改為0，庫存狀態改為缺貨，不會刪除商品資料
		results.stream().forEach(p->p.setTotalStockQty(0));
		asyncUpdateMagentoStock(results);
		return results;
	}
	@Override
	@Transactional
	public List<?> deleteByIds(List<String> ids){
		Session s = sfw.currentSession();
		List<?> results = deleteByIds(ids, s);
		return results;
	}
	
	/**
	 * 這個方法呼叫adjustProdStock驗證修改庫存的需求。<br>
	 * 如果部分資料無法通過檢查，<br>
	 * 整批資料都不會跟資料庫同步。<br>
	 * 為了設計上的簡便，<br>
	 * 一旦修改庫存被檢查出有問題，<br>
	 * 直接丟出例外，<br>
	 * 錯誤訊息可以讓前端轉換後直接輸出。
	 */
	@Override
	public List<Product> batchSaveOrMerge(List<Product> targets, BiFunction<Product, Session, Product> before, Session s){
		Map<String, Product> needToModifyStock = 
			targets.stream()
				.filter(p->isStockRelated(p.getWarning()) && StringUtils.isNotBlank(p.getId()))
				.collect(Collectors.toMap(Product::getId, Function.identity()));
		
		List<Product> olds = Collections.emptyList();
		
//		log("needToModifyStock:\n" + printJson(needToModifyStock)); 
		if(needToModifyStock.size() != 0){
			String q = "SELECT DISTINCT p FROM " + Product.class.getName() + " p WHERE p.id IN (:ids) ORDER BY p.id DESC";
			olds = 
				s.createQuery(q)
				.setParameterList("ids", needToModifyStock.keySet())
				.list();			
			olds.forEach(o->s.evict(o));
		}
			
		List<Product> filterOut = adjustProdStock(targets, needToModifyStock, olds);
		List<Product> results = Collections.emptyList();
		if(filterOut.size() == 0){
			results = super.batchSaveOrMerge(targets, before, s);
			
			asyncUpdateMagentoStock(targets);
		}else{
			String msg = "<h4>修改庫存狀態有誤:</h4>";
			String w = filterOut.stream().filter(p->StringUtils.isNotBlank(p.getWarning())).map(p->"<h4>" + p.getWarning() + "</h4>").collect(Collectors.joining());
			if(StringUtils.isNotBlank(w)){
				msg += w;
			}
			targets.addAll(filterOut);
			
			results = targets;
			// TODO 是否寄信給相關人士
			throw new RuntimeException(msg);
		}

		return results;
	}
	
	@Override
	@Transactional
	public List<Product> batchSaveOrMerge(List<Product> targets, BiFunction<Product, Session, Product> before){
		Session s = sfw.currentSession();
		List<Product> results = batchSaveOrMerge(targets, before, s);
		return results;
	}
	/**
	 * 編輯時將修改Magenot庫存<br>
	 * 如果兩者庫存有差異<br>
	 * 讓庫存與庫存表一致
	 * @param targets
	 */
	void asyncUpdateMagentoStock(List<Product> targets){
		List<Product> cs = new ArrayList<>();
		for(Product t : targets){
			Product c = new Product();
			cs.add(c);
			
			c.setModelId(t.getModelId());
			c.setTotalStockQty(t.getTotalStockQty());
			c.setTotalStockChangeNote(t.getTotalStockChangeNote());
		}
		CompletableFuture.supplyAsync(()->magentoProductService.updateStockIfDifferentFromMagento(cs))
			.exceptionally((ex)-> {
				String msg = "Contents:\n"; 
				msg += cs.stream().map(p->p.getModelId()+":"+p.getTotalStockChangeNote()).collect(Collectors.joining("\n"));				
				sendToAdmin("asyncUpdateMagentoStock Errors", msg + "error:\n" + ex); // TODO stacktrace formatted
				return null;
			});
//		System.out.println("asyncUpdateMagentoStock end...");	
	}
	/**
	 * 用庫存表所有商品去找<br>
	 * 如果兩邊都有該商品且Magento庫存比較多<br>
	 * 就讓Magento庫存與庫存表一致
	 */
	public void updateStockIfMagentoIsMore(){
		try{
			List<Product> all = genCondtitionsAfterExecuteQueryList().getResults();
			magentoProductService.updateStockIfMagentoIsMore(all);
		}catch(Throwable e){
			 // TODO stacktrace formatted
			sendToAdmin("updateStockIfMagentoIsMore asyncUpdateMagentoStock Errors Errors", e.toString());
		}
	}
	/**
	 * 產生庫存的診斷報告
	 * @return
	 */
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
			 // TODO stacktrace formatted
			sendToAdmin("generateStockReport asyncUpdateMagentoStock Errors Errors", e.toString());
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
	public static String genTotalStockChangeNote(String action, String title, int stockChanged, String stockType){
		if(stockChanged == 0){
			return null;
		}
		if(StringUtils.isBlank(stockType)){
			stockType = "總庫存";
		}
		List<String> template = new ArrayList<>();
		template.add(action);
		template.add(title);
		if(stockChanged > 0){
			template.add(stockType + "+" + stockChanged);
		}
		if(stockChanged < 0){
			template.add(stockType + stockChanged);
		}
		String note = StringUtils.join(template, "_");
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
		private String getCompareHTML(String title, List<StockInfo> infos){
			String headerTmp = "<h1><span>%s(%o筆)</span></h1>";
			String header = String.format(headerTmp, title, infos.size());
			if(infos.size() == 0){
				return header;
			}
			String trTmp = "<tr><td>%s</td><td>%s</td><td>%s</td></tr>";
			String tr = infos.stream()
				.map(si->String.format(trTmp, si.getSku(), si.getMagentoStockQty()+"", si.getTotalStockQty()+""))
				.collect(Collectors.joining());
			String tableTmp = "%s<table border='0' valign='top' ><tr><th style='width: 35%%;'>型號</th><th style='width: 35%%;'>購物網站</th><th style='width: 30%%;'>庫存表</th></tr>%s</table>";
			String table = String.format(tableTmp, header, tr);
			return table;
		}
		private String getOtherHTML(String title, int count){
			String otherTmp = "<h3><span>%s: %o</span></h3>";
			return String.format(otherTmp, title, count);
		}
		private String getDiffHTML(String title, Set<String> diff){
			String diffHeader = "<h3><span>%s有%o筆</span></h3>";
			String diffTr = "<tr><td>%s</td></tr>";
			String diffTable = "%s<table border='0' valign='top'><tr><th>型號</th></tr>%s</table>";
			
			String header = String.format(diffHeader, title, diff.size());
			String tr = diff.stream().map(s->String.format(diffTr, s)).collect(Collectors.joining());
			String table = String.format(diffTable, header, tr);
			return table;
		}
		public String toHtml(){
			StringBuffer sb = new StringBuffer();
		
			sb.append(getOtherHTML("庫存表商品數", totalCount));
			sb.append(getOtherHTML("購物網站商品數", magentoCount));
			sb.append(getOtherHTML("兩者商品皆存在數", intersection.size()));
			
			sb.append(getCompareHTML("庫存表庫存較多", totalMore));
			sb.append(getCompareHTML("購物網站庫存較多", magentoMore));
			sb.append(getCompareHTML("兩者庫存相等", twoEquals));
					
			sb.append(getDiffHTML("庫存表存在，購物網站沒有的型號", totalDiff));
			sb.append(getDiffHTML("購物網站存在，庫存表沒有的型號", magentoDiff));
			
			return sb.toString();
		}
	}
	void sendToAdmin(String subject, String content){
		mailService.subject(subject)
			.content(content)
			.sendSimple();
	}
	void sendHTMLToAdmin(String subject, String content){		
		mailService.subject(subject)
			.content(content)
			.sendHTML();
	}
		
	static final String ADD_TAOBAO = "taobao_+_";
	static final String SUBTRACT_TAOBAO = "taobao_-_";
	static final String ADD_TOTAL = "total_+_";
	static final String SUBTRACT_TOTAL = "total_-_";
	
	static boolean isStockRelated(String warning){
		if(StringUtils.isBlank(warning)){
			return false;
		}
		
		return warning.startsWith(ADD_TAOBAO)
			|| warning.startsWith(SUBTRACT_TAOBAO)
			|| warning.startsWith(ADD_TOTAL)
			|| warning.startsWith(SUBTRACT_TOTAL);
	}
	
	/**
	 * 
	 * @param targets
	 * @param needToModifyStock
	 * @param olds
	 * @return
	 */
	List<Product> adjustProdStock(List<Product> targets, Map<String, Product> needToModifyStock, List<Product> olds){			
		List<Product> filterOut = new ArrayList<>();
		for (Product old : olds){
			String id = old.getId();
			int oldTotalStockQty = old.getTotalStockQty();
			int oldTaobaoStockQty = old.getTaobaoStockQty();
				
			Product prod = needToModifyStock.get(id);
			int newTotalStockQty = prod.getTotalStockQty();
			int newTaobaoStockQty = prod.getTaobaoStockQty();
			
			String warning = prod.getWarning();
			String[] intentions = warning.split("_");
			String type = intentions[0];
			String add = intentions[1];
			int count = Integer.parseInt(intentions[2]);
				
			prod.setWarning(null);
				
			String msg = "增加";
			if(!"+".equals(add)){
				count = -count;
				msg = "減去";
			}
			
			String modelId = prod.getModelId();
			if(StringUtils.isBlank(modelId)){
				modelId = id;
			}
			msg += modelId;
			
			if("taobao".equals(type)){
				int diff = newTaobaoStockQty - oldTaobaoStockQty;
				msg += "淘寶庫存";
				msg += Math.abs(count);
				msg += ":";
				if(diff != count){
					prod.setWarning(msg+"淘寶庫存已先被異動");
					filterOut.add(targets.remove(targets.indexOf(prod)));
					continue;
				}
				if(newTotalStockQty != oldTotalStockQty){
					prod.setWarning(msg+"總庫存已先被異動");
					filterOut.add(targets.remove(targets.indexOf(prod)));
					continue;
				}
				// 連動總庫存
				if("+".equals(add)){
					if(newTaobaoStockQty > newTotalStockQty){
						prod.setWarning(msg+"淘寶庫存已大於總庫存");
						filterOut.add(targets.remove(targets.indexOf(prod)));
						continue;
					}
				}else{
					int totalStock = newTotalStockQty + count;
					if(totalStock < 0){
						prod.setWarning(msg+"總庫存跟著連動會小於0");
						filterOut.add(targets.remove(targets.indexOf(prod)));
						continue;
					}else{
						prod.setTotalStockQty(totalStock);
					}
				}
			}
				
			if("total".equals(type)){
				msg += "總庫存";
				msg += Math.abs(count);
				msg += ":";
				int diff = newTotalStockQty - oldTotalStockQty;
				if(diff != count){
					prod.setWarning(msg+"總庫存已先被異動");
					filterOut.add(targets.remove(targets.indexOf(prod)));
					continue;
				}
				if(!"+".equals(add)){ // 減總庫存
					if(newTotalStockQty < newTaobaoStockQty){
						String out = msg+"總庫存已小於淘寶庫存";
						prod.setWarning(out);
						filterOut.add(targets.remove(targets.indexOf(prod)));
						continue;
					}
				}
			}
		}
		return filterOut;
	}
	
	public static String printJson(Object obj){
		ObjectMapper om = new ObjectMapper();
		String json = "Writing Err...";
		try {
			json = om.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("ProductKendoUiService.printJson(Object) err...");
		}
		return json;
	}
	
	public static void log(String msg){
		System.out.println(msg);
	}
}
