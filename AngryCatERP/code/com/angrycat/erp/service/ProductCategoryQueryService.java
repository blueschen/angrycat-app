package com.angrycat.erp.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.model.ProductCategory;

@Service
@Scope("prototype")
public class ProductCategoryQueryService extends
		KendoUiService<ProductCategory, ProductCategory> {
	private static final long serialVersionUID = 393105901257362707L;

}
