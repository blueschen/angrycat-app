package com.angrycat.erp.web.controller;

import static com.angrycat.erp.condition.ConditionFactory.putSqlDate;
import static com.angrycat.erp.condition.ConditionFactory.putStrCaseInsensitive;
import static com.angrycat.erp.condition.MatchMode.ANYWHERE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.angrycat.erp.service.ProductKendoUiService;
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
			super.saveOrMerge(target, s);
			List<String> modelIds = target.getPurchaseBillDetails().stream().map(p->p.getModelId()).collect(Collectors.toList());
			String q = "SELECT p FROM " + Product.class.getName() + " p WHERE p.modelId IN (:modelIds)";
			List<Product> products = s.createQuery(q).setParameterList("modelIds", modelIds).list();
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
