package com.angrycat.erp.jackson.mixin;

import java.util.Set;

import com.angrycat.erp.model.VipDiscountDetail;
import com.fasterxml.jackson.annotation.JsonIgnore;

public interface MemberIgnoreDetail {
	@JsonIgnore public Set<VipDiscountDetail> getVipDiscountDetails();
}
