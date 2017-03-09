package com.angrycat.erp.service;

import static com.angrycat.erp.common.DatetimeUtil.DF_yyyyMMdd_DASHED;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UnknownFormatConversionException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.log.DataChangeLogger;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.ModuleConfig;
import com.angrycat.erp.model.Parameter;
import com.angrycat.erp.query.QueryScrollable;
import com.angrycat.erp.sql.ISqlNode;
import com.angrycat.erp.sql.ISqlRoot;
import com.angrycat.erp.sql.Join;
import com.angrycat.erp.sql.OrderBy;
import com.angrycat.erp.sql.SqlRoot;
import com.angrycat.erp.sql.SqlTarget;
import com.angrycat.erp.sql.Where;
import com.angrycat.erp.sql.condition.CollectConds;
import com.angrycat.erp.sql.condition.ISqlCondition;
import com.angrycat.erp.sql.condition.SimpleCondition;
import com.angrycat.erp.sql.condition.SqlCondition.Junction;
import com.angrycat.erp.sql.condition.StrCondition.MatchMode;
import com.angrycat.erp.test.BaseTest;
import com.angrycat.erp.web.WebUtils;
import com.angrycat.erp.web.component.ConditionConfig;

@Service
@Scope("prototype")
public class KendoUiService<T, R> implements Serializable{

	private static final long serialVersionUID = 5612145044684815434L;
	
	private static final String SIMPLE_CONDITION_PREFIX = "cond_";
	private static final String CURRENT_PAGE			= "currentPage";
	private static final String COUNT_PER_PAGE			= "countPerPage";
	private static final String KENDO_UI_GRID_FILTER	= "filter";
	public static final String KENDO_UI_DATA	= "kendoData";
	private static final String GROUP_AS_KENDO_UI_FILTER = "GROUP_AS_KENDO_UI_FILTER";
	
	@Autowired
	private ExecutableQuery<T> q;
	@Autowired
	SessionFactoryWrapper sfw;
	@Autowired
	private ModelPropertyService modelPropertyService;
	@Autowired
	protected DataChangeLogger dataChangeLogger;
	
	private int filterCount;
	private String alias;
	private SqlTarget target;
	private Map<String, String> filterFieldConverter = Collections.emptyMap(); // 將前端回傳的field，轉成適當或預期的名字，讓hql可以正常執行；譬如member->member.name
	private Map<String, Class<?>> customDeclaredFieldTypes = Collections.emptyMap(); // 自定義field的型別，一般來說應該不需要
	
	@PostConstruct
	void init(){
		Type superClz = getClass().getGenericSuperclass();
		if(superClz != null && superClz instanceof ParameterizedType){
			@SuppressWarnings("unchecked")
			Class<T> genericType = (Class<T>)((ParameterizedType)superClz).getActualTypeArguments()[0];
			q.getSqlRoot()
				.select()
					.target("p").getRoot()
				.from()
					.target(genericType, "p");
		}
	}
	
	public ConditionConfig<T> copyToConditionConfig(){
		ConditionConfig<T> cc = new ConditionConfig<T>();
		Map<String, Object> conds = cc.getConds();
		// simpleExpression
		getSqlRootImpl().findSimpleConditions()
		.forEach((s)->{
			conds.put(SIMPLE_CONDITION_PREFIX + s.getId(), s.getValue());
		});
		// paging
		conds.put(CURRENT_PAGE, q.getCurrentPage());
		conds.put(COUNT_PER_PAGE, q.getCountPerPage());
		if(q.getPageNavigator()!=null){
			cc.setPageNavigator(q.getPageNavigator());
		}
		// sorting
//		conds.put(ORDER_TYPE, null);
		return cc;
	}
	
	public void copyFromConditionConfig(ConditionConfig<T> conds){
		SqlRoot root = getSqlRootImpl();
		Map<String, Object> all = conds.getConds();
		// predefined conditions populate value
		all.keySet().stream().filter(k->k.startsWith(SIMPLE_CONDITION_PREFIX)).forEach(k->{
			String id = k.replace(SIMPLE_CONDITION_PREFIX, "");
			List<ISqlNode> founds = root.findNodeById(id).getFounds();
			if(founds.size() == 1){
				SimpleCondition s = (SimpleCondition)founds.get(0);
				addValToSimpleCondition(s, all.get(k));
			}
		});
		
		@SuppressWarnings("unchecked")
		Map<String, Object> kendoData = (Map<String, Object>)all.get(KENDO_UI_DATA);		
		if(kendoData != null){
			HttpSession session = getCurrentHttpSession();
			if(session != null){
				if(all.get("moduleName")!=null){
					String moduleName = (String)all.get("moduleName");
					String kendoDataJson = CommonUtil.parseToJson(kendoData); 
					session.setAttribute(moduleName + "KendoData", kendoDataJson); // 前端使用，代表最後一次的查詢條件
					session.setAttribute(moduleName + "KendoDataPojo", kendoData); // 後端使用，代表最後一次的查詢條件
					if(all.get("selectedCondition")!=null){// 前後端使用，代表最後一次選擇的條件設定範圍
						Object selectedCondition = all.get("selectedCondition");
						String json = CommonUtil.parseToJson(selectedCondition);
						session.setAttribute(moduleName + "SelectedCondition", json);
						System.out.println("add Session selecedCondition: " + json);
					}
				}
			}

			// kendo ui filter conditions
			Object filter = kendoData.get(KENDO_UI_GRID_FILTER);
			// remove kendo ui filter conditions
			root.find(n->(n instanceof ISqlCondition && (GROUP_AS_KENDO_UI_FILTER.equals(((ISqlCondition)n).getGroupMark())))).remove();
			if(null != filter){
				adjustConditionByKendoUIGridFilter(filter);
			}
			
			// paging configuration
			Integer currentPage = getInteger(kendoData.get("page"));
			Integer countPerPage = getInteger(kendoData.get("pageSize"));
			if(null != currentPage){
				q.setCurrentPage(currentPage);
			}
			if(null != countPerPage){
				q.setCountPerPage(countPerPage);
			}
		
			// order by configuration
			@SuppressWarnings("unchecked")
			List<Map<String, String>> orderTypes = (List<Map<String, String>>)kendoData.get("sort");
			OrderBy orderBy = root.find(OrderBy.class);
			if(null == orderBy){
				orderBy = root.orderBy();
			}
			orderBy.getChildren().clear();
			if(null != orderTypes){
				for(int i = 0; i < orderTypes.size(); i++){
					Map<String, String> orderType = orderTypes.get(i); // Kendo UI Grid排序回傳的資料結構 
					String field = orderType.get("field");
					String dir = orderType.get("dir");
					String aliasField = convertFilterFieldStartsWithAlias(field);
					if("asc".equals(dir)){
						orderBy.asc(aliasField);
					}else if("desc".equals(dir)){
						orderBy.desc(aliasField);
					}
				}
			}
			
		}
	}
	public List<T> batchSaveOrMerge(List<T> targets, BiFunction<T, Session, T> before, Session s){
		int batchSize = sfw.getBatchSize();
		int count = 0;
		
		for(int i = 0; i < targets.size(); i++){
			T target = targets.get(i);
			T oldSnapshot = null;
			String pk = null;
			try{
				Object propVal = PropertyUtils.getProperty(target, q.getIdFieldName());
				if(null != propVal && propVal.getClass() == String.class){
					pk = (String)propVal;
				}
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
			if(before != null){
				target = before.apply(target, s);
			}
			if(StringUtils.isBlank(pk)){
				s.save(target);
			}else{
				oldSnapshot  = (T)s.createQuery("SELECT DISTINCT p FROM " + target.getClass().getName() + " p WHERE p." + q.getIdFieldName() + " = :pk").setString("pk", pk).uniqueResult();
				s.evict(oldSnapshot);
				s.update(target);
			}
			dataChangeLogger.setUser(WebUtils.getSessionUser());
			if(oldSnapshot == null){
				dataChangeLogger.logAdd(target, s);
			}else{
				dataChangeLogger.logUpdate(oldSnapshot, target, s);
			}
			if(++count % batchSize == 0){
				s.flush();
				s.clear();
			}
		}
		s.flush();
		s.clear();
		
		return targets;
	}
	@Transactional
	public List<T> batchSaveOrMerge(List<T> targets, BiFunction<T, Session, T> before){
		Session s = sfw.currentSession();
		batchSaveOrMerge(targets, before, s);
		return targets;
	}
	List<?> deleteByIds(List<String> ids, Session s){
		String queryHql = "SELECT DISTINCT p FROM " + q.findFirstSqlTarget().getTargetClass().getName() + " p WHERE p."+ q.getIdFieldName() +" IN (:ids)";
		ScrollableResults results = s.createQuery(queryHql).setParameterList("ids", ids).scroll(ScrollMode.FORWARD_ONLY);
		List<Object> saved = new ArrayList<>();
		while(results.next()){
			Object target = results.get()[0];
			s.evict(target);
			saved.add(target);
			dataChangeLogger.setUser(WebUtils.getSessionUser());
			dataChangeLogger.logDelete(target, s);
			s.delete(target);
		}
		s.flush();
		s.clear();
		return saved;
	}
	@Transactional
	public List<?> deleteByIds(List<String> ids){
		Session s = sfw.currentSession();
		List<?> saved = deleteByIds(ids, s);
		return saved;
	}
	
	@Transactional
	public void saveModuleConfig(ModuleConfig config){
		Session s = sfw.currentSession();
		s.save(config);
		s.flush();
		s.clear();
	}
	
	@Transactional
	public void deleteModuleConfigs(List<String> configIds){
		String deleteHql = "DELETE FROM " + ModuleConfig.class.getName() + " m WHERE m.id IN (:configIds)";
		Session s = sfw.currentSession();
		s.createQuery(deleteHql).setParameterList("configIds", configIds).executeUpdate();
		s.flush();
		s.clear();
	}
	
	public List<Map<String, Object>> listModuleConfigs(String moduleName){
		List<ModuleConfig> results = sfw.executeFindResults(s->{
			String queryHql = "SELECT DISTINCT p FROM " + ModuleConfig.class.getName() + " p WHERE p.moduleName = :moduleName";
			List<ModuleConfig> moduleConfigs = s.createQuery(queryHql).setString("moduleName", moduleName).list();
			return moduleConfigs;
		});
		
		List<Map<String, Object>> transformed = 
				results.stream().map(config->{
				Map<String, Object> result = moduleConfigToMap(config);
				return result;
			}).collect(Collectors.toList());
		
		return transformed;
	}
	
	public Map<String, List<Parameter>> listParameters(List<String> catNames){
		List<Parameter> founds = sfw.executeFindResults(s->{
			String queryHql = "SELECT DISTINCT p FROM " + Parameter.class.getName() + " p WHERE p.parameterCategory.name IN (:catNames)";
			List<Parameter> params = s.createQuery(queryHql).setParameterList("catNames", catNames).list();
			return params;
		});
		
		Map<String, List<Parameter>> results = new LinkedHashMap<>();
		founds.forEach(p->{
			List<Parameter> subset = results.get(p.getParameterCategory().getName());
			if(subset == null){
				subset = new ArrayList<>();
				results.put(p.getParameterCategory().getName(), subset);
			}
			subset.add(p);
		});
		return results;
	}
	
	public static Map<String, Object> moduleConfigToMap(ModuleConfig config){
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("id", config.getId());
		result.put("name", config.getName());
		Map<?, ?> json = CommonUtil.parseJsonStrToMap(config.getJson());
		result.put("json", json);
		return result;
	}
	
	private void adjustConditionByKendoUIGridFilter(Object filterObj){		
		SqlRoot root = getSqlRootImpl();
		Where where = root.find(Where.class);
		if(null == where){
			where = root.where();
		}
		
		CollectConds conds = null;
		List<ISqlNode> children = where.getChildren();
		if(!children.isEmpty()){
			ISqlNode lastChild = children.get(children.size()-1);
			CollectConds last = (CollectConds)lastChild;
			if(last.getJunction() == Junction.AND){
				conds = last;
			}
		}
		
		if(conds == null){
			conds = where.andConds();
		}
		
		@SuppressWarnings("unchecked")
		Map<String, Object> filter = (Map<String, Object>)filterObj;
		String logic = (String)filter.get("logic");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> filters = (List<Map<String, Object>>)filter.get("filters");
		
		filterCount = 0;
		addFilterCondtions(filters, conds, logic);
	}
	
	private void addFilterCondtions(List<Map<String, Object>> filters, CollectConds parent, String ParentLogic){
		parent.enableGroupMark(GROUP_AS_KENDO_UI_FILTER);
		for(int i = 0; i < filters.size(); i++){
			Map<String, Object> filter = filters.get(i);
			String logic = (String)filter.get("logic");
			if(StringUtils.isNotBlank(logic)){
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> f= (List<Map<String, Object>>)filter.get("filters");
				addFilterCondtions(f, parent.andCollectConds(), logic);
			}else{
				addFilterCondition(filter, parent, ParentLogic);
			}
		}
		parent.disableGroupMark();
	}
	
	private void addFilterCondition(Map<String, Object> f, CollectConds conds, String logic){
		String operator = (String)f.get("operator");
		String field = (String)f.get("field");
		field = convertFilterField(field);
		Object value = f.get("value");
		
		filterCount++;
		
		String alias = getAlias();
		String expression = convertFilterFieldStartsWithAlias(field) + " ";
		String nameParam = " :" + alias + firstLetterToUpperCase(field) + "_FILTER_" + filterCount;
		MatchMode matchMode = null;
		Object convertedVal = convertValueByType(field, value);
		switch(operator){
			case "isnull":
				expression += "IS NULL";
				addStatement(conds, expression, logic);
				break;
			case "isnotnull":
				expression += "IS NOT NULL";
				addStatement(conds, expression, logic);
				break;
			case "isempty":
				expression += "IS EMPTY";
				addStatement(conds, expression, logic);
				break;
			case "isnotempty":
				expression += "IS NOT EMPTY";
				addStatement(conds, expression, logic);
				break;
				
			case "eq":
				expression += ("=" + nameParam);
				addSimpleCond(conds, expression, logic, value, convertedVal);
				break;						
			case "neq":
				expression += ("!=" + nameParam);
				addSimpleCond(conds, expression, logic, value, convertedVal);
				break;						
			case "gte":
				expression += (">=" + nameParam);
				addSimpleCond(conds, expression, logic, value, convertedVal);
				break;						
			case "gt":
				expression += (">" + nameParam);
				addSimpleCond(conds, expression, logic, value, convertedVal);
				break;						
			case "lte":
				expression += ("<=" + nameParam);
				addSimpleCond(conds, expression, logic, value, convertedVal);
				break;						
			case "lt":
				expression += ("<" + nameParam);
				addSimpleCond(conds, expression, logic, value, convertedVal);
				break;
				
			case "startswith":
				matchMode = MatchMode.START;
				expression += ("LIKE" + nameParam);
				addStrCond(conds, expression, value, matchMode, logic);
				break;						
			case "endswith":
				matchMode = MatchMode.END;
				expression += ("LIKE" + nameParam);
				addStrCond(conds, expression, value, matchMode, logic);						
				break;						
			case "contains":
				matchMode = MatchMode.ANYWHERE;
				expression += ("LIKE" + nameParam);
				addStrCond(conds, expression, value, matchMode, logic);						
				break;
			case "doesnotcontain":
				matchMode = MatchMode.ANYWHERE;
				expression += ("NOT LIKE" + nameParam);
				addStrCond(conds, expression, value, matchMode, logic);
				break;
		}
	}
	// 這裡直接加入不管大小寫的邏輯
	private void addStrCond(CollectConds conds, String expression, Object value, MatchMode matchMode, String logic){
		if("and".equals(logic)){
			conds.andStrToUpperCase(expression, matchMode, (String)value);
		}else{
			conds.orStrToUpperCase(expression, matchMode, (String)value);
		}
	}
	
	private void addStatement(CollectConds conds, String expression, String logic){
		if("and".equals(logic)){
			conds.andStatement(expression);
		}else{
			conds.orStatement(expression);
		}
	}
	
	private void addSimpleCond(CollectConds conds, String expression, String logic, Object value, Object convertedVal){
		if("and".equals(logic)){
			conds.andSimpleCond(expression, value.getClass(), convertedVal);
		}else{
			conds.orSimpleCond(expression, value.getClass(), convertedVal);
		}
	}
	
	/**
	 * 必要時轉換Kendo UI所提供的fieldName，如果沒有轉換就輸出原來的值
	 * @param fieldName
	 * @return
	 */
	private String convertFilterField(String fieldName){
		if(filterFieldConverter.containsKey(fieldName)){
			return filterFieldConverter.get(fieldName);
		}
		return fieldName;
	}
	/**
	 * 如果需要加上alias就加，如果本身已經含alias(譬如屬於join的條件)，就略過
	 * @param fieldName
	 * @return
	 */
	private String convertFilterFieldStartsWithAlias(String fieldName){
		String field = convertFilterField(fieldName);
		
		SqlRoot root = getSqlRootImpl();
		List<Join> joins = root.findMultiple(Join.class);
		boolean isStartsWithJoin = joins.stream().anyMatch(j->field.startsWith(j.getAlias()+"."));
		
		String alias = getAlias();
		String expression = isStartsWithJoin ? field : (alias + "." + field);
		
		return expression;
	}
	private Integer getInteger(Object val){
		if(val == null){
			return null;
		}
		if(val instanceof String){
			return Integer.parseInt((String)val);
		}
		if(val instanceof Integer){
			return (Integer)val;
		}
		return null;
	}
	
	public ConditionConfig<T> findTargetPageable(ConditionConfig<T> conditionConfig){
		ConditionConfig<T> cc = executeQueryPageable(conditionConfig);
		return cc;
	}
	
	public ConditionConfig<T> executeQueryPageable(ConditionConfig<T> conditionConfig){
		if(conditionConfig != null){
			copyFromConditionConfig(conditionConfig);
		}
		return genCondtitionsAfterExecuteQueryPageable();
	}
	
	public ConditionConfig<T> genCondtitionsAfterExecuteQueryPageable(){
		List<T> results = q.executeQueryPageable();
		ConditionConfig<T> cc = copyToConditionConfig();
		cc.setResults(results);
		return cc;
	}
	
	public ConditionConfig<T> findTargetList(ConditionConfig<T> conditionConfig){
		ConditionConfig<T> cc = executeQueryList(conditionConfig);
		return cc;
	}
	
	public ConditionConfig<T> executeQueryList(ConditionConfig<T> conditionConfig){
		if(conditionConfig != null){
			copyFromConditionConfig(conditionConfig);
		}
		return genCondtitionsAfterExecuteQueryList();
	}
	
	public ConditionConfig<T> genCondtitionsAfterExecuteQueryList(){
		List<T> results = q.executeQueryList();
		ConditionConfig<T> cc = copyToConditionConfig();
		cc.setResults(results);
		return cc;
	}	
	public QueryScrollable executeQueryScrollable(ConditionConfig<T> conditionConfig){
		if(conditionConfig != null){
			copyFromConditionConfig(conditionConfig);
		}
		return q;
	}
	
	public ISqlRoot getSqlRoot(){
		return q.getSqlRoot();
	}
	
	private SqlRoot getSqlRootImpl(){
		return (SqlRoot)getSqlRoot();
	}
	
	public void setFilterFieldConverter(Map<String, String> filterFieldConverter){
		this.filterFieldConverter = filterFieldConverter;
	}
	
	public void setCustomDeclaredFieldTypes(Map<String, Class<?>> customDeclaredFieldTypes){
		this.customDeclaredFieldTypes = customDeclaredFieldTypes;
	}
	
	private void addValToSimpleCondition(SimpleCondition s, Object obj){
		if(obj == null){
			return;
		}
//		Class<?> type = s.getType();
		String field = s.getPropertyName().replace(getFirstSqlTarget().getAlias() + ".", "");
		Object casted = convertValueByType(field, obj);
		s.value(casted);
	}
	
	private Object convertValueByType(String field, Object obj){
		Class<?> type = this.customDeclaredFieldTypes.get(field);
		if(type == null){
			type = this.modelPropertyService.getModelPropertyTypes().get(getFirstSqlTarget().getTargetClass()).get(field);
		}
		
		Object casted = transformByType(type, obj);
		return casted;
	}
	
	private static Object transformByType(Class<?> type, Object obj){
		if(null == obj || obj.getClass() == type){
			return obj;
		}
		if(Integer.class.isInstance(obj)){
			obj = Integer.class.cast(obj).toString();
		}
		Object casted = null;
		try{
			if(obj instanceof String){
				String val = (String)obj;
				if(StringUtils.isBlank(val)){
					return casted;
				}
				val = val.trim();
				if(type == String.class){
					casted = val;
				}else if(type == Boolean.class || type == boolean.class){
					casted = Boolean.parseBoolean(val);
				}else if(type == Integer.class || type == int.class){
					casted = Integer.parseInt(val);
				}else if(type == Double.class || type == double.class){
					casted = Double.parseDouble(val);
				}else if(type == Float.class || type == float.class){
					casted = Float.parseFloat(val);
				}else if(type == Date.class){
					casted = new Date(DF_yyyyMMdd_DASHED.parse(val).getTime());
				}else if(type == Timestamp.class){
					casted = new Timestamp(DF_yyyyMMdd_DASHED.parse(val).getTime());
				}else{
					throw new UnknownFormatConversionException("type: " + type + " NOT defined yet");
				}
			}else{
				casted = obj;
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
//		System.out.println("KendoUiService.transformByType type: " + type + "obj: " + obj + ", casted: " + casted);
		return casted;
	}
	
	private SqlTarget getFirstSqlTarget(){
		if(target == null){
			target = q.findFirstSqlTarget();
		}
		return target;
	}
	
	private String getAlias(){
		if(StringUtils.isBlank(alias)){
			alias = q.findFirstSqlTargetAlias();
		}
		return alias;
	}
	
	private static Integer parseInteger(Object obj){
		Integer i = null;
		if(obj == null){
			return i;
		}
		if(obj instanceof String && NumberUtils.isNumber((String)obj)){
			i = Integer.parseInt((String)obj);
		}else if(obj instanceof Integer){
			i = (Integer)obj;
		}
		return i;
	}
	/**
	 * ex: name->Name, member.name->MemberName
	 * @param input
	 * @return
	 */
	private static String firstLetterToUpperCase(String input){
		String result = "";
		String[]splits = input.split("\\.");
		for(String split : splits){
			String first = split.substring(0, 1);
			String firstToUpper = first.toUpperCase();
			String firstRemoved = StringUtils.removeStart(split, first);
			result += (firstToUpper + firstRemoved);
		}
		result = result.replace("[", "_").replace("]", "_");
		return result;
	}
	
	public HttpSession getCurrentHttpSession(){
		return WebUtils.currentSession();
	}
	
	private static void testBaseOperation(){
		BaseTest.executeApplicationContext(acac->{
			KendoUiService<Member, Member> q = acac.getBean(KendoUiService.class);
			q.getSqlRoot()
			.select()
				.target("p").getRoot()
			.from()
				.target(Member.class.getName(), "p").getRoot()
			.where()
				.andConds()
					.andSimpleCond("p.name = :pName", String.class)
					.andSimpleCond("p.idNo = :pIdNo", String.class)
					.andSimpleCond("p.mobile = :pMobile", String.class);
			ConditionConfig<Member> c = q.genCondtitionsAfterExecuteQueryPageable();
			c.getResults().forEach(m->{
				System.out.println(m.getId());
			});
		});
	}
	
	private static void testSwitch(String operator){
		String expression = "p.name = ";
		switch(operator){
		case "isnull":
			expression += "IS NULL";
		case "isnotnull":
			expression += "IS NOT NULL";				
		case "isempty":
			expression += "IS EMPTY";
		case "isnotempty":
			expression += "IS NOT EMPTY";
			break;
		}
		System.out.println(expression);
	}
	
	private static void testFirstLetterToUpperCase(){
		String t1 = "name";
		String result1 = firstLetterToUpperCase(t1);
		System.out.println(result1);
		String t2 = "member.name";
		String result2 = firstLetterToUpperCase(t2);
		System.out.println(result2);
	}
	
	private static void testTransformByType(){
		System.out.println(transformByType(Date.class, "2016-03-01T16:00:00.000Z"));
	}

	public static void main(String[]args){
		testFirstLetterToUpperCase();
	}
}
