package com.angrycat.erp.service;

import static com.angrycat.erp.common.CommonUtil.getPropertyVal;
import static com.angrycat.erp.common.CommonUtil.setProperty;

import java.io.File;
import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.ds.TimeUID;
import com.angrycat.erp.initialize.StartupWebAppInitializer;
import com.angrycat.erp.model.Exam;
import com.angrycat.erp.model.ExamItem;
import com.angrycat.erp.model.Parameter;
import com.angrycat.erp.model.Product;

/**
 * 隨機出考題，資料源包括手動鍵入的題庫，和(從庫存表而來的)商品.<br>
 * 因為商品的資料並非考題形式，所以系統要自動轉換成隨機考題.<br>
 * 關於商品的題目有型號、品名、圖片.<br>
 * 關於商品的題項有型號、品名、價格、圖片.<br>
 * 關於商品的題型變化就有3 x 4 - 3 = 9種
 * @author JerryLin
 *
 */
@Service
@Scope("prototype")
public class RandomExamService {
	private static final String MODELID = "型號";
	private static final String PRODUCT_NAME = "品名";
	private static final String PIC = "圖片";
	private static final String PRICE = "價格";
	
	private static final List<String> PRODUCT_TOPICS = Arrays.asList(MODELID,PRODUCT_NAME, PIC);
	private static final List<String> PRODUCT_QUESTIONS = Arrays.asList(MODELID,PRODUCT_NAME,PRICE, PIC);
	private static final List<int[]> PRODUCT_COORDINATES = new LinkedList<>();
	
	static{
		int topicSize = PRODUCT_TOPICS.size();
		int questionSize = PRODUCT_QUESTIONS.size();
		for(int i=0; i<topicSize; i++){
			for(int j=0; j<questionSize; j++){
				String topic = PRODUCT_TOPICS.get(i);
				String question = PRODUCT_QUESTIONS.get(j);
				if(!topic.equals(question)){
					PRODUCT_COORDINATES.add(new int[]{i,j});
				}
			}
		}
	}
	private static final Map<String, String> modelFields = new LinkedHashMap<>();
	static{
		modelFields.put(MODELID, "modelId");
		modelFields.put(PRODUCT_NAME, "nameEng");
		modelFields.put(PRICE, "suggestedRetailPrice");
		modelFields.put(PIC, "imgDir");
	}
	private static final String RANDON_QUESTION_TEMPLATE = "(\\S+)的(\\S+)為何?";
	public static final Pattern RANDON_QUESTION_PATTERN = Pattern.compile(RANDON_QUESTION_TEMPLATE);
	
	@Autowired
	private SessionFactoryWrapper sfw;
	private Map<String, File> archives = new LinkedHashMap<>();
	/**
	 * 隨機產生題目索引
	 * @return
	 */
	private int randomTopicIdx(){
		int topicIdx = ThreadLocalRandom.current().nextInt(PRODUCT_TOPICS.size());
		return topicIdx;
	}
	/**
	 * 以題目索引為基礎，隨機產生問題/題項索引
	 * @param topicIdx
	 * @return
	 */
	private int randomQuestionIdx(int topicIdx){
		List<Integer> topicQuestionIdx = PRODUCT_COORDINATES.stream().filter(c->c[0] == topicIdx).map(c->c[1]).collect(Collectors.toList());
		int questionIdx = ThreadLocalRandom.current().nextInt(topicQuestionIdx.size());
		return topicQuestionIdx.get(questionIdx);
	}
	/**
	 * 隨機產生不重複商品，做為題目和題項的資料源<br>
	 * 
	 * @param itemCount
	 * @param questionField
	 * @param question
	 * @param topicField
	 * @param topic
	 * @return
	 */
	public List<Product> randomProduct(int itemCount, String questionField, String question, String topicField, String topic){
		Session s = sfw.currentSession();

		String queryCount = "SELECT COUNT(p.id) FROM " + Product.class.getName() + " p";
		Long count = (Long)s.createQuery(queryCount).uniqueResult();
		if(count == 0){
			return Collections.emptyList();
		}
		String queryProduct = "SELECT p FROM " + Product.class.getName() + " p ORDER BY p.id DESC";
		String queryTopCount = "SELECT COUNT(p.id) FROM " + Product.class.getName() + " p WHERE p." + topicField + " = :topic"; 
		LinkedHashSet<Object> questionVals = new LinkedHashSet<>(); // 題項內容避免重複
		LinkedHashSet<Object> randomVals = new LinkedHashSet<>(); // 隨機選項避免重複
		List<Product> products = new LinkedList<>();
		while(products.size() < itemCount){
			int rand = ThreadLocalRandom.current().nextInt(count.intValue());
			Product p = (Product)s.createQuery(queryProduct).setFirstResult(rand).setMaxResults(1).uniqueResult();
			s.evict(p);
			Object topVal = getPropertyVal(p, topicField);
			if(topVal == null){// 如果沒有題目
				continue;
			}
			Long topicCount = (Long)s.createQuery(queryTopCount).setParameter("topic", topVal).uniqueResult();
			if(topicCount > 1){// 如果題目內容有重複的話，略過。譬如:品名叫Fleur De Lis在庫存表有好幾筆， 所以不能提問:"品名Fleur De Lis的價格為何"這樣的問題
				continue;
			}
			String rootPath = StartupWebAppInitializer.getUploadRoot();
			if(PIC.equals(topic)){ // 如果題目是圖片，但沒有找到這張圖
				File f = new File(rootPath+topVal);
				if(f.exists()){
					String uid = TimeUID.generateByHand();
					archives.put(uid, f);
					setProperty(p, topicField, uid);
				}else{
					continue;
				}
			}
			
			Object questionVal = getPropertyVal(p, questionField);
			if(questionVal == null){ // 如果沒答案
				continue;
			}
			if(PIC.equals(question)){ // 如果要的答案是圖片，但沒有找到
				File f = new File(rootPath+questionVal);
				if(f.exists()){
					String uid = TimeUID.generateByHand();
					archives.put(uid, f);
					setProperty(p, questionField, uid);
				}else{
					continue;
				}
			}
			
			if(!questionVals.contains(questionVal) && !randomVals.contains(rand)){
				questionVals.add(questionVal);
				products.add(p);
				randomVals.add(rand);
			}
		}
		return products;
	
	}
	/**
	 * 產生商品題組，題目不能重複
	 * @param count
	 * @return
	 */
	private List<Exam> setRandomProductExams(int count){
		LinkedHashSet<String> descriptions = new LinkedHashSet<>();
		LinkedList<Exam> exams = new LinkedList<>();
		int examId = 0;
		while(exams.size() < count){
			Exam exam = setRandomProductExam();
			String description = exam.getDescription();
			if(!descriptions.contains(description)){
				descriptions.add(description);
				exam.setId(String.valueOf(examId++)); // 並非真的從題庫而來，手動加入ID
				exams.add(exam);
			}
		}
		return exams;
	}
	/**
	 * 從題庫隨機產生題組，題目不得重複
	 * @param itemCount
	 * @return
	 */
	private List<Exam> setRandomExams(int itemCount){
		Session s = sfw.currentSession();
		String queryCount = "SELECT COUNT(p.id) FROM " + Exam.class.getName() + " p";
		Long count = (Long)s.createQuery(queryCount).uniqueResult();
		if(count == 0){
			return Collections.emptyList();
		}
		String queryProduct = "SELECT p FROM " + Exam.class.getName() + " p left join fetch p.items ORDER BY p.id DESC";
		LinkedHashSet<Object> randomVals = new LinkedHashSet<>(); // 避免隨機選項重複
		List<Exam> exams = new LinkedList<>();
		while(exams.size() < itemCount){
			int rand = ThreadLocalRandom.current().nextInt(count.intValue());
			Exam e = (Exam)s.createQuery(queryProduct).setFirstResult(rand).setMaxResults(1).uniqueResult();		
			if(!randomVals.contains(rand)){
				s.evict(e);
				Collections.sort(e.getItems(), new Comparator<ExamItem>(){
					@Override
					public int compare(ExamItem o1, ExamItem o2) {
						return o1.getSequence()-o2.getSequence();
					}
				});
				exams.add(e);
				randomVals.add(rand);
			}
		}
		return exams;
			
	}
	/**
	 * 商品隨機出題
	 * @return
	 */
	public Exam setRandomProductExam(){
		int topicIdx = randomTopicIdx();
		String topic = PRODUCT_TOPICS.get(topicIdx);
		String topicField = modelFields.get(topic);
		int questionIdx = randomQuestionIdx(topicIdx);
		String question = PRODUCT_QUESTIONS.get(questionIdx);
		String questionField = modelFields.get(question);
				
		int itemCount = 4; // 有?個選項
		List<Product> products = randomProduct(itemCount, questionField, question, topicField, topic);
		Product correct = products.get(0); // 預設第一項當作主要問題及答案，其餘作為混淆選項
		Collections.shuffle(products);// 隨機排過一次
		List<ExamItem> items = 
			IntStream.range(0, products.size()).boxed().map(i->{
				Product p = products.get(i);
				ExamItem ei = new ExamItem();
				ei.setCorrect(p.getId().equals(correct.getId())); // 設定正確答案
				Object val = getPropertyVal(p, questionField);
				ei.setDescription(val.toString());
				ei.setSequence(i+1); // 題項序號
				return ei;
			}).collect(Collectors.toList());
		
		Object val = getPropertyVal(correct, topicField);
		String completeTopic = topic+val;	
			
		String desc = RANDON_QUESTION_TEMPLATE
				.replaceFirst("\\(\\\\S\\+\\)", completeTopic)
				.replace("(\\S+)", question);
		
//		System.out.println("題目:" + desc + ", 答案:" + getPropertyVal(correct, questionField));
		
		Exam exam = new Exam();
		exam.setDescription(desc);
		exam.setCategory("商品");
		exam.setCreateDate(new Date(System.currentTimeMillis()));
		exam.setItems(items);
		exam.setTopicImaged(topic.equals(PIC));
		exam.setQuestionImaged(question.equals(PIC));
		
		return exam;
	}
	/**
	 * 開始出題
	 * @return
	 */
	@Transactional
	public List<Exam> setExamGroup(){
		archives.clear(); // 清除暫存圖片檔
		TestCount testCount = renderTestCount();
		int productCount = testCount.defaultProductCount(5);
		int examCount = testCount.defaultExamCount(0);
		
		int examTotal = findExamTotalCount();
		if(examTotal < examCount){
			productCount += (examCount-examTotal);
			examCount = examTotal;
		}
		
		List<Exam> products = setRandomProductExams(productCount);
		List<Exam> exams = setRandomExams(examCount);
		List<Exam> all = new LinkedList<>();
		all.addAll(products);
		all.addAll(exams);
		return all;
	}
	public Map<String, File> getArchives(){
		return this.archives;
	}
	private static Parameter findTestCount(Session s){
		String query = "SELECT p FROM " + Parameter.class.getName() + " p WHERE p.parameterCategory.name = :catName AND p.nameDefault = :name";
		List<Parameter> list = s.createQuery(query).setString("catName", "出題").setString("name", "配題數").list();
		return list.get(0);
	}
	public static Parameter findTestCount(SessionFactoryWrapper sfw){
		List<Parameter> p = 
		sfw.executeFindResults(s->{
			return Arrays.asList(findTestCount(s));
		});
		return p.get(0);
	}
	private int findExamTotalCount(){
		Session s = sfw.currentSession();
		String q = "SELECT COUNT(p.id) FROM " + Exam.class.getName() + " p";
		Long count = (Long)s.createQuery(q).uniqueResult();
		return count.intValue();
	}
	private TestCount renderTestCount(){
		Parameter p = findTestCount(sfw.currentSession());
		TestCount tc = new TestCount(p);
		return tc;
	}
	private static class TestCount{
		private Parameter p;
		public TestCount(Parameter p){
			this.p = p;
		}
		public String getProperty(String key){
			String prop = p.getLocaleNames().get(key);
			return prop;
		}
		public int defaultCount(String key, int defaultCount){
			String count = getProperty(key);
			if(!StringUtils.isNumeric(count)){
				return defaultCount;
			}
			return Integer.parseInt(count);
		}
		public int defaultProductCount(int defaultCount){
			int count = defaultCount("product", defaultCount);
			return count;
		}
		public int defaultExamCount(int defaultCount){
			int count = defaultCount("exam", defaultCount);
			return count;
		}
	}
}
