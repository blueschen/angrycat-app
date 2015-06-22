package com.angrycat.erp.security.extend;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.angrycat.erp.model.PersonalInfo;

@Entity
@DiscriminatorValue("1")
public class UserInfo extends PersonalInfo {

}
