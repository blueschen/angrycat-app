package com.angrycat.erp.service;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.angrycat.erp.initialize.config.RootConfig;
import com.angrycat.erp.model.Exam;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.service.RandomExamService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class RandomExamServicetTests {
	@Autowired
	private RandomExamService serv;

	@Test
	public void randomProduct(){
		for(int i=0; i<5; i++){
			List<Product> products = serv.randomProduct(3, "nameEng", "品名", "modelId", "型號");
			System.out.println("product id:" + products.get(0).getId());
		}
	}
	@Test
	public void testSplitStr(){
		String t ="下列型號的圖片為何?";
		Pattern p = Pattern.compile("下列(\\S+)的(\\S+)為何?");
		Matcher m = p.matcher(t);
		while(m.find()){
			String first = m.group(1);
			String second = m.group(2);
			System.out.println("first:" + first + ", second:" + second);
		}
	}
	@Test
	public void setRandomProductExam(){
		for(int i=0; i<10; i++){
			Exam exam = serv.setRandomProductExam();
			System.out.println(ReflectionToStringBuilder.toString(exam, ToStringStyle.MULTI_LINE_STYLE));
			exam.getItems().forEach(ei->{
				System.out.println(ReflectionToStringBuilder.toString(ei, ToStringStyle.MULTI_LINE_STYLE));
			});
		}
	}
	@Test
	public void setExamGroup(){
		for(int i=0; i<1; i++){
			List<Exam> exams = serv.setExamGroup();
			exams.forEach(e->{
				System.out.println(ReflectionToStringBuilder.toString(e, ToStringStyle.MULTI_LINE_STYLE));
				e.getItems().forEach(ei->{
					System.out.println(ReflectionToStringBuilder.toString(ei, ToStringStyle.MULTI_LINE_STYLE));
				});
			});
		}
	}
}
