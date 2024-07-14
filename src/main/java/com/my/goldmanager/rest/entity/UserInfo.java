package com.my.goldmanager.rest.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * UserInfo 
 */
public class UserInfo {

	@Getter
	@Setter
	private String userName;
	
	@Getter
	@Setter
	private boolean isActive;
}
