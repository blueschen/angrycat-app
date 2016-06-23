package com.angrycat.erp.jackson.mixin;

import java.util.List;

import com.angrycat.erp.model.ExamItem;
import com.fasterxml.jackson.annotation.JsonIgnore;

public interface ExamIgnoreDetail {
	@JsonIgnore public List<ExamItem> getItems();
}
