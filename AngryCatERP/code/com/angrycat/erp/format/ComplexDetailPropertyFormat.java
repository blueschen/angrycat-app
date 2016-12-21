package com.angrycat.erp.format;

import static com.angrycat.erp.common.CommonUtil.getPropertyVal;
import static com.angrycat.erp.common.CommonUtil.getStringProperty;
import static com.angrycat.erp.log.DataChangeLogger.removeReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
/**
 * 新舊物件之間的修改關係，<br>
 * 如果牽涉到明細的新增、修改、刪除運算，<br>
 * 可以使用這個類別
 * @author JerryLin
 *
 */
public class ComplexDetailPropertyFormat extends PropertyFormat {
	private static final Pattern FIND_NAME_TEMPLATE = Pattern.compile("\\{\\{(\\w+)\\}\\}");
	private List<Object> oldContainer = Collections.emptyList();
	private List<Object> newContainer = Collections.emptyList();
	private FormatList detailFormats;
	private List<FormattedValue> values = new ArrayList<>();
	private List<String> updated = Collections.emptyList();
	private List<String> added = Collections.emptyList();
	private List<String> deleted = Collections.emptyList();
	
	public ComplexDetailPropertyFormat(String name, String detailProperty, FormatList detailFormats) {
		super(name, detailProperty);
		this.detailFormats = detailFormats;
	}
	public ComplexDetailPropertyFormat(String name, String detailProperty, FormatList formatList, Object oldMaster, Object newMaster) {
		this(name, detailProperty, formatList);
		init(oldMaster, newMaster);
	}
	/**
	 * 取得id值，呼叫之前要確認該物件有String型別的id屬性
	 * @param bean
	 * @return
	 */
	private static String getId(Object bean){
		return getStringProperty(bean, "id");
	}
	/**
	 * 用新舊主表物件初始化
	 * 新舊物件皆需有值
	 * @param oldMaster
	 * @param newMaster
	 */
	public void init(Object oldMaster, Object newMaster){
		Collection<?> oldDetails = getPropertyVal(oldMaster, getProperty());
		Collection<?> newDetails = getPropertyVal(newMaster, getProperty());
		oldDetails = oldDetails != null ? oldDetails : Collections.emptyList();
		newDetails = newDetails != null ?  newDetails: Collections.emptyList();
		if(oldDetails.isEmpty() && newDetails.isEmpty()){
			return;
		}
		
		Map<String, Object> oldDetailIdx = oldDetails.stream().collect(Collectors.toMap(o->getId(o), Function.identity()));
		Map<String, Object> newDetailIdx = newDetails.stream().collect(Collectors.toMap(n->getId(n), Function.identity()));
		Set<String> oldDetailIds = oldDetailIdx.keySet();
		// 兩者皆有代表修改
		updated = newDetails.stream().filter(n->oldDetailIds.contains(getId(n))).map(n->getId(n)).collect(Collectors.toList());
		// 只有新物件有，代表新增
		added = newDetails.stream().filter(n->!updated.contains(getId(n))).map(n->getId(n)).collect(Collectors.toList());
		// 只有舊物件有，代表刪除
		deleted = oldDetailIds.stream().filter(o->!updated.contains(o)).collect(Collectors.toList());
		
		// 在比較明細的時候，必須確保有兩個(元素)數量一致的陣列，
		// 並且用對應索引的元素代表新增、修改、刪除，
		// 譬如: 索引位置為0的元素在代表舊物件的容器有值，但在新物件沒值，即為新增
		oldContainer = new ArrayList<>();
		newContainer = new ArrayList<>();
		
		added.forEach(id->{
			oldContainer.add(null);
			newContainer.add(newDetailIdx.get(id));
		});
		updated.forEach(id->{
			oldContainer.add(oldDetailIdx.get(id));
			newContainer.add(newDetailIdx.get(id));
		});
		deleted.forEach(id->{
			oldContainer.add(oldDetailIdx.get(id));
			newContainer.add(null);
		});	
		String name = getName();
		IntStream.range(0, oldContainer.size())
			.boxed()
			.forEachOrdered(i->{
				Object oldDetail = oldContainer.get(i);
				Object newDetail = newContainer.get(i);
				detailFormats.forEach(f->{
					String fName = f.getName();
					String dfName = findNameTemplateField(fName);
					Object dfVal = null;
					String nameFormatted = name + "_";
					if(dfName == null){// 如果沒有定義提示替換字串，預設最後加上索引號
						nameFormatted += (fName + "_" + (i+1));
					}else if(dfName.equals("[idx]")){// 標明索引號的位置
						nameFormatted += fName.replace("{{[idx]}}", (i+1)+"");
					}else{// 標明其他屬性做為替換字串
						dfVal = getPropertyVal(getFirstNotNull(oldDetail, newDetail), dfName);
						nameFormatted += fName.replace("{{"+dfName+"}}", dfVal.toString());
					}
					// TODO 目前只允許一種帶入形式，如果想要混合多種替換字串，需要更複雜的做法
					String oldVal = oldDetail != null ? removeReturn(f.getValue(oldDetail)) : null;
					String newVal = newDetail != null ? removeReturn(f.getValue(newDetail)) : null;
					values.add(new FormattedValue(oldVal, newVal, nameFormatted));
				});
			});
	
	}
	/**
	 * 取出第一個提示替換字串<br>
	 * 譬如"xxxx{{model}}yyyzzz"會取得"model"<br>
	 * TODO 將來若是要取出多個提示替換值這個method需要修改<br>
	 * @param detailFormatName
	 * @return
	 */
	public static String findNameTemplateField(String detailFormatName){
		String field = null;
		Matcher m = FIND_NAME_TEMPLATE.matcher(detailFormatName);
		while(m.find()){
			int start = m.start(1);
			int end = m.end(1);
			field = detailFormatName.substring(start, end);
		}
		return field;
	}
	private static <T>T getFirstNotNull(T... t){
		Optional<T> optional = Arrays.asList(t).stream().filter(e->e != null).findFirst();
		if(optional.isPresent()){
			return optional.get();
		}
		return null;
	}
	public List<FormattedValue> getValues(){
		return values;
	}
	List<String> getUpdated(){
		return updated;
	}
	List<String> getAdded(){
		return added;
	}
	List<String> getDeleted(){
		return deleted;
	}
}
