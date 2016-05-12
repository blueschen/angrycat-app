package com.angrycat.erp.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.angrycat.erp.common.DatetimeUtil;
import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.condition.Order;
import com.angrycat.erp.condition.SimpleExpression;
import com.angrycat.erp.condition.TimestampEndExpression;
import com.angrycat.erp.condition.TimestampStartExpression;
import com.angrycat.erp.log.DataChangeLogger;
import com.angrycat.erp.query.ConditionalQuery;
import com.angrycat.erp.query.QueryGenerator;
import com.angrycat.erp.query.QueryScrollable;
import com.angrycat.erp.security.User;
import com.angrycat.erp.security.extend.UserInfo;
import com.angrycat.erp.web.WebUtils;
import com.angrycat.erp.web.component.ConditionConfig;

@Service
@Scope("prototype")
public class QueryBaseService<T, R> extends ConditionalQuery<T> implements ConditionalQueryService<T, R>, QueryScrollable {
	private static final long serialVersionUID = -8528962281827660052L;
		
	public static final String DEFAULT_ROOT_ALIAS = "p";
	public static final String SIMPLE_EXPRESSION_PREFIEX	= "condition_";
	public static final String CURRENT_PAGE					= "currentPage";
	public static final String COUNT_PER_PAGE				= "countPerPage";
	public static final String ORDER_TYPE					= "orderType";
	private static final List<String> CONFIG_RANGES			= Arrays.asList(CURRENT_PAGE, COUNT_PER_PAGE, ORDER_TYPE);
	
	private SessionFactoryWrapper sfw;
	private Class<R> root;
	
	@Autowired
	private DataChangeLogger dataChangeLogger;
	
	@Autowired
	public QueryBaseService(SessionFactoryWrapper sfw){
		super(sfw.getSessionFactory());
		this.sfw = sfw;
	}
	
	public QueryBaseService(SessionFactoryWrapper sfw, Class<R> root){
		this(sfw);
		this.sfw = sfw;
		this.root = root;
	}

	public void init() {
		String rootAlias = DEFAULT_ROOT_ALIAS;
		createFromAlias(root.getName(), rootAlias)
		.addSelect(rootAlias);
	}
	
	public void setRoot(Class<R> root){
		this.root = root;
	}
	
	public void setRootAndInitDefault(Class<R> root){
		setRoot(root);
		init();
	}
	
	@Transactional
	@Override
	public <F>F executeScrollableQuery(BiFunction<ScrollableResults, SessionFactoryWrapper, F> executeLogic){
		Session s = sfw.currentSession();
		
		QueryGenerator gen = toQueryGenerator();
		String hql = gen.toCompleteStrWithIdOrderBy();
		Map<String, Object> params = gen.getParams();
		
		ScrollableResults rs = s.createQuery(hql).setProperties(params).scroll(ScrollMode.FORWARD_ONLY);
		F target = executeLogic.apply(rs, sfw);
		
		return target;
	}
	
	
	/**
	 * copy condition configurations, currently there are four types:<br>
	 * 1.simple expression condition value, whose key is prefixed with 'condition_';<br>
	 * 2.paging configurations, including currentPage and countPerPage<br>
	 * 3.sorting type, ex. p.code DESC, default clear other sorting settings before add order type<br>
	 * 4.other customized items, copying into requestParam
	 */
	@Override
	public void copyConditionConfig(ConditionConfig<T> conditionConfig) {

		Map<String, Object> conds = conditionConfig.getConds();
		conds.forEach((k,v)->{
			// simpleExpression
			if(k.startsWith(SIMPLE_EXPRESSION_PREFIEX)){
				String id = k.replace(SIMPLE_EXPRESSION_PREFIEX, "");
				SimpleExpression se = getSimpleExpressions().get(id);
				if(se != null){
					if(!se.isFixed()){
						se.setValue(parseSimpleExprValueType(se, v));
					}
				}else{
					System.out.println("null id:" + id);
				}
			// other customized items
			}else if(!CONFIG_RANGES.contains(k)){
				addRequestParam(k,v);
			}
		});
		// paging
		Object currentPage = conds.get(CURRENT_PAGE);
		if(currentPage instanceof String && StringUtils.isNotBlank((String)currentPage)){
			setCurrentPage(Integer.parseInt(currentPage.toString()));
		}else if(currentPage instanceof Integer){
			setCurrentPage((Integer)currentPage);
		}
		Object countPerPage = conds.get(COUNT_PER_PAGE);
		if(countPerPage instanceof String && StringUtils.isNotBlank((String)countPerPage)){
			setCountPerPage(Integer.parseInt(countPerPage.toString()));
		}else if(countPerPage instanceof Integer){
			setCountPerPage((Integer)countPerPage);
		}
		// sorting
		if(StringUtils.isNotBlank((String)conds.get(ORDER_TYPE))){
			Order order = Order.translate(conds.get(ORDER_TYPE).toString());
			if(order!=null){
				addOrderAfterClear(order);
			}
		}
	}

	/**
	 * retrieve condition configurations
	 * also see the method {@link #copyConditionConfig(ConditionConfig)}.
	 */
	@Override
	public ConditionConfig<T> getConditionConfig() {
		ConditionConfig<T> cc = new ConditionConfig<T>();
		Map<String, Object> conds = cc.getConds();
		// simpleExpression
		getSimpleExpressions().forEach((id,v)->{
			conds.put(SIMPLE_EXPRESSION_PREFIEX + id, v.getValue());
		});
		// paging
		conds.put(CURRENT_PAGE, getCurrentPage());
		conds.put(COUNT_PER_PAGE, getCountPerPage());
		if(getPageNavigator()!=null){
			cc.setPageNavigator(getPageNavigator());
		}
		// sorting
		conds.put(ORDER_TYPE, null);
		return cc;
	}
	
	public ConditionConfig<T> resetConditions(){
		// simpleExpression
		getSimpleExpressions().forEach((id,v)->{
			if(!v.isFixed()){
				v.setValue(null);
			};
		});
		setCurrentPage(1);
		setCountPerPage(10);
		getOrders().clear();
		return getConditionConfig();
	}
	
	
	protected Object parseSimpleExprValueType(SimpleExpression se, Object val){
		if(val==null){
			return null;
		}
		Class<?> clz = se.getType();
		if(val.getClass() == clz){
			return val;
		}
		Object result = null;
		try{
			if(val instanceof Long){
				Long v = (Long)val;
				if(clz == Date.class){
					result = new Date(v);
				}else if(clz == Timestamp.class){
					result = new Timestamp(v);
				}
			}else if(val instanceof String){
				String str = (String)val;
				if(StringUtils.isBlank(str)){
					return result;
				}
				str = str.trim();
				if(clz == String.class){
					result = str;
				}else if(clz == Integer.class){
					result = Integer.parseInt(str);
				}else if(clz == Double.class){
					result = Double.parseDouble(str);
				}else if(clz == Float.class){
					result = Float.parseFloat(str);
				}else if(clz == Date.class){
					result = new Date(DatetimeUtil.DF_yyyyMMdd_DASHED.parse(str).getTime());
				}else if(clz == Timestamp.class){
					if(se instanceof TimestampStartExpression || se instanceof TimestampEndExpression){
						result = str;
					}else{
						result = new Timestamp(DatetimeUtil.DF_yyyyMMdd_DASHED_EXTEND_TO_SEC.parse(str).getTime());
					}
				}else if(clz == Boolean.class){
					result = Boolean.parseBoolean(str);
				}
			}	
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		return result;
		
	}
	
	@Override
	public ConditionConfig<T> executeQueryPageableAfterDelete(List<String> ids){
		List<T> results = executeQueryPageableAfterDelete(null, ids);
		ConditionConfig<T> cc = getConditionConfig();
		cc.setResults(results);
		return cc;
	}
	
	@Override
	public ConditionConfig<T> executeQueryPageable(ConditionConfig<T> conditionConfig){
		if(conditionConfig != null){
			copyConditionConfig(conditionConfig);
		}
		return genCondtitionsAfterExecuteQueryPageable();
	}
	
	public ConditionConfig<T> genCondtitionsAfterExecuteQueryPageable(){
		List<T> results = executeQueryPageable();
		ConditionConfig<T> cc = getConditionConfig();
		cc.setResults(results);
		return cc;
	}
	
	/**
	 * ref. https://abramsm.wordpress.com/2008/04/23/hibernate-batch-processing-why-you-may-not-be-using-it-even-if-you-think-you-are/
	 * @param beforeDelete
	 * @param ids
	 * @return
	 */
	protected List<T> executeQueryPageableAfterDelete(Consumer<Session> beforeDelete, List<String> ids){
		User currentUser = defaultUserIfNotExisted();
		
		Session s = sfw.openSession();
		List<T> r = Collections.emptyList();
		if(ids != null && !ids.isEmpty()){
			if(beforeDelete!=null){
				beforeDelete.accept(s);
			}
			
			QueryGenerator qg = toQueryGenerator();
			String alias = qg.getRootAlias();
			String from = qg.getFrom();
			String id = "id";
			
			int currentCount = 0;
			int batchSize = sfw.getBatchSize();
			
			Transaction tx = s.beginTransaction();
			String queryAll = "SELECT " + alias +" FROM " + from + " WHERE " + alias + "." + id + " IN (:ids)";
			ScrollableResults results = 
				s.createQuery(queryAll).setParameterList("ids", ids).scroll(ScrollMode.FORWARD_ONLY);
			try{
				while(results.next()){
					currentCount++;
					if(currentCount % batchSize == 0){
						s.flush();
						s.clear();
					}
					Object obj = results.get()[0];
					dataChangeLogger.logDelete(obj, s, currentUser);
					s.delete(obj);
				}
				tx.commit();
			}catch(Throwable e){
				tx.rollback();
				throw new RuntimeException(e);
			}finally{
				r = executeQueryPageable(s);
				s.close();
			}
		}
		return r;
	
	}
	
	@Override
	public T findById(String id){
		List<T> list = 
		executeSession(s->{
			return s.createQuery("FROM " + root.getName() + " p WHERE p.id = :id").setString("id", id).list();
		});
		return list.size() > 0 ? list.get(0) : null;
	}
	
	private User defaultUserIfNotExisted(){
		User u = WebUtils.getSessionUser();
		if(u == null){
			String defaultName = "SomeBody";
			u = new User();
			u.setUserId(defaultName);
			UserInfo info = new UserInfo();
			info.setName(defaultName);
			u.setInfo(info);
		}
		return u;
	}
	
	protected SessionFactoryWrapper getSessionFactoryWrapper(){
		return sfw;
	}
}
