package com.angrycat.erp.security.extend;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.angrycat.erp.model.PersonalInfo;

@Entity
@DiscriminatorValue("2")
public class GroupInfo extends PersonalInfo {

}
