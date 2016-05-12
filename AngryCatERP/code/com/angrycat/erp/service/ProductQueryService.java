package com.angrycat.erp.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.model.Product;

@Service
@Scope("prototype")
public class ProductQueryService extends KendoUiService<Product, Product> {
	private static final long serialVersionUID = -7448886081913187226L;

}
