package com.angrycat.erp.test;

import static com.angrycat.erp.common.CommonUtil.getStringProperty;
import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.angrycat.erp.model.ExamItem;
public class DataChangeLogTests {
	private List<ExamItem> details(int start, int count){
		List<ExamItem> details = new ArrayList<>();
		int i = start;
		int current = 0;
		while(current < count){
			ExamItem ei = new ExamItem();
			ei.setId(i+"");
			ei.setSequence(i-2);
			ei.setDescription("desc" + (i-2));
			details.add(ei);
			++current;
			++i;
		}
		return details;
	}
	private <T>List<T> collectCoordinate(Collection<T> c1, Collection<T> c2, BiPredicate<T, T> evaluate){
		List<T> results = c1.stream().filter(
			oe->c2.stream().anyMatch(ie->evaluate.test(oe, ie))	
		).collect(Collectors.toList());
		return results;
	}
	private <T>List<T> collectDuplicate(Collection<T> c1, Collection<T> c2){
		List<T> results = collectCoordinate(c1, c2, (oe, ie)->{
			String pk = "id";
			String oeId = getStringProperty(oe, pk);
			String ieId = getStringProperty(ie, pk);
			if(StringUtils.isBlank(oeId)
			|| StringUtils.isBlank(ieId)){
				return false;
			}
			return oeId.equals(ieId);
		});
		return results;
	}
	private <T>List<T> removeDuplicate(Collection<T> c1, Collection<T> c2){
		String pk = "id";
		List<String> duplicates = collectDuplicate(c1, c2).stream().map(e->{
			String id = getStringProperty(e, pk);
			return id;
		}).collect(Collectors.toList());
		List<T> copyC2 = c2.stream().collect(Collectors.toList());
		copyC2.removeIf(e->{
			String id = getStringProperty(e, pk);
			return duplicates.contains(id);
		});
		return copyC2;
	}
	private <T>List<T> collectNew(Collection<T> oldDetails, Collection<T> newDetails){
		String pk = "id";
		List<T> duplicated = collectDuplicate(oldDetails, newDetails);
		List<String> dupIds = duplicated.stream().map(e->{
			String id = getStringProperty(e, pk);
			return id;
		}).collect(Collectors.toList());
		List<T> oldMore = removeDuplicate(newDetails, oldDetails);
		List<String> oldMoreIds = oldMore.stream().map(e->{
			String id = getStringProperty(e, pk);
			return id;
		}).collect(Collectors.toList());
		List<T> newMore = removeDuplicate(oldDetails, newDetails);
		List<String> newMoreIds = newMore.stream().map(e->{
			String id = getStringProperty(e, pk);
			return id;
		}).collect(Collectors.toList());
		
		List<T> results = newDetails.stream().map(e->{
			String id = getStringProperty(e, pk);
			if(dupIds.contains(id) || newMoreIds.contains(id)){
				return e;
			}
			return null;
		}).collect(Collectors.toList());
		results.addAll(oldMoreIds.stream().map(e->(T)null).collect(Collectors.toList()));
		return results;
	}
	@Test
	public void removeDuplicatedById(){
		List<ExamItem> oldDetails = details(2, 7);
		System.out.println("oldDetails");
		oldDetails.forEach(ei->{System.out.println(ei.getId());});
		List<ExamItem> newDetails = details(1, 3);
		System.out.println("newDetails");
		newDetails.forEach(ei->{System.out.println(ei.getId());});
		
		List<ExamItem> duplicated = collectDuplicate(newDetails, oldDetails);
		List<ExamItem> both1 = removeDuplicate(newDetails, oldDetails);
		List<ExamItem> both2 = removeDuplicate(oldDetails, newDetails);
		
		System.out.println("duplicated");
		assertArrayEquals(new String[]{"2", "3"}, duplicated.stream().map(ei->ei.getId()).collect(Collectors.toList()).toArray());
		duplicated.forEach(ei->{
			System.out.println(ei.getId());
		});
		System.out.println("oldDetails not duplicated");
		assertArrayEquals(new String[]{"4", "5", "6", "7", "8"}, both1.stream().map(ei->ei.getId()).collect(Collectors.toList()).toArray());
		both1.forEach(ei->{
			System.out.println(ei.getId());
		});
		System.out.println("newDetails not duplicated");
		assertArrayEquals(new String[]{"1"}, both2.stream().map(ei->ei.getId()).collect(Collectors.toList()).toArray());
		both2.forEach(ei->{
			System.out.println(ei.getId());
		});
	}
	@Test
	public void testCollectNew(){
		List<ExamItem> oldDetails = details(2, 7);
		List<ExamItem> newDetails = details(1, 3);
		List<ExamItem> collect = collectNew(newDetails, oldDetails);
		collect.forEach(e->{
			if(e != null){
				System.out.println(e.getId());
			}else{
				System.out.println("null");
			}
		});
	}
}
