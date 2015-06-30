package com.angrycat.erp.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Service;

import com.angrycat.erp.condition.ConditionConfigurable;
import com.angrycat.erp.condition.Order;
import com.angrycat.erp.condition.SimpleExpression;
import com.angrycat.erp.query.ConditionalQuery;
import com.angrycat.erp.query.HibernateQueryExecutable;
import com.angrycat.erp.query.PageNavigator;
import com.angrycat.erp.query.QueryConfigurable;
import com.angrycat.erp.query.QueryGenerator;
import com.angrycat.erp.web.component.ConditionConfig;

@Service
@Scope("prototype")
public class CrudBaseService<T, R> extends ConditionalQuery<T> implements CrudService<T, R> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8528962281827660052L;

	private LocalSessionFactoryBean lsfb;
	private Class<R> root;
	
	public static final String DEFAULT_ROOT_ALIAS = "p";
	final int DEFAULT_BATCH_SIZE = 100;
	
	public static final String SIMPLE_EXPRESSION_PREFIEX	= "condition_";
	public static final String CURRENT_PAGE					= "currentPage";
	public static final String COUNT_PER_PAGE				= "countPerPage";
	public static final String ORDER_TYPE					= "orderType";
	private static final List<String> CONFIG_RANGES			= Arrays.asList(CURRENT_PAGE, COUNT_PER_PAGE, ORDER_TYPE);
	
	private DateFormat dateFormatFS = new SimpleDateFormat("yyyy-MM-dd");
	private DateFormat timeFormatFS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	public CrudBaseService(LocalSessionFactoryBean lsfb){
		super(lsfb.getObject());
		this.lsfb = lsfb;
	}
	
	public CrudBaseService(LocalSessionFactoryBean lsfb, Class<R> root){
		super(lsfb.getObject());
		this.lsfb = lsfb;
		this.root = root;
	}

	public void setRoot(Class<R> root){
		this.root = root;
	}
	
	public void setRootAndInitDefault(Class<R> root){
		setRoot(root);
		init();
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
		System.out.println("copyConditionConfig: ");
		conds.forEach((k,v)->{
			// simpleExpression
			if(k.startsWith(SIMPLE_EXPRESSION_PREFIEX)){
				String id = k.replace(SIMPLE_EXPRESSION_PREFIEX, "");
				SimpleExpression se = getSimpleExpressions().get(id);
				se.setValue(parseSimpleExprValueType(se.getType(), v));
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
			if(v.getType() == Date.class){
				System.out.println("simple express val: " + v.getValue());
			}
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
	
	protected Object parseSimpleExprValueType(Class<?>clz, Object val){
		if(val==null){
			return null;
		}
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
					result = new Date(dateFormatFS.parse(str).getTime());
				}else if(clz == Timestamp.class){
					result = new Timestamp(timeFormatFS.parse(str).getTime());
				} 
			}	
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		return result;
		
	}
	
	public void init() {
		String rootAlias = DEFAULT_ROOT_ALIAS;
		createFromAlias(root.getName(), rootAlias)
		.addSelect(rootAlias);
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
		copyConditionConfig(conditionConfig);
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
	public List<T> executeQueryPageableAfterDelete(Consumer<Session> beforeDelete, List<String> ids){
		return executeSession(s->{
			int deleteCount = 0;
			String batchSizeStr = lsfb.getConfiguration().getProperty("hibernate.jdbc.batch_size");
			if(ids != null && !ids.isEmpty()){
				if(beforeDelete!=null){
					beforeDelete.accept(s);
				}
				
				QueryGenerator qg = toQueryGenerator();
				String alias = qg.getRootAlias();
				String from = qg.getFrom();
				String id = "id";
				
				int currentCount = 0;
				int batchSize = StringUtils.isNumeric(batchSizeStr) ? Integer.parseInt(batchSizeStr) : DEFAULT_BATCH_SIZE;
				
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
						s.delete(results.get()[0]);
					}
					tx.commit();
				}catch(Throwable e){
					e.printStackTrace();
					tx.rollback();
				}finally{
					
				}

				deleteCount = currentCount;
				System.out.println("delete successfully: " + deleteCount + " ...");
			}
			List<T> r = executeQueryPageable(s);
			System.out.println("executeQueryPageableAfterDelete found resting count: " + r.size());
			return r;
		});
	}
	
	/**
	 * abstracting session execute boilerplate code to avoid duplicate
	 * @param execution
	 * @return
	 */
	List<T> executeSession(Function<Session, List<T>> execution){
		Session s = null;
		List<T> resultset = Collections.emptyList();
		try{
			s = openSession();
			resultset = execution.apply(s);
		}catch(Throwable e){
			e.printStackTrace();
		}finally{
			s.close();
		}
		return resultset;
	}

	@Override
	public R saveOrMerge(Object...obj){
		Session s = null;
		Transaction tx = null;
		R r = null;
		try{
			s = openSession();
			tx = s.beginTransaction();
			
			for(Object o : obj){
				s.saveOrUpdate(o);
				s.flush();
				if(o.getClass() == root){
					r = (R)o; 
				}
			}
			
			tx.commit();
		}catch(Throwable e){
			e.printStackTrace();
		}finally{
			
		}
		return r;
	}
	
	@Override
	public T findById(String id){
		List<T> list = 
		executeSession(s->{
			return s.createQuery("FROM " + root.getName() + " p WHERE p.id = ?").setString(0, id).list();
		});
		return list.size() > 0 ? list.get(0) : null;
	}
	
	private SessionFactory getSessionFactory(){
		return lsfb.getObject();
	}
	
	Session openSession(){
		return getSessionFactory().openSession();
	}
	
	Session currentSession(){
		return getSessionFactory().getCurrentSession();
	}
}
