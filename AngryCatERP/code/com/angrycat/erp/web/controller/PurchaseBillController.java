package com.angrycat.erp.web.controller;

import static com.angrycat.erp.condition.ConditionFactory.putSqlDate;
import static com.angrycat.erp.condition.ConditionFactory.putStrCaseInsensitive;
import static com.angrycat.erp.condition.MatchMode.ANYWHERE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.angrycat.erp.excel.ExcelExporter;
import com.angrycat.erp.excel.ExcelImporter;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.model.PurchaseBill;
import com.angrycat.erp.model.PurchaseBillDetail;
import com.angrycat.erp.service.ProductKendoUiService;
import com.angrycat.erp.service.SalesDetailKendoUiService;
import com.angrycat.erp.service.TimeService;
import com.angrycat.erp.web.component.ConditionConfig;

@Controller
@RequestMapping(value="/purchasebill")
@Scope("session")
public class PurchaseBillController extends
		BaseUpdateController<PurchaseBill, PurchaseBill> {
	private static final long serialVersionUID = -7769226656225855198L;
	@Autowired
	private TimeService timeService;
	@Autowired
	@Qualifier("productKendoUiService")
	private ProductKendoUiService productKendoUiService;
	
	@Override
	@PostConstruct
	public void init(){
		super.init();
		
		queryBaseService
			.addWhere(putStrCaseInsensitive("p.no LIKE :pNo", ANYWHERE))
			.addWhere(putSqlDate("p.arriveDate >= :pArriveDateStart"))
			.addWhere(putSqlDate("p.arriveDate <= :pArriveDateEnd"))
			.addWhere(putSqlDate("p.stockDate >= :pStockDateStart"))
			.addWhere(putSqlDate("p.stockDate <= :pStockDateEnd"))
			.addWhere(putStrCaseInsensitive("p.note LIKE :pNote", ANYWHERE))
			;
		
		findTargetService
			.createAssociationAlias("left join fetch p.purchaseBillDetails", "details", null);
	}
	@RequestMapping(value="/noDuplicated/{no}", method=RequestMethod.GET)
	public @ResponseBody Map<String, Boolean> noDuplicated(@PathVariable("no") String no){
		Map<String, Boolean> results = new HashMap<>();
		sfw.executeSession(s->{
			long count = 0;
			count = (long)s.createQuery("SELECT COUNT(m.id) FROM " + PurchaseBill.class.getName() + " m WHERE m.no = :no").setString("no", no).uniqueResult();
			results.put("isValid", count == 0);
		});
		return results;
	}
	@Override
	PurchaseBill saveOrMerge(PurchaseBill pb, Session s){
		// 入庫之後修改進貨單=>若需要再次入庫，得手動調整庫存
		// 入庫之後刪除進貨單或明細=>若需要再次入庫，得手動調整庫存
		if(pb.getPurchaseBillDetails() != null){
			Collections.reverse(pb.getPurchaseBillDetails());
		}
		PurchaseBill oldSnapshot = null;
			if(StringUtils.isBlank(pb.getId())){// add
				int detailCount = pb.getPurchaseBillDetails().size();
				if(detailCount > 0){
					List<PurchaseBillDetail> detail = pb.getPurchaseBillDetails();
					pb.setPurchaseBillDetails(new LinkedList<PurchaseBillDetail>());
					s.save(pb);
					s.flush();
					detail.stream().forEach(d->{
						d.setPurchaseBillId(pb.getId());
					});
					pb.getPurchaseBillDetails().addAll(detail);
				}
			}else{// update
				findTargetService.getSimpleExpressions().get("pId").setValue(pb.getId());
				List<PurchaseBill> pbs = findTargetService.executeQueryList(s);
				
				if(!pbs.isEmpty()){
					oldSnapshot = pbs.get(0);// old data detached
					s.evict(oldSnapshot);
					
					PurchaseBill sessionPurchaseBill = findTargetService.executeQueryList(s).get(0);
					Iterator<PurchaseBillDetail> details = sessionPurchaseBill.getPurchaseBillDetails().iterator();
					boolean deleted = false;
					while(details.hasNext()){// delete details in memory
						boolean deleting = true;
						PurchaseBillDetail detail = details.next(); // data in database 
						for(PurchaseBillDetail d : pb.getPurchaseBillDetails()){// data in memery (not in relation with session)
							if(detail.getId().equals(d.getId())){// if both existed, representing not deleted yet
								deleting = false;
								break;
							}
						}
						if(deleting){
							details.remove();
							deleted = true;
						}
					}
					if(deleted){// change database to really delete details
						s.saveOrUpdate(sessionPurchaseBill);
						s.flush();
					}
					s.evict(sessionPurchaseBill);
				}
			}
			s.saveOrUpdate(pb);// update pb, or add or update detail
			s.flush();
			if(oldSnapshot == null){
				dataChangeLogger.logAdd(pb, s);
			}else{
//				Collections.reverse(oldSnapshot.getPurchaseBillDetails());
//				Collections.reverse(pb.getPurchaseBillDetails());
				dataChangeLogger.logUpdate(oldSnapshot, pb, s);
			}
			s.flush();
			Collections.reverse(pb.getPurchaseBillDetails());
		return pb;
	}
	@RequestMapping(value="/toStock",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody PurchaseBill toStock(@RequestBody PurchaseBill target){
		if(target.getStockDate() != null
		|| target.getPurchaseBillDetails() == null
		|| target.getPurchaseBillDetails().isEmpty()){
			return target;
		}
		target.setStockDate(timeService.todayMidnight());
		sfw.executeSaveOrUpdate(s->{
			saveOrMerge(target, s);
			Map<String, Integer> stockAdded = target.getPurchaseBillDetails().stream().collect(Collectors.toMap(p->p.getModelId(), p->p.getCount()));
			String q = "SELECT p FROM " + Product.class.getName() + " p WHERE p.modelId IN (:modelIds)";
			List<Product> products = s.createQuery(q).setParameterList("modelIds", stockAdded.keySet()).list();
			s.clear();
			products.stream().forEach(p->{
				int oriStock = p.getTotalStockQty();
				int addedStock = stockAdded.get(p.getModelId());
				int newStock = oriStock + addedStock;
				p.setTotalStockQty(newStock);
				p.setTotalStockChangeNote(ProductKendoUiService.genTotalStockChangeNote(SalesDetailKendoUiService.ACTION_NEW, "進貨單"+target.getNo(), addedStock));
			});
			productKendoUiService.batchSaveOrMerge(products, null, s);
		});
		return target;
	}
	@RequestMapping(value="/queryProductAutocomplete",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody ConditionConfig<Product> queryProductAutocomplete(@RequestBody ConditionConfig<Product> conditionConfig){
		ConditionConfig<Product> result = productKendoUiService.findTargetPageable(conditionConfig);
		return result;
	}
	@Override
	<I extends ExcelImporter> I getExcelImporter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	String getTemplateFrom() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	Class<PurchaseBill> getRoot() {
		return PurchaseBill.class;
	}

	@Override
	<E extends ExcelExporter<PurchaseBill>> E getExcelExporter() {
		// TODO Auto-generated method stub
		return null;
	}

}
