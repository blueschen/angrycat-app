package com.angrycat.erp.service;

import static com.angrycat.erp.common.EmailContact.JERRY;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.angrycat.erp.genserial.GenSerialUtil;
import com.angrycat.erp.model.AmericanGroupBuy;
import com.angrycat.erp.model.AmericanGroupBuyOrderForm;
import com.angrycat.erp.service.AmericanGroupBuyCalculationService.AmericanGroupBuyCalculation;
@Service
@Scope("prototype")
public class AmericanGroupBuyOrderFormKendoUiService extends
		KendoUiService<AmericanGroupBuyOrderForm, AmericanGroupBuyOrderForm> {
	private static final long serialVersionUID = -2439784264540019976L;
	
	@Autowired
	private MailService mailService;
	@Autowired
	private AmericanGroupBuyCalculationService americanGroupBuyCalculationService;
	
	private AmericanGroupBuy americanGroupBuy;
	public AmericanGroupBuy getAmericanGroupBuy(){
		return this.americanGroupBuy;
	}
	public AmericanGroupBuyCalculationService getAmericanGroupBuyCalculationService(){
		return this.americanGroupBuyCalculationService;
	}
	public MailService getMailService(){
		return mailService;
	}
	@PostConstruct
	void init(){
		super.init();
		List<AmericanGroupBuy> results = 
				sfw.executeFindResults(s->{
					Query q = s.createQuery("FROM " + AmericanGroupBuy.class.getName() + " a order by a.id DESC");
					q.setMaxResults(1);
					q.setFirstResult(0);
				List<AmericanGroupBuy> r = q.list();
				return r;
			});
		int size = results.size();
		// TODO 如果沒有記錄或有效記錄超過一筆的處理方式??
		if(size == 1){
			americanGroupBuy = results.get(0);
			americanGroupBuyCalculationService.setAmericanGroupBuy(americanGroupBuy);
		}
		// TODO 以後要更改寄件者和收件者
		mailService.from(JERRY);
	}
	@Override
	@Transactional
	public List<AmericanGroupBuyOrderForm> batchSaveOrMerge(List<AmericanGroupBuyOrderForm> targets, BiFunction<AmericanGroupBuyOrderForm, Session, AmericanGroupBuyOrderForm> before){
		if(americanGroupBuy == null && americanGroupBuy.isOrderFormDisabled()){
			return targets;
		}
		// 在每一項訂購明細加入對應的活動名稱
		targets.stream().filter(t->StringUtils.isBlank(t.getActivity())).forEach(t->t.setActivity(americanGroupBuy.getActivity()));
		
		Optional<AmericanGroupBuyOrderForm> found = targets.stream().filter(t->StringUtils.isNotBlank(t.getSalesNo())).findFirst();
		// 有可能使用者已經儲存過，所以同一批可能有的是新增、有的是修改
		if(found.isPresent()){
			String salesNo = found.get().getSalesNo();
			targets.stream().filter(t->StringUtils.isBlank(t.getSalesNo())).forEach(t->t.setSalesNo(salesNo));
		}else{
			Session s = sfw.currentSession();
			String nextNo = GenSerialUtil.getNext(AmericanGroupBuyOrderForm.SALESNO_GENERATOR_ID);
			String newSalesNo =  americanGroupBuy.getNo() + nextNo + "-" + RandomStringUtils.randomAlphabetic(5);
			targets.forEach(t->t.setSalesNo(newSalesNo));
		}
		return super.batchSaveOrMerge(targets, before);
	}
	@Override
	@Transactional
	public List<?> deleteByIds(List<String> ids){
		if(americanGroupBuy == null && americanGroupBuy.isOrderFormDisabled()){
			return Collections.emptyList();
		}
		Session s = sfw.currentSession();
		List<?> saved = deleteByIds(ids, s);
		return saved;
	}
	private static StringBuffer appendTdAsRow(StringBuffer sb, String...items){
		String col = "<td>{item}</td>";
		int size = items.length;
		sb.append("<tr>");
		for(int i = 0; i < size; i++){
			String item = items[i];
			if(StringUtils.isBlank(item)){
				item = "";
			}
			sb.append(col.replace("{item}", item));
		}
		sb.append("</tr>\n");
		return sb;
	}
	private static StringBuffer appendGridRow(StringBuffer sb, Map<String, String> data){
		String row = "<span>{label}:</span><span>{value}</span><br>\n";
		for(Map.Entry<String, String> d : data.entrySet()){
			sb.append(row.replace("{label}", d.getKey()).replace("{value}", d.getValue()));
		}
		return sb;
	}
	public String genMailContent(List<AmericanGroupBuyOrderForm> results){
		if(americanGroupBuy == null || results.isEmpty()){
			return "";
		}
		StringBuffer content = new StringBuffer("<span>訂購項目</span><br>\n");
		content.append("<table>\n<tr><th>項目</th><th>類型</th><th>名稱</th><th>尺寸</th><th>型號</th><th>價格</th></tr>\n");
		for(int i = 0; i < results.size(); i++){
			AmericanGroupBuyOrderForm result = results.get(i);
			appendTdAsRow(content, (i+1)+"", result.getSalesType(), result.getProductName(), result.getSize(), result.getModelId(), result.getProductAmtUSD().toString());
		}
		content.append("</table><br>\n");
		
		americanGroupBuyCalculationService.setOrders(results);
		AmericanGroupBuyCalculation cal = americanGroupBuyCalculationService.calculateScaleToLastNonZero();
		
		Map<String, String> calculation = new LinkedHashMap<>();
		calculation.put("訂單號碼", results.get(0).getSalesNo());
		calculation.put("小計USD", cal.getSubAmtUSD().toString());
		calculation.put("折扣USD", cal.getDiscountUSD().toString());
		calculation.put("代購服務費NTD", cal.getServiceChargeNTD().toString());
		calculation.put("代購總金額NTD", results.get(0).getTotalAmtNTD()+""); // 理論上後端計算應與前端一致，但為求謹慎，此處以前端回傳結果為準 TODO 為了安全性，是否要以後端計算為準
		
		content = appendGridRow(content, calculation);
		return content.toString();
	}
	public void sendEmail(List<AmericanGroupBuyOrderForm> results){
		String to = results.get(0).getEmail();
		sendEmail(to, results);
	}
	public void sendEmail(String to, List<AmericanGroupBuyOrderForm> results){
		if(StringUtils.isBlank(to)){
			return;
		}
		String activity = americanGroupBuy.getActivity();
		String content = genMailContent(results);
		mailService
			.subject(activity + "訂單")
			.to(to)
			.content(content)
			.sendHTML();
//		CompletableFuture
//			.runAsync(()->mailService.sendHTML())
//			.exceptionally((ex)->{
//				// TODO
//				return null;
//			});
	}
}
