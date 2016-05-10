package com.angrycat.erp.service;

import java.io.Serializable;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.jackson.mixin.MemberIgnoreDetail;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.ParameterCategory;
import com.angrycat.erp.model.Product;
import com.angrycat.erp.web.component.ConditionConfig;

@Service
@Scope("session")
public class KendoUiAutocompleteService implements Serializable{
	private static final long serialVersionUID = -6179812186529843517L;
	@Autowired
	private KendoUiService<Member, Member> queryMemberService;
	@Autowired
	private KendoUiService<Product, Product> queryProductService;
	@Autowired
	private KendoUiService<ParameterCategory, ParameterCategory> queryParameterCategoryService;	
	
	@PostConstruct
	public void init(){
		queryMemberService.getSqlRoot()
			.select()
				.target("p").getRoot()
			.from()
				.target(Member.class, "p");	
		queryProductService.getSqlRoot()
			.select()
				.target("p").getRoot()
			.from()
				.target(Product.class, "p");
		queryParameterCategoryService.getSqlRoot()
			.select()
				.target("p").getRoot()
			.from()
				.target(ParameterCategory.class, "p");		
	}
	
	public String queryMembers(ConditionConfig<Member> conditionConfig){
		ConditionConfig<Member> cc = queryMemberService.executeQueryPageable(conditionConfig);
		String result = CommonUtil.parseToJson(cc, Member.class, MemberIgnoreDetail.class);
		return result;
	}
	
	public String queryProducts(ConditionConfig<Product> conditionConfig){
		ConditionConfig<Product> cc = queryProductService.executeQueryPageable(conditionConfig);
		String result = CommonUtil.parseToJson(cc);
		return result;
	}
	
	public String queryParameterCategories(ConditionConfig<ParameterCategory> conditionConfig){
		ConditionConfig<ParameterCategory> cc = queryParameterCategoryService.executeQueryPageable(conditionConfig);
		String result = CommonUtil.parseToJson(cc);
		return result;
	}	
}
