package com.angrycat.erp.web.controller;

import static com.angrycat.erp.condition.ConditionFactory.putSqlDate;
import static com.angrycat.erp.condition.ConditionFactory.putStrCaseInsensitive;
import static com.angrycat.erp.condition.MatchMode.ANYWHERE;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.angrycat.erp.excel.ExcelExporter;
import com.angrycat.erp.model.ExamStatistics;
import com.angrycat.erp.security.User;
import com.angrycat.erp.web.component.ConditionConfig;

@Controller
@RequestMapping(value="/examstatistics")
@Scope("session")
public class ExamStatisticsController extends
		BaseQueryController<ExamStatistics, ExamStatistics> {
	private static final long serialVersionUID = 7788958095712499816L;
	
	@Override
	public void init(){
		super.init();
		
		queryBaseService
			.addWhere(putStrCaseInsensitive("p.examinee.userId LIKE :pUserId", ANYWHERE))
			.addWhere(putSqlDate("p.examDate >= :pExamDateStart"))
			.addWhere(putSqlDate("p.examDate <= :pExamDateEnd"))
	;
	}
	@Override
	Class<ExamStatistics> getRoot() {
		return ExamStatistics.class;
	}

	@Override
	<E extends ExcelExporter<ExamStatistics>> E getExcelExporter() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	String conditionConfigToJsonStr(Object obj){
		if(obj instanceof ConditionConfig){
			ConditionConfig<ExamStatistics> cc = (ConditionConfig<ExamStatistics>)obj;
			cc.getResults().forEach(es->{
				User user = es.getExaminee();
				user.setDefaultGroup(null);
				user.setGroups(null);
				user.setRoles(null);
			});
		}
		String result = super.conditionConfigToJsonStr(obj);
		return result;
	}

}
