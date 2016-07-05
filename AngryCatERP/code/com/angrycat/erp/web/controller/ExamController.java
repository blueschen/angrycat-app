package com.angrycat.erp.web.controller;

import static com.angrycat.erp.condition.ConditionFactory.putSqlDate;
import static com.angrycat.erp.condition.ConditionFactory.putStrCaseInsensitive;
import static com.angrycat.erp.condition.MatchMode.ANYWHERE;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Equator;
import org.apache.commons.collections4.functors.DefaultEquator;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.excel.ExcelExporter;
import com.angrycat.erp.excel.ExcelImporter;
import com.angrycat.erp.jackson.mixin.ExamIgnoreDetail;
import com.angrycat.erp.model.Exam;
import com.angrycat.erp.model.ExamItem;

@Controller
@RequestMapping(value="/exam")
@Scope("session")
public class ExamController extends BaseUpdateController<Exam, Exam> {
	private static final long serialVersionUID = 4151794885343478391L;
	
	@Override
	public void init(){
		super.init();
		
		queryBaseService
			.addWhere(putStrCaseInsensitive("p.description LIKE :pDescription", ANYWHERE))
			.addWhere(putStrCaseInsensitive("p.category LIKE :pCategory", ANYWHERE))
			.addWhere(putStrCaseInsensitive("p.hint LIKE :pHint", ANYWHERE))
			.addWhere(putSqlDate("p.createDate >= :pCreateDateStart"))
			.addWhere(putSqlDate("p.createDate <= :pCreateDateEnd"))
		;
		findTargetService
			.createAssociationAlias("left join fetch p.items", "details", null);
	}
	
	@Override
	public String conditionConfigToJsonStr(Object val){
		return examIgnoreDetail(val);
	}
	
	private String examIgnoreDetail(Object val){
		String result = CommonUtil.parseToJson(val, Exam.class, ExamIgnoreDetail.class);
		return result;
	}
	
	@Override
	@RequestMapping(
		value="/save",
		method=RequestMethod.POST,
		produces={"application/xml", "application/json"},
		headers="Accept=*/*")
	public @ResponseBody Exam saveOrMerge(@RequestBody Exam exam){
		sfw.executeSaveOrUpdate(s->{
			Exam oldSnapshot = null;
			if(StringUtils.isBlank(exam.getId())){ // add
				int itemCount = exam.getItems().size();
				if(itemCount > 0){
					List<ExamItem> items = exam.getItems();
					exam.setItems(new LinkedList<>());
					s.save(exam);
					s.flush();
					items.forEach(i->{
						if(StringUtils.isBlank(i.getExamId())){
							i.setExamId(exam.getId());
						}
					});
					exam.getItems().addAll(items);
				}
			}else{// update
				findTargetService.getSimpleExpressions().get("pId").setValue(exam.getId());
				List<Exam> exams = findTargetService.executeQueryList(s);
				if(!exams.isEmpty()){
					oldSnapshot = exams.get(0);
					s.evict(oldSnapshot);
					
					Exam sessionExam = findTargetService.executeQueryList(s).get(0);
					Collection<String> retaned = 
						CollectionUtils.retainAll(
							sessionExam.getItems(), 
							exam.getItems(), 
							idEquator())
								.stream()
								.map(ei->ei.getId())
								.collect(Collectors.toList());
					sessionExam.getItems().removeIf(ei->!retaned.contains(ei.getId()));
					s.saveOrUpdate(sessionExam);
					s.flush();
					s.evict(sessionExam);
				}
				
			}
			s.saveOrUpdate(exam);
			s.flush();
			if(oldSnapshot == null){
				dataChangeLogger.logAdd(exam, s);
			}else{
				dataChangeLogger.logUpdate(oldSnapshot, exam, s);
			}
			s.flush();
		});
		return exam;
	}
	
	static Equator<Object> idEquator(){
		return new Equator<Object>(){
			private String getId(Object obj){
				String id = null;
				try{
					String ID = "id";
					id = (String)PropertyUtils.getProperty(obj, ID);
				}catch(Throwable e){
					throw new RuntimeException(e);
				}
				return id;
			}
			@Override
			public boolean equate(Object t1, Object t2) {
				if(t1 == null
				|| t2 == null){
					return false;
				}
				String t1_id = getId(t1);
				String t2_id = getId(t2);
				if(StringUtils.isBlank(t1_id)
				|| StringUtils.isBlank(t2_id)
				|| !t1_id.equals(t2_id)){
					return false;
				}
				return true;
			}
			@Override
			public int hash(Object t) {
				if(t == null){
					return DefaultEquator.HASHCODE_NULL;
				}
				String id = getId(t);
				if(StringUtils.isBlank(id)){
					return 0;
				}
				return id.hashCode();
			}
		};
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
	Class<Exam> getRoot() {
		return Exam.class;
	}

	@Override
	<E extends ExcelExporter<Exam>> E getExcelExporter() {
		// TODO Auto-generated method stub
		return null;
	}

}
